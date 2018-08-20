"use strict";

function addProjectButton(project) {
	var form = template("template");
	form.title.text(project.title);
	form.version.text(project.version);
	form.root.attr("href", URI("/info.html")
		.search({ item: project.item, token: project.token }));
	form.root.insertBefore($("#overflow"));
	return form.root;
}

function initProjects(projects) {
	if (projects.length > 0) {
		var items = [];
		$.each(projects, function(_, project) {
			items.push(addProjectButton(project));
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
	API.get("load list of projects", "/api/publish/list", {}, initProjects);
});
