package bwfdm.sara.git.gitlab;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriUtils;

import bwfdm.sara.auth.OAuthCode;
import bwfdm.sara.git.Branch;
import bwfdm.sara.git.GitRepo;
import bwfdm.sara.git.Tag;

/** high-level abstraction of the GitLab API. */
public class GitLab extends GitRepo {
	private final String root;
	private final String appID;
	private final String appSecret;
	private RESTHelper rest;
	private OAuthCode auth;
	private String project;

	/**
	 * @param id
	 * @param gitlab
	 *            URL to GitLab root
	 * @param appID
	 * @param appSecret
	 */
	public GitLab(final String id, final String root, final String appID,
			final String appSecret) {
		super(id);
		this.root = root;
		this.appID = appID;
		this.appSecret = appSecret;
	}

	@Override
	public void setProject(final String project) {
		this.project = project;
		rest = new RESTHelper(root, project);
	}

	@Override
	public String getProject() {
		return project;
	}

	@Override
	public boolean hasWorkingToken() {
		if (!rest.hasToken())
			return false;

		try {
			getProjectInfo();
			return true;
		} catch (final IllegalArgumentException e) {
			// guess that didn't work
			return false;
		}
	}

	@Override
	public RedirectView triggerLogin(final String redirURI,
			final RedirectAttributes redir, final HttpSession session) {
		if (hasWorkingToken())
			return null;

		auth = new OAuthCode(appID, appSecret, root + "/oauth");
		return auth.trigger(redirURI, redir);
	}

	@Override
	public boolean parseLoginResponse(
			final java.util.Map<String, String> params,
			final HttpSession session) {
		if (auth == null)
			return false;

		final String token = auth.parse(params);
		rest.setToken(token);
		return token != null;
	}

	@Override
	public String getProjectViewURL() {
		try {
			return root + "/" + UriUtils.decode(project, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 not supported?!", e);
		}
	}

	@Override
	public List<? extends Branch> getBranches() {
		final GLProjectInfo projectInfo = getProjectInfo();
		final List<? extends GLBranch> list = rest.getList(
				"/repository/branches",
				new ParameterizedTypeReference<List<GLBranch>>() {
				});
		for (final GLBranch branch : list)
			if (branch.name.equals(projectInfo.master))
				branch.isDefault = true;
		return list;
	}

	@Override
	public List<? extends Tag> getTags() {
		return rest.getList("/repository/tags",
				new ParameterizedTypeReference<List<GLTag>>() {
				});
	}

	@Override
	public GLProjectInfo getProjectInfo() {
		return rest.get("" /* the project itself */,
				new ParameterizedTypeReference<GLProjectInfo>() {
				});
	}

	@Override
	public List<GLCommit> getCommits(final String ref, final int limit) {
		return rest.get(
				rest.uri("/repository/commits").queryParam("ref_name", ref)
						.queryParam("per_page", Integer.toString(limit)),
				new ParameterizedTypeReference<List<GLCommit>>() {
				});
	}
}
