package bwfdm.sara.project;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import bwfdm.sara.Config;
import bwfdm.sara.api.Authorization;
import bwfdm.sara.auth.AuthProvider;
import bwfdm.sara.publication.Item;
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
	private String publicationId;
	private String seed;

	private boolean verified;

	private PublicationSession(final UUID sourceUUID, final AuthProvider auth,
			final UUID itemUUID, final Config config) {
		this.sourceUUID = sourceUUID;
		this.auth = auth;
		this.itemUUID = itemUUID;
		this.config = config;
		this.verified = false;
		seed = config.getToken();
		updateHash();
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
		final Item item = config.getPublicationDatabase().getItem(itemUUID);
		if (!item.source_uuid.equals(sourceUUID)
				|| !item.source_user_id.equals(userID))
			throw new InvalidItemException(userID);

		// initialize publication metadata from item
		meta.put(PublicationField.TITLE, item.title);
		meta.put(PublicationField.VERSION, item.version);
		meta.put(PublicationField.DESCRIPTION, item.description);
		// TODO split submitter field here
		meta.put(PublicationField.SUBMITTER, item.submitter_surname + ", "
				+ item.submitter_givenname);
		meta.put(PublicationField.ARCHIVE_URL, item.archive_url);
		// initialization with reasonable defaults: email from git repo
		meta.put(PublicationField.PUBREPO_LOGIN_EMAIL, item.contact_email);
		// FIXME! This needs to be somehow configurable!!!
		meta.put(PublicationField.VERIFY_USER, "true");
		// mark item as not yet published
		meta.remove(PublicationField.REPOSITORY_URL);
		this.userID = userID;
		this.item = item;

		updateHash();
	}

	void updateHash() {
		Hash h = new Hash();
		// this seed is unique per session
		h.add(seed);
		// do not change if login email persists
		h.add(meta.get(PublicationField.PUBREPO_LOGIN_EMAIL));

		// split into pubid / vcode
		String s = h.getHash();
		publicationId = s.substring(0, s.length() / 2 - 1);
		// TODO replace characters that break selection
		// via double click in the email
		verificationCode = s.substring(s.length() / 2).replaceAll("-", "");
	}

	public String getPubID() {
		return publicationId;
	};

	public boolean isVerified() {
		return this.verified;
	}

	// this will return a pseudo-random hash code
	public String getVerificationCode() {
		updateHash();
		return verificationCode;
	}

	// check whether the generated code matches the given one!
	public boolean verifyCode(String code) {
		verified = (this.verificationCode.equals(code));
		return verified;
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

		// this relies on the fact that only actually publishing the item sets
		// the repository URL
		if (meta.containsKey(PublicationField.REPOSITORY_URL)) {
			// do not lose an open session while item is not yet submitted
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
		updateHash();
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
