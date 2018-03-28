package bwfdm.sara.auth;

import java.util.NoSuchElementException;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * REST helper with {@code Authorization: Bearer t0k3N} authentication. Can
 * actually use any string instead of "Bearer", for weird providers that want
 * more intuitive strings there (GitHub...).
 */
public class OAuthREST extends AuthenticatedREST {
	final MultiValueMap<String, String> authMap = new LinkedMultiValueMap<String, String>();
	private String token;
	private String method;

	/**
	 * @param root
	 *            URL root, passed directly to {@link AuthenticatedREST}
	 * @param method
	 *            string to use before the token, ie.
	 *            {@code Authorization: method t0k3N}
	 */
	public OAuthREST(final String root, String method) {
		super(root);
		this.method = method;
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

		authMap.set("Authorization", method + " " + token);
		setAuth(authMap);
	}

	public String getToken() {
		if (!hasToken())
			throw new NoSuchElementException("no token available!");
		return token;
	}

	/** @return <code>true</code> if a token has been set */
	public boolean hasToken() {
		return token != null;
	}
}