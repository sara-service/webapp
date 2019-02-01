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

	// FIXME should extend ArchiveMetadata instead!!
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
		public final String submitter_surname;
		@JsonProperty
		public final String submitter_givenname;
		@JsonProperty
		@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
		public final Date date;
		@JsonProperty
		public final String token;

		public ItemInfo(final Item item, final boolean authenticated) {
			this.item = item.uuid;
			isPublic = item.is_public;
			title = item.title;
			description = item.description;
			version = item.version;
			submitter_surname = item.submitter_surname;
			submitter_givenname = item.submitter_givenname;
			date = item.date_created;
			url = isPublic ? item.archive_url : null;
			token = authenticated ? item.token : null;
		}
	}
}
