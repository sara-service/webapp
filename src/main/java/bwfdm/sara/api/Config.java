package bwfdm.sara.api;

import java.util.NoSuchElementException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

public class Config {
	private static final String WEBROOT_ATTR = "sara.webroot";

	// FIXME there can be more than one, and parameters shouldn't be hardcoded
	public static final String APP_ID = "d6f80baadb28e3d9d20b79f6a27c0747f6692a67321375e40be1a1b0fd8bb430";
	public static final String GITLAB = "https://arbeits-gitlab.unikn.netfuture.ch";
	public static final String APP_SECRET = "6df3596f15aaa0b4e1ecd4297c697e42632b2cab4c4224bbdd6a34b9ed0674f1";

	public static String getWebRoot(final HttpSession session) {
		return getWebRoot(session.getServletContext());
	}

	public static String getWebRoot(final ServletContext context) {
		return getContextParam(context, WEBROOT_ATTR);
	}

	private static String getContextParam(final ServletContext context,
			final String name) {
		final String attr = context.getInitParameter(name);
		if (attr == null)
			throw new NoSuchElementException("missing context attribute "
					+ name);
		return attr;
	}
}
