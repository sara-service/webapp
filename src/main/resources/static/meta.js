"use strict";

function resetTitle(info) {
	autosave.reset("title", info.name);
	autosave.reset("description", info.description);
	$("#reset_title_loading").css("display", "none");
}

function resetVersion(ref) {
	API.get("autodetect version number", "/api/extract/version",
		{ ref: ref.path }, function(ex) {
			if (ex.version === null)
				ex.version = "";
			autosave.reset("version", ex.version);
			// change the text on the "update version" button as well
			$("[data-versionfile]").text(ex.path);
			$("#update_version").data("can-update", ex.canUpdate);
			branchChanged();
			// remember which branch the version was extracted from
			saveSourceBranch(ref);

			// remove the loader iff the license extraction is also
			// finished
			var loader = $("#lazy_loading");
			loader.data("version-done", true);
			if (loader.data("license-done"))
				$("#lazy_loading").css("display", "none");
		});
}

function resetLicenses() {
	API.get("autodetect licenses", "/api/extract/licenses", {},
		function(list) {
			// TODO make user confirm that the licenses are ok
			var licenses = [];
			$.each(list, function(_, lic) {
				licenses.push(lic.license);
			});
			autosave.reset("license", licenses.join(", "));
			
			// remove the loader iff the version extraction is also
			// finished
			var loader = $("#lazy_loading");
			loader.data("license-done", true);
			if (loader.data("version-done"))
				$("#lazy_loading").css("display", "none");
		});
}

function save(value, id, autoset) {
	API.put("save field " + id, "/api/meta/" + id,
		{ value: value, autodetected: autoset },
		function() {
			autosave.success(id);
		});
}

function branchChanged() {
	var ref = $("#lazy_branch :selected").data("ref");
	$("[data-branch]").text(ref.type + " " + ref.name);

	var update = $("#update_version");
	if (autosave.isValid("version") && ref.type == "branch"
		&& update.data("can-update"))
			autosave.configureUpdateButton("version",
				function(value, id) {
					API.post("update file "
							+ $("[data-versionfile]").text(),
						"/api/extract/version",
						{ version: value, branch: ref.name },
						function() {
							autosave.reset(id, value);
							saveSourceBranch(ref);
						});
					});
	else
		autosave.configureUpdateButton("version", null);
}

function saveSourceBranch(ref) {
	API.put("remember " + ref.type + " used for autodetection",
		"/api/meta/source-ref", { value: ref.path });
}

function loadLazyBranches(refs, source) {
	// update list of branches
	var select = $("#lazy_branch");
	select.empty();
	$.each(refs, function(_, ref) {
		var name = ref.type + " " + ref.name;
		var option = $("<option>").attr("value", ref.path)
			.text(name).data("ref", ref);
		select.append(option);
		// select that item if it's the one the user used last time
		if (!source.autodetected && source.value == ref.path)
			select.val(source.value);
	});
	// event handler for "lazy" button
	$("#lazy_button").click(function() {
		var loader = $("#lazy_loading");
		loader.removeAttr("style");
		loader.data("version-done", false);
		loader.data("license-done", false);
		var ref = $("#lazy_branch :selected").data("ref");
		resetVersion(ref);
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
		API.get("load project name and description",
			"/api/repo/project-info", {}, function(data) {
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
	API.get("load branches and tags marked for publication",
		"/api/repo/selected-refs", {}, function(refs) {
			loadLazyBranches(refs, info['source-ref']);
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
		API.post("update project name", "/api/repo/project-info",
			{ name: value }, function() {
				autosave.reset(id, value);
			});
	});
	autosave.init("description", save);
	autosave.configureUpdateButton("description", function(value, id) {
		API.post("update project description", "/api/repo/project-info",
			{ description: value },
			function() {
				autosave.reset(id, value);
			});
	});
	autosave.init("version", save, validateNotEmpty);
	autosave.init("license", save, validateNotEmpty);
	API.get("load field values", "/api/meta", {}, initFields);

	$("#reset_title").click(function() {
		$("#reset_title_loading").removeAttr("style");
		API.get("load project name and description",
			"/api/repo/project-info", {}, resetTitle);
	});

	$("#next_button").click(function() {
		var valid = true;
		// check required fields. note description isn't required!
		$.each(["title", "version", "license"], function(_, id) {
			if (!autosave.validate(id)) {
				valid = false;
				$("#" + id).focus();
				return false;
			}
		});
		if (valid)
			// FIXME will become contributors then overview
			location.href = "/clone.html";
	});
}
