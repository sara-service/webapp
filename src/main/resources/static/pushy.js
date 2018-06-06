$(function() {
	API.get("initialize page", "/api/session-info", {}, function(info) {
		$("*[data-old]").text(info.project);
		$("#change_project").click(function() {
			location.replace(new URI("/api/auth/new")
				.search(document.location.search));
		});
		$("#keep_project").click(function() {
			location.replace("/branches.html");
		});
		$("#change_project,#keep_project").removeClass("disabled");
		$("#warning").removeClass("hidden");
		$("#loading").remove();
	});
});
