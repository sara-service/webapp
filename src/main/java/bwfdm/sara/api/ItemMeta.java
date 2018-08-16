package bwfdm.sara.api;

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.Config;
import bwfdm.sara.project.ArchiveAccessMode;
import bwfdm.sara.project.Project;
import bwfdm.sara.publication.Item;
import bwfdm.sara.publication.ItemType;

@RestController
@RequestMapping("/api/item")
public class ItemMeta {
	@Autowired
	private Config config;

	@GetMapping("{uuid}")
	public ItemInfo getItemInfo(@PathVariable("uuid") final UUID itemID,
			final HttpSession session) {
		final Item item = config.getPublicationDatabase()
				.updateFromDB(new Item(itemID));
		if (!canAccess(item, session))
			throw new NoSuchElementException(
					"item " + itemID + " not accessible for logged-in user");
		return new ItemInfo(item);
	}

	private boolean canAccess(final Item item, final HttpSession session) {
		final ItemType type = ItemType.valueOf(item.item_type);
		if (type == ItemType.ARCHIVE_PUBLIC)
			return true;
		if (type != ItemType.ARCHIVE_HIDDEN)
			throw new UnsupportedOperationException("ItemType " + type);

		if (!Project.hasInstance(session))
			return false;
		final Project project = Project.getInstance(session);
		final UUID sourceID = UUID.fromString(project.getRepoID());
		final String userID = project.getGitRepo().getUserInfo().userID;
		return item.source_uuid.equals(sourceID)
				&& item.source_user_id.equals(userID);
	}

	@JsonInclude(Include.NON_NULL)
	public static class ItemInfo {
		@JsonProperty
		public final ArchiveAccessMode access;
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

		public ItemInfo(final Item item) {
			access = ArchiveAccessMode.forItemType(item.getType());
			title = item.meta_title;
			description = item.meta_description;
			version = item.meta_version;
			url = item.archive_url;
			submitter = item.meta_submitter;
			date = item.date_created;
		}
	}
}
