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

function initTitle(info) {
	if (info.title === null || info.description === null) {
		API.get("/api/repo/project-info", {}, function(data) {
			var title = info.title !== null ? info.title : data.name;
			var description = info.description !== null
					? info.description : data.description;
			setTitleDescription(title, description);
		});
	} else
		setTitleDescription(info.title, info.description);

	autosave.value("version", info.version);
	autosave.value("license", info.license);
	$("#version_block").removeAttr("style");
	$("#version_loading").css("display", "none");
}

function save(value, id, autoset) {
	API.put("/api/meta/" + id, { value: value, autodetected: autoset },
		function() {
			autosave.success(id);
		});
}

function addUpdateHandler(id, endpoint, field) {
	$("#update_" + id).click(function() {
		autosave.cancelTimeout(id);
		var value = $("#" + id).val();
		var data = {};
		data[field] = value;
		API.post(endpoint, data, function() {
			autosave.reset(id, value);
		});
	});
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
		//var branch = $("#lazy_branch :selected").data("branch");
		//addBranch(branch);
	});
}

function validateNotEmpty(value) {
	return value.trim() != "";
}

function initPage(session) {
	autosave.init("title", save, validateNotEmpty);
	addUpdateHandler("title", "/api/repo/project-info", "name");
	autosave.init("description", save);
	addUpdateHandler("description", "/api/repo/project-info",
			"description");
	autosave.init("version", save, validateNotEmpty);
	addUpdateHandler("version", "/api/repo/version", "version");
	autosave.init("license", save, validateNotEmpty);

	API.get("/api/meta", {}, initTitle);
	$("#reset_title").click(function() {
		$("#reset_title_loading").removeAttr("style");
		API.get("/api/repo/project-info", {}, resetTitle);
	});

	API.get("/api/repo/refs", {}, loadLazyBranches);
}
