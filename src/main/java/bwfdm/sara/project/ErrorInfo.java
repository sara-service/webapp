package bwfdm.sara.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class ErrorInfo {
	@JsonProperty
	public final String exception;
	@JsonProperty
	public final String message;

	public ErrorInfo(final Exception e) {
		exception = e.getClass().getSimpleName();
		message = e.getMessage();
	}
}