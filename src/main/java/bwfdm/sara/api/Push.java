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
import bwfdm.sara.transfer.TransferRepo;

@RestController
@RequestMapping("/api/push")
public class Push {
	@GetMapping("cancel")
	public RedirectView abortPush(final HttpSession session) {
		Project.getInstance(session).getTransferRepo().cancelPush();
		return new RedirectView("/overview.html");
	}

	@GetMapping("status")
	public TaskStatus getStatus(final HttpSession session) {
		return Project.getInstance(session).getTransferRepo().getPushStatus();
	}

	@PostMapping("trigger")
	public TaskStatus triggerClone(final HttpSession session) {
		final TransferRepo tx = Project.getInstance(session).getTransferRepo();
		tx.startPush();
		return tx.getPushStatus();
	}

	@GetMapping("web-url")
	@ResponseBody
	public String getWebUrl(final HttpSession session) {
		// TODO should kill the session at around this point!
		// we're done now; there is no point for the user to go back
		return Project.getInstance(session).getTransferRepo().getWebURL();
	}
}
