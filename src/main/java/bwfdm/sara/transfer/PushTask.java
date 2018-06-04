package bwfdm.sara.transfer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;

import bwfdm.sara.Config;
import bwfdm.sara.git.ArchiveProject;
import bwfdm.sara.git.ArchiveRepo;
import bwfdm.sara.git.ArchiveRepo.ProjectExistsException;
import bwfdm.sara.project.MetadataField;
import bwfdm.sara.project.Ref;
import bwfdm.sara.publication.Archive;
import bwfdm.sara.publication.Item;
import bwfdm.sara.publication.PublicationRepository;
import bwfdm.sara.publication.Source;
import bwfdm.sara.publication.db.PublicationDatabase;

public class PushTask extends Task {
	private static final String TARGET_REMOTE = "target";

	private final Repository repo;
	private final Collection<Ref> refs;
	private final ArchiveRepo archive;
	private final Map<MetadataField, String> meta;
	private final boolean visible;
	private final PublicationDatabase pubDB;
	private ArchiveProject project;

	public PushTask(final TransferRepo repo, final Collection<Ref> refs, final ArchiveRepo archive,
			final Map<MetadataField, String> meta, final boolean visible,
			final PublicationDatabase pubDB) {
		this.pubDB = pubDB;
		this.repo = repo.getRepo();
		this.refs = refs;
		this.archive = archive;
		this.meta = meta;
		this.visible = visible;
	}

	@Override
	protected void cleanup() {
		if (project == null || !project.isEmpty())
			return; // never remove projects that we didn't create!
		// FIXME SARA user should not need this permission on the archive
		project.deleteProject();
		project = null;
	}

	@Override
	protected void execute() throws GitAPIException, URISyntaxException,
			IOException, ProjectExistsException {
		beginTask("NOT committing metadata to git archive"
				+ " (not implemented yet, either)", 1);
		// TODO commit submitted_metadata.xml to repo

		final String id = Config.getRandomID();
		beginTask("Creating project " + id, 1);
		project = archive.createProject(id, visible, meta);

		beginTask("Preparing repository for upload", 1);
		final Git git = Git.wrap(repo);
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
		final ArrayList<RefSpec> spec = new ArrayList<RefSpec>(refs.size());
		for (final Ref r : refs) {
			final String path = Constants.R_REFS + r.path;
			// TODO send locally-created refs unchanged
			spec.add(new RefSpec().setSourceDestination(path, path)
					.setForceUpdate(true));
		}
		push.setRefSpecs(spec);
		push.setRemote(TARGET_REMOTE);

		project.setCredentials(push);
		push.setProgressMonitor(this).call();

		beginTask("write dspace item into database", 1);
		createItemInDB(project.getWebURL(), meta);
		endTask();
	}

	private String createItemInDB(final String webURL, Map<MetadataField, String> meta) {
		final String sourceName = "Generisches Arbeits-GitLab (bwCloud Konstanz)";
		final String archiveName = "Testarchiv";
		final String contactEMail = "s.k@gmail.com";

		List<Source> sources = pubDB.getList(Source.class);
		Source source = null;

		for (final Source s : sources) {
			if (s.display_name.equals(sourceName)) {
				source = s;
				break;
			}
		}

		List<Archive> archives = pubDB.getList(Archive.class);
		Archive archive = null;

		for (final Archive a : archives) {
			if (a.display_name.equals(archiveName)) {
				archive = a;
				break;
			}
		}

		if ( (source == null) || (archive == null) ) {
			logger.error("Error creating item with source=" + sourceName + "//archive=" + archiveName);
			return null;
		} else {
			logger.info("Creating item with source=" + sourceName + "//archive=" + archiveName);
		}
		
		
		Item i = new Item();
		i.archive_uuid = archive.uuid;
		i.source_uuid = source.uuid;
		i.item_state = "CREATED";
		i.item_state_sent = "CREATED";
		i.item_type = "ARCHIVE_HIDDEN";
		i.contact_email = contactEMail;
		
		i.date_created = new Date();
		i.date_last_modified = i.date_created;
		
		i = pubDB.insertInDB(i);
		

		/*
		Map<String, String> metadataMap = new HashMap<String, String>();

		metadataMap.put("abstract", meta.get(MetadataField.DESCRIPTION));
		metadataMap.put("contributor", meta.get(MetadataField.PUBREPO_LOGIN_EMAIL));
		metadataMap.put("title", meta.get(MetadataField.TITLE));
		metadataMap.put("identifier", meta.get(MetadataField.VERSION)); 
		metadataMap.put("type", "Software Sources");
		metadataMap.put("publisher", "SARA Service");
		metadataMap.put("source", webURL);
		metadataMap.put("dateSubmitted", new Date().toString());
		*/

		logger.info("Item submission succeeded with item uuid " + i.uuid.toString());

		return "HAPPY WORKFLOW COMPLETED :)";
	}

	public String getWebURL() {
		if (!isDone())
			throw new IllegalStateException(
					"getWebURL() on in-progress PushTask");
		return project.getWebURL();
	}
}
