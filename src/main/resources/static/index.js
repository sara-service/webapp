"use strict";

function addRepoButton(repo, project) {
	var form = template("template");
	form.display.text(repo.display_name);
	form.url.text(repo.url);
	if (repo.logo != null)
		form.icon.attr("src", repo.logo);
	form.root.attr("href", new URI("/api/auth/login")
			.search({ repo: repo.id }));
	form.root.insertBefore($("#overflow"));
	return form.root;
}

function initRepos(repos) {
	if (repos.length > 0) {
		var items = [];
		$.each(repos, function(_, repo) {
			items.push(addRepoButton(repo));
		});
		initSearch($('#search'), items, $('#overflow'), 5);

		$("#repos").removeClass("hidden");
		$("#search_block").removeClass("hidden");
		// pre-focus the box so the user just has to start typing
		$('#search').focus();
	} else
		$("#broken").removeClass("hidden")
	$("#loading").remove();
}

$(function() {
	// custom error handling: there really isn't a lot that can go here. if
	// anything does fail, we probably want the user to report that
	// immediately. showing the "retry" dialog in a loop is definitely not
	// something we want to do as the very first step!
	APIERR.handle = function() {
		$("#broken").removeClass("hidden");
		$("#loading").remove();	
	};
	API.get("load list of git repos", "/api/repo-list", {}, initRepos);
});
