package bwfdm.sara.project;

import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import bwfdm.sara.Config;
import bwfdm.sara.api.Authorization;
import bwfdm.sara.auth.AuthProvider;
import bwfdm.sara.publication.Item;
import bwfdm.sara.publication.db.PublicationDatabase;

public class PublicationSession {
	private static final String PROJECT_ATTR = PublicationSession.class.getCanonicalName();

	private final Config config;
	private final UUID sourceUUID;
	private final UUID itemUUID;
	private final AuthProvider auth;
	private String userID;
	private Item item;

	private PublicationSession(final UUID sourceUUID, final AuthProvider auth,
			final UUID itemUUID, final Config config) {
		this.sourceUUID = sourceUUID;
		this.auth = auth;
		this.itemUUID = itemUUID;
		this.config = config;
	}

	public UUID getSourceUUID() {
		return sourceUUID;
	}

	public UUID getItemUUID() {
		return itemUUID;
	}

	public String getSourceUserID() {
		checkHaveItem();
		return userID;
	}

	public void initialize() {
		final String userID = auth.getUserInfo().userID;
		// check that the user actually owns the item, and show a nasty error
		// message if not
		final Item item = config.getPublicationDatabase()
				.updateFromDB(new Item(itemUUID));
		if (!item.source_uuid.equals(sourceUUID)
				|| !item.source_user_id.equals(userID))
			throw new InvalidItemException(userID);

		this.userID = userID;
		this.item = item;
	}

	private void checkHaveItem() {
		if (!hasItem())
			throw new NoSessionException();
	}

	public boolean hasItem() {
		return item != null;
	}

	public Item getItem() {
		checkHaveItem();
		return item;
	}

	public PublicationDatabase getPublicationDatabase() {
		checkHaveItem();
		return config.getPublicationDatabase();
	}

	public static PublicationSession getInstance(final HttpSession session) {
		final PublicationSession repo = (PublicationSession) session.getAttribute(PROJECT_ATTR);
		if (repo == null)
			throw new NoSessionException();
		return repo;
	}

	public static boolean hasInstance(final HttpSession session) {
		return session.getAttribute(PROJECT_ATTR) != null;
	}

	/**
	 * Creates a new {@link PublicationSession} instance, overwriting the
	 * previous one. Meant to be called by the {@link Authorization login /
	 * session creation code} only!
	 *
	 * @param session
	 *            the user's {@link HttpSession}
	 * @param sourceUUID
	 *            UUID of the data source
	 * @param auth
	 *            {@link AuthProvider} that authenticated the user
	 * @param itemUUID
	 *            UUID of the archival item which is about to be published (must
	 *            not be <code>null</code>)
	 * @param config
	 *            the global {@link Config} object (use {@link Autowired} to
	 *            have Spring inject it)
	 */
	public static PublicationSession createInstance(final HttpSession session,
			final UUID sourceUUID, final AuthProvider auth, final UUID itemUUID,
			final Config config) {
		final PublicationSession publish = new PublicationSession(sourceUUID,
				auth, itemUUID, config);
		session.setAttribute(PROJECT_ATTR, publish);
		return publish;
	}

	public AuthProvider getAuth() {
		return auth;
	}

	@SuppressWarnings("serial")
	public static class NoSessionException extends RuntimeException {
		private NoSessionException() {
			super("session expired or not found");
		}
	}

	@SuppressWarnings("serial")
	public class InvalidItemException extends RuntimeException {
		private InvalidItemException(final String email) {
			super("user (" + sourceUUID + ", " + email + ") not owner of "
					+ itemUUID);
		}
	}
}
