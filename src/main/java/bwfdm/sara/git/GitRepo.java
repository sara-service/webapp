package bwfdm.sara.git;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import bwfdm.sara.project.Project;

/**
 * This interface exposes only the methods that can be invoked on GitLab without
 * having a project selected. In most cases, {@link GitProject} is the interface
 * to use.
 */
public interface GitRepo {
	/**
	 * @return <code>true</code> if we already have a token for GitLab,
	 *         <i>and</i> that token actually works (IOW this method must test
	 *         whether the token still works)
	 */
	public boolean hasWorkingToken();

	/**
	 * Trigger the authorization process.
	 * 
	 * @param redirURI
	 *            login response URL. anything sent here will end up in
	 *            {@link #parseAuthResponse(RedirectAttributes, HttpSession)}
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
	public RedirectView triggerAuth(final String redirURI,
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
	public boolean parseAuthResponse(final Map<String, String> params,
			final HttpSession session);

	/** @return a list of all projects in GitLab */
	public List<ProjectInfo> getProjects();

	/**
	 * Get a project in this git repo by path / ID. This should attempt not to
	 * invalidate an existing authorization if possible.
	 * 
	 * <p>
	 * Intended to be called by {@link Project#setProjectPath(String)} only;
	 * call that method instead.
	 * 
	 * @param project
	 *            path / ID used to identify the project in the git repo
	 * @return the {@link GitProject}, exposing functionality that needs to have
	 *         a project path set
	 */
	public GitProject getGitProject(final String project);

	/**
	 * @return the "home page" url of the repo, used when there is no other
	 *         place to redirect the user to
	 */
	public String getHomePageURL();
}
