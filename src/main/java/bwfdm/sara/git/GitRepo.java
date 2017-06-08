package bwfdm.sara.git;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

public abstract class GitRepo {
	private final String id;

	protected GitRepo(final String id) {
		this.id = id;
	}

	/**
	 * @return ID of the repository, as given in the login API {@code repo=}
	 *         query variable
	 */
	public String getID() {
		return id;
	}

	/**
	 * @return name of the project, as given in the login API {@code project=}
	 *         query variable
	 */
	public abstract String getProject();

	/**
	 * @param project
	 *            name of the project, as given in the login API
	 *            {@code project=} query variable
	 */
	public abstract void setProject(final String project);

	/** @return the url of the "main" page to view a project */
	public abstract String getProjectViewURL();

	/**
	 * @param ref
	 *            the branch or tag containing the {@code LICENSE} file to edit,
	 *            in git ref format (ie. {@code heads/master} or
	 *            {@code tags/v1.0})
	 * @return the url of a page where the user can edit the {@code LICENSE}
	 *         file
	 */
	public abstract String getEditLicenseURL(final String ref);

	/** @return a list of all branches in the given project */
	public abstract List<Branch> getBranches();

	/** @return a list of all tags in the given project */
	public abstract List<Tag> getTags();

	/** @return the project metadata */
	public abstract ProjectInfo getProjectInfo();

	/**
	 * Updates the project metadata with the fields provided. <code>null</code>
	 * means to keep the current value.
	 * 
	 * @param name
	 *            new project name, or <code>null</code>
	 * @param description
	 *            new project description, or <code>null</code>
	 */
	public abstract void updateProjectInfo(String name, String description);

	/**
	 * @param ref
	 *            git ref, should be {@code heads/master} or {@code tags/test}
	 * @param limit
	 *            maximum number of items to return. GitLab clamps this to 100
	 *            max
	 * @return a list of the first few commits in a given branch or tag
	 */
	public abstract List<Commit> getCommits(final String ref, final int limit);

	/**
	 * @return <code>true</code> if we already have a token for GitLab,
	 *         <i>and</i> that token actually works (IOW this method must test
	 *         whether the token still works)
	 */
	public abstract boolean hasWorkingToken();

	/**
	 * Trigger the authorization process.
	 * 
	 * @param redirURI
	 *            login response URL. anything sent here will end up in
	 *            {@link #parseLoginResponse(RedirectAttributes, HttpSession)}
	 * @param redir
	 *            {@link RedirectAttributes} for setting the query string
	 *            attributes of the returned {@link RedirectView} (weird API)
	 * @param session
	 *            the user's {@link HttpSession}, for storing auth-related state
	 * 
	 * @return a {@link RedirectView} for the authorization URL. usually
	 *         <i>not</i> {@code redirURI} but something that interacts with the
	 *         user and <i>then</i> goes to {@code redirURI}. should return
	 *         <code>null</code> if there is already a working token so that the
	 *         user doesn't have to go through authorization again.
	 */
	public abstract RedirectView triggerLogin(final String redirURI,
			RedirectAttributes redir, HttpSession session);

	/**
	 * Called when the authorization process has finished.
	 * 
	 * @param params
	 *            the parameters that were posted to the login response URL
	 * @param session
	 *            the user's {@link HttpSession}, for retrieving that stored
	 *            auth-related state
	 * 
	 * @return <code>true</code> if authorization was successful;
	 *         <code>false</code> redirects to an error page instead
	 */
	public abstract boolean parseLoginResponse(
			final Map<String, String> params, final HttpSession session);
}
