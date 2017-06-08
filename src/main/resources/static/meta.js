function resetTitle(info) {
	autosave.reset("title", info.name);
	autosave.reset("description", info.description);
	$("#reset_title_loading").css("display", "none");
}

function initTitle(info) {
	autosave.value("title", info.title);
	autosave.value("description", info.description);
	$("#title_block").removeAttr("style");
	$("#title_loading").css("display", "none");
}

function save(value, id) {
	if (value != null)
		API.put("/api/meta/" + id, { value: value }, function() {
			autosave.success(id);
		});
	else
		API.delete("/api/meta/" + id, function() {
			autosave.success(id);
		});
}

function addUpdateHandler(id, field) {
	$("#update_" + id).click(function() {
		autosave.cancelTimeout("title");
		var value = $("#" + id).val();
		var data = {};
		data[field] = value;
		API.post("/api/repo/project-info", data, function() {
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
	// event handler for "add" button
	$("#lazy_button").click(function() {
		//var branch = $("#lazy_branch :selected").data("branch");
		//addBranch(branch);
	});
}

function initPage(session) {
	autosave.init("title", save, function(value, id) {
		return value.trim() != "";
	});
	autosave.init("description", save);
	addUpdateHandler("title", "name");
	addUpdateHandler("description", "description");

	API.get("/api/meta", {}, initTitle);
	$("#reset_title").click(function() {
		$("#reset_title_loading").removeAttr("style");
		API.get("/api/repo/project-info", {}, resetTitle);
	});

	API.get("/api/repo/refs", {}, loadLazyBranches);
}
