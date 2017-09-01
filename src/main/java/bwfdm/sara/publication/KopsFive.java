package bwfdm.sara.publication;

import bwfdm.sara.publication.dspace.DSpaceConfig;
import bwfdm.sara.publication.dspace.DSpaceVersionFive;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author vk
 */
public class KopsFive extends OparuFive {

	private final DSpaceVersionFive dspaceFive;
	private final String urlServer;

	// Constructor
	public KopsFive(@JsonProperty("url") final String url,
			@JsonProperty("rest") final String rest,
			@JsonProperty("username") final String user,
			@JsonProperty("password") final String pass) {
		super(url, rest, user, pass);

		urlServer = url;

		dspaceFive = new DSpaceVersionFive("KOPS-5", url, rest,
				DSpaceConfig.SSL_VERIFY_KOPS_FIVE,
				DSpaceConfig.RESPONSE_STATUS_OK_KOPS_FIVE);

		System.out.println("Constructor kops-5.");
	}
}
