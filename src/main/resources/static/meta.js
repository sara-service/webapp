"use strict";

function resetField(id, info) {
	$("#" + id + "_loading").addClass("hidden");
	var elem = $("#" + id);
	elem.val(info[id].value);
	elem.data("auto", info[id].autodetected);
}

function updateAuxButtons(value, valid, id) {
	var update = $("#update_" + id);
	var label = $("#update_" + id).parent("label");
	var changed = value.trim() != $("#" + id).data("auto").trim();
	var canWriteBack = fields[id].canWriteBack ?
		fields[id].canWriteBack(value) : true;

	if (valid && changed && canWriteBack) {
		update.prop("disabled", false);
		label.removeClass("text-muted");
	} else {
		update.prop("disabled", true);
		label.addClass("text-muted");
	}

	var reset = $("#reset_" + id);
	reset.prop("disabled", false);
}

function initField(id, info) {
	$("#reset_" + id).click(function() {
		$("#" + id + "_loading").removeClass("hidden");
		var resetInfo = fields[id].getResetInfo ? fields[id].getResetInfo() :
			{};
		API.post("reset " + id, "/api/meta/" + id + "/reset", resetInfo,
			function(info) {
				resetField(id, info);
				validate.check(id);
			});
	});
	resetField(id, info);

	validate.init(id, info[id].value, fields[id].validator, updateAuxButtons);
}

var fields = { title: {}, description: {}, version: {} };

fields.title.validator = function(value) {
	if (value.trim() == "")
		return "What title do you want to use for your publication?";
	return true;
};
fields.title.canWriteBack = function(value) {
	// FIXME "Name can contain only letters, digits, emojis, '_', '.', dash, space. It must start with letter, digit, emoji or '_'."
	return true;
};

fields.description.validator = function(value) {
	if (value.trim() != "")
		return true;
	return null;
};

fields.version.validator = function(value) {
	if (value.trim() == "")
		return "What version of your software artefact are you publishing?";
	return true;
};
fields.version.canWriteBack = function(value) {
	// no branch selected yet, no updating
	if ($("#version_branch").val() === null)
		return false;

	var action = $("#version_branch :selected").data("refAction");
	// only allow updating branches that haven't been pushed back. tags
	// cannot be written to, and trying to update branches a few commits
	// back is impossible to do consistently.
	return action.ref.type == "branch" && action.firstCommit == "HEAD";
};
fields.version.getResetInfo = function() {
	// no branch selected yet, no resetting
	if ($("#version_branch").val() === null)
		return false;

	var action = $("#version_branch :selected").data("refAction");
	return { ref: action.ref.path };
};

function initFields(info) {
	initField("title", info);
	initField("description", info);
	initField("version", info);

	API.get("load branches and tags marked for publication",
		"/api/repo/actions", {}, function(refs) {
			loadLazyBranches(refs, info);
		});

	$("#main").removeClass("hidden");
	$("#main_loading").css("display", "none");
}

function branchChanged() {
	var action = $("#version_branch :selected").data("refAction");
	$("[data-branch]").text(action.ref.type + " " + action.ref.name);
	validate.check("version", true);
}

function loadLazyBranches(refs, info) {
	// update list of branches
	var select = $("#version_branch");
	select.empty();
	$.each(refs, function(_, action) {
		var name = action.ref.type + " " + action.ref.name;
		var option = $("<option>").attr("value", action.ref.path)
			.text(name).data("refAction", action);
		select.append(option);
		// select that item if it's the one last used for version
		// detection. this will be the one the user picked last time if
		// he did pick something, or just the autodetected one if not.
		if (action.ref.path == info.versionbranch)
			select.val(info.versionbranch);
	});
	$("#version_branch").on("select change", branchChanged);
	branchChanged();
}

$(function() {
	//$("#reset_title").click(function() {
		//$("#title_loading").removeClass("hidden");
		//API.post("reset to project name",
			//"/api/meta/title/reset", {}, resetTitle);
	//});
	//$("#reset_version").click(function() {
		//$("#version_loading").removeClass("hidden");
		//var action = $("#version_branch :selected").data("ref");
		//API.post("reset version number", "/api/meta/version/reset",
			//{ ref: action.ref.path }, resetVersion);
	//});

	API.get("load field values", "/api/meta", {}, initFields);

	$("#next_button").click(function() {
		if (validate.all("title", "description", "version"))
			save();
	});
});
