"use strict";

function addProjectButton(repo, project) {
	var form = template("template");
	form.display.text(project.name);
	form.internal.text(project.path);
	form.description.text(project.description);
	form.root.attr("href", new URI("/api/auth/login")
		.search({ project: project.path, repo: repo }));
	form.root.insertBefore($("#overflow"));
	return form.root;
}

function initProjects(info) {
	if (info.projects.length > 0) {
		var items = [];
		$.each(info.projects, function(_, project) {
			items.push(addProjectButton(info.repo, project));
		});
		initSearch($('#search'), items, $('#overflow'));

		$("#projects").removeClass("hidden");
		$("#search_block").removeClass("hidden");
		// pre-focus the box so the user just has to start typing
		$('#search').focus();
	} else
		$("#noprojects").removeClass("hidden");
	$("#loading").remove();
}

$(function() {
	API.get("load list of projects", "/api/project-list", {}, initProjects);
});
