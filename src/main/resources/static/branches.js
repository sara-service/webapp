function template(name)  {
	// clone whole subtree and change ID to be unique
	var root = $("#" + name).clone();
	root.removeAttr("style");
	template.index++;
	root.attr("id", "form_" + template.index);
	var form = { root: root };
	// for all .form-control's and named labels, change their ID to be
	// unique, but also collect them in the object that gets returned.
	var elements = $(".form-control, label[name]", root);
	elements.each(function() {
		var id = $(this).attr("name");
		var name = id + "_" + template.index;
		$(this).attr("id", name).siblings("label").attr("for", name);
		form[id] = $(this);
	});
	return form;
}
template.index = 0;

var project = new URI(location).search(true).project;
var forms = [];

function save(branch) {
	$.ajax("/api/refs", {
		method: "POST",
		data: {
			project: project,
			ref: branch.ref,
			action: branch.action
		},
		//contentType: "application/json",
		success: function() {
			console.log("save ok");
		},
		error: function(xhr, status, error) {
			console.log("save", status);
		}
	});
}

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
	form.branch_label.text(branch.type + " " + branch.name);
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
	forms[branch.ref] = form;
	$("#branches").append(form.root);
}

function addBranches(branches) {
	// update list of branches
	var select = $("#add_branch");
	select.empty();
	$.each(branches, function(_, branch) {
		var name = branch.type + " " + branch.name;
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
	$.ajax("/api/refs", {
		dataType: "json",
		data: { "project": project },
		success: function(branches) {
			addBranches(branches);
		},
		error: function(xhr, status, error) {
			alert(status);
		}
	});
});
