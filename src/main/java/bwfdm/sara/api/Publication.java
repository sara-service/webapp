package bwfdm.sara.api;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.Config;
import bwfdm.sara.api.Metadata.MetadataValue;
import bwfdm.sara.project.MetadataField;
import bwfdm.sara.project.Project;
import bwfdm.sara.publication.Hierarchy;
import bwfdm.sara.publication.PublicationRepository;
import bwfdm.sara.publication.Repository;

@RestController
@RequestMapping("/api/publish")
public class Publication {
	private static final Log logger = LogFactory.getLog(Publication.class);
	@Autowired
	private Config config;

	@GetMapping("")
	public PubrepoConfig getPubRepoConfig(final HttpSession session) {
		final Project project = Project.getInstance(session);

		final Map<MetadataField, MetadataValue> meta = Metadata
				.getAllFields(null, project);
		final List<Repository> repos = config.getPublicationDatabase()
				.getList(Repository.class);

		return new PubrepoConfig(meta, repos);
	}

	public class PubrepoConfig {
		@JsonProperty("meta")
		public final Map<MetadataField, MetadataValue> metadata;
		@JsonProperty("repos")
		public final List<Repository> repos;

		public PubrepoConfig(Map<MetadataField, MetadataValue> meta,
				List<Repository> repos) {
			this.metadata = meta;
			this.repos = repos;
		}
	}

	@GetMapping("trigger")
	public RedirectView triggerPublication() {
		if (true)
			// TODO
			throw new UnsupportedOperationException(
					"here be code to publish stuff");
		return new RedirectView("/get-lost.html");
	}

	@PostMapping("query-hierarchy")
	public CollectionList queryHierarchy(
			@RequestParam("user_email") final String user_email,
			@RequestParam("repo_uuid") final String repo_uuid) {
		List<PublicationRepository> pubRepos = config.getPublicationDatabase()
				.getPubRepos();
		PublicationRepository repo = getPubRepo(pubRepos, repo_uuid);

		if (!repo.isUserRegistered(user_email)) {
			logger.info("User " + user_email + " is NOT registered!");
			return new CollectionList(false);
		}
		logger.debug("User " + user_email + " is registered!");

		if (!repo.isUserAssigned(user_email)) {
			logger.info("User " + user_email
					+ " is registered but does not have submit rights to anything!");
			return new CollectionList(true);
		}
		logger.debug("User " + user_email
				+ " is registered and has submit rights to some collections!");

		Hierarchy root = repo.getHierarchy(user_email);
		// root.dump("");
		return new CollectionList(root);

	}

	private PublicationRepository getPubRepo(
			final List<PublicationRepository> pubRepos,
			final String repo_uuid) {
		final UUID uuid = UUID.fromString(repo_uuid);
		for (PublicationRepository r : pubRepos)
			if (r.getDAO().uuid.equals(uuid))
				return r;
		throw new NoSuchElementException(
				"no PublicationRepository with UUID " + repo_uuid);
	}

	public class CollectionList {
		@JsonProperty("user-valid")
		public final boolean isUserValid;
		@JsonProperty("hierarchy")
		public final Hierarchy accessibleCollections;

		public CollectionList(boolean isUserValid) {
			this.isUserValid = isUserValid;
			this.accessibleCollections = null;
		}

		public CollectionList(Hierarchy accessibleCollections) {
			this.isUserValid = true;
			this.accessibleCollections = accessibleCollections;
		}
	}
}
