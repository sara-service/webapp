package bwfdm.sara.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import bwfdm.sara.project.ErrorInfo;
import bwfdm.sara.project.Project.NoProjectException;
import bwfdm.sara.project.Project.NoSessionException;

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

}
