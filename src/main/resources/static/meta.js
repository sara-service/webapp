"use strict";

function resetTitle(info) {
	autosave.reset("title", info.name);
	autosave.reset("description", info.description);
	$("#reset_title_loading").css("display", "none");
}

function resetVersion(branch) {
	API.get("/api/extract/version", { ref: branch.ref }, function(ex) {
		if (ex.version === null)
			ex.version = "";
		autosave.reset("version", ex.version);
		// change the text on the "update version" button as well
		$("[data-versionfile]").text(ex.path);
		$("#update_version").data("can-update", ex.canUpdate);
		branchChanged();
		// remember which branch the version was extracted from
		saveSourceBranch(branch);

		// remove the loader iff the license extraction is also finished
		var loader = $("#lazy_loading");
		loader.data("version-done", true);
		if (loader.data("license-done"))
			$("#lazy_loading").css("display", "none");
	});
}

function resetLicenses() {
	API.get("/api/extract/licenses", {}, function(list) {
		// TODO make user confirm that the licenses are ok
		var licenses = [];
		$.each(list, function(_, lic) {
			licenses.push(lic.license);
		});
		autosave.reset("license", licenses.join(", "));
		
		// remove the loader iff the version extraction is also finished
		var loader = $("#lazy_loading");
		loader.data("license-done", true);
		if (loader.data("version-done"))
			$("#lazy_loading").css("display", "none");
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
	if (autosave.isValid("version") && branch.type == "BRANCH"
			&& update.data("can-update"))
		autosave.configureUpdateButton("version", function(value, id) {
		API.post("/api/extract/version",
			{ version: value, branch: branch.name },
			function() {
				autosave.reset(id, value);
				saveSourceBranch(branch);
			});
		});
	else
		autosave.configureUpdateButton("version", null);
}

function saveSourceBranch(branch) {
	API.put("/api/meta/sourceBranch", { value: branch.ref });
}

function loadLazyBranches(branches, source) {
	// update list of branches
	var select = $("#lazy_branch");
	select.empty();
	$.each(branches, function(_, branch) {
		var name = reftype_names[branch.type] + branch.name;
		var option = $("<option>").attr("value", branch.ref).text(name)
				.data("branch", branch);
		select.append(option);
		// select that item if it's the one the user used last time
		if (!source.autodetected && source.value == branch.ref)
			select.val(source.value);
	});
	// event handler for "lazy" button
	$("#lazy_button").click(function() {
		var loader = $("#lazy_loading");
		loader.removeAttr("style");
		loader.data("version-done", false);
		loader.data("license-done", false);
		var branch = $("#lazy_branch :selected").data("branch");
		resetVersion(branch);
		resetLicenses();
	});
	$("#lazy_branch").on("select change", branchChanged);
	branchChanged();
}

function initFields(info) {
	function setField(field, value) {
		if (value.autodetected)
			// field value was read from project settings, so we want to
			// write it back
			autosave.reset(field, value.value);
		else
			// field value came from the database; no need to write it
			// back to the database
			autosave.value(field, value.value);
	}

	function setTitleDescription(title, description) {
		setField("title", title);
		setField("description", description);
		$("#title_block").removeAttr("style");
		$("#title_loading").css("display", "none");
	}

	if (info.title.autodetected || info.description.autodetected) {
		// if the values were autodetected last time, the user probably
		// wants them to track changes made in the git repo, so we just
		// autodetect them again here.
		API.get("/api/repo/project-info", {}, function(data) {
			if (info.title.autodetected)
				info.title.value = data.name;
			if (info.description.autodetected)
				info.description.value = data.description;
			setTitleDescription(info.title, info.description);
		});
	} else
		setTitleDescription(info.title, info.description);

	// these cannot be autodetected at first-time startup because no
	// branch has been selected yet
	autosave.value("version", info.version.value);
	autosave.value("license", info.license.value);
	API.get("/api/repo/selected-refs", {}, function(branches) {
		loadLazyBranches(branches, info.sourceBranch);
		$("#version_block").removeAttr("style");
		$("#version_loading").css("display", "none");
	});
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
}
