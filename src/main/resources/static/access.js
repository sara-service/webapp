"use strict";

function commit() {
	var public_access = "invalid";
	if ($("#private").prop("checked"))
		public_access = false;
	else if ($("#public").prop("checked"))
		public_access = true;
	else {
		$("#public").prop("checked", true);
		$("#public").focus();
		throw "Invalid access selection!";
	}
	var record = $("#record").prop("checked");
	API.post("save access permissions", "/api/push/commit",
		{ public_access: public_access, record: record }, function() {
			location.replace("/api/push/redirect");
		});
}

$(function() {
	$("#next_button").click(commit);
	$("#next_button").removeClass("disabled");
});
