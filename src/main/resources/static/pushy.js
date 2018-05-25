$(function() {
	API.get("initialize page", "/api/session-info", {}, function(info) {
		$("*[data-old]").text(info.project);
		$("#change_project").attr("href",
			new URI("/api/auth/new").search(document.location.search));
	});
});
