package bwfdm.sara.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.db.ConfigDatabase;
import bwfdm.sara.db.FrontendDatabase;
import bwfdm.sara.extractor.LicenseFile;
import bwfdm.sara.extractor.MetadataExtractor;
import bwfdm.sara.git.GitProject;
import bwfdm.sara.project.LicensesInfo.LicenseInfo;
import bwfdm.sara.transfer.TransferRepo;

/** Data class containing all the information needed to archive an item. */
public class ArchiveJob {
	@JsonProperty
	public final UUID sourceUUID;
	@JsonProperty
	public final String sourceProject;
	@JsonProperty
	public final String gitrepoEmail;
	@JsonProperty
	public final String sourceUserID;
	@JsonProperty
	private List<RefAction> actions;
	@JsonProperty
	public final List<Ref> selectedRefs;
	@JsonProperty
	public final Map<MetadataField, String> meta;
	@JsonProperty
	public final Set<MetadataField> update;
	@JsonIgnore
	public final Map<Ref, String> licenses;
	@JsonIgnore
	private Set<LicenseFile> licensesSet;
	@JsonProperty
	public final UUID archiveUUID;
	@JsonIgnore
	public final TransferRepo clone;
	/** <b>For writing back metadata ONLY!! */
	@JsonIgnore
	public final GitProject gitProject;
	@JsonIgnore
	private Map<Ref, LicenseFile> detectedLicenses;
	@JsonIgnore
	public final ConfigDatabase config;
	@JsonIgnore
	public final LicensesInfo licensesInfo;

	public ArchiveJob(final Project project, final String archiveUUID) {
		final FrontendDatabase frontend = project.getFrontendDatabase();
		final MetadataExtractor metadataExtractor = project
				.getMetadataExtractor();
		config = project.getConfigDatabase();

		// index.html
		// UUID.fromString() performs an implicit check for a valid UUID
		sourceUUID = UUID.fromString(project.getRepoID());
		// projects.html
		sourceProject = project.getProjectPath();
		gitProject = project.getGitProject();
		checkNullOrEmpty("sourceProject", sourceProject);
		// oauth metadata
		gitrepoEmail = metadataExtractor.getEmail();
		sourceUserID = metadataExtractor.getUserID();
		checkNullOrEmpty("gitrepoEmail", gitrepoEmail);
		checkNullOrEmpty("sourceUserID", sourceUserID);
		// branches.html
		actions = frontend.getRefActions();
		actions.sort(new Comparator<RefAction>() {
			@Override
			public int compare(RefAction o1, RefAction o2) {
				return o1.ref.path.compareTo(o2.ref.path);
			}
		});
		selectedRefs = new ArrayList<Ref>(actions.size());
		for (RefAction action : actions) {
			selectedRefs.add(action.ref);
			// path implicitly checked by Ref constructor
			if ( action.publicationMethod == null)
				throw new IllegalArgumentException(
						action.ref.path + ".publicationMethod is null");
			checkNullOrEmpty(action.ref.path + ".firstCommit",
					action.firstCommit);
		}
		if (actions.isEmpty())
			throw new IllegalArgumentException(
					"no branches selected for publication");
		clone = project.getTransferRepo();
		// meta.html
		meta = metadataExtractor.get(MetadataField.values());
		meta.putAll(frontend.getMetadata());
		update = frontend.getUpdateMetadata();
		checkField(MetadataField.TITLE, false);
		checkField(MetadataField.DESCRIPTION, true);
		checkField(MetadataField.VERSION, false);
		checkField(MetadataField.MAIN_BRANCH, false);
		checkField(MetadataField.SUBMITTER_FAMILY, false);
		checkField(MetadataField.SUBMITTER_GIVEN, false);
		final Ref master = new Ref(meta.get(MetadataField.MAIN_BRANCH));
		if (!selectedRefs.contains(master))
			throw new IllegalArgumentException(
					"main branch branch not selected for publication");
		// license(s).html
		detectedLicenses = metadataExtractor.getLicenses();
		licenses = frontend.getLicenses();
		licensesSet = metadataExtractor.getLicenseSet();
		licensesInfo = new LicensesInfo(config.getLicenses(), selectedRefs,
				detectedLicenses, licensesSet, licenses);
		if (licensesInfo.hasUndefinedLicenses())
			throw new IllegalArgumentException(
					"not all branches have licenses!");
		// archive selection, currently just hardcoded
		this.archiveUUID = UUID.fromString(archiveUUID); // implicit check
	}

	private void checkField(MetadataField field, boolean mayBeEmpty) {
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

	@JsonProperty("heads")
	public Map<Ref, String> getHeads() {
		HashMap<Ref, String> res = new HashMap<Ref, String>();
		for (Ref ref : selectedRefs)
			res.put(ref, getHead(ref));
		return res;
	}

	@JsonProperty("licenses")
	public List<LicenseInfo> getLicensesPerBranch() {
		return licensesInfo.branches;
	}

	public LicenseFile getDetectedLicense(Ref ref) {
		return detectedLicenses.get(ref);
	}

	private String getHead(final Ref a) {
		try {
			return clone.getCommit(a).getName();
		} catch (IOException e) {
			throw new IllegalStateException("accessing TransferRepo failed", e);
		}
	}

	@JsonProperty("token")
	public String getHash() {
		final Hash buffer = new Hash();
		// index.html
		buffer.add(sourceUUID.toString());
		// projects.html
		buffer.add(sourceProject);
		// oauth metadata
		buffer.add(sourceUserID.toString());
		buffer.add(gitrepoEmail.toString());
		// branches.html
		for (RefAction a : actions) {
			buffer.add(a.ref.path);
			buffer.add(a.publicationMethod.name());
			buffer.add(a.firstCommit);
			buffer.add(getHead(a.ref));
		}
		// meta.html
		// enum values are in a defined order (same order as declared) so we'll
		// be adding them in the same order every time
		for (MetadataField field : MetadataField.values()) {
			buffer.add(meta.get(field));
			buffer.add(update.contains(field) ? "yes" : "no");
		}
		// license(s).html
		for (RefAction a : actions)
			buffer.add(licenses.get(a.ref));
		// archive selection, currently just hardcoded
		buffer.add(archiveUUID.toString());
		return buffer.getHash();
	}

	@Override
	public boolean equals(Object obj) {
		final ArchiveJob job = (ArchiveJob) obj;
		// index.html
		if (!job.sourceUUID.equals(sourceUUID))
			return false;
		// projects.html
		if (!job.sourceProject.equals(sourceProject))
			return false;
		// oauth metadata
		if (!job.sourceUserID.equals(sourceUserID))
			return false;
		if (!job.gitrepoEmail.equals(gitrepoEmail))
			return false;
		// branches.html
		// the lists are sorted, so if they're identical, they must be in the
		// same order as well. we can thus just compare them element by element
		for (int i = 0; i < actions.size(); i++) {
			RefAction a = actions.get(i);
			RefAction b = job.actions.get(i);
			if (!a.ref.equals(b.ref))
				return false;
			if (!a.publicationMethod.equals(b.publicationMethod))
				return false;
			if (!a.firstCommit.equals(b.firstCommit))
				return false;
			if (!getHead(a.ref).equals(getHead(b.ref)))
				return false;
		}
		// meta.html
		for (MetadataField field : MetadataField.values())
			if (!job.meta.get(field).equals(meta.get(field)))
				return false;
		// license(s).html
		for (RefAction a : actions) {
			final String lic = job.licenses.get(a.ref);
			final String other = licenses.get(a.ref);
			if ((lic == null && other != null)
					|| (lic != null && other == null))
				return false;
			if (!lic.equals(other))
				return false;
		}
		// archive selection, currently just hardcoded
		if (!job.archiveUUID.equals(archiveUUID))
			return false;
		return true;
	}
}
