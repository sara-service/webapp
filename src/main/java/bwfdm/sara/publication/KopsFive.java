package bwfdm.sara.publication;

import bwfdm.sara.publication.dspace.DSpaceConfig;
import bwfdm.sara.publication.dspace.DSpaceVersionFive;

/**
 * 
 * @author vk
 */
public class KopsFive extends OparuFive {

	private final DSpaceVersionFive dspaceFive;
	private final String urlServer;

	// Constructor
	public KopsFive() {

		urlServer = DSpaceConfig.URL_OPARU_FIVE;

		dspaceFive = new DSpaceVersionFive("KOPS-5",
				DSpaceConfig.URL_KOPS_FIVE, DSpaceConfig.URL_KOPS_FIVE_REST,
				DSpaceConfig.SSL_VERIFY_KOPS_FIVE,
				DSpaceConfig.RESPONSE_STATUS_OK_KOPS_FIVE);

		System.out.println("Constructor oparu-5.");
	}
}
