package bwfdm.sara.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bwfdm.sara.db.FrontendDatabase;
import bwfdm.sara.extractor.MetadataExtractor;
import bwfdm.sara.git.ArchiveRepo;
import bwfdm.sara.project.RefAction.PublicationMethod;
import bwfdm.sara.publication.db.PublicationDatabase;
import bwfdm.sara.transfer.TransferRepo;

public class ArchiveJob {
	public final String sourceUUID;
	public final String gitrepoEmail;
	public final String archiveUUID;
	public final Map<MetadataField, String> meta;
	public final ArchiveRepo archive;
	public final List<Ref> selectedRefs;
	public final TransferRepo clone;
	public final boolean isArchiveOnly;
	public final PublicationDatabase pubDB;

	public ArchiveJob(final Project project, final String archiveUUID) {
		final FrontendDatabase frontend = project.getFrontendDatabase();
		final MetadataExtractor metadataExtractor = project
				.getMetadataExtractor();

		sourceUUID = project.getRepoID();
		this.archiveUUID = archiveUUID;
		meta = metadataExtractor.get(MetadataField.values());
		meta.putAll(frontend.getMetadata());
		gitrepoEmail = metadataExtractor.getEmail();
		archive = project.getConfigDatabase().newGitArchive(archiveUUID);
		clone = project.getTransferRepo();
		pubDB = project.getPublicationDatabase();

		List<RefAction> actions = frontend.getRefActions();
		selectedRefs = new ArrayList<Ref>(actions.size());
		boolean isArchiveOnly = true;
		for (RefAction action : actions) {
			selectedRefs.add(action.ref);
			if (action.publicationMethod != PublicationMethod.ARCHIVE_HIDDEN)
				isArchiveOnly = false;
		}
		this.isArchiveOnly = isArchiveOnly;
	}
}
