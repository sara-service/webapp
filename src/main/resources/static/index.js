"use strict";

function initRepos(list) {
	console.log(list);
	$.each(list, function(_, repo) {
		var link = $("<a>").addClass("btn btn-default btn-array");
		link.text(repo.display_name);
		link.attr("href", new URI("/api/auth/login")
			.addSearch("repo", repo.id));
		$("#buttons").append(link);
	});
	$("#loading").remove();
}

function fail(xhr, status, http) {
	console.log("load failure", status, http, xhr.responseText);
	$("#broken").removeClass("hidden");
	$("#loading").remove();
}

$(function() {
	$.ajax("/api/repo-list",
		{ method: "GET", success: initRepos, error: fail });
});
