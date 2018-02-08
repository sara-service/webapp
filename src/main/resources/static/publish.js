"use strict";

function initRepos(list) {
	console.log(list);
	$.each(list, function(_, repo) {
		var link = $("<a>").addClass("btn btn-default");
		link.text(repo.display_name);
		link.attr("href", new URI("/branches.html")
			.addSearch("repo", repo.id));
		$("#buttons").append(link);
	});
	$("#loading").remove();
}

function initPage(session) {
	API.get("load list of publication repositories",
		"/api/pubrepo-list", {}, initRepos);
}
