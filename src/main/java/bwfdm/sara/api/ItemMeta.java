package bwfdm.sara.api;

import java.util.Date;
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
import bwfdm.sara.project.Project;
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
		final Item item = config.getPublicationDatabase()
				.updateFromDB(new Item(itemID));
		if (!canAccess(item, token, session))
			throw new NoSuchElementException(
					"item " + itemID + " not accessible to logged-in user");
		return new ItemInfo(item);
	}

	private boolean canAccess(final Item item, final String token,
			final HttpSession session) {
		if (item.is_public)
			return true;
		if (token != null)
			return Config.normalizeToken(token).equals(item.token);

		if (!Project.hasInstance(session))
			return false;
		final Project project = Project.getCompletedInstance(session);
		final UUID sourceID = UUID.fromString(project.getRepoID());
		final String userID = project.getGitRepo().getUserInfo().userID;
		return item.source_uuid.equals(sourceID)
				&& item.source_user_id.equals(userID);
	}

	@JsonInclude(Include.NON_NULL)
	public static class ItemInfo {
		@JsonProperty
		public final UUID item;
		@JsonProperty("public_access")
		public final boolean isPublic;

		@JsonProperty
		public final String title;
		@JsonProperty
		public final String description;
		@JsonProperty
		public final String version;
		@JsonProperty
		public final String url;
		@JsonProperty
		public final String submitter;
		@JsonProperty
		@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
		public final Date date;
		@JsonProperty
		public final String token;

		public ItemInfo(final Item item) {
			this.item = item.uuid;
			isPublic = item.is_public;
			title = item.meta_title;
			description = item.meta_description;
			version = item.meta_version;
			submitter = item.meta_submitter;
			date = item.date_created;
			if (isPublic) {
				url = item.archive_url;
				// deliberately not revealing the token here. if we ever use it
				// for anything on public items, we definitely don't want to
				// reveal it to everybody!
				token = null;
			} else {
				url = null;
				token = item.token;
			}
		}
	}
}
