package bwfdm.sara.publication.dspace;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * 
 * @author vk
 */
public class DSpaceVersionFive extends DSpaceRestCommon {

	// Constructor
	public DSpaceVersionFive(final String repoType, final String urlServer,
			final String urlRest, final String verify,
			final int responseStatusOK) {

		super(repoType, urlServer, urlRest, verify, responseStatusOK);
		System.out.println("Constructor DSpace-5.");
	}

	@Override
	public boolean login(final String email, final String password) {
		// Login command:
		// curl -H "Content-Type: application/json" --data
		// '{"email":"admin@dspace.org", "password":"dspace"}'
		// http://localhost:8080/rest/login

		System.out.println("--- " + repoType + ", login ---");

		boolean loginCorrect = false;
		final Invocation.Builder invocationBuilder = loginWebTarget.request();
		invocationBuilder.header("Content-Type",
				DSpaceConfig.HEADER_CONTENT_TYPE_OPARU);
		final String data = "{" + "\"email\":\"" + email + "\", "
				+ "\"password\":\"" + password + "\"" + "}";
		final Response response = invocationBuilder.post(Entity.json(data));
		if (response.getStatus() == responseStatusOK) {
			token = response.readEntity(String.class);
			loginCorrect = true;
		}
		System.out.println("response login: " + response.getStatus());
		response.close(); // not realy needed but better to close
		return loginCorrect;
	}

	@Override
	public boolean logout() {
		// Logout command:
		// curl -X POST -H "Content-Type: application/json" -H
		// "rest-dspace-token: 1febef81-5eb6-4e76-a0ea-a5be245563a5"
		// http://localhost:8080/rest/logout

		System.out.println("--- " + repoType + ", login ---");

		boolean logoutCorrect = false;
		final Invocation.Builder invocationBuilder = logoutWebTarget.request();
		invocationBuilder.header("Content-Type",
				DSpaceConfig.HEADER_CONTENT_TYPE_OPARU);
		invocationBuilder.header("rest-dspace-token", token);
		final Response response = invocationBuilder.post(Entity.json(""));
		if (response.getStatus() == responseStatusOK) {
			token = response.readEntity(String.class);
			logoutCorrect = true;
		}
		System.out.println("response logout: " + response.getStatus());
		response.close(); // not realy needed but better to close
		return logoutCorrect;
	}

	@Override
	public boolean isAuthenticated() {
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
	public String getConnectionStatus() {
		System.out.println("--- " + repoType + ", get connection status");

		final Invocation.Builder invocationBuilder = statusWebTarget.request();
		invocationBuilder.header("Content-Type",
				DSpaceConfig.HEADER_CONTENT_TYPE_OPARU);
		invocationBuilder.header("Accept",
				DSpaceConfig.HEADER_ACCEPT_TYPE_OPARU);
		invocationBuilder.header("rest-dspace-token", token);
		final Response response = invocationBuilder.get();
		return response.readEntity(String.class);
	}

	// @Override
	public String getToken() {
		return token;
	}

	/* COMMUNITIES */

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
		invocationBuilder.header("Content-Type",
				DSpaceConfig.HEADER_CONTENT_TYPE_OPARU);
		invocationBuilder.header("rest-dspace-token", token);
		final String data = "{" + "\"name\":" + "\"" + communityName + "\""
				+ "}";
		System.out.println(data);
		System.out.println(newCommunityWebTarget.toString());
		// System.out.println(invocationBuilder.toString());
		final Response response = invocationBuilder.post(Entity.json(data));
		System.out.println("response status - create community: "
				+ response.getStatus());
		return response.readEntity(String.class); // Connection will be closed
													// automatically after the
													// "readEntity"
	}

	@Override
	public String deleteCommunity(final String communityID) {
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
	public String updateCommunity(final String communityName,
			final String communityID) {
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

	@Override
	public String createCollection(final String collectionName,
			final String parentCommunityID) {
		System.out.println("--- " + repoType + ", create collection ---");

		final WebTarget newCollectionWebTarget = communitiesWebTarget.path(
				parentCommunityID).path("collections");

		final Invocation.Builder invocationBuilder = newCollectionWebTarget
				.request();
		invocationBuilder.header("Content-Type",
				DSpaceConfig.HEADER_CONTENT_TYPE_OPARU);
		invocationBuilder.header("rest-dspace-token", token);
		final String data = "{" + "\"name\":" + "\"" + collectionName + "\""
				+ "}";
		System.out.println(data);
		System.out.println(newCollectionWebTarget.toString());
		final Response response = invocationBuilder.post(Entity.json(data));
		System.out.println("response status - create collection: "
				+ response.getStatus());
		return response.readEntity(String.class); // Connection will be closed
													// automatically after the
													// "readEntity"
	}

	@Override
	public String deleteCollection(final String collectionID) {
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
	public String updateCollection(final String collectionName,
			final String collectionID) {
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
	public String createItem(final String itemName, final String itemTitle,
			final String collectionID) {
		System.out.println("--- " + repoType + ", create new item");

		final WebTarget newItemWebTarget = collectionsWebTarget.path(
				collectionID).path("items");

		final Invocation.Builder invocationBuilder = newItemWebTarget.request();
		invocationBuilder.header("Content-Type",
				DSpaceConfig.HEADER_CONTENT_TYPE_OPARU);
		invocationBuilder.header("Accept",
				DSpaceConfig.HEADER_ACCEPT_TYPE_OPARU);
		// invocationBuilder.header("user", DSpaceConfig.EMAIL_OPARU);
		// invocationBuilder.header("pass",
		// DSpaceConfig.getPassword(DSpaceConfig.EMAIL_OPARU, this));
		invocationBuilder.header("rest-dspace-token", getToken());
		// invocationBuilder.header("login", token);
		final String data = "{" + "\"name\":" + "\"" + itemName + "\"" + "}";
		// String data = "{"
		// + "\"metadata\":["
		// + "{"
		// + "\"key\":" + "\"dc.contributor.author\""
		// + "\"value\":" + "\"" + itemName + "\""
		// + "}"
		// + "]}";
		System.out.println(data);
		System.out.println(newItemWebTarget.toString());
		final Response response = invocationBuilder.post(Entity.json(data));
		System.out.println("response status - create item: "
				+ response.getStatus());
		return response.readEntity(String.class); // Connection will be closed
													// automatically after the
													// "readEntity"
	}

	@Override
	public String deleteItem(final String itemID) {
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
	public String addItemMetadata(final String itemID, final String metadata) {
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
	public String updateItemMetadata(final String itemID, final String metadata) {
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
	public String clearItemMetadata(final String itemID) {
		return "";
	}

	@Override
	public String addItemBitstream(final String itemID,
			final String bitstreamToAdd) {
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
