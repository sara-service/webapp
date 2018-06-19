package bwfdm.sara.auth;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.annotation.JsonCreator;

import bwfdm.sara.auth.AuthProvider.UserInfo;

/**
 * Shibboleth authentication using shibd / mod_shib. Basically needs to read a
 * few headers from a special request, which must have been to a location which
 * is Shibboleth-protected in the Apache config.
 */
public class ShibAuth {
	private final String displayNameAttribute;
	private final String emailAttribute;
	private final String userIDAttribute;
	private String userID;
	private String email;
	private String displayName;

	/**
	 * @param displayNameAttribute
	 *            attribute containing display name from Shibboleth (usually
	 *            {@code display-name})
	 * @param displayEmail
	 *            attribute containing email from Shibboleth (usually
	 *            {@code email})
	 * @param userIDAttribute
	 *            attribute containing persistent user ID from Shibboleth
	 *            (usually {@code persistent-id} to use the eduPersonTargetedID)
	 */
	@JsonCreator
	public ShibAuth(final String displayNameAttribute,
			final String emailAttribute, final String userIDAttribute) {
		this.displayNameAttribute = displayNameAttribute;
		this.emailAttribute = emailAttribute;
		this.userIDAttribute = userIDAttribute;
	}

	private String getShibAttr(final HttpServletRequest request,
			final String name) {
		// check that there is exactly one, non-null and non-empty header of the
		// desired name. this is to guard against configuration errors where
		// shib headers might be added instead of replacing existing headers.
		// such a situation would allow an attacker to inject Shibboleth
		// headers, and must be prevented.
		final Enumeration<String> headers = request.getHeaders(name);
		if (!headers.hasMoreElements())
			throw new IllegalArgumentException(
					"missing Shibboleth header " + name);
		final String attr = headers.nextElement();
		if (attr == null || attr.isEmpty())
			throw new IllegalArgumentException(
					"null Shibboleth header " + name);
		if (headers.hasMoreElements())
			throw new IllegalArgumentException(
					"duplicate Shibboleth header " + name);

		// handle multi-valued attributes by picking the first value, which
		// essentially means picking a random one of the values sent by the IdP
		// because values aren't ordered.
		// note: as of shibd 2.6.0, semicolons within values are escaped with
		// backslashes, but backslashes within values are NOT escaped in any
		// way. that is, attribute values better never end with a backslash...
		final String[] values = attr.split("(?<!\\\\);");
		if (values.length == 0 || values[0].length() == 0)
			throw new IllegalArgumentException(
					"invalid Shibboleth attribute " + name);
		if (values[0].isEmpty())
			throw new IllegalArgumentException(
					"empty Shibboleth attribute " + name);
		return values[0];
	}

	public boolean hasValidInfo() {
		return displayName != null && email != null && userID != null;
	}

	public void parseAuthResponse(final HttpServletRequest request) {
		this.displayName = getShibAttr(request, displayNameAttribute);
		this.email = getShibAttr(request, emailAttribute);
		this.userID = getShibAttr(request, userIDAttribute);
	}

	public UserInfo getUserInfo() {
		return new UserInfo(userID, email, displayName);
	}
}
