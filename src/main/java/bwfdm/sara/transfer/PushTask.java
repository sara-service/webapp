package bwfdm.sara.transfer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;

import bwfdm.sara.Config;
import bwfdm.sara.git.ArchiveProject;
import bwfdm.sara.git.ArchiveRepo.ProjectExistsException;
import bwfdm.sara.project.ArchiveJob;
import bwfdm.sara.project.MetadataField;
import bwfdm.sara.project.Ref;
import bwfdm.sara.publication.Item;
import bwfdm.sara.publication.db.PublicationDatabase;

public class PushTask extends Task {
	private static final String TARGET_REMOTE = "target";
	private final PublicationDatabase pubDB;
	
	private final ArchiveJob job;
	private ArchiveProject project;

	public PushTask(final ArchiveJob job, final PublicationDatabase pubDB) {
		this.job = job;
		this.pubDB = pubDB;
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
		project = job.archive.createProject(id, false, job.meta);

		beginTask("Preparing repository for upload", 1);
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
		for (final Ref r : job.selectedRefs) {
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

		createItemInDB(project.getWebURL(), job.meta);
		endTask();
	}

	private String createItemInDB(final String webURL, Map<MetadataField, String> meta) {
		Item i = new Item();
		i.archive_uuid = UUID.fromString(job.archiveUUID);
		i.source_uuid = UUID.fromString(job.sourceUUID);
		i.item_state = "CREATED";
		i.item_state_sent = "CREATED";
		if (job.isArchiveOnly) {
			i.item_type = "ARCHIVE_HIDDEN";
		} else {
			i.item_type = "ARCHIVE_PUBLIC";
			// TODO make configurable via saradb whether items will be archived externally or within the IRs
		}
		
		i.contact_email = job.gitrepoEmail;

		i.contact_email = job.gitrepoEmail;
		if (i.contact_email == null ) {
			i.contact_email = "NN@nowhere.noob";
		}
		
		i.date_created = new Date();
		i.date_last_modified = i.date_created;
		
		i = pubDB.insertInDB(i);
		

		/* TODO write respective metadatamapping / metadatavalue
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

	public ArchiveJob getArchiveJob() {
		return job;
	}

	public UUID getItemUUID() {
		if (!isDone())
			throw new IllegalStateException(
					"getItemUUID() on in-progress PushTask");
		// FIXME return the actual item UUID here
		return UUID.fromString("deadbeef-dead-dead-dead-beeeeeeeeeef");
	}
}
