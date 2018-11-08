package bwfdm.sara.transfer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import bwfdm.sara.Config;
import bwfdm.sara.db.ArchiveAccess;
import bwfdm.sara.db.License;
import bwfdm.sara.extractor.LicenseFile;
import bwfdm.sara.extractor.MetadataExtractor;
import bwfdm.sara.git.ArchiveProject;
import bwfdm.sara.git.ArchiveRepo;
import bwfdm.sara.git.ArchiveRepo.ProjectExistsException;
import bwfdm.sara.project.ArchiveJob;
import bwfdm.sara.project.LicensesInfo.LicenseInfo;
import bwfdm.sara.project.MetadataField;
import bwfdm.sara.project.Ref;
import bwfdm.sara.publication.Item;
import bwfdm.sara.publication.ItemState;
import bwfdm.sara.publication.ItemType;
import bwfdm.sara.publication.db.PublicationDatabase;

/** Pushes a repository to a git archive. */
public class PushTask extends Task {
	private static final String PUSH_REPO = "Preparing repository for upload";
	private static final String CREATE_PROJECT = "Creating project in archive";
	private static final String COMMIT_META = "Committing metadata to git archive";
	private static final String CREATE_METADATA = "Recording metadata for publication";
	private static final ISO8601DateFormat ISO8601 = new ISO8601DateFormat();
	private static final String METADATA_FILENAME = "submitted_metadata.xml";
	private static final String TARGET_REMOTE = "target";
	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
	static {
		JSON_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
	}

	private final ArchiveJob job;
	private final ArchiveRepo archive;
	private final PublicationDatabase pubDB;

	private ArchiveProject project;
	private UUID itemUUID;
	private Map<Ref, String> heads;
	private Date now;
	private Item item;

	/**
	 * @param job
	 *            all the information needed to archive this job (and probably
	 *            some extra info that isn't relevant)
	 * @param archive
	 *            handle to the archive into which the item must be uploaded
	 * @param pubDB
	 *            handle to the publication database for storing the metadata of
	 *            the archived item
	 */
	public PushTask(final ArchiveJob job, final ArchiveRepo archive,
			final PublicationDatabase pubDB) {
		this.job = job;
		this.archive = archive;
		this.pubDB = pubDB;
		declareSteps(COMMIT_META, CREATE_PROJECT, PUSH_REPO);
	}

	@Override
	protected void cleanup() {
		if (project == null || !project.isEmpty())
			return; // never remove projects that we didn't create!
		// FIXME impl create-and-move workflow so we can remove project here
		// project.deleteProject();
		project = null;
	}

	@Override
	protected void execute() throws GitAPIException, URISyntaxException,
			IOException, ProjectExistsException {
		now = new Date();
		beginTask(COMMIT_META, job.selectedRefs.size());
		commitMetadataToRepo();

		final String id = Config.getRandomID();
		beginTask(CREATE_PROJECT, 1);
		project = archive.createProject(id, job.access == ArchiveAccess.PUBLIC,
				job.meta);

		beginTask(PUSH_REPO, 1);
		pushRepoToArchive();

		beginTask(CREATE_METADATA, 1);
		itemUUID = createItemInDB(project.getWebURL(), job.meta,
				job.access == ArchiveAccess.PUBLIC);

		// FIXME for create-and-move, move project to final archive here

		// now that we're done, get rid of the temporary clone
		// TODO determine how to handle ZIP file generation without extra clone
		// for that we'd like to keep this thing around, but we might never
		// learn that the user just isn't going to create a bibliographical
		// record any more. we can add a "done" button somewhere, but the user
		// needs not click it.
		job.clone.dispose();
	}

	private void commitMetadataToRepo() throws IOException {
		final TransferRepo repo = job.clone;
		final String version = job.meta.get(MetadataField.VERSION);
		final ObjectId versionFile = repo.insertBlob(version);
		// TODO this should definitely be configurable!
		final PersonIdent sara = new PersonIdent("SARA Service",
				"ingest@sara-service.org");

		heads = new HashMap<Ref, String>();
		for (Ref ref : job.selectedRefs) {
			final LicenseInfo license = job.licensesInfo.getLicense(ref);
			final License replace = license.getReplacementLicense();
			final ObjectId licenseFile;
			if (replace != null) {
				final String data = job.config.getLicenseText(replace.id);
				// TODO replace placeholders in license text
				licenseFile = repo.insertBlob(data);
			} else
				licenseFile = license.getLicenseFileToKeep().hash;
			final String metaXML = getMetadataXML(
					license.getEffectiveLicense().id);

			final Map<String, ObjectId> metaFiles = new HashMap<>(4);
			// TODO maybe only update in master branch??
			metaFiles.put(MetadataExtractor.VERSION_FILE, versionFile);
			metaFiles.put(METADATA_FILENAME, repo.insertBlob(metaXML));
			// canonicalize license filename. that is, delete the existing
			// license file if we don't like its name, and always create one
			// with the proper name.
			final LicenseFile existingLicense = job.getDetectedLicense(ref);
			if (existingLicense != null && !existingLicense.path
					.equals(MetadataExtractor.PREFERRED_LICENSE_FILE))
				metaFiles.put(existingLicense.path, null);
			metaFiles.put(MetadataExtractor.PREFERRED_LICENSE_FILE,
					licenseFile);

			final CommitBuilder commit = new CommitBuilder();
			commit.setCommitter(sara);
			commit.setAuthor(sara);
			commit.setMessage("archive version " + version);
			commit.addParentId(repo.getCommit(ref).getId());
			commit.setTreeId(repo.updateFiles(ref, metaFiles));
			final ObjectId commitId = repo.insertCommit(commit);

			final RefUpdate ru = repo.getRepo()
					.updateRef(Constants.R_REFS + "rewritten/" + ref.path);
			ru.setNewObjectId(commitId);
			ru.forceUpdate();
			heads.put(ref, ru.getName());
		}
	}

	private String getMetadataXML(final String licenseID) {
		final MetadataFormatter formatter = new MetadataFormatter();
		formatter.addDC("title", job.meta.get(MetadataField.TITLE));
		formatter.addDC("description", job.meta.get(MetadataField.DESCRIPTION));
		final String submitter = job.meta.get(MetadataField.SUBMITTER_SURNAME)
				+ ", " + job.meta.get(MetadataField.SUBMITTER_GIVENNAME);
		formatter.addDC("publisher", submitter);
		formatter.addDC("date", ISO8601.format(now));
		formatter.addDC("type", "Software");
		formatter.addDC("rights", licenseID);
		// TODO include version and main branch as non-DC items?
		return formatter.getSerializedXML();
	}

	private void pushRepoToArchive() throws GitAPIException, URISyntaxException,
			InvalidRemoteException, TransportException {
		final Git git = Git.wrap(job.clone.getRepo());
		// remove remote before recreating it. it may otherwise still contain
		// stale information from a previous execution.
		final RemoteRemoveCommand rm = git.remoteRemove();
		rm.setName(TARGET_REMOTE);
		rm.call();
		final RemoteAddCommand add = git.remoteAdd();
		add.setName(TARGET_REMOTE);
		add.setUri(new URIish(project.getPushURI()));
		add.call();
		// again not calling endTask() here; push will take a while to connect
		// and get started
		update(1);

		final PushCommand push = git.push();
		final ArrayList<RefSpec> spec = new ArrayList<RefSpec>(
				job.selectedRefs.size());
		for (final Ref r : job.selectedRefs)
			spec.add(
					new RefSpec()
							.setSourceDestination(heads.get(r),
									Constants.R_REFS + r.path)
							.setForceUpdate(true));
		push.setRefSpecs(spec);
		push.setRemote(TARGET_REMOTE);

		project.setCredentials(push);
		push.setProgressMonitor(this).call();
	}

	private UUID createItemInDB(final String webURL,
			Map<MetadataField, String> meta, boolean isPublic) {
		final Item i = new Item();
		i.archive_uuid = job.archiveUUID;
		i.source_uuid = job.sourceUUID;
		i.item_state = ItemState.CREATED.name();
		i.item_state_sent = i.item_state;

		i.item_type = (isPublic ? ItemType.ARCHIVE_PUBLIC
				: ItemType.ARCHIVE_HIDDEN).name();

		i.source_user_id = job.sourceUserID;
		i.contact_email = job.gitrepoEmail;
		i.date_created = now;
		i.date_last_modified = i.date_created;

		// initialization with reasonable defaults
		// email from working gitlab
		i.repository_login_id = job.gitrepoEmail;
		// submitter of publication
		// FIXME store as separate fields in item!
		i.meta_submitter = job.meta.get(MetadataField.SUBMITTER_SURNAME) + ", "
				+ job.meta.get(MetadataField.SUBMITTER_GIVENNAME);
		// version of git project
		i.meta_version = meta.get(MetadataField.VERSION);
		// title of git project
		i.meta_title = meta.get(MetadataField.TITLE);
		// description of git project
		i.meta_description = meta.get(MetadataField.DESCRIPTION);
		// URL where the archive has been deposited
		i.archive_url = webURL;

		i.is_public = isPublic;
		// randomly generated access token for the user
		i.token = Config.getToken();

		this.item = pubDB.insertInDB(i);

		logger.info("Item submission succeeded with item uuid "
				+ item.uuid.toString());
		return item.uuid;
	}

	public ArchiveJob getArchiveJob() {
		return job;
	}

	public UUID getItemUUID() {
		if (itemUUID == null)
			throw new IllegalStateException(
					"getItemUUID() on running PushTask");
		return itemUUID;
	}

	public String getWebURL() {
		return project.getWebURL();
	}

	public String getAccessToken() {
		return item.token;
	}
}
