package bwfdm.sara.publication.dspace;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import bwfdm.sara.publication.dspace.dto.StatusObjectDSpaceSix;
import bwfdm.sara.utils.JsonUtils;

/**
 * 
 * @author vk
 */
public class DSpaceVersionSix extends DSpaceRestCommon {

	public DSpaceVersionSix(final String repoType, final String urlServer,
			final String urlRest, final String verify,
			final int responseStatusOK) {

		super(repoType, urlServer, urlRest, verify, responseStatusOK);
		System.out.println("Constructor DSpace-6.");
	}

	/**
	 * Login, email/password
	 * 
	 * @param email
	 * @param password
	 * @return
	 */
	@Override
	public boolean login(final String email, final String password) {
		// Login command:
		// curl -v -X POST --data "email=admin@dspace.org&password=mypass"
		// https://dspace.myu.edu/rest/login
		System.out.println("--- " + repoType + ", login ---");

		// Check if login already done
		if (isAuthenticated()) {
			return true;
		}
		System.out.println("cookie full string: " + getCookie());
		final Invocation.Builder invocationBuilder = loginWebTarget.request();
		invocationBuilder.header("Content-Type",
				"application/x-www-form-urlencoded");
		final Form form = new Form();
		form.param("email", email);
		form.param("password", password);
		final Response response = invocationBuilder.post(Entity.form(form));
		if (response.getStatus() == responseStatusOK) {
			setCookie(response.getCookies().get(
					DSpaceConfig.COOKIE_KEY_OPARU_SIX));
		}
		// this.cookie =
		// response.getCookies().get(DSpaceConfig.COOKIE_KEY_OPARU_SIX);
		System.out.println("cookie full string: " + getCookie());
		System.out.println("response login: " + response.getStatus());
		System.out.println("response coockie: " + getCookie().getValue());
		System.out.println("cookie name: " + getCookie().getName());
		response.close(); // not realy needed but better to close
		return isAuthenticated();
	}

	/**
	 * Logout.
	 * 
	 * @return true or false
	 */
	@Override
	public boolean logout() {
		// Command:
		// curl -v -X POST --cookie
		// "JSESSIONID=6B98CF8648BCE57DCD99689FE77CB1B8"
		// https://dspace.myu.edu/rest/logout
		System.out.println("--- " + repoType + ", logout ---");

		final Invocation.Builder invocationBuilder = logoutWebTarget.request();
		invocationBuilder.cookie(getCookie());
		// invocationBuilder.cookie(new Cookie("",""));
		final Response response = invocationBuilder.post(Entity.json(null)); // ohne
																				// "--data"
		System.out.println("response logout: " + response.getStatus());
		System.out.println("response string: "
				+ response.readEntity(String.class));
		System.out.println("cookie before = " + getCookie());
		if ((response.getStatus() == responseStatusOK) && !isAuthenticated()) { // even
																				// if
																				// cookie
																				// is
																				// wrong
																				// response
																				// Status
																				// is
																				// 200!
			setCookie(new Cookie("", ""));
			System.out.println("cookie after = " + getCookie());
			return true;
		}

		System.out.println("-------");
		return false;
	}

	/**
	 * Check if authenticated
	 * 
	 * @return true/false
	 */
	@Override
	public boolean isAuthenticated() {
		System.out.println("--- " + repoType + ", is authenticated ---");

		final String status = getConnectionStatus();
		final StatusObjectDSpaceSix cookieStatus = JsonUtils
				.jsonStringToObject(status, StatusObjectDSpaceSix.class);
		return cookieStatus.isAuthenticated();
	}

	/**
	 * Check connection status.
	 * 
	 * @return response string, JSON
	 */
	@Override
	public String getConnectionStatus() {
		System.out.println("--- " + repoType + ", get connection status");

		final Invocation.Builder invocationBuilder = statusWebTarget.request();
		invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
		invocationBuilder.cookie(getCookie());
		final Response response = invocationBuilder.get();
		final String status = response.readEntity(String.class); // Connection
																	// will be
																	// closed
																	// automatically
		return status;
	}

	/**
	 * Get cookie
	 * 
	 * @return Cookie
	 */
	public Cookie getCookie() {
		return cookie;
	}

	/**
	 * Set cookie.
	 * 
	 * @param cookie
	 */
	public void setCookie(final Cookie cookie) {
		this.cookie = cookie;
	}

	/* COMMUNITIES */

	/**
	 * Create new community
	 * 
	 * @param communityName
	 * @param parentCommunityID
	 * @return String, response
	 */
	@Override
	public String createCommunity(final String communityName,
			final String parentCommunityID) {
		System.out.println("--- " + repoType + ", create new community ---");

		WebTarget newCommunityWebTarget = communitiesWebTarget;
		if (!parentCommunityID.equals("")) {
			newCommunityWebTarget = communitiesWebTarget
					.path(parentCommunityID).path("communities");
		}
		final Invocation.Builder invocationBuilder = newCommunityWebTarget
				.request();
		invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
		invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
		invocationBuilder.cookie(getCookie());
		final String data = "{" + "\"name\":" + "\"" + communityName + "\""
				+ "}";
		final Response response = invocationBuilder.post(Entity.json(data));

		if (response.getStatus() != responseStatusOK) {
			return DSpaceConfig.RESPONSE_ERROR_JSON;
		}

		return response.readEntity(String.class); // Connection will be closed
													// automatically after the
													// "readEntity"
	}

	/**
	 * Delete community
	 * 
	 * @param communityID
	 * @return response string
	 */
	@Override
	public String deleteCommunity(final String communityID) {
		System.out.println("--- " + repoType + ", delete community ---");

		if (communityID.equals("")) {
			return DSpaceConfig.RESPONSE_ERROR_JSON; // empty ID, error
		}

		WebTarget deleteCommunityWebTarget = communitiesWebTarget;
		deleteCommunityWebTarget = communitiesWebTarget.path(communityID);

		final Invocation.Builder invocationBuilder = deleteCommunityWebTarget
				.request();
		invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
		invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
		invocationBuilder.cookie(getCookie());

		final Response response = invocationBuilder.delete();

		System.out.println("delete, response: " + response.getStatus());
		if (response.getStatus() != responseStatusOK) {
			return DSpaceConfig.RESPONSE_ERROR_JSON;
		}

		return response.readEntity(String.class);
	}

	/**
	 * Update community
	 * 
	 * @param newCommunityName
	 * @param communityID
	 * @return
	 */
	@Override
	public String updateCommunity(final String newCommunityName,
			final String communityID) {
		System.out.println("--- " + repoType + ", update community ---");

		if (communityID.equals("")) {
			return DSpaceConfig.RESPONSE_ERROR_JSON; // empty ID, error
		}

		WebTarget newCommunityWebTarget = communitiesWebTarget;
		newCommunityWebTarget = communitiesWebTarget.path(communityID);

		final Invocation.Builder invocationBuilder = newCommunityWebTarget
				.request();
		invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
		invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
		invocationBuilder.cookie(getCookie());
		final String data = "{" + "\"name\":" + "\"" + newCommunityName + "\""
				+ "}";
		final Response response = invocationBuilder.put(Entity.json(data));

		if (response.getStatus() != responseStatusOK) {
			return DSpaceConfig.RESPONSE_ERROR_JSON;
		}

		return response.readEntity(String.class); // Connection will be closed
													// automatically after the
													// "readEntity"
	}

	@Override
	public String getObjectProperties(final String objectName,
			final String objectID) {
		throw new UnsupportedOperationException("Not supported yet."); // To
																		// change
																		// body
																		// of
																		// generated
																		// methods,
																		// choose
																		// Tools
																		// |
																		// Templates.
	}

	/* COLLECTIONS */

	/**
	 * Create collection
	 * 
	 * @param collectionName
	 * @param parentCommunityID
	 * @return response string
	 */
	@Override
	public String createCollection(final String collectionName,
			final String parentCommunityID) {
		System.out.println("--- " + repoType + ", create collection ---");

		final WebTarget newCollectionWebTarget = communitiesWebTarget.path(
				parentCommunityID).path("collections");

		final Invocation.Builder invocationBuilder = newCollectionWebTarget
				.request();
		invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
		invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
		invocationBuilder.cookie(getCookie());
		final String data = "{" + "\"name\":" + "\"" + collectionName + "\""
				+ "}";
		final Response response = invocationBuilder.post(Entity.json(data));

		if (response.getStatus() != responseStatusOK) {
			return DSpaceConfig.RESPONSE_ERROR_JSON;
		}

		return response.readEntity(String.class); // Connection will be closed
													// automatically after the
													// "readEntity"
	}

	/**
	 * Delete collection
	 * 
	 * @param collectionID
	 * @return response string
	 */
	@Override
	public String deleteCollection(final String collectionID) {
		System.out.println("--- " + repoType + ", delete collection ---");

		final WebTarget newCollectionWebTarget = collectionsWebTarget
				.path(collectionID);

		final Invocation.Builder invocationBuilder = newCollectionWebTarget
				.request();
		invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
		invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
		invocationBuilder.cookie(getCookie());

		final Response response = invocationBuilder.delete();

		System.out.println("delete, response: " + response.getStatus());
		if (response.getStatus() != responseStatusOK) {
			return DSpaceConfig.RESPONSE_ERROR_JSON;
		}

		return response.readEntity(String.class); // Connection will be closed
													// automatically after the
													// "readEntity"

	}

	/**
	 * Update collection (name)
	 * 
	 * @param newCollectionName
	 * @param collectionID
	 * @return
	 */
	@Override
	public String updateCollection(final String newCollectionName,
			final String collectionID) {
		System.out.println("--- " + repoType + ", update collection --");

		if (collectionID.equals("")) {
			return "Update collection, error: empty ID."; // empty ID, error
		}

		WebTarget collectionWebTarget = collectionsWebTarget;
		collectionWebTarget = collectionsWebTarget.path(collectionID);

		final Invocation.Builder invocationBuilder = collectionWebTarget
				.request();
		invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
		invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
		invocationBuilder.cookie(getCookie());
		final String data = "{" + "\"name\":" + "\"" + newCollectionName + "\""
				+ "}";
		final Response response = invocationBuilder.put(Entity.json(data));

		System.out.println("update, response: " + response.getStatus());
		if (response.getStatus() != responseStatusOK) {
			return DSpaceConfig.RESPONSE_ERROR_JSON;
		}

		return response.readEntity(String.class); // Connection will be closed
													// automatically after the
													// "readEntity"

	}

	/* ITEMS */

	/**
	 * Create new item
	 * 
	 * @param itemName
	 * @param itemTitle
	 * @param collectionID
	 * @return response string, JSON
	 */
	@Override
	public String createItem(final String itemName, final String itemTitle,
			final String collectionID) {
		System.out.println("--- " + repoType + ", create new item");

		if (collectionID.equals("")) {
			return DSpaceConfig.RESPONSE_ERROR_JSON; // empty ID, error
		}

		final WebTarget newItemWebTarget = collectionsWebTarget.path(
				collectionID).path("items");

		final Invocation.Builder invocationBuilder = newItemWebTarget.request();
		invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
		invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
		invocationBuilder.cookie(getCookie());
		final String data = "{" + "\"name\":" + "\"" + itemName + "\"" + "}";
		final Response response = invocationBuilder.post(Entity.json(data));

		System.out.println("create item, response: " + response.getStatus());
		if (response.getStatus() != responseStatusOK) {
			return DSpaceConfig.RESPONSE_ERROR_JSON;
		}

		return response.readEntity(String.class); // Connection will be closed
													// automatically after the
													// "readEntity"

	}

	/**
	 * Delete item by ID
	 * 
	 * @param itemID
	 * @return
	 */
	@Override
	public String deleteItem(final String itemID) {
		System.out.println("--- " + repoType + ", delete item by ID ---");

		WebTarget itemWebTarget = itemsWebTarget;
		itemWebTarget = itemsWebTarget.path(itemID);

		final Invocation.Builder invocationBuilder = itemWebTarget.request();
		invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
		invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
		invocationBuilder.cookie(getCookie());

		final Response response = invocationBuilder.delete();

		System.out.println("delete item, response: " + response.getStatus());

		if (response.getStatus() != responseStatusOK) {
			return DSpaceConfig.RESPONSE_ERROR_JSON;
		}
		return response.readEntity(String.class);
	}

	// TODO:
	// 1. Escape characters (\n.....)
	// 2. Special metadata field for the GitLab link.
	//
	/**
	 * Add metadata to the item
	 * 
	 * @param itemID
	 * @param metadata
	 * @return
	 * 
	 *         TODO: 1. Escape characters (\n.....) 2. Special metadata field
	 *         for the GitLab link.
	 */
	@Override
	public String addItemMetadata(final String itemID, final String metadata) {
		System.out
				.println("--- " + repoType + ", add metadata to the item ---");

		WebTarget itemMetadataWebTarget = itemsWebTarget;
		itemMetadataWebTarget = itemsWebTarget.path(itemID).path("metadata");

		final Invocation.Builder invocationBuilder = itemMetadataWebTarget
				.request();
		invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
		invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
		invocationBuilder.cookie(getCookie());
		final String data = metadata;

		final Response response = invocationBuilder.post(Entity.json(data));

		System.out.println("add item metadata, response: "
				+ response.getStatus());

		if (response.getStatus() != responseStatusOK) {
			return DSpaceConfig.RESPONSE_ERROR_JSON;
		}

		return response.readEntity(String.class);
	}

	/**
	 * Update metadata of the item
	 * 
	 * @param itemID
	 * @param metadata
	 * @return
	 */
	@Override
	public String updateItemMetadata(final String itemID, final String metadata) {
		System.out.println("--- " + repoType
				+ ", update metadata of the item ---");

		WebTarget itemMetadataWebTarget = itemsWebTarget;
		itemMetadataWebTarget = itemsWebTarget.path(itemID).path("metadata");

		final Invocation.Builder invocationBuilder = itemMetadataWebTarget
				.request();
		invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
		invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
		invocationBuilder.cookie(getCookie());
		final String data = metadata;

		final Response response = invocationBuilder.put(Entity.json(data));

		System.out.println("add item metadata, response: "
				+ response.getStatus());

		if (response.getStatus() != responseStatusOK) {
			return DSpaceConfig.RESPONSE_ERROR_JSON;
		}
		return response.readEntity(String.class);
	}

	/**
	 * Clear metadata of the item. Remain only "dc.date.accessioned" and
	 * "dc.date.available"
	 * 
	 * @param itemID
	 * @return response string
	 */
	@Override
	public String clearItemMetadata(final String itemID) {

		System.out.println("--- " + repoType
				+ ", clear metadata of the item ---");

		WebTarget itemMetadataWebTarget = itemsWebTarget;
		itemMetadataWebTarget = itemsWebTarget.path(itemID).path("metadata");

		final Invocation.Builder invocationBuilder = itemMetadataWebTarget
				.request();
		invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
		invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
		invocationBuilder.cookie(getCookie());

		final Response response = invocationBuilder.delete();

		System.out.println("clear item metadata, response: "
				+ response.getStatus());

		if (response.getStatus() != responseStatusOK) {
			return DSpaceConfig.RESPONSE_ERROR_JSON;
		}
		return response.readEntity(String.class);
	}

	@Override
	public String addItemBitstream(final String itemID,
			final String bitstreamDescription) {
		System.out.println("--- " + repoType
				+ ", add bitstream to the item ---");

		WebTarget itemBitstreamWebTarget = itemsWebTarget;
		itemBitstreamWebTarget = itemsWebTarget.path(itemID).path("bitstreams");

		final Invocation.Builder invocationBuilder = itemBitstreamWebTarget
				.request();
		invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
		invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
		invocationBuilder.cookie(getCookie());

		final String data = bitstreamDescription;

		// String data = "{"
		// + "\"name\":" + "\"" + "test-bitstream" + "\""
		// + "}";

		final Response response = invocationBuilder.post(Entity.json(data));

		System.out.println("add item bitstream, response: "
				+ response.getStatus());

		if (response.getStatus() != responseStatusOK) {
			return DSpaceConfig.RESPONSE_ERROR_JSON;
		}

		return response.readEntity(String.class);
	}

	@Override
	public String deleteItemBitstream(final String itemID,
			final String bitstreamToDelete) {
		throw new UnsupportedOperationException("Not supported yet."); // To
																		// change
																		// body
																		// of
																		// generated
																		// methods,
																		// choose
																		// Tools
																		// |
																		// Templates.
	}

	@Override
	public String downloadBitstream(final String bitstreamID,
			final String filenameToSave) {
		throw new UnsupportedOperationException("Not supported yet."); // To
																		// change
																		// body
																		// of
																		// generated
																		// methods,
																		// choose
																		// Tools
																		// |
																		// Templates.
	}

}