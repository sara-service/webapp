"use strict";

function addProjectButton(project) {
	var form = template("template");
	form.title.text(project.title);
	form.version.text(project.version);
	form.select.attr("href", URI("/done.html").search({ item: project.item }));
	$("#projects").append(form.root);
}

$(function() {
	API.get("load list of projects", "/api/publish/list", {}, function(list) {
		if (list.length === 0)
			$("#noprojects").removeAttr("style")
		else
			$.each(list, function(_, project) {
				addProjectButton(project);
			});
		$("#loading").remove();	
	});
});
