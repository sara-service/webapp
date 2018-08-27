package bwfdm.sara.auth;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.annotation.JsonCreator;

import bwfdm.sara.auth.AuthProvider.UserInfo;
import bwfdm.sara.auth.DisplayNameSplitter.Name;

/**
 * Shibboleth authentication using shibd / mod_shib. Basically needs to read a
 * few headers from a special request, which must have been to a location which
 * is Shibboleth-protected in the Apache config.
 */
public class ShibAuth {
	private final String displayNameAttribute;
	private final String surnameAttribute;
	private final String givenNameAttribute;
	private final String emailAttribute;
	private final String userIDAttribute;
	private final DisplayNameSplitter nameSplitter;
	private String userID;
	private String email;
	private String surname;
	private String givenName;
	private String displayName;

	/**
	 * @param surnameAttribute
	 *            attribute containing surname from Shibboleth (usually
	 *            {@code sn})
	 * @param givenNameAttribute
	 *            attribute containing given name from Shibboleth (usually
	 *            {@code given-name})
	 * @param userIDAttribute
	 *            attribute containing persistent user ID from Shibboleth
	 *            (usually {@code persistent-id} to use the eduPersonTargetedID)
	 * @param displayEmail
	 *            attribute containing email from Shibboleth (usually
	 *            {@code email})
	 * @param displayNameAttribute
	 *            attribute containing display name from Shibboleth (usually
	 *            {@code display-name}), or <code>null</code> to ignore the
	 *            display name
	 * @param nameRegex
	 *            pattern for {@link DisplayNameSplitter}, or <code>null</code>
	 *            when ignoring the display name
	 */
	@JsonCreator
	public ShibAuth(final String surnameAttribute,
			final String givenNameAttribute, final String emailAttribute,
			final String userIDAttribute, final String displayNameAttribute,
			final String nameRegex) {
		this.displayNameAttribute = displayNameAttribute;
		this.surnameAttribute = surnameAttribute;
		this.givenNameAttribute = givenNameAttribute;
		this.emailAttribute = emailAttribute;
		this.userIDAttribute = userIDAttribute;
		this.nameSplitter = nameRegex == null ? null
				: new DisplayNameSplitter(nameRegex);
	}

	/**
	 * @param surnameAttribute
	 *            attribute containing surname from Shibboleth (usually
	 *            {@code sn})
	 * @param givenNameAttribute
	 *            attribute containing given name from Shibboleth (usually
	 *            {@code given-name})
	 * @param userIDAttribute
	 *            attribute containing persistent user ID from Shibboleth
	 *            (usually {@code persistent-id} to use the eduPersonTargetedID)
	 * @param displayNameAttribute
	 *            attribute containing display name from Shibboleth (usually
	 *            {@code display-name})
	 * @param displayEmail
	 *            attribute containing email from Shibboleth (usually
	 *            {@code email})
	 * @param nameRegex
	 *            pattern for {@link DisplayNameSplitter}
	 */
	@JsonCreator
	public ShibAuth(final String surnameAttribute,
			final String givenNameAttribute, final String emailAttribute,
			final String userIDAttribute) {
		this(surnameAttribute, givenNameAttribute, emailAttribute,
				userIDAttribute, null, null);
	}

	private String getShibAttr(final HttpServletRequest request,
			final String name, final boolean required) {
		// check that there is exactly one, non-null and non-empty header of the
		// desired name. this is to guard against configuration errors where
		// shib headers might be added instead of replacing existing headers.
		// such a situation would allow an attacker to inject Shibboleth
		// headers, and must be prevented.
		final Enumeration<String> headers = request.getHeaders(name);
		if (!headers.hasMoreElements())
			if (required)
				throw new IllegalArgumentException(
						"missing Shibboleth header " + name);
			else
				return null;
		final String attr = headers.nextElement();
		if (attr == null)
			throw new IllegalArgumentException(
					"null Shibboleth header " + name);
		if (attr.isEmpty())
			if (required)
				throw new IllegalArgumentException(
						"empty Shibboleth header " + name);
			else
				return null;
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
			if (required)
				throw new IllegalArgumentException(
						"empty Shibboleth attribute " + name);
			else
				return null;
		return values[0];
	}

	public boolean hasValidInfo() {
		return displayName != null && email != null && userID != null;
	}

	public void parseAuthResponse(final HttpServletRequest request) {
		this.userID = getShibAttr(request, userIDAttribute, true);
		this.email = getShibAttr(request, emailAttribute, true);
		this.displayName = displayNameAttribute == null ? null
				: getShibAttr(request, displayNameAttribute, false);
		this.surname = getShibAttr(request, surnameAttribute, false);
		this.givenName = getShibAttr(request, givenNameAttribute, false);
		if (displayName == null && surname == null)
			throw new IllegalArgumentException(
					"neither display name nor given name provided");
	}

	public UserInfo getUserInfo() {
		// if we have an explicit surname from Shib, use it even if we don't
		// have a given name. it's still better than splitting the display name
		// incorrectly.
		if (surname != null)
			return new UserInfo(userID, email, surname,
					givenName != null ? givenName : "");

		final Name name = nameSplitter.split(displayName);
		return new UserInfo(userID, email, name.surname, name.givenname);
	}
}
