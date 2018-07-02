package bwfdm.sara.api;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.project.Project.NoProjectException;
import bwfdm.sara.project.Project.NoSessionException;
import bwfdm.sara.project.Project.ProjectCompletedException;

@ControllerAdvice("bwfdm.sara.api")
public class Error {
	private static final Log logger = LogFactory.getLog(Error.class);

	@ResponseBody
	@ExceptionHandler
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ErrorInfo handleNoSession(final NoSessionException e) {
		return new ErrorInfo(e);
	}

	@ResponseBody
	@ExceptionHandler
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorInfo handleProjectCompleted(final ProjectCompletedException e) {
		return new ProjectCompletedErrorInfo(e, e.itemID);
	}

	@ResponseBody
	@ExceptionHandler
	@ResponseStatus(HttpStatus.FAILED_DEPENDENCY)
	public ErrorInfo handleNoProject(final NoProjectException e) {
		return new ErrorInfo(e);
	}

	@ResponseBody
	@ExceptionHandler
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorInfo handleBlanketException(final Exception e) {
		// report the exception to make debugging easier
		logger.warn("uncaught exception: " + e.getClass().getSimpleName()
				+ ": " + e.getMessage(), e);
		// best-guess error handling: just reload the page. this is what the
		// user will do anyway.
		return new ErrorInfo(e);
	}

	// TODO set up a handler for /error and return something nice for 404s etc
	@JsonInclude(Include.NON_NULL)
	public static class ErrorInfo {
		@JsonProperty
		public final String exception;
		@JsonProperty
		public final String message;

		public ErrorInfo(final Exception e) {
			exception = e.getClass().getSimpleName();
			message = e.getMessage();
		}
	}

	@JsonInclude(Include.NON_NULL)
	public static class ProjectCompletedErrorInfo extends ErrorInfo {
		@JsonProperty
		public final UUID itemID;

		public ProjectCompletedErrorInfo(final Exception e, final UUID itemID) {
			super(e);
			this.itemID = itemID;
 		}
	}
}
