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
	@JsonProperty
	public final String step;

	public ErrorInfo(final Exception e) {
		this(e, null);
	}

	public ErrorInfo(final Exception e, final String step) {
		exception = e.getClass().getSimpleName();
		message = e.getMessage();
		this.step = step;
	}
}