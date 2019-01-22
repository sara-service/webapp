package bwfdm.sara.project;

import java.util.Calendar;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import bwfdm.sara.Config;
import bwfdm.sara.api.Authorization;
import bwfdm.sara.auth.AuthProvider;
import bwfdm.sara.publication.Item;
import bwfdm.sara.publication.ItemState;
import bwfdm.sara.publication.db.PublicationDatabase;
import bwfdm.sara.publication.db.PublicationField;

public class PublicationSession {
	private static final String PROJECT_ATTR = PublicationSession.class
			.getCanonicalName();

	private final Config config;
	private final UUID sourceUUID;
	private final UUID itemUUID;
	private final AuthProvider auth;
	private final EnumMap<PublicationField, String> meta = new EnumMap<>(
			PublicationField.class);
	private String userID;
	private Item item;
	private String verificationCode;

	private PublicationSession(final UUID sourceUUID, final AuthProvider auth,
			final UUID itemUUID, final Config config) {
		this.sourceUUID = sourceUUID;
		this.auth = auth;
		this.itemUUID = itemUUID;
		this.config = config;
		this.verificationCode = null;
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

		// initialize publication metadata from item
		meta.put(PublicationField.TITLE, item.meta_title);
		meta.put(PublicationField.VERSION, item.meta_version);
		meta.put(PublicationField.DESCRIPTION, item.meta_description);
		meta.put(PublicationField.SUBMITTER, item.meta_submitter);
		meta.put(PublicationField.ARCHIVE_URL, item.archive_url);
		meta.put(PublicationField.PUBREPO_LOGIN_EMAIL,
				item.repository_login_id);
		meta.put(PublicationField.VERIFY_USER, "true"); // FIXME! This needs to
														// be
														// somehow
														// configurable!!!

		this.userID = userID;
		this.item = item;

		updatePubID();
	}

	void updatePubID() {
		Hash h = new Hash();
		// FIXME this is better to be casted somehow, order may change the hash! Help!!! Matthias?
		for (Map.Entry<PublicationField, String> entry : this.meta.entrySet())
		{
			if (entry.getKey() != PublicationField.PUBID) {
				h.add(entry.getKey().getDisplayName());
				h.add(entry.getValue());
			}
		}
		meta.put(PublicationField.PUBID, h.getHash());
	}

	// this will return a pseudo-random hash code
	public String getVerificationCode() {
		updatePubID();
		Hash h = new Hash();
		h.add(meta.get(PublicationField.PUBID));
		h.add(String.valueOf(Calendar.getInstance().getTimeInMillis()));
		// TODO replace characters that break selection
		// via double click in the email
		this.verificationCode = h.getHash().replaceAll("-", "");
		return this.verificationCode;
	}

	// check whether the generated code matches the given one!
	public boolean verifyCode(String code) {
		// we have no generated code hence
		// do not want verification
		if (this.verificationCode == null) {
			return true;
		}
		// we want verification
		return code.equals(this.verificationCode);
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

	public boolean isDone() {
		// if uninitialized, there is no well-defined answer to whether it's
		// done or not. just throw an exception instead...
		checkHaveItem();

		// update the item
		final Item item = config.getPublicationDatabase()
				.updateFromDB(new Item(itemUUID));
		// do not lose an open session while item is in CREATED state
		if (item.item_state.equals(ItemState.CREATED.name())) {
			return false;
		} else {
			return true;
		}

	}

	public PublicationDatabase getPublicationDatabase() {
		checkHaveItem();
		return config.getPublicationDatabase();
	}

	public Map<PublicationField, String> getMetadata() {
		return meta;
	}

	public void setMetadata(final Map<PublicationField, String> values) {
		meta.putAll(values);
		updatePubID();
	}

	public static PublicationSession getInstance(final HttpSession session) {
		final PublicationSession repo = (PublicationSession) session
				.getAttribute(PROJECT_ATTR);
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
