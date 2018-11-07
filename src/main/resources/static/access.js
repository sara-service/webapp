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
	API.put("set archive access", "/api/archive", { access: access },
		function() {
			location.href = "/overview.html";
		});
}

$(function() {
	API.get("get archive access", "/api/archive", {}, function(archive) {
		$("#" + archive.access).prop("checked", true);
		$("#next_button").click(commit);
		$("#next_button").removeClass("disabled");
		$("#access").removeClass("hidden");
		$("#loading").remove();
	});
});
