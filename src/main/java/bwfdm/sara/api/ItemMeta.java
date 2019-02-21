package bwfdm.sara.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.Config;
import bwfdm.sara.project.ArchiveMetadata;
import bwfdm.sara.project.Project;
import bwfdm.sara.project.PublicationSession;
import bwfdm.sara.publication.Item;

@RestController
@RequestMapping("/api/item")
public class ItemMeta {
	@Autowired
	private Config config;

	@GetMapping("{uuid}")
	public ItemInfo getItemInfo(@PathVariable("uuid") final UUID itemID,
			@RequestParam(name = "token", required = false) final String token,
			final HttpSession session) {
		final Item item = config.getPublicationDatabase().getItem(itemID);

		if (isAuthenticated(item, token, session))
			return new ItemInfo(item, true);
		else if (item.is_public)
			return new ItemInfo(item, false);
		else
			throw new NoSuchElementException(
					"item " + itemID + " not accessible to logged-in user");
	}

	private boolean isAuthenticated(final Item item, final String token,
			final HttpSession session) {
		if (token != null && Config.normalizeToken(token).equals(item.token))
			return true;

		if (!Project.hasInstance(session))
			return false;
		final Project project = Project.getCompletedInstance(session);
		final UUID sourceID = UUID.fromString(project.getRepoID());
		final String userID = project.getGitRepo().getUserInfo().userID;
		return item.source_uuid.equals(sourceID)
				&& item.source_user_id.equals(userID);
	}

	@GetMapping("list")
	public List<ItemInfo> getArchivedItems(final HttpSession session) {
		// special case: this can be called directly after login, before there
		// is a PublicationSession. if so, get the relevant info from the
		// Project instead.
		final UUID sourceUUID;
		final String userID;
		if (PublicationSession.hasInstance(session)) {
			final PublicationSession project = PublicationSession
					.getInstance(session);
			sourceUUID = project.getSourceUUID();
			userID = project.getSourceUserID();
		} else {
			// NoSessionException thrown here if there is no session.
			// (and we definitely want that exception here)
			final Project project = Project.getCompletedInstance(session);
			sourceUUID = UUID.fromString(project.getRepoID());
			userID = project.getGitRepo().getUserInfo().userID;
		}

		// WARNING this discloses the token (so it's in the URL when coming from
		// the resume screen). that is, make absolutely sure this isn't publicly
		// accessible!!
		final List<Item> items = config.getPublicationDatabase()
				.getPublishedItems(sourceUUID, userID);
		final List<ItemInfo> res = new ArrayList<ItemInfo>(items.size());
		for (final Item item : items)
			res.add(new ItemInfo(item, true));
		return res;
	}

	@JsonInclude(Include.NON_NULL)
	public static class ItemInfo extends ArchiveMetadata {
		@JsonProperty
		public final UUID item;

		@JsonProperty
		public final UUID source;
		@JsonProperty
		public final String url;
		@JsonProperty
		@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
		public final Date date;

		@JsonProperty("public_access")
		public final boolean isPublic;
		@JsonProperty
		public final String token;

		public ItemInfo(final Item item, final boolean authenticated) {
			super(item.title, item.description, item.version, item.master,
					item.submitter_surname, item.submitter_givenname);
			setAuthors(item.authors);
			this.source = item.source_uuid;
			this.item = item.uuid;
			isPublic = item.is_public;
			date = item.date_created;
			url = isPublic ? item.archive_url : null;
			token = authenticated ? item.token : null;
		}
	}
}
