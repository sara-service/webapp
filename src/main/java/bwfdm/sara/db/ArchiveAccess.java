package bwfdm.sara.db;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ArchiveAccess {
	@JsonProperty("public")
	PUBLIC, //
	@JsonProperty("private")
	PRIVATE
}