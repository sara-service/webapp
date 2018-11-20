package bwfdm.sara.publication;

import java.io.File;

/**
 * General Interface for the publication repository.
 * 
 * @author sk, vk
 */

import org.springframework.util.MultiValueMap;

public interface PublicationRepository {

	public class SubmissionInfo {
		// identifier to refer to the item (internally for the IR)
		public String item_ref = null;
		// link for submitters to edit their items metadata
		// may be "null" which means they can't make changes afterwards!
		public String edit_ref = null;
		// true: the user will edit their submission in a subsequent step
		// login in the IR is required
		// false: the user submits the metadata 'as is' to the IR
		// login in the IR is NOT required
		public boolean inProgress = true;
	}

	public class CollectionInfo {
		public String name;
		public String policy;
	}

	/**
	 * Check if publication repository is accessible via API
	 * 
	 * @return
	 */
	public boolean isAccessible();

	/**
	 * Check if user is registered in the publication repository
	 * 
	 * @param loginName
	 * @return
	 */
	public boolean isUserRegistered(final String loginName);

	/**
	 * Check if user is assigned to publish something in the repository
	 *
	 * @param loginName
	 * @return {@code true} if count of user available collections is great than
	 *         zero, otherwise {@code false}
	 */
	public boolean isUserAssigned(final String loginName);

	/**
	 * Get collections which are available for the user
	 * 
	 * @param loginName
	 *            - login name of user, if 'null' service user is taken
	 * @return hierarchy tree containing leafs (collections) and branches
	 *         (communities)
	 */
	public Hierarchy getHierarchy(final String loginName);

	/**
	 * Publish metada only (without any file) to some collection, which is
	 * available for the user. Metadata are described as a
	 * {@link java.util.Map}.
	 * 
	 * @param userLogin
	 * @param collectionURL
	 * @param metadataMap
	 * @return
	 */
	public SubmissionInfo publishMetadata(final String userLogin,
			final String collectionURL,
			final MultiValueMap<String, String> metadataMap);

	/**
	 * Publish a file together with the metadata. Metadata are described as a
	 * {@link java.util.Map}.
	 * 
	 * @param userLogin
	 * @param collectionURL
	 * @param fileFullPath
	 * @param metadataMap
	 * @return
	 */
	public SubmissionInfo publishFileAndMetadata(final String userLogin,
			final String collectionURL, final File fileFullPath,
			final MultiValueMap<String, String> metadataMap);

	public Repository getDAO();

	public void dump();
}
