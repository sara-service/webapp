package bwfdm.sara.auth;

import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * REST helper with authentication. must be subclassed by something that handles
 * creation of the actual authorization headers.
 */
public abstract class AuthenticatedREST {
	private final RestTemplate rest = new RestTemplate();
	private final MultiValueMap<String, String> defaultHeaders = new LinkedMultiValueMap<>();
	private final String root;
	private HttpEntity<Void> auth;
	private MultiValueMap<String, String> headers;

	protected AuthenticatedREST(final String root) {
		this.root = root;
	}

	/**
	 * Sets a header to send in all requests.
	 * 
	 * @param key
	 *            header name
	 * @param values
	 *            header value(s)
	 */
	public void addHeader(final String key, String... values) {
		for (String value : values)
			defaultHeaders.add(key, value);
	}

	/**
	 * Sets the headers to use for authentication.
	 * 
	 * @param authMap
	 *            authentications headers to use for each request, or
	 *            <code>null</code> to invalidate authorization
	 */
	protected void setAuth(final MultiValueMap<String, String> authMap) {
		this.headers = mergeHeaders(defaultHeaders, authMap);
		if (authMap != null)
			auth = new HttpEntity<Void>(headers);
		else
			auth = null;
	}

	@SafeVarargs
	private final MultiValueMap<String, String> mergeHeaders(
			MultiValueMap<String, String>... maps) {
		MultiValueMap<String, String> res = new LinkedMultiValueMap<>();
		for (MultiValueMap<String, String> map : maps)
			if (map != null)
				for (String key : map.keySet())
					for (String value : map.get(key))
						res.add(key, value);
		return res;
	}

	/** @return <code>true</code> if authorization headers have been set */
	public boolean hasAuth() {
		return auth != null;
	}

	public UriComponentsBuilder uri(final String endpoint) {
		return UriComponentsBuilder.fromHttpUrl(root + endpoint);
	}

	/**
	 * Performs a {@link HttpMethod#POST} request, sending the specified
	 * parameters as url-encoded form data.
	 * 
	 * @param ucb
	 *            the URL as a {@link UriComponentsBuilder}
	 * @param args
	 *            name:value pairs to pass as parameters
	 */
	public void post(final UriComponentsBuilder ucb,
			final Map<String, String> args) {
		rest.exchange(ucb.build(true).toUri(), HttpMethod.POST,
				new HttpEntity<>(args, headers), (Class<?>) null);
	}

	/**
	 * Performs a {@link HttpMethod#POST} request, sending the specified
	 * parameters as url-encoded form data, and converting the result into an
	 * instance of the specified class.
	 * 
	 * @param ucb
	 *            the URL as a {@link UriComponentsBuilder}
	 * @param args
	 *            name:value pairs to pass as parameters
	 * @param type
	 *            instance of {@link ParameterizedTypeReference} with correct
	 *            type parameters for return value
	 */
	public <T> T post(final UriComponentsBuilder ucb,
			final Map<String, String> args,
			final ParameterizedTypeReference<T> type) {
		return rest.exchange(ucb.build(true).toUri(), HttpMethod.POST,
				new HttpEntity<>(args, headers), type).getBody();
	}

	/**
	 * Performs a {@link HttpMethod#PUT} request. Can be given either a single
	 * object (which will be sent as JSON) or a {@link Map} of name:value pairs
	 * (which will be sent as form data).
	 * 
	 * @param ucb
	 *            the URL as a {@link UriComponentsBuilder}
	 * @param args
	 *            an object to serialize or a {@link Map} of name:value pairs
	 */
	public void put(final UriComponentsBuilder ucb, final Object args) {
		rest.exchange(ucb.build(true).toUri(), HttpMethod.PUT,
				new HttpEntity<>(args, headers), (Class<?>) null);
	}

	/**
	 * Performs a {@link HttpMethod#DELETE} request. Doesn't send any
	 * parameters.
	 * 
	 * @param ucb
	 *            the URL as a {@link UriComponentsBuilder}
	 */
	public void delete(final UriComponentsBuilder ucb) {
		rest.exchange(ucb.build(true).toUri(), HttpMethod.DELETE, auth,
				(Class<?>) null);
	}

	/**
	 * Performs a {@link HttpMethod#GET} request, deserializing the returned
	 * JSON into an object of the given type.
	 * 
	 * @param ucb
	 *            the URL as a {@link UriComponentsBuilder}
	 * @param type
	 *            instance of {@link ParameterizedTypeReference} with correct
	 *            type parameters
	 * @return an instance of the specified object, deserialized from JSON
	 */
	public <T> T get(final UriComponentsBuilder ucb,
			final ParameterizedTypeReference<T> type) {
		return getResponse(ucb, type).getBody();
	}

	/**
	 * Performs a {@link HttpMethod#GET} request, deserializing the returned
	 * JSON into an object of the given type. Returns a {@link ResponseEntity}
	 * so that headers can be inspected; the deserialized object can be obtained
	 * with {@link ResponseEntity#getBody()}.
	 * 
	 * @param ucb
	 *            the URL as a {@link UriComponentsBuilder}
	 * @param type
	 *            instance of {@link ParameterizedTypeReference} with correct
	 *            type parameters
	 * @return an {@link ResponseEntity} containing the headers and the
	 *         deserialized object
	 */
	public <T> ResponseEntity<T> getResponse(final UriComponentsBuilder ucb,
			final ParameterizedTypeReference<T> type) {
		return rest.exchange(ucb.build(true).toUri(), HttpMethod.GET, auth,
				type);
	}

	/**
	 * Performs a {@link HttpMethod#GET} request, returning the raw data
	 * received as a {@code byte[]}.
	 * 
	 * @param ucb
	 *            the URL as a {@link UriComponentsBuilder}
	 * @return a byte array containing the raw response
	 */
	public byte[] getBlob(final UriComponentsBuilder ucb) {
		return rest.exchange(ucb.build(true).toUri(), HttpMethod.GET, auth,
				byte[].class).getBody();
	}

}