package bwfdm.sara.api;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/api/clone")
public class Clone {
	@GetMapping("cancel")
	public RedirectView abortClone(final HttpSession session) {
		// final TransferRepo tx =
		// Project.getInstance(session).getTransferRepo();
		// tx.abort();
		return new RedirectView("/meta.html"); // FIXME will become overview
	}

	@GetMapping("status")
	public List<CloneStep> getStatus(final HttpSession session) {
		final List<CloneStep> status = new ArrayList<>();
		status.add(new CloneStep("init", "initialize temporary repository",
				CloneStatus.DONE));
		status.add(new CloneStep("branch:master", "clone branch master",
				CloneStatus.DONE));
		status.add(new CloneStep("branch:webapp", "clone branch webapp",
				(System.currentTimeMillis() / 1000 % 100) / 100f));
		status.add(new CloneStep("branch:test", "clone branch test",
				CloneStatus.PENDING));
		status.add(new CloneStep("tag:foo", "clone tag foo",
				CloneStatus.PENDING));
		return status;
	}

	@JsonInclude(Include.NON_NULL)
	private static class CloneStep {
		@JsonProperty
		private final String id;
		@JsonProperty
		private final CloneStatus status;
		@JsonProperty
		private final String text;
		@JsonProperty
		private final float progress;

		private CloneStep(final String id, final String text,
				final CloneStatus status) {
			this.id = id;
			this.status = status;
			this.text = text;
			progress = status == CloneStatus.DONE ? 1 : 0;
		}

		private CloneStep(final String id, final String text,
				final float progress) {
			this.id = id;
			status = CloneStatus.WORKING;
			this.text = text;
			this.progress = progress;
		}
	}

	private enum CloneStatus {
		@JsonProperty("pending")
		PENDING, //
		@JsonProperty("working")
		WORKING, //
		@JsonProperty("done")
		DONE
	}
}
