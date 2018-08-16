"use strict";

function commit() {
	var access = "invalid";
	if ($("#private").prop("checked"))
		access = "PRIVATE";
	else if ($("#public").prop("checked"))
		access = "PUBLIC";
	else {
		$("#public").prop("checked", true);
		$("#public").focus();
		throw "Invalid access selection!";
	}
	var record = $("#record").prop("checked");
	API.post("save access permissions", "/api/push/commit",
		{ access: access, record: record }, function() {
			location.href = "/api/push/redirect";
		});
}

$(function() {
	$("#next_button").click(commit);
	$("#next_button").removeClass("disabled");
});
