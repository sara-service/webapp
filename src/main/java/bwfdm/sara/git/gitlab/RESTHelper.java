package bwfdm.sara.git.gitlab;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

/** low-level helper class for making REST calls to the GitLab API. */
class RESTHelper {
	static final String API_PREFIX = "/api/v4";
	/**
	 * date format pattern used by GitLab, {@link SimpleDateFormat} style.
	 * currently ISO8601 ({@code 2012-09-20T11:50:22.000+03:00}).
	 */
	static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	/**
	 * date format used by GitLab, as a {@link SimpleDateFormat}. currently
	 * ISO8601 ({@code 2012-09-20T11:50:22.000+03:00}).
	 */
	static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			DATE_FORMAT_PATTERN);

	private static final int MAX_PER_PAGE = 100;
	private static final Pattern LINK_URL_FIELD = Pattern.compile("<(.*)>");
	private static final Pattern LINK_REL_NEXT_FIELD = Pattern
			.compile("rel=(\"?)next\\1");

	private final AuthenticatedREST rest;
	private final String root;

	/**
	 * @param rest
	 *            the {@link AuthenticatedREST} instance ot use for accessing
	 *            the API
	 * @param root
	 *            URL to GitLab root
	 * @param project
	 *            name of project whose API to access
	 */
	RESTHelper(final AuthenticatedREST rest, final String root,
			final String project) {
		this.rest = rest;
		this.root = root + API_PREFIX + "/projects/"
				+ UrlEncode.encodePathSegment(project);
	}

	UriComponentsBuilder uri(final String endpoint) {
		return UriComponentsBuilder.fromHttpUrl(root + endpoint);
	}

	/**
	 * Get a single item from GitLab. Works for lists, but will only return the
	 * first page of 100 elements at most. Use
	 * {@link #getList(String, ParameterizedTypeReference)} to get all elements.
	 * 
	 * @param endpoint
	 *            API path, relative to project
	 * @param type
	 *            instance of {@link ParameterizedTypeReference} with correct
	 *            type parameters
	 * @return an instance of the specified object, deserialized from JSON
	 */
	<T> T get(final String endpoint, final ParameterizedTypeReference<T> type) {
		return rest.get(uri(endpoint), type);
	}

	/**
	 * Variant of {@link #get(String, ParameterizedTypeReference)} that takes a
	 * {@link UriComponentsBuilder}.
	 */
	<T> List<T> get(final UriComponentsBuilder queryParam,
			final ParameterizedTypeReference<List<T>> type) {
		return rest.get(queryParam, type);
	}

	/**
	 * Performs a {@link HttpMethod#GET} request, returning the raw data
	 * received as a {@code byte[]}.
	 * 
	 * @param ucb
	 *            the URL as a {@link UriComponentsBuilder}
	 * @return a byte array containing the raw response
	 */
	byte[] getBlob(final UriComponentsBuilder ucb) {
		return rest.getBlob(ucb);
	}

	/**
	 * Get a list of items from GitLab, working around the pagination misfeature
	 * by requesting pages one by one. If you need just a few, use
	 * {@link AuthenticatedREST#get(UriComponentsBuilder, ParameterizedTypeReference)}
	 * and set {@code per_page} manually.
	 * 
	 * @param endpoint
	 *            API path, relative to project
	 * @param type
	 *            instance of {@link ParameterizedTypeReference} with correct
	 *            type parameters
	 * @return a list of all objects that GitLab returns
	 */
	<T> List<T> getList(final String endpoint,
			final ParameterizedTypeReference<List<T>> type) {
		return getList(uri(endpoint), type);
	}

	<T> List<T> getList(final UriComponentsBuilder ucb,
			final ParameterizedTypeReference<List<T>> type) {
		return getList(rest, ucb, type);
	}

	/**
	 * Static version of
	 * {@link #getList(UriComponentsBuilder, ParameterizedTypeReference)} for
	 * requesting the project list.
	 */
	static <T> List<T> getList(final AuthenticatedREST rest,
			final UriComponentsBuilder ucb,
			final ParameterizedTypeReference<List<T>> type) {
		final List<T> list = new ArrayList<T>();
		// 100 per page is the limit. use it for max efficiency.
		UriComponentsBuilder uri = ucb.queryParam("per_page",
				Integer.toString(MAX_PER_PAGE));
		do {
			final ResponseEntity<List<T>> resp = rest.getResponse(uri, type);
			list.addAll(resp.getBody());
			// work around the pagination misfeature
			uri = getNextLink(resp, uri);
		} while (uri != null);
		return list;
	}

	/** Workaround for GitLab's pagination "feature". */
	private static UriComponentsBuilder getNextLink(
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

	/**
	 * Convenience function for calling
	 * {@link AuthenticatedREST#put(UriComponentsBuilder, Object)}.
	 */
	void put(final String endpoint, final Object args) {
		rest.put(uri(endpoint), args);
	}

	/**
	 * Convenience function for calling
	 * {@link AuthenticatedREST#post(UriComponentsBuilder, Object)}.
	 */
	void post(final String endpoint, final Map<String, String> args) {
		rest.post(uri(endpoint), args);
	}
}
