package bwfdm.sara.api;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import bwfdm.sara.project.Project;
import bwfdm.sara.transfer.CloneTask;
import bwfdm.sara.transfer.Task.TaskStatus;

@RestController
@RequestMapping("/api/clone")
public class Clone {
	@GetMapping("cancel")
	public RedirectView abortClone(final HttpSession session) {
		Project.getInstance(session).resetTransferRepo();
		return new RedirectView("/branches.html");
	}

	@GetMapping("status")
	public TaskStatus getStatus(final HttpSession session) {
		return Project.getInstance(session).getInitStatus();
	}

	@PostMapping("trigger")
	public TaskStatus triggerClone(final HttpSession session) {
		final CloneTask clone = Project.getInstance(session)
				.createTransferRepo();
		return clone.getStatus();
	}

	@GetMapping("trigger")
	public RedirectView triggerCloneAndRedirect(final HttpSession session) {
		triggerClone(session);
		return new RedirectView("/clone.html");
	}
}
