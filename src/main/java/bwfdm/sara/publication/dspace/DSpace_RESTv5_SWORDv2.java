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

import bwfdm.sara.publication.PubRepo;
import bwfdm.sara.publication.db.RepositoryDAO;

import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** high-level abstraction of the DSpace RESTv5 + SWORDv2 API. */
public class DSpace_RESTv5_SWORDv2 implements PubRepo {

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
	public DSpace_RESTv5_SWORDv2(
			@JsonProperty("uuid") final String rRef,
			@JsonProperty("url") final String u,
			@JsonProperty("rest_user") final String ru,
			@JsonProperty("rest_pwd") final String rp, @JsonProperty("rest_api_endpoint") final String re,
			@JsonProperty("sword_user") final String su, @JsonProperty("sword_pwd") final String sp,
			@JsonProperty("sword_api_endpoint") final String se) {

		repository_uuid = UUID.fromString(rRef);
		url = u;
		
		if (url.endsWith("/"))
			throw new IllegalArgumentException("url must not end with slash: " + url);

		rest_user = ru; rest_pwd = rp; rest_api_endpoint = re;
		sword_user = su; sword_pwd = sp; sword_api_endpoint = se;
	}


	@Override
	public UUID getUUID() {
		return repository_uuid;
	}
	
	@Override
	public List<String> getAvailableCollections() {
		return null;
	}

    @Override
    public Boolean isUserRegistered(String loginName) {
    	// TODO Auto-generated method stub
    	return null;
    }
    
    @Override
    public Boolean isUserAssigned(String loginName) {
    	// TODO Auto-generated method stub
    	return null;
    }
    
    @Override
    public void dump() {
    	System.out.println("id=" + repository_uuid.toString());
    	System.out.println("url=" + rest_user);
    	System.out.println("name=" + rest_pwd);
    }
}
