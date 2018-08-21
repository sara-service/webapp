package bwfdm.sara.api;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import bwfdm.sara.db.FrontendDatabase;
import bwfdm.sara.project.ArchiveJob;
import bwfdm.sara.project.Project;
import bwfdm.sara.transfer.PushTask;
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
		// getInstance() because the project only switches to "completed" once
		// commitToArchive() has been called, which only happens after
		// archiving.
		return Project.getInstance(session).getPushTask().getStatus();
	}

	@PostMapping("trigger")
	public TaskStatus triggerPush(@RequestParam("token") final String hash,
			final HttpSession session) {
		final Project project = Project.getInstance(session);
		final ArchiveJob job = project.getArchiveJob();
		final String curHash = job.getHash();
		if (!curHash.equals(hash))
			throw new IllegalArgumentException("trying to trigger " + hash
					+ " but session has data for " + curHash);
		project.startPush(job);
		return project.getPushTask().getStatus();
	}

	@GetMapping("trigger")
	public RedirectView triggerPushAndRedirect(
			@RequestParam("token") final String hash,
			final HttpSession session) {
		triggerPush(hash, session);
		return new RedirectView("/push.html");
	}

	@GetMapping("overview")
	public ArchiveJob getPushSummary(final HttpSession session) {
		return Project.getInstance(session).getArchiveJob();
	}

	@PostMapping("commit")
	public void commitToArchive(
			@RequestParam("public_access") final boolean isPublic,
			@RequestParam("record") final boolean record,
			final HttpSession session) {
		final Project project = Project.getCompletedInstance(session);
		// note: setArchiveAccess throws an exception if we try to change the
		// access rights on an already-committed item. we depend on that to
		// avoid incorrect behavior!
		project.getFrontendDatabase().setArchiveAccess(isPublic, record);
		project.getPushTask().commitToArchive(isPublic);
		project.disposeTransferRepo();
	}

	@GetMapping("redirect")
	public RedirectView redirectAfterArchiving(final HttpSession session,
			final RedirectAttributes redir) {
		final Project project = Project.getCompletedInstance(session);
		if (!project.isDone())
			throw new IllegalStateException(
					"redirectAfterArchiving before push is done");

		final PushTask push = project.getPushTask();
		final FrontendDatabase db = project.getFrontendDatabase();
		if (db.isPublic() && db.createPublicationRecord()) {
			redir.addAttribute("item", push.getItemUUID());
			return new RedirectView("/api/auth/publish");
		}

		redir.addAttribute("token", push.getAccessToken());
		redir.addAttribute("item", push.getItemUUID());
		return new RedirectView("/info.html");
	}
}
