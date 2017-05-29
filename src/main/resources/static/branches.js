var project = new URI(location).search(true).project;
var forms = [];

function save(branch) {
	API.postAndForget("/api/repo/refs", {
		project: project,
		ref: branch.ref,
		action: branch.action
	});
}

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
}

var reftype_names = {
	BRANCH: "branch ",
	TAG: "tag ",
};

function addBranch(branch) {
	if (forms[branch.ref]) {
		// user trying to add a branch twice
		forms[branch.ref].action.focus();
		console.log("StupidUserException: ref " + branch.ref
				+ " already in list");
		return;
	}

	var form = template("template");
	form.branch = branch;
	form.branch_label.text(reftype_names[branch.type] + branch.name);
	// default to publishing everything the user adds, because that's
	// what we prefer.
	if (!branch.action)
		branch.action = "PUBLISH_FULL";
	form.action.val(branch.action);

	// event handler stuff
	form.remove.click(function() {
		branch.action = null;
		delete forms[branch.ref];
		form.root.remove();
		save(branch);
	});
	form.action.on("select change", function() {
		branch.action = $(this).val();
		save(branch);
	});
	form.commit.on("select change", function() {
		branch.start = $(this).val();
		save(branch);
	});
	forms[branch.ref] = form;
	$("#branches").append(form.root);

	// in the background, load list of commits and update starting point
	// selection box
	API.get("/api/repo/commits", { 
		project: project,
		ref: branch.ref,
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
		var name = reftype_names[branch.type] + branch.name;
		var option = $("<option>").attr("value", branch.ref).text(name)
				.data("branch", branch);
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
	$("#loading").remove();	
}

$(function() {
	$("title").text(project + " – SARA software publishing");
	API.get("/api/repo/refs", { project: project }, addBranches);
});
