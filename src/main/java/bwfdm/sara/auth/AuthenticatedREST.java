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

/** REST helper with {@code Authorization: Bearer t0k3N} authentication. */
public class AuthenticatedREST {
	private final MultiValueMap<String, String> authMap = new LinkedMultiValueMap<String, String>();
	private final RestTemplate rest = new RestTemplate();
	private final String root;
	private HttpEntity<Void> auth;

	public AuthenticatedREST(final String root) {
		this.root = root;
	}

	public UriComponentsBuilder uri(final String endpoint) {
		return UriComponentsBuilder.fromHttpUrl(root + endpoint);
	}

	/**
	 * Sets the token to use for authentication.
	 * 
	 * @param token
	 *            token to pass in the header
	 */
	public void setToken(final String token) {
		if (token == null) {
			auth = null;
			return;
		}

		authMap.set("Authorization", "Bearer " + token);
		auth = new HttpEntity<Void>(authMap);
	}

	/** @return <code>true</code> if a token has been set */
	public boolean hasToken() {
		return auth != null;
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
				new HttpEntity<>(args, authMap), (Class<?>) null);
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
				new HttpEntity<>(args, authMap), (Class<?>) null);
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