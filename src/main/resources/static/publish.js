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
		$("#login_email").setVal("");
		$("#irs").empty();
		var user_email = $("#login_email").val();
		var repo_uuid = $("#irs").children(':selected').data("id");

		API.get("check whether user exists on pub-repo",
			"/api/query-hierarchy", {repo_uuid, user_email}, processHierarchy);
	});
	$("#loading").remove();
}

function setCollectionList(collection_path, hierarchy) {
	// FIXME TODO .... 
	if (hierarchy.children.length != 0) {
		var child;
		$.each(hierarchy.children, function(_, child) {
			var cp = collection_path;
			cp += " -> ";
			cp += child.name;
			setCollectionList(cp, child);
		});
	} else {
		var select = $("#collections");
		var option = $("<option>").attr("value", collection_path).text(collection_path).data("url", hierarchy.url);
		select.append(option);
	}
}

function validateEmail(email) {
    var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
}

function setNextButtonEnabled(enabled) {
	if (enabled) {
		$("#next_button").removeClass("disabled");
	} else {
		$("#next_button").addClass("disabled");
	}
	
	var repoid = $("#irs").children(':selected').data("id");
	var reponame = $("#irs").children(':selected').val();
	var email = $("#login_email").val();
	
	API.get("update selected IR", "/api/set-pubrepo-cfg", { field: 'pubrepo_uuid', value: repoid }, {});
	API.get("update selected IR", "/api/set-pubrepo-cfg", { field: 'pubrepo_displayname', value: reponame }, {});
	API.get("update selected IR", "/api/set-pubrepo-cfg", { field: 'login', value: email }, {});
}

function processHierarchy(hierarchy) {
	$("#collections").empty();
	setNextButtonEnabled(false);
	
	if (hierarchy=="") {
		$("#user_status").css("color","red");
		$("#user_status").text("Bad - this email does not correspond to any registered user!");		
	} else if (hierarchy.children.length == 0) {
		$("#user_status").css("color","red");
		$("#user_status").text("Bad - user is registered, but cannot submit to any collection!");
	} else {
		$("#user_status").text("Good - this email corresponds to a registered user!");
		$("#user_status").css("color","green");
		setNextButtonEnabled(true);
		setCollectionList("", hierarchy);
		$("#collections").on('change', function () {
			var user_email = $("#login_email").val();
			var collection = $("#collections").children(':selected').data("url");
			var collection_name = $("#collections").children(':selected').val();
			API.get("update selected IR", "/api/set-pubrepo-cfg", { field: 'collection_url', value: collection }, {});
			API.get("update selected IR", "/api/set-pubrepo-cfg", { field: 'collection_displayname', value: collection_name }, {});
		});
	}
}

var timerId="";

function initPage(session) {
	API.get("loading list of configured publication repositories",
			"/api/pubrepo-list", {}, initPubRepos);

	$('body').on('input', '#login_email', function() {
		var user_email = $("#login_email").val();
		var repo_uuid = $("#irs").children(':selected').data("id");
		clearTimeout(timerId);
		if (!validateEmail(user_email)) {
			$("#user_status").css("color","red");
			$("#user_status").text("Bad - this is not even a valid email address!");
		} else {
			$("#user_status").css("color","brown");
			$("#user_status").text("Checking ... wait a moment, please!");
			
			timerId = setTimeout(
					function() {
						var user_email = $("#login_email").val();
						var repo_uuid = $("#irs").children(':selected').data("id");

						API.get("check whether user exists on pub-repo",
							"/api/query-hierarchy", {repo_uuid, user_email}, processHierarchy);
						}, 1000 );
		}
	});
	
	$("#next_button").attr("href", "/meta.html");
}
