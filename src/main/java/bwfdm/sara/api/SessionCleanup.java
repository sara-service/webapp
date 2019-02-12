package bwfdm.sara.api;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import bwfdm.sara.project.Project;

@WebListener
public class SessionCleanup implements HttpSessionListener {
	@Override
	public void sessionCreated(final HttpSessionEvent se) {
		return;
	}

	@Override
	public void sessionDestroyed(final HttpSessionEvent se) {
		final HttpSession session = se.getSession();
		if (Project.hasInstance(session))
			Project.getCompletedInstance(session).disposeTransferRepo();
	}
}
