package bwfdm.sara.git.gitlab;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import bwfdm.sara.auth.AuthenticatedREST;

/** REST helper with GitLab {@code PRIVATE-TOKEN: t0k3N} authentication. */
class PrivateTokenREST extends AuthenticatedREST {
	PrivateTokenREST(final String root, final String token) {
		super(root);
		MultiValueMap<String, String> auth = new LinkedMultiValueMap<>();
		auth.add("PRIVATE-TOKEN", token);
		setAuth(auth);
	}
}
