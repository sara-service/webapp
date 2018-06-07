package bwfdm.sara.api;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.Config;
import bwfdm.sara.project.Project;
import bwfdm.sara.project.PublicationSession;
import bwfdm.sara.publication.Hierarchy;
import bwfdm.sara.publication.Item;
import bwfdm.sara.publication.PublicationRepository;
import bwfdm.sara.publication.Repository;
import bwfdm.sara.publication.db.PublicationField;

@RestController
@RequestMapping("/api/publish")
public class Publication {
	private static final Log logger = LogFactory.getLog(Publication.class);
	@Autowired
	private Config config;

	@GetMapping("")
	public PubrepoConfig getPubRepoConfig(final HttpSession session) {
		return new PubrepoConfig(getMetadata(session), getPubRepos());
	}

	public class PubrepoConfig {
		@JsonProperty("meta")
		public final Map<PublicationField, String> metadata;
		@JsonProperty("repos")
		public final List<Repository> repos;

		public PubrepoConfig(Map<PublicationField, String> meta,
				List<Repository> repos) {
			this.metadata = meta;
			this.repos = repos;
		}
	}

	@GetMapping("repos")
	public List<Repository> getPubRepos() {
		return config.getPublicationDatabase().getList(Repository.class);
	}

	@GetMapping("meta")
	public Map<PublicationField, String> getMetadata(
			final HttpSession session) {
		PublicationSession project = PublicationSession.getInstance(session);
		final Map<PublicationField, String> data = project
				.getPublicationDatabase().getMetadata(project.getItemUUID());
		// make sure all keys exist (JavaScript needs this)
		for (PublicationField field : PublicationField.values())
			if (!data.containsKey(field))
				data.put(field, null);
		return data;
	}

	@PutMapping("meta")
	public void setMetadata(
			@RequestBody final Map<PublicationField, String> values,
			final HttpSession session) {
		final PublicationSession project = PublicationSession
				.getInstance(session);
		project.getPublicationDatabase().setMetadata(project.getItemUUID(),
				values);
	}

	@GetMapping("trigger")
	public RedirectView triggerPublication() {
		if (true)
			// TODO
			throw new UnsupportedOperationException(
					"here be code to publish stuff");
		return new RedirectView("/get-lost.html");
	}

	@GetMapping("list")
	public List<PublicationItem> getArchivedItems(final HttpSession session) {
		// special case: this can be called directly after login, before there
		// is a PublicationSession. if so, get the relevant info from the
		// Project instead.
		final UUID sourceUUID;
		final String email;
		if (PublicationSession.hasInstance(session)) {
		PublicationSession project = PublicationSession.getInstance(session);
			sourceUUID = project.getSourceUUID();
			email = project.getSourceEmail();
		} else {
			Project project = Project.getInstance(session);
			sourceUUID = UUID.fromString(project.getRepoID());
			email = project.getGitRepo().getUserInfo().email;
		}

		final List<Item> items = config.getPublicationDatabase()
				.getPublishedItems(sourceUUID, email);
		final List<PublicationItem> res = new ArrayList<PublicationItem>(
				items.size());
		for (Item item : items)
			res.add(new PublicationItem(sourceUUID, item));
		return res;
	}

	@PostMapping("query-hierarchy")
	public CollectionList queryHierarchy(
			@RequestParam("user_email") final String user_email,
			@RequestParam("repo_uuid") final String repo_uuid) {
		// FIXME should use WHERE clause instead!
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

	public class PublicationItem {
		@JsonProperty("source")
		public final UUID source;
		@JsonProperty("title")
		public final String title;
		@JsonProperty("version")
		public final String version;
		@JsonProperty("item")
		public final UUID item;

		public PublicationItem(final UUID source, final Item item) {
			this.item = item.uuid;
			this.source = source;
			// FIXME how do we get the title and version here??
			title = "$" + item.uuid.toString();
			version = "1.0";
		}
	}
}
