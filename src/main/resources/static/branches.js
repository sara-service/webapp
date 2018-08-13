"use strict";

var forms = {};

function addCommits(branch, select, commits) {
	$.each(commits, function(_, commit) {
		var shortid = commit.id.substr(0, 7);
		var title = shortid + " and before: " + commit.title + " ("
			+ commit.date + ")";
		var option = $("<option>").attr("value", commit.id).text(title);
		select.append(option);
	});
	// get rid of the placeholder
	$("option:disabled", select).remove();
	// select previously selected option
	if (branch.action)
		select.val(branch.action.firstCommit);
}

function addBranch(branch) {
	if (forms[branch.ref.path]) {
		// user trying to add a branch twice
		forms[branch.ref.path].action.focus();
		console.log("StupidUserException: ref " + branch.ref.path
			+ " already in list");
		return;
	}

	var form = template("template");
	form.branch = branch;
	form.branch_label.text(branch.ref.type + " " + branch.ref.name);
	// default to publishing everything the user adds, because that's
	// what we prefer.
	if (!branch.action)
		branch.action = { publish: "FULL", firstCommit: "HEAD" };
	// remove the name attributes, forcing validate.all to use the unique
	// element ID as a key for the dropdown's value.
	form.action.removeAttr("name");
	form.commit.removeAttr("name");
	validate.init(form.action, branch.action.publish, function(value) {
			if (value == null)
				return "What do you want to do with this branch?";
			return true;
		});
	validate.init(form.commit, "HEAD", function(value) {
			if (value == null)
				return "Rewind this branch a few commits or archive HEAD?";
			return true;
		});
	// real firstCommit restored when list of commits has been loaded. else
	// it just stays at HEAD, which is a good default.

	// event handler stuff
	form.remove.click(function() {
		branch.action.publish = null;
		delete forms[branch.ref.path];
		form.root.remove();
	});
	forms[branch.ref.path] = form;
	$("#branches").append(form.root);

	// in the background, load list of commits and update starting point
	// selection box
	API.get("load list of commits in " + branch.ref.type + " "
		+ branch.ref.name, "/api/repo/commits", {
			ref: branch.ref.path,
			limit: 20,
		}, function(commits) {
			addCommits(branch, form.commit, commits);
		});
}

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
	});

	// enable the "next" button only after branches have been loaded.
	// this prevents the user from clicking it before we have a valid
	// branch selection.
	$("#next_button").click(function() {
		var fields = [];
		$.each(forms, function(ref, form) {
			fields.push(form.action, form.commit);
		});
		var values = validate.all(fields);

		var data = [];
		$.each(forms, function(ref, form) {
			data.push({
				ref: ref,
				publish: values[form.action.attr("id")],
				firstCommit: values[form.commit.attr("id")]
			});
		});

		if (data.length == 0) {
			// this is actually quite hard: the default is only empty if there
			// are no branches in the repo (empty repo? maybe not even there).
			// ie. the user really has to remove everything and then expect
			// "next" to do something...
			console.log("StupidUserException: no refs in the list");
			$("#add_branch").focus(); // highlighting the button is too subtle
		} else
			API.put("save branch selection", "/api/repo/actions", data,
				function() {
					location.href = "/api/clone/trigger";
				});
	});
	$("#next_button").removeClass("disabled");
	$("#loading").remove();
}

$(function() {
	API.get("load list of tags and branches", "/api/repo/refs", {},
		addBranches);
});
