"use strict";

function resetTitle(info) {
	autosave.reset("title", info.name);
	autosave.reset("description", info.description);
	$("#reset_title_loading").css("display", "none");
}

function setTitleDescription(title, description) {
	autosave.value("title", title);
	autosave.value("description", description);
	$("#title_block").removeAttr("style");
	$("#title_loading").css("display", "none");
}

function resetVersion(branch) {
	API.get("/api/extract/version", { ref: branch.ref }, function(ex) {
		if (ex.version === null)
			ex.version = "";
		autosave.value("version", ex.version);
		$("[data-versionfile]").text(ex.path);
		$("#update_version").data("can-update", ex.canUpdate)
		branchChanged();
		// TODO remember that branch somewhere
	});
}

function save(value, id, autoset) {
	API.put("/api/meta/" + id, { value: value, autodetected: autoset },
		function() {
			autosave.success(id);
		});
}

function branchChanged() {
	var branch = $("#lazy_branch :selected").data("branch");
	$("[data-branch]").text(reftype_names[branch.type] + branch.name);

	var update = $("#update_version");
	if (autosave.isValid("version") && branch.type == "BRANCH")
		autosave.configureUpdateButton("version", function(value, id) {
		API.post("/api/extract/version", { version: value },
			function() {
				autosave.reset(id, value);
				// TODO remember that branch somewhere
			});
		});
	else
		autosave.configureUpdateButton("version", null);
}

function loadLazyBranches(branches) {
	// update list of branches
	var select = $("#lazy_branch");
	select.empty();
	$.each(branches, function(_, branch) {
		if (!branch.action)
			// don't allow selection of branches that the user doesn't
			// want to publish
			return;

		var name = reftype_names[branch.type] + branch.name;
		var option = $("<option>").attr("value", branch.ref).text(name)
				.data("branch", branch);
		select.append(option);
	});
	// event handler for "lazy" button
	$("#lazy_button").click(function() {
		var branch = $("#lazy_branch :selected").data("branch");
		resetVersion(branch);
		//resetLicenses(); // TODO
	});
	$("#lazy_branch").on("select change", branchChanged);
	branchChanged();
}

function initFields(info) {
	if (info.title === null || info.description === null) {
		API.get("/api/repo/project-info", {}, function(data) {
			if (info.title === null)
				info.title = data.name;
			if (info.description === null)
				info.description = data.description;
			setTitleDescription(info.title, info.description);
		});
	} else
		setTitleDescription(info.title, info.description);

	// these cannot be autodetected at startup because no branch has
	// been selected yet
	autosave.value("version", info.version);
	autosave.value("license", info.license);
	$("#version_block").removeAttr("style");
	$("#version_loading").css("display", "none");
}

function validateNotEmpty(value) {
	return value.trim() != "";
}

function initPage(session) {
	autosave.init("title", save, validateNotEmpty);
	autosave.configureUpdateButton("title", function(value, id) {
		API.post("/api/repo/project-info", { name: value }, function() {
			autosave.reset(id, value);
		});
	});
	autosave.init("description", save);
	autosave.configureUpdateButton("description", function(value, id) {
		API.post("/api/repo/project-info", { description: value },
			function() {
				autosave.reset(id, value);
			});
	});
	autosave.init("version", save, validateNotEmpty);
	autosave.init("license", save, validateNotEmpty);

	API.get("/api/meta", {}, initFields);
	$("#reset_title").click(function() {
		$("#reset_title_loading").removeAttr("style");
		API.get("/api/repo/project-info", {}, resetTitle);
	});
	$("#update_version").data("can-update", false)
	API.get("/api/repo/refs", {}, loadLazyBranches);
}
