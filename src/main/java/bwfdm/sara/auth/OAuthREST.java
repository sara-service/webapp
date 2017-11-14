package bwfdm.sara.auth;

import java.util.NoSuchElementException;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/** REST helper with {@code Authorization: Bearer t0k3N} authentication. */
public class OAuthREST extends AuthenticatedREST {
	final MultiValueMap<String, String> authMap = new LinkedMultiValueMap<String, String>();
	private String token;

	public OAuthREST(final String root) {
		super(root);
	}

	/**
	 * Sets the token to use for authentication.
	 * 
	 * @param token
	 *            token to pass in the header, or <code>null</code> to
	 *            invalidate authorization
	 */
	public void setToken(final String token) {
		this.token = token;
		if (token == null) {
			setAuth(null);
			return;
		}

		authMap.set("Authorization", "Bearer " + token);
		setAuth(authMap);
	}

	public String getToken() {
		if (!hasToken())
			throw new NoSuchElementException("no token available!");
		return token;
	}

	/** @return <code>true</code> if a token has been set */
	public boolean hasToken() {
		return hasAuth();
	}
}