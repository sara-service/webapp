function initPage(info) {
	$("*[data-old]").text(info.project);
	var query = new URI(location).search(true);
	$("*[data-new]").text(query.project);
	$("#change_project").attr("href",
			new URI("/api/auth/new")
			.addSearch("project", query.project)
			.addSearch("repo", query.repo));
}
