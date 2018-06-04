package bwfdm.sara.api;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import bwfdm.sara.project.Project;
import bwfdm.sara.project.RefAction;
import bwfdm.sara.project.RefAction.PublicationMethod;
import bwfdm.sara.transfer.Task.TaskStatus;

@RestController
@RequestMapping("/api/push")
public class Push {
	@GetMapping("cancel")
	public RedirectView abortPush(final HttpSession session) {
		Project.getInstance(session).cancelPush();
		return new RedirectView("/overview.html");
	}

	@GetMapping("status")
	public TaskStatus getStatus(final HttpSession session) {
		return Project.getInstance(session).getPushStatus();
	}

	@PostMapping("trigger")
	public TaskStatus triggerClone(final HttpSession session) {
		final Project tx = Project.getInstance(session);
		tx.startPush();
		return tx.getPushStatus();
	}

	@GetMapping("trigger")
	public RedirectView triggerCloneAndRedirect(final HttpSession session) {
		triggerClone(session);
		return new RedirectView("/push.html");
	}

	@GetMapping("redirect")
	public RedirectView getWebUrl(final HttpSession session) {
		// TODO should kill the TransferRepo at around this point
		// (but definitely not HERE!)
		Project project = Project.getInstance(session);
		if (isArchiveOnly(project))
			return new RedirectView("/done.html");
		return new RedirectView("/publish.html");
	}

	private boolean isArchiveOnly(final Project project) {
		for (RefAction action : project.getFrontendDatabase().getRefActions())
			if (action.publicationMethod != PublicationMethod.ARCHIVE_HIDDEN)
				return false;
		return true;
	}
}
