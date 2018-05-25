"use strict";

function addRepoButton(repo) {
	var form = template("template");
	form.display.text(repo.display_name);
	form.select.attr("href", URI("/api/auth/login").search({
		repo: repo.id
	}));
	$("#repos").append(form.root);
}

function initRepos(list) {
	if (list.length === 0)
		$("#broken").removeClass("hidden")
	else
		$.each(list, function(_, repo) {
			addRepoButton(repo);
		});
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
