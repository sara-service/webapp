package bwfdm.sara.git.gitlab;

import bwfdm.sara.auth.AuthenticatedREST;

/** REST helper with GitLab {@code PRIVATE-TOKEN: t0k3N} authentication. */
class PrivateTokenREST extends AuthenticatedREST {
	PrivateTokenREST(final String root, final String token) {
		super(root);
		addHeader("PRIVATE-TOKEN", token);
	}
}
