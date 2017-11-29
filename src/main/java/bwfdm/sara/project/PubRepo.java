package bwfdm.sara.project;

import java.util.List;
import java.util.Map;

public interface PubRepo {
	/** @return internal ID */
	public String getID();

	/** @return user-visible name of the repository */
	public String getDisplayName();

	/** @return URL to logo */
	public String getLogoURL();

	/**
	 * @param email
	 *            the email address of a user
	 * @return a list of all collections this user can submit to (ie. possibly
	 *         an empty list but never <code>null</code>)
	 */
	public List<PubCollection> getCollections(String email);

	/**
	 * @param email
	 *            the email address of a user
	 * @return <code>true</code> if the user exists on this repository
	 */
	public boolean isValidUser(String email);

	/**
	 * Submits an item for publication.
	 * 
	 * @param email
	 *            the email address of a user
	 * @param metadata
	 *            a list of metadata fields, using SARA conventions
	 */
	public void publish(String email, Map<String, String> metadata);

	public interface PubCollection {
		public String getID();

		public String getDisplayName();
	}
}
