package bwfdm.sara.gitlab;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

class GitLabREST {
	private static final String MAX_PER_PAGE = "100";
	private static final Pattern URL_FIELD = Pattern.compile("<(.*)>");
	private static final Pattern REL_NEXT_FIELD = Pattern
			.compile("rel=(\"?)next\\1");

	private final HttpEntity<Void> auth;
	private final RestTemplate rest;
	private final String root;

	GitLabREST(final String gitlab, final String project, final String token) {
		if (token == null)
			throw new IllegalStateException("no GitLab token available");

		try {
			root = gitlab + "/api/v4/projects/"
					+ UriUtils.encodePathSegment(project, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 unsupported?!", e);
		}
		final MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.set("Authorization", "Bearer " + token);
		auth = new HttpEntity<Void>(map);
		rest = new RestTemplate();
	}

	public <T> T get(final String endpoint,
			final ParameterizedTypeReference<T> type) {
		return get(UriComponentsBuilder.fromHttpUrl(root + endpoint), type);
	}

	private <T> T get(final UriComponentsBuilder ucb,
			final ParameterizedTypeReference<T> type) {
		return rest.exchange(ucb.build(true).toUri(), HttpMethod.GET, auth,
				type).getBody();
	}

	public <T> List<T> getList(final String endpoint,
			final ParameterizedTypeReference<List<T>> type) {
		return getList(UriComponentsBuilder.fromHttpUrl(root + endpoint), type);
	}

	private <T> List<T> getList(final UriComponentsBuilder ucb,
			final ParameterizedTypeReference<List<T>> type) {
		final List<T> list = new ArrayList<T>();
		// 100 per page is the limit. use it for max efficiency.
		URI uri = ucb.queryParam("per_page", MAX_PER_PAGE).build(true).toUri();
		do {
			final ResponseEntity<List<T>> resp = rest.exchange(uri,
					HttpMethod.GET, auth, type);
			list.addAll(resp.getBody());
			// work around the pagination misfeature
			uri = getNextLink(resp, uri);
		} while (uri != null);
		return list;
	}

	/** Workaround for GitLab's pagination "feature". */
	private URI getNextLink(final ResponseEntity<?> resp, final URI base) {
		final List<String> linkHeaders = resp.getHeaders()
				.get(HttpHeaders.LINK);
		if (linkHeaders == null)
			return null; // header missing; probably no next page

		for (final String linkHeader : linkHeaders)
			for (final String link : linkHeader.split(",\\s*")) {
				final String[] fields = link.split(";\\s*");
				final Matcher m = URL_FIELD.matcher(fields[0]);
				if (!m.matches())
					throw new IllegalArgumentException(
							"malformed Link header: " + link);
				final String url = m.group(1);

				for (int i = 1; i < fields.length; i++) {
					if (REL_NEXT_FIELD.matcher(fields[i]).matches())
						try {
							// to handle relative URLs
							return new URL(base.toURL(), url).toURI();
						} catch (MalformedURLException | URISyntaxException e) {
							throw new IllegalArgumentException(
									"malformed URL in Link header: " + url, e);
						}
				}
			}
		return null; // header present but no "next" link, ie. last page
	}
}
