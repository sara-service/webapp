package bwfdm.sara.transfer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
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
import bwfdm.sara.extractor.LicenseFile;
import bwfdm.sara.extractor.MetadataExtractor;
import bwfdm.sara.git.ArchiveProject;
import bwfdm.sara.git.ArchiveRepo;
import bwfdm.sara.git.ArchiveRepo.ProjectExistsException;
import bwfdm.sara.project.ArchiveJob;
import bwfdm.sara.project.MetadataField;
import bwfdm.sara.project.Ref;
import bwfdm.sara.project.Ref.RefType;
import bwfdm.sara.publication.Item;
import bwfdm.sara.publication.ItemState;
import bwfdm.sara.publication.ItemType;
import bwfdm.sara.publication.db.PublicationDatabase;

/** Pushes a repository to a git archive. */
public class PushTask extends Task {
	private static final ISO8601DateFormat ISO8601 = new ISO8601DateFormat();
	private static final String METADATA_FILENAME = "submitted_metadata";
	private static final Charset UTF8 = Charset.forName("UTF-8");
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
	}

	@Override
	protected void cleanup() {
		if (project == null || !project.isEmpty())
			return; // never remove projects that we didn't create!
		// FIXME SARA user should not need this permission on the archive
		// FIXME implement create and move workflow so we can remove the project
		// project.deleteProject();
		project = null;
	}

	@Override
	protected void execute() throws GitAPIException, URISyntaxException,
			IOException, ProjectExistsException {
		if (!job.update.isEmpty()) {
			beginTask("updating metadata in git repo", 1);
			updateMetadataInGitRepo();
		}

		now = new Date();
		beginTask("committing metadata to git archive",
				job.selectedRefs.size());
		commitMetadataToRepo();

		final String id = Config.getRandomID();
		beginTask("Creating project " + id, 1);
		project = archive.createProject(id, true, job.meta);

		beginTask("Preparing repository for upload", 1);
		pushRepoToArchive();

		beginTask("Record metadata in database", 1);
		itemUUID = createItemInDB(project.getWebURL(), job.meta);
		endTask();
	}

	private void updateMetadataInGitRepo() {
		final String title = getUpdateValue(MetadataField.TITLE);
		final String desc = getUpdateValue(MetadataField.DESCRIPTION);
		if (title != null || desc != null)
			job.gitProject.updateProjectInfo(title, desc);

		final String version = getUpdateValue(MetadataField.VERSION);
		if (version != null) {
			final Ref ref = new Ref(job.meta.get(MetadataField.MAIN_BRANCH));
			if (!ref.type.equals(RefType.BRANCH))
				throw new IllegalArgumentException(
						"attempt to update VERSION in " + ref);
			// TODO should probably use JGit here
			// if we're ever going to write back the LICENSE, with JGit that's
			// doable in a single commit. with putBlob (or the GitLab API, for
			// that matter), committing several files currently isn't possible.
			job.gitProject.putBlob(ref.name, MetadataExtractor.VERSION_FILE,
					"update version to " + version, version.getBytes(UTF8));
		}
	}

	private String getUpdateValue(MetadataField field) {
		if (job.update.contains(field))
			return job.meta.get(field);
		return null;
	}

	private void commitMetadataToRepo() throws IOException {
		final String version = job.meta.get(MetadataField.VERSION);
		final String metaJSON = JSON_MAPPER.writeValueAsString(job);
		// TODO this should definitely be configurable!
		final PersonIdent sara = new PersonIdent("SARA Service",
				"ingest@sara-service.org");

		heads = new HashMap<Ref, String>();
		for (Ref ref : job.selectedRefs) {
			final String licenseID = job.licenses.get(ref);
			final String license = job.config.getLicenseText(licenseID);
			// TODO replace placeholders in license text

			final MetadataFormatter formatter = new MetadataFormatter();
			formatter.addDC("title", job.meta.get(MetadataField.TITLE));
			formatter.addDC("description",
					job.meta.get(MetadataField.DESCRIPTION));
			formatter.addDC("publisher", job.meta.get(MetadataField.SUBMITTER));
			formatter.addDC("date", ISO8601.format(now));
			formatter.addDC("type", "Software");
			formatter.addDC("rights", licenseID);
			// TODO include version and main branch as non-DC items?
			final String metaXML = formatter.getSerializedXML();

			final Map<String, String> metaFiles = new HashMap<>(4);
			metaFiles.put(MetadataExtractor.VERSION_FILE, version);
			metaFiles.put(METADATA_FILENAME + ".json", metaJSON);
			metaFiles.put(METADATA_FILENAME + ".xml", metaXML);
			final LicenseFile existingLicense = job.getDetectedLicense(ref);
			final String licenseFile = existingLicense != null
					? existingLicense.path
					: MetadataExtractor.PREFERRED_LICENSE_FILE;
			metaFiles.put(licenseFile, license);

			final CommitBuilder commit = new CommitBuilder();
			commit.setCommitter(sara);
			commit.setAuthor(sara);
			commit.setMessage("archive version " + version);
			commit.addParentId(job.clone.getCommit(ref).getId());
			commit.setTreeId(job.clone.updateFiles(ref, metaFiles));
			final ObjectId commitId = job.clone.insertCommit(commit);

			final RefUpdate ru = job.clone.getRepo()
					.updateRef(Constants.R_REFS + "rewritten/" + ref.path);
			ru.setNewObjectId(commitId);
			ru.forceUpdate();
			heads.put(ref, ru.getName());
		}
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
			Map<MetadataField, String> meta) {
		Item i = new Item();
		i.archive_uuid = job.archiveUUID;
		i.source_uuid = job.sourceUUID;
		i.item_state = ItemState.CREATED.name();
		i.item_state_sent = i.item_state;

		if (job.isArchiveOnly) {
			i.item_type = ItemType.ARCHIVE_HIDDEN.name();
		} else {
			i.item_type = ItemType.ARCHIVE_PUBLIC.name();
			// TODO make configurable via saradb whether items will be archived
			// externally or within the IRs
		}

		i.source_user_id = job.sourceUserID;
		i.contact_email = job.gitrepoEmail;
		i.date_created = now;
		i.date_last_modified = i.date_created;

		// initialization with reasonable defaults
		// email from working gitlab
		i.repository_login_id = job.gitrepoEmail;
		// submitter of publication
		i.meta_submitter = meta.get(MetadataField.SUBMITTER);
		// version of git project
		i.meta_version = meta.get(MetadataField.VERSION);
		// title of git project
		i.meta_title = meta.get(MetadataField.TITLE);
		// description of git project
		i.meta_description = meta.get(MetadataField.DESCRIPTION);
		// URL where the archive has been deposited
		i.archive_url = webURL;

		i = pubDB.insertInDB(i);

		logger.info("Item submission succeeded with item uuid "
				+ i.uuid.toString());
		return i.uuid;
	}

	public ArchiveJob getArchiveJob() {
		return job;
	}

	public UUID getItemUUID() {
		if (!isDone())
			throw new IllegalStateException(
					"getItemUUID() on in-progress PushTask");
		return itemUUID;
	}
}
