package bwfdm.sara.api;

import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import bwfdm.sara.project.Project;
import bwfdm.sara.project.PublicationSession;
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
		return Project.getInstance(session).getPushTask().getStatus();
	}

	@PostMapping("trigger")
	public TaskStatus triggerClone(final HttpSession session) {
		final Project tx = Project.getInstance(session);
		tx.startPush();
		return tx.getPushTask().getStatus();
	}

	@GetMapping("trigger")
	public RedirectView triggerCloneAndRedirect(final HttpSession session) {
		triggerClone(session);
		return new RedirectView("/push.html");
	}

	@GetMapping("redirect")
	public RedirectView redirectAfterArchiving(final HttpSession session,
			final RedirectAttributes redir) {
		if (!Project.getInstance(session).getPushTask().isDone())
			// TODO or maybe just redirect to push.html instead?
			throw new IllegalStateException(
					"finishArchiving before push is done");

		// FIXME kill Project so everything just redirects to END
		// TODO and also get rid of the TransferRepo!

		final Project project = Project.getInstance(session);
		final PushTask push = project.getPushTask();
		final UUID item = push.getItemUUID();
		if (push.getArchiveJob().isArchiveOnly) {
			redir.addAttribute("item", item);
			return new RedirectView("/done.html");
		}

		final PublicationSession publish;
		if (PublicationSession.hasInstance(session)) {
			// no need to panic if we already have a publication session. but do
			// panic if it's for a different item; that should never happen.
			publish = PublicationSession.getInstance(session);
			UUID prevItem = publish.getItemUUID();
			if (prevItem != item)
				throw new IllegalStateException(
						"finishArchiving would override exiting session: "
								+ prevItem + " → " + item);
		} else
			// directly create new session, inheriting authorization from
			// archiving part. this is the common case, obviously.
			publish = PublicationSession.createInstance(session,
					UUID.fromString(project.getRepoID()), project.getGitRepo(),
					item, project.getConfig());
		publish.initialize();

		return new RedirectView("/publish.html");
	}
}
