package bwfdm.sara.auth;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import bwfdm.sara.project.Name;

public interface AuthProvider {
	/**
	 * @return <code>true</code> if we already have a token for GitLab,
	 *         <i>and</i> that token actually works (IOW this method must test
	 *         whether the token still works)
	 */
	public boolean hasWorkingToken();

	/**
	 * Get a user's validated credentials. The user must not be able to change
	 * the {@link UserInfo#email} and {@link UserInfo#userID} fields without
	 * validating them! If that isn't possible to ensure, use
	 * {@link #getShibAuth()} to wrap everything with Shibboleth and get a
	 * validated email and user ID that way.
	 * 
	 * @return a {@link UserInfo} object containing the user's display name and
	 *         email
	 */
	public UserInfo getUserInfo();

	/**
	 * Trigger the authorization process.
	 * 
	 * @param redirURI
	 *            login response URL. anything sent here will end up in
	 *            {@link #parseAuthResponse(RedirectAttributes, HttpSession)}
	 * @param redir
	 *            {@link RedirectAttributes} for setting the query string
	 *            attributes of the returned {@link RedirectView} (weird API)
	 * @param session
	 *            the user's {@link HttpSession}, for storing auth-related state
	 * 
	 * @return a {@link RedirectView} for the authorization URL. usually
	 *         <i>not</i> {@code redirURI} but something that interacts with the
	 *         user and <i>then</i> goes to {@code redirURI}. should return
	 *         <code>null</code> if there is already a working token so that the
	 *         user doesn't have to go through authorization again.
	 */
	public RedirectView triggerAuth(final String redirURI,
			RedirectAttributes redir, HttpSession session);

	/**
	 * Called when the authorization process has finished.
	 * 
	 * @param params
	 *            the parameters that were posted to the login response URL
	 * @param session
	 *            the user's {@link HttpSession}, for retrieving that stored
	 *            auth-related state
	 * @return <code>true</code> if authorization was successful;
	 *         <code>false</code> redirects to an error page instead
	 */
	public boolean parseAuthResponse(final Map<String, String> params,
			final HttpSession session);

	/**
	 * @return the {@link ShibAuth} instance when using Shibboleth. Usually just
	 *         <code>null</code> to skip Shibboleth auth.
	 */
	public ShibAuth getShibAuth();

	public static class UserInfo {
		/**
		 * The user's unique user ID. Unspecified format; can be the email
		 * address if it's guaranteed to be unique.
		 */
		public final String userID;
		/**
		 * The user's primary email address, verified to belong to that user
		 * (ie. the user clicked a link to confirm it).
		 */
		public final String email;
		/**
		 * The user's preferred way of representing his/her name in a Unicode
		 * string.
		 */
		public final Name name;

		public UserInfo(final String userID, final String email,
				final Name name) {
			this.userID = userID;
			this.email = email;
			this.name = name;
		}
	}
}
