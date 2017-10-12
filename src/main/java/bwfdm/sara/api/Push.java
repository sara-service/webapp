package bwfdm.sara.api;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import bwfdm.sara.project.Project;
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

	@GetMapping("web-url")
	@ResponseBody
	public String getWebUrl(final HttpSession session) {
		// TODO should kill the TransferRepo at around this point
		// (but definitely not HERE!)

		// we're done now; there is no point for the user to go back
		return Project.getInstance(session).getWebURL();
	}
}
