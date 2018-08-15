"use strict";

function commit() {
	var access = "invalid";
	if ($("#private").prop("checked"))
		access = "private";
	else if ($("#public").prop("checked"))
		access = "public";
	else {
		$("#public").prop("checked", true);
		$("#public").focus();
		throw "Invalid access selection!";
	}
	var record = $("#record").prop("checked");
	API.post("save access permissions", "/api/push/commit",
		{ access: access, record: record },
		function() {
			location.href = "/api/push/redirect";
			if (access == "private")
				location.href = "/token.html";
			else if (record)
				location.href = "/publish.html";
			else
				location.href = "/done.html";
		});
}

$(function() {
	$("#next_button").click(commit);
	$("#next_button").removeClass("disabled");
});
