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
	initCollectionList($("#irs option:first-child").data("id"));
	$("#loading").remove();
}

function setCollectionList(list) {
	console.log(list);
	var select = $("#collections");
	select.empty();
	$.each(list, function(_, coll) {
		var option = $("<option>").attr("value", coll.foreign_collection_uuid)
		.text(coll.display_name).data("id", coll.id);
		select.append(option);
	});
}

function initCollectionList(repo_uuid) {
	API.get("loading collections for selected repositories",
			"api/collection-list", {repo_uuid}, setCollectionList);
}

function validateEmail(email) {
    var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
}

function initPage(session) {
	API.get("loading list of publication repositories",
			"/api/pubrepo-list", {}, initPubRepos);

	$('body').on('input', '#login_email', function() {
		// check if we have a valid email
		var email = $("#login_email").val();
		var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
		if (re.test(email.toLowerCase())) {
			$("#next_button").removeClass("disabled");
		} else {
			$("#next_button").addClass("disabled");
		};
	});
	
	// TODO move elsewhere
	$("#next_button").attr("href", "/meta.html");
}
