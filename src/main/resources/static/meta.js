"use strict";

function setField(id, info) {
	autosave.value(id, info[id].value);
}

function resetTitle(info) {
	setField("title", info);
	setField("description", info);
	$("#reset_title_loading").css("display", "none");
}

function resetVersion(info) {
	setField("version", info);
	branchChanged();

	$("#version_loading").css("display", "none");
}

function branchChanged() {
	var branch = $("#version_branch :selected");
	// update button label
	$("[data-branch]").text(branch.text());

	var action = branch.data("ref");
	// only allow updating branches that haven't been pushed back. tags
	// cannot be written to, and trying to update branches a few commits
	// back is impossible to do consistently.
	if (autosave.isValid("version") && action.ref.type == "branch"
			&& action.firstCommit == "HEAD")
		autosave.configureUpdateButton("version", function(value, id) {
			API.post("update VERSION file", "/api/meta/version",
				{ value: value, branch: action.ref.name },
				function(info) {
					setField(id, info);
				});
			});
	else
		autosave.configureUpdateButton("version", null);
}

function save(value, id) {
	API.put("save field " + id, "/api/meta/" + id, { value: value },
		function() {
			autosave.success(id);
		});
}

function loadLazyBranches(refs, info) {
	// update list of branches
	var select = $("#version_branch");
	select.empty();
	$.each(refs, function(_, action) {
		var name = action.ref.type + " " + action.ref.name;
		var option = $("<option>").attr("value", action.ref.path)
			.text(name).data("ref", action);
		select.append(option);
		// select that item if it's the one last used for version
		// detection. this will be the one the user picked last time if
		// he did pick something, or just the autodetected one if not.
		if (action.ref.path == info.versionbranch)
			select.val(info.versionbranch);
	});
	$("#version_branch").on("select change", branchChanged);
	branchChanged();

	$("#reset_version").prop("disabled", false);
}

function initFields(info) {
	setField("title", info);
	setField("description", info);
	setField("version", info);
	$("#main").removeAttr("style");
	$("#main_loading").css("display", "none");

	API.get("load branches and tags marked for publication",
		"/api/repo/actions", {}, function(refs) {
			loadLazyBranches(refs, info);
		});
}

function updateMeta(value, id) {
	API.post("update project " + id, "/api/meta/" + id,
		{ value: value }, function(info) {
			setField(id, info);
		});
}

function validateNotEmpty(value) {
	return value.trim() != "";
}

function initPage(session) {
	autosave.init("title", save, validateNotEmpty);
	autosave.configureUpdateButton("title", updateMeta);
	autosave.init("description", save);
	autosave.configureUpdateButton("description", updateMeta);
	$("#reset_title").click(function() {
		$("#reset_title_loading").removeAttr("style");
		API.post("reset to project name and description",
			"/api/meta/project-info/reset", {}, resetTitle);
	});

	autosave.init("version", save, validateNotEmpty);
	$("#reset_version").click(function() {
		$("#reset_version_loading").removeAttr("style");
		var action = $("#version_branch :selected").data("ref");
		API.post("reset version number", "/api/meta/version/reset",
			{ ref: action.ref.path }, resetVersion);
	});

	API.get("load field values", "/api/meta", {}, initFields);

	$("#next_button").click(function() {
		var valid = true;
		// check required fields. note description isn't required!
		$.each(["title", "version"], function(_, id) {
			if (!autosave.validate(id)) {
				valid = false;
				$("#" + id).focus(); // let's hope the user notices
				return false;
			}
		});
		if (valid)
			// FIXME will become contributors-then-overview
			location.href = "/overview.html";
	});
}
