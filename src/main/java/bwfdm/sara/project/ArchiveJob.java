package bwfdm.sara.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import bwfdm.sara.db.FrontendDatabase;
import bwfdm.sara.extractor.MetadataExtractor;
import bwfdm.sara.git.ArchiveRepo;
import bwfdm.sara.project.RefAction.PublicationMethod;
import bwfdm.sara.publication.db.PublicationDatabase;
import bwfdm.sara.transfer.TransferRepo;

public class ArchiveJob {
	public final UUID sourceUUID;
	public final String gitrepoEmail;
	public final UUID archiveUUID;
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

		sourceUUID = UUID.fromString(project.getRepoID());
		this.archiveUUID = UUID.fromString(archiveUUID);
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

		// a few final sanity checks that we have all required values
		checkNullOrEmpty("gitrepoEmail", gitrepoEmail);
		checkField(MetadataField.TITLE, false);
		checkField(MetadataField.DESCRIPTION, true);
		checkField(MetadataField.VERSION, false);
		checkField(MetadataField.MAIN_BRANCH, false);
		if (selectedRefs.isEmpty())
			throw new IllegalArgumentException("no branches selected for publication");
		final Ref master = new Ref(meta.get(MetadataField.MAIN_BRANCH));
		if (!selectedRefs.contains(master))
			throw new IllegalArgumentException(
					"main branch branch not selected for publication");
	}

	public void checkField(MetadataField field, boolean mayBeEmpty) {
		if (!meta.containsKey(field))
			throw new NoSuchElementException(
					"metadata field " + field + " missing");
		String value = meta.get(field);
		if (value == null)
			throw new NoSuchElementException(
					"metadata field " + field + " is null");
		if (!mayBeEmpty && value.isEmpty())
			throw new NoSuchElementException(
					"metadata field " + field + " is empty");
	}

	private static void checkNullOrEmpty(String name, String value) {
		if (value == null)
			throw new NullPointerException(name + " is null");
		if (value.isEmpty())
			throw new IllegalArgumentException(name + " is empty");
	}
}
