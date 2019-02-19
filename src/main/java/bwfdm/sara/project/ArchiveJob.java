package bwfdm.sara.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.db.ArchiveAccess;
import bwfdm.sara.db.ConfigDatabase;
import bwfdm.sara.db.FrontendDatabase;
import bwfdm.sara.extractor.LicenseFile;
import bwfdm.sara.extractor.MetadataExtractor;
import bwfdm.sara.git.ArchiveRepoFactory;
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
	public final ArchiveMetadata meta;
	@JsonIgnore
	public final Map<Ref, String> licenses;
	@JsonIgnore
	private Set<LicenseFile> licensesSet;
	@JsonProperty
	public final ArchiveAccess access;
	@JsonProperty
	public final ArchiveRepoFactory archive;
	@JsonIgnore
	public final TransferRepo clone;
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
		// FIXME userMeta should never be null here, but sometimes is!
		final ArchiveMetadata userMeta = frontend.getMetadata();
		meta = metadataExtractor.getMetadata().overrideFrom(userMeta);
		checkNullOrEmpty("title", meta.title);
		checkNull("description", meta.description);
		checkNullOrEmpty("version", meta.version);
		checkNullOrEmpty("master", meta.master);
		checkNullOrEmpty("submitter.surname", meta.submitter.surname);
		checkNullOrEmpty("submitter.givenname", meta.submitter.givenname);
		final Collection<Name> authors = meta.getAuthors();
		if (authors.isEmpty())
			throw new IllegalArgumentException("author list is empty");
		for (final Name a : authors) {
			checkNullOrEmpty("author.surname", a.surname);
			checkNullOrEmpty("author.givenname", a.givenname);
		}
		final Ref master = new Ref(meta.master);
		if (!selectedRefs.contains(master))
			throw new IllegalArgumentException(
					"main branch not selected for publication");
		// license(s).html
		detectedLicenses = metadataExtractor.getLicenses();
		licenses = frontend.getLicenses();
		licensesSet = metadataExtractor.getLicenseSet();
		licensesInfo = new LicensesInfo(config.getLicenses(), selectedRefs,
				detectedLicenses, licensesSet, licenses);
		if (licensesInfo.hasUndefinedLicenses())
			throw new IllegalArgumentException(
					"not all branches have licenses!");
		// access.html
		access = frontend.getArchiveAccess();
		// archive selection, currently just hardcoded
		// getting the factory implicitly checks for existence
		this.archive = config.getGitArchive(archiveUUID);
	}

	private static void checkNullOrEmpty(String name, String value) {
		if (value == null)
			throw new NullPointerException(name + " is null");
		if (value.isEmpty())
			throw new IllegalArgumentException(name + " is empty");
	}

	private static void checkNull(String name, String value) {
		if (value == null)
			throw new NullPointerException(name + " is null");
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
		buffer.add(meta.title);
		buffer.add(meta.description);
		buffer.add(meta.version);
		buffer.add(meta.master);
		buffer.add(meta.submitter.surname);
		buffer.add(meta.submitter.givenname);
		for (final Name a : meta.getAuthors()) {
			buffer.add(a.surname);
			buffer.add(a.givenname);
		}
		// license(s).html
		for (RefAction a : actions)
			buffer.add(licenses.get(a.ref));
		// access.html
		buffer.add(access.name());
		// archive selection, currently just hardcoded
		buffer.add(archive.id);
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
		if (!meta.equals(job.meta))
			return false;
		// license(s).html
		for (RefAction a : actions) {
			final String lic = job.licenses.get(a.ref);
			final String other = licenses.get(a.ref);
			if ((lic == null && other != null)
					|| (lic != null && other == null))
				return false;
			if (lic != null && other != null && !lic.equals(other))
				return false;
		}
		// archive selection, currently just hardcoded
		if (!job.archive.id.equals(archive.id))
			return false;
		return true;
	}
}
