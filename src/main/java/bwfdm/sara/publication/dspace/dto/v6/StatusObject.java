package bwfdm.sara.publication.dspace.dto.v6;

/**
 * 
 * @author vk
 */
public class StatusObject {

	private boolean okay;
	private boolean authenticated;
	private String email;
	private String fullname;
	private String sourceVersion;
	private String apiVersion;

	public boolean isOkay() {
		return okay;
	}

	public void setOkay(final boolean okay) {
		this.okay = okay;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(final boolean authenticated) {
		this.authenticated = authenticated;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(final String fullname) {
		this.fullname = fullname;
	}

	public String getSourceVersion() {
		return sourceVersion;
	}

	public void setSourceVersion(final String sourceVersion) {
		this.sourceVersion = sourceVersion;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(final String apiVersion) {
		this.apiVersion = apiVersion;
	}

}
