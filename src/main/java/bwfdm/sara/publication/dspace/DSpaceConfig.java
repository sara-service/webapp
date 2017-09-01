package bwfdm.sara.publication.dspace;

import bwfdm.sara.publication.PublicationRepository;

/**
 * Configuration of DSpace: login, password, types...
 * 
 * TODO: create secure(!) authentification!!! Remove login/password!
 * 
 * @author vk
 */
public class DSpaceConfig {

	/**
	 * OPARU-5 config
	 */
	public static final String URL_OPARU_FIVE = "https://vtstest.rz.uni-ulm.de";
	public static final String URL_OPARU_FIVE_REST = "https://vtstest.rz.uni-ulm.de/rest";
	public static final String SSL_VERIFY_OPARU_FIVE = "false";
	public static final int RESPONSE_STATUS_OK_OPARU_FIVE = 200;
	public static final String PASSWORD_OPARU_FIVE = "SaraTest";

	/**
	 * OPARU-6 config
	 */
	public static final String URL_OPARU_SIX = "https://bib-test.rz.uni-ulm.de";
	public static final String URL_OPARU_SIX_REST = "https://bib-test.rz.uni-ulm.de/rest";
	public static final String SSL_VERIFY_OPARU_SIX = "false";
	public static final String COOKIE_KEY_OPARU_SIX = "JSESSIONID";
	public static final int RESPONSE_STATUS_OK_OPARU_SIX = 200;
	public static final String PASSWORD_OPARU_SIX = "Sara+Test";

	/**
	 * DemoDSpaceOrg-6 config
	 */
	public static final String URL_DemoDSpaceOrg_SIX = "https://demo.dspace.org";
	public static final String URL_DemoDSpaceOrg_SIX_REST = "https://demo.dspace.org/rest";
	public static final String SSL_VERIFY_DemoDSpaceOrg_SIX = "false";
	public static final String COOKIE_KEY_DemoDSpaceOrg_SIX = "JSESSIONID";
	public static final int RESPONSE_STATUS_OK_DemoDSpaceOrg_SIX = 200;
	public static final String PASSWORD_DemoDSpaceOrg_SIX = "dspace";// "Sara2017";

	public static final String EMAIL_DemoDSpaceOrgSix = "dspacedemo+admin@gmail.com";// "volodymyr.kushnarenko@uni-ulm.de";

	/**
	 * OPARU-general config
	 */
	public static final String EMAIL_OPARU = "project-sara@uni-konstanz.de";

	/**
	 * Common config
	 */
	public static final String HEADER_CONTENT_TYPE_OPARU = "application/json";
	public static final String HEADER_ACCEPT_TYPE_OPARU = "application/json";
	public static final String RESPONSE_ERROR_JSON = "{\"Error\":\"bad response status\"}";
	public static final String RESPONSE_EMPTY_JSON = "{\"\":\"\"}";
	public static final String RESPONSE_REST_TEST = "REST api is running.";

	// public static final String HEADER_CONTENT_TYPE_OPARU =
	// " {" +
	// " \"Content-Type\": \"application/" + REQUEST_TYPE_OPARU + "\"" +
	// " }";
	// public static final String HEADER_ACCEPT_TYPE_OPARU =
	// " {" +
	// " \"Accept\": \"application/" + REQUEST_TYPE_OPARU + "\"" +
	// " }";
	// public static final String HEADER_TOKEN_OPARU =
	// " {" +
	// " \"rest-dspace-token\": \"\"" +
	// " }";

	/**
	 * KOPS config
	 * 
	 */
	public static final String URL_KOPS_FIVE = "https://vtstest.rz.uni-ulm.de";
	public static final String URL_KOPS_FIVE_REST = "https://vtstest.rz.uni-ulm.de/rest";
	public static final String SSL_VERIFY_KOPS_FIVE = "false";
	public static final int RESPONSE_STATUS_OK_KOPS_FIVE = 200;
	public static final String PASSWORD_KOPS_FIVE = "SaraTest";

	/**
	 * TODO: implement some secure algorithm/mechanism to get password from the
	 * outside.
	 * 
	 * but for now just return a constant value from this class
	 * 
	 * @param email
	 * @param obj
	 * @return password string
	 */
	public static String getPassword(final String email,
			final PublicationRepository obj) {

		switch (email) {
		case EMAIL_OPARU:
			if (obj.getRepositoryUrl().equals(URL_OPARU_SIX))
				return PASSWORD_OPARU_SIX;
			if (obj.getRepositoryUrl().equals(URL_OPARU_FIVE))
				return PASSWORD_OPARU_FIVE;

		case EMAIL_DemoDSpaceOrgSix:
			if (obj.getRepositoryUrl().equals(URL_DemoDSpaceOrg_SIX))
				return PASSWORD_DemoDSpaceOrg_SIX;

		default:
			return "some_default_password";
		}
	}

}
