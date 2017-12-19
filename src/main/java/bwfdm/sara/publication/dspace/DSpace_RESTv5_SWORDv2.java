package bwfdm.sara.publication.dspace;

/*
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
*/

import bwfdm.sara.publication.PublicationRepository;
import bwfdm.sara.publication.db.ItemDAO;
import bwfdm.sara.publication.db.ItemState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** high-level abstraction of the DSpace RESTv5 + SWORDv2 API. */
public class DSpace_RESTv5_SWORDv2 implements PublicationRepository {

	private final UUID repository_uuid;
	private final String url;
	private final String rest_user, rest_pwd, rest_api_endpoint;
	private final String sword_user, sword_pwd, sword_api_endpoint;

	/**
	 * @param appID
	 *            OAuth application ID
	 * @param appSecret
	 *            OAuth application secret
	 * @param gitlab
	 *            URL to GitLab root
	 */
	@JsonCreator
	public DSpace_RESTv5_SWORDv2(@JsonProperty("uuid") final String rRef, @JsonProperty("url") final String u,
			@JsonProperty("rest_user") final String ru, @JsonProperty("rest_pwd") final String rp,
			@JsonProperty("rest_api_endpoint") final String re, @JsonProperty("sword_user") final String su,
			@JsonProperty("sword_pwd") final String sp, @JsonProperty("sword_api_endpoint") final String se) {

		repository_uuid = UUID.fromString(rRef);
		url = u;

		if (url.endsWith("/"))
			throw new IllegalArgumentException("url must not end with slash: " + url);

		rest_user = ru;
		rest_pwd = rp;
		rest_api_endpoint = re;
		sword_user = su;
		sword_pwd = sp;
		sword_api_endpoint = se;
	}

	@Override
	public Boolean isAccessible() {
		System.out.println("Testing REST Access...");
		System.out.println("Testing SWORD Access...");
		return true;
	}

	@Override
	public UUID getUUID() {
		return repository_uuid;
	}

	@Override
	public Boolean isUserRegistered(String loginName) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Boolean isUserAssigned(String loginName) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getCollectionName(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getMetadataName(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getAvailableCollections() {
		Map<String, String> coll = new HashMap<>();
		coll.put("0815", "Coffee Management");
		return coll;
	}
	
	@Override
	public Boolean publishItem(ItemDAO item) {
		// TODO Auto-generated method stub
		if (item.isArchiveOnly()) {
			System.out.println("Item is not meant to be published (archive-only)");
			return false;
		}
		
		if (item.isVerified()) {
			System.out.println("Not yet implemented!");
			return false;
		} else {
			System.out.println("ItemState needs to be verified");
			return false;
		}
	}

	@Override
	public void dump() {
		System.out.println("uuid=" + repository_uuid.toString());
		System.out.println("url=" + url);
		System.out.println("rest_user=" + rest_user);
		System.out.println("rest_pwd=" + rest_pwd);
		System.out.println("rest_api_endpoint=" + rest_api_endpoint);
		System.out.println("sword_user=" + sword_user);
		System.out.println("sword_pwd=" + sword_pwd);
		System.out.println("sword_api_endpoint=" + sword_api_endpoint);
	}
}
