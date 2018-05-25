"use strict";

function addProjectButton(repo, project) {
	var form = template("template");
	form.display.text(project.name);
	form.internal.text(project.path);
	form.select.attr("href", URI("/api/auth/login").search({
		project: project.path,
		repo: repo
	}));
	$("#projects").append(form.root);
}

$(function() {
	API.get("initialize page", "/api/session-info", {}, function(info) {
		API.get("load list of projects", "/api/project-list", {},
			function(list) {
				if (list.length === 0)
					$("#noprojects").removeAttr("style")
				else
					$.each(list, function(_, project) {
						addProjectButton(info.repo, project);
					});
				$("#loading").remove();	
			});
		});
});
