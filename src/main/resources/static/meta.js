"use strict";

function resetTitle(info) {
	autosave.reset("title", info.name);
	autosave.reset("description", info.description);
	$("#reset_title_loading").css("display", "none");
}

function setTitleDescription(title, description) {
	autosave.value("title", title.value);
	autosave.value("description", description.value);
	$("#title_block").removeAttr("style");
	$("#title_loading").css("display", "none");
}

function resetVersion(branch) {
	API.get("/api/extract/version", { ref: branch.ref }, function(ex) {
		if (ex.version === null)
			ex.version = "";
		autosave.reset("version", ex.version);
		$("[data-versionfile]").text(ex.path);
		$("#update_version").data("can-update", ex.canUpdate)
		branchChanged();
		// TODO remember that branch somewhere
	});
}

function resetLicenses() {
	API.get("/api/extract/licenses", {}, function(list) {
		var licenses = [];
		$.each(list, function(_, lic) {
			licenses.push(lic.license);
		});
		autosave.reset("license", licenses.join(", "));
		branchChanged();
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
		var name = reftype_names[branch.type] + branch.name;
		var option = $("<option>").attr("value", branch.ref).text(name)
				.data("branch", branch);
		select.append(option);
	});
	// event handler for "lazy" button
	$("#lazy_button").click(function() {
		var branch = $("#lazy_branch :selected").data("branch");
		resetVersion(branch);
		resetLicenses();
	});
	$("#lazy_branch").on("select change", branchChanged);
	branchChanged();
}

function initFields(info) {
	if (info.title.autodetected || info.description.autodetected) {
		API.get("/api/repo/project-info", {}, function(data) {
			if (info.title.autodetected)
				info.title.value = data.name;
			if (info.description.autodetected)
				info.description.value = data.description;
			setTitleDescription(info.title, info.description);
		});
	} else
		setTitleDescription(info.title, info.description);

	// these cannot be autodetected at startup because no branch has
	// been selected yet
	autosave.value("version", info.version.value);
	autosave.value("license", info.license.value);
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
	API.get("/api/repo/selected-refs", {}, loadLazyBranches);
}
