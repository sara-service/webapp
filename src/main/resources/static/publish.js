"use strict";

function initRepos(list) {
	console.log(list);
	var select = $("#irs");
	select.empty();
	$.each(list, function(_, repo) {
		var option = $("<option>").attr("value", repo.display_name)
		.text(repo.display_name).data(repo.uuid, "coffee management");
		select.append(option);
	});
	$("#loading").remove();
}

function initPage(session) {
	API.get("load list of publication repositories",
		"/api/pubrepo-list", {}, initRepos);
}


/*
$(function() {
	$.ajax("/api/pubrepo-list",
		{ method: "GET", success: initRepos, error: fail });
});
*/

function addBranches(branches) {
	// update list of branches
	var select = $("#add_branch");
	select.empty();
	$.each(branches, function(_, branch) {
		var name = branch.ref.type + " " + branch.ref.name;
		var option = $("<option>").attr("value", branch.ref.path)
			.text(name).data("branch", branch);
		select.append(option);
	});
	// add all branches which have an action set
	$.each(branches, function(_, branch) {
		if (branch.action)
			addBranch(branch);
	});
	// event handler for "add" button
	$("#add_button").click(function() {
		var branch = $("#add_branch :selected").data("branch");
		addBranch(branch);
		// save immediately because the user might not change the
		// selection before clicking next
		save(branch);
	});
	// enable the "next" button only after branches have been loaded.
	// this prevents the user from clicking it before we have a valid
	// branch selection.
	$("#next_button").attr("href", "/clone.html");
	$("#next_button").removeClass("disabled");
	$("#loading").remove();	
}
