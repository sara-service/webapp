package bwfdm.sara.git.gitlab;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import bwfdm.sara.auth.AuthenticatedREST;

/** REST helper with GitLab {@code PRIVATE-TOKEN: t0k3N} authentication. */
class PrivateTokenREST extends AuthenticatedREST {
	PrivateTokenREST(final String root, final String token) {
		super(root);
		final MultiValueMap<String, String> authMap = new LinkedMultiValueMap<String, String>();
		authMap.set("PRIVATE-TOKEN", token);
		setAuth(authMap);
	}
}
