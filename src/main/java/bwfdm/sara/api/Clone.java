package bwfdm.sara.api;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import bwfdm.sara.project.Project;
import bwfdm.sara.transfer.Task.TaskStatus;
import bwfdm.sara.transfer.TransferRepo;

@RestController
@RequestMapping("/api/clone")
public class Clone {
	@GetMapping("cancel")
	public RedirectView abortClone(final HttpSession session) {
		Project.getInstance(session).getTransferRepo().invalidate();
		return new RedirectView("/meta.html"); // FIXME will become branches
	}

	@GetMapping("status")
	public TaskStatus getStatus(final HttpSession session) {
		return Project.getInstance(session).getTransferRepo().getStatus();
	}

	@PostMapping("trigger")
	public TaskStatus triggerClone(final HttpSession session) {
		final TransferRepo tx = Project.getInstance(session).getTransferRepo();
		tx.initialize();
		return tx.getStatus();
	}
}
