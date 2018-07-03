package bwfdm.sara.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import bwfdm.sara.Config;
import bwfdm.sara.project.Project;
import bwfdm.sara.project.PublicationSession;
import bwfdm.sara.publication.Hierarchy;
import bwfdm.sara.publication.Item;
import bwfdm.sara.publication.ItemState;
import bwfdm.sara.publication.PublicationRepository;
import bwfdm.sara.publication.Repository;
import bwfdm.sara.publication.db.PublicationField;

@RestController
@RequestMapping("/api/publish")
public class Publication {
	private static final ISO8601DateFormat ISO8601 = new ISO8601DateFormat();
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
		return config.getPublicationDatabase().getList(Repository.class,
				"where enabled");
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
	public RedirectView triggerPublication(final HttpSession session) {
		final PublicationSession project = PublicationSession
				.getInstance(session);

		// TODO Metadata Mapping
		// TODO Error Handling
		final Map<PublicationField, String> meta = project
				.getPublicationDatabase()
				.getMetadata(project.getItemUUID());

		final UUID repository_uuid = UUID
				.fromString(meta.get(PublicationField.PUBLICATION_REPOSITORY));
		final PublicationRepository repo = project.getPublicationDatabase()
				.getPubRepo(repository_uuid);

		Map<String, String> metadataMap = new HashMap<String, String>();
		final String collectionURL = meta
				.get(PublicationField.PUBREPO_COLLECTION);
		final String userLogin = meta.get(PublicationField.PUBREPO_LOGIN_EMAIL);

		metadataMap.put("sara-abstract", meta.get(PublicationField.DESCRIPTION));
		metadataMap.put("sara-submitter", meta.get(PublicationField.SUBMITTER));
		metadataMap.put("sara-author", meta.get(PublicationField.SUBMITTER)+"(Author)");
		metadataMap.put("sara-title", meta.get(PublicationField.TITLE));
		metadataMap.put("sara-version", meta.get(PublicationField.VERSION));
		// FIXME dc.type needs to be configurable by IR maintainers
		metadataMap.put("sara-type", "Software");
		metadataMap.put("sara-publisher", "SARA Service version 'Prototype'");
		metadataMap.put("sara-archiveUrl", meta.get(PublicationField.ARCHIVE_URL));
		
		Item i = project.getItem();
		i.date_last_modified = new Date();
		i.item_state = ItemState.SUBMITTED.name();
		i.repository_uuid = repository_uuid;
		i.collection_id = collectionURL;

		metadataMap.put("sara-dateArchived", ISO8601.format(i.date_created));
		
		final String depositUrl = repo.publishMetadata(userLogin, collectionURL,
				metadataMap);

		i.repository_url = depositUrl;

		project.getPublicationDatabase().updateInDB(i);

		Map<PublicationField, String> m = new HashMap<PublicationField, String>();
		m.put(PublicationField.REPOSITORY_URL, i.repository_url);
		config.getPublicationDatabase().setMetadata(project.getItemUUID(), m);

		return new RedirectView("/final.html");
	}

	@GetMapping("list")
	public List<PublicationItem> getArchivedItems(final HttpSession session) {
		// special case: this can be called directly after login, before there
		// is a PublicationSession. if so, get the relevant info from the
		// Project instead.
		final UUID sourceUUID;
		final String userID;
		if (PublicationSession.hasInstance(session)) {
			PublicationSession project = PublicationSession
					.getInstance(session);
			sourceUUID = project.getSourceUUID();
			userID = project.getSourceUserID();
		} else {
			// NoSessionException thown here if there is no session.
			// (and we definitely want that exception here)
			Project project = Project.getCompletedInstance(session);
			sourceUUID = UUID.fromString(project.getRepoID());
			userID = project.getGitRepo().getUserInfo().userID;
		}

		final List<Item> items = config.getPublicationDatabase()
				.getPublishedItems(sourceUUID, userID);
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
		@JsonProperty("description")
		public final String description;
		@JsonProperty("archive_url")
		public final String archive_url;
		@JsonProperty("item")
		public final UUID item;

		public PublicationItem(final UUID source, final Item item) {
			this.item = item.uuid;
			this.source = source;
			title = item.meta_title;
			version = item.meta_version;
			description = item.meta_description;
			archive_url = item.archive_url;
		}
	}
}
