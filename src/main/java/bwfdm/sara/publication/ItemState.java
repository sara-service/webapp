package bwfdm.sara.publication;

/**
 * @author sk
 */

public enum ItemState {
	/**
	 * Item has been pushed to archive repo and created in SARA-service
	 * database.
	 */
	CREATED,
	/** User has verified his email address for this item. */
	VERIFIED,
	/** Item has been submitted to institutional repository. */
	SUBMITTED,
	/**
	 * Item has successfully completed the submission workflow in the
	 * institutional repository.
	 */
	ACCEPTED,
	/** Item has been moved to final archive. */
	DONE,
	/** Item has been permanently rejected by the institutional repository. */
	REJECTED,
	/** Processing of the item has been cancelled irreversibly. */
	DELETED;
}
