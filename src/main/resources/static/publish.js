"use strict";

function initPubRepos(list) {
	console.log(list);
	var select = $("#irs");
	select.empty();
	$.each(list, function(_, repo) {
		var option = $("<option>").attr("value", repo.display_name)
		.text(repo.display_name).data("id", repo.uuid);
		select.append(option);
	});

	$("#irs").on('change', function () {
		var repo_uuid = $(this).children(':selected').data("id");
		initCollectionList(repo_uuid);
	});

	$("#loading").remove();
}

function setCollectionList(list) {
	console.log(list);
	var select = $("#collections");
	select.empty();
	$.each(list, function(_, coll) {
		var option = $("<option>").attr("value", coll.foreign_collection_uuid)
		.text(coll.display_name  + "("coll.foreign_collection_uuid + ")").data("id", coll.id);
		select.append(option);
	});
	$("#next_button").attr("href", "/meta.html");
	$("#next_button").removeClass("disabled");
}

function initCollectionList(repo_uuid) {
	API.get("loading collections for selected repositories",
			"api/collection-list", {repo_uuid}, setCollectionList);
}

function initPage(session) {
	API.get("loading list of publication repositories",
			"/api/pubrepo-list", {}, initPubRepos);
	// TODO initCollectionList(...)
}