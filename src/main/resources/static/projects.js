"use strict";

function addProjectButton(repo, project) {
	var form = template("template");
	$("span", form.select).text(project.name);
	$("tt", form.select).text(project.path);
	form.select.attr("href", URI("/api/auth/login").search({
		project: project.path,
		repo: repo
	}));
	$("#projects").append(form.root);
}

function initPage(info) {
	API.get("/api/project-list", {}, function(list) {
		$.each(list, function(_, project) {
			addProjectButton(info.repo, project);
		});
		$("#loading").remove();	
	});
}
