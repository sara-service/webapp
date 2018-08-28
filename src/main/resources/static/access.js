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
	API.post("complete archiving process", "/api/push/commit",
		{ public_access: public_access, record: false }, function() {
			location.href = "/api/push/redirect";
		});
}

$(function() {
	// this also fires ProjectCompletedException if the item has already been
	// committed
	API.get("check for completion", "/api/push/status", {}, function(status) {
		if (status.status == "success") {
			$("#next_button").click(commit);
			$("#next_button").removeClass("disabled");
			$("#loading").remove();
		} else
			location.href = "/push.html";
	});
});
