package bwfdm.sara.git.gitlab;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import bwfdm.sara.auth.AuthenticatedREST;

/** low-level helper class for making REST calls to the GitLab API. */
class RESTHelper {
	private static final int MAX_PER_PAGE = 100;
	private static final Pattern LINK_URL_FIELD = Pattern.compile("<(.*)>");
	private static final Pattern LINK_REL_NEXT_FIELD = Pattern
			.compile("rel=(\"?)next\\1");

	private final AuthenticatedREST rest;
	private final String prefix;

	RESTHelper(final AuthenticatedREST rest, final String prefix) {
		this.rest = rest;
		this.prefix = prefix;

		// anything else will cause double slashes in the final URL
		assert prefix.startsWith("/") || prefix.isEmpty();
		assert prefix.endsWith("/");
	}

	protected UriComponentsBuilder uri(final String endpoint) {
		return rest.uri(prefix + endpoint);
	}

	/**
	 * Get a list of items from GitLab, working around the pagination misfeature
	 * by requesting pages one by one. If you need just a few, use
	 * {@link #get(UriComponentsBuilder, ParameterizedTypeReference)} and set
	 * {@code per_page} manually.
	 * 
	 * @param endpoint
	 *            API path, relative to project
	 * @param type
	 *            instance of {@link ParameterizedTypeReference} with correct
	 *            type parameters
	 * @return a list of all objects that GitLab returns
	 */
	protected <T> List<T> getList(final UriComponentsBuilder ucb,
			final ParameterizedTypeReference<List<T>> type) {
		final List<T> list = new ArrayList<T>();
		// 100 per page is the limit. use it for max efficiency.
		UriComponentsBuilder uri = ucb.queryParam("per_page",
				Integer.toString(MAX_PER_PAGE));
		do {
			final ResponseEntity<List<T>> resp = rest.getResponse(uri, type);
			list.addAll(resp.getBody());
			// work around the pagination misfeature
			uri = getNextPageLink(resp, uri);
		} while (uri != null);
		return list;
	}

	/** Workaround for GitLab's pagination "feature". */
	private static UriComponentsBuilder getNextPageLink(
			final ResponseEntity<?> resp, final UriComponentsBuilder base) {
		final List<String> linkHeaders = resp.getHeaders()
				.get(HttpHeaders.LINK);
		if (linkHeaders == null)
			return null; // header missing; probably no next page

		for (final String linkHeader : linkHeaders)
			for (final String link : linkHeader.split(",\\s*")) {
				final String[] fields = link.split(";\\s*");
				final Matcher m = LINK_URL_FIELD.matcher(fields[0]);
				if (!m.matches())
					throw new IllegalArgumentException(
							"malformed Link header: " + link);
				final String url = m.group(1);

				for (int i = 1; i < fields.length; i++) {
					if (LINK_REL_NEXT_FIELD.matcher(fields[i]).matches())
						// correctly resolve relative URLs, just in case
						return UriComponentsBuilder.fromUri(base.build(true)
								.toUri().resolve(url));
				}
			}
		return null; // header present but no "next" link, ie. the last page
	}

	<T> T get(final UriComponentsBuilder ucb,
			final ParameterizedTypeReference<T> type) {
		return rest.get(ucb, type);
	}

	<T> ResponseEntity<T> getResponse(final UriComponentsBuilder ucb,
			final ParameterizedTypeReference<T> type) {
		return rest.getResponse(ucb, type);
	}

	byte[] getBlob(final UriComponentsBuilder ucb) {
		return rest.getBlob(ucb);
	}

	void post(final UriComponentsBuilder ucb, final Map<String, String> args) {
		rest.post(ucb, args);
	}

	void put(final UriComponentsBuilder ucb, final Object args) {
		rest.put(ucb, args);
	}

	void setToken(final String token) {
		rest.setToken(token);
	}

	boolean hasToken() {
		return rest.hasToken();
	}
}
