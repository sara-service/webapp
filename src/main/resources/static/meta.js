"use strict";

var meta, cache = {}, branches = [];

function writeBackProblem() {
	var action = $("#master :selected").data("refAction");
	// only allow updating branches that haven't been pushed back. tags
	// cannot be written to, and trying to update branches a few commits
	// back is impossible to do consistently.
	if (action.ref.type == "tag")
		return "cannot commit to a tag";
	if (action.ref.type != "branch")
		return "can only commit to branches";
	if (action.firstCommit != "HEAD")
		return "not archiving HEAD of " + action.ref.name;
	return null;
};

function initField(id, reset, branchDependent, validator, speshul) {
	var elem = $("#" + id);
	$("#" + reset).click(function() {
		// don't bother asking the server because we already have all the
		// required info...
		elem.val(meta[id].autodetected);
		validate.check(id);
	});
	elem.val(meta[id].value);

	var update = $("#update_" + id);
	validate.init(id, meta[id].value, validator, function(value, valid) {
		var label = $("#update_" + id).parent("label");
		var changed = value != meta[id].autodetected;
		var allow = branchDependent ? writeBackProblem() == null : true;

		if (valid && changed && allow) {
			update.prop("disabled", false);
			label.removeClass("text-muted");
		} else {
			update.prop("disabled", true);
			update.prop("checked", false);
			label.addClass("text-muted");
		}

		if (speshul)
			speshul();
	});
	update.prop("checked", meta[id].update);
}

function updateBranchSpecific() {
	validate.check("version", true);
}

function initFields(info) {
	meta = cache[info.master.value] = info;

	initField("submitter_surname", "reset_submitter", false, function(value) {
		if (value.trim() == "")
			return "Please provide your surname";
		return true;
	});
	initField("submitter_givenname", "reset_submitter", false, function(value) {
		if (value.trim() == "")
			return "Please provide your given name";
		return true;
	});
	initField("title", "reset_title", false, function(value) {
		if (value.trim() == "")
			return "What title do you want to use for your publication?";
		return true;
	});
	initField("description", "reset_description", false, function(value) {
		if (value.trim() != "")
			return true;
		return null;
	});
	initField("version", "reset_version", true, function(value) {
		if (value.trim() == "")
			return "What version of your software artefact are you publishing?";
		return true;
	});

	// special case: the saved user selection isn't in the list.
	// → just replace with autodetected best guess...
	if (!branches.includes(meta.master.value))
		meta.master.value = meta.master.autodetected;
	initField("master", "reset_master", false, function() { return true; }, function() {
			var action = $("#master :selected").data("refAction");
			if (!action)
				return;
			$("[data-master]").text(action.ref.type + " " + action.ref.name);
			var problem = writeBackProblem();
			if (problem) {
				$("[data-noproblem]").css("text-decoration", "line-through");
				$("[data-problem]").text("(" + problem + ")");
			} else {
				$("[data-noproblem]").css("text-decoration", "");
				$("[data-problem]").text("");
			}

			var ref = action.ref.path;
			if (!cache[ref]) {
				$("#master_loading").removeClass("hidden");
				API.get("get metadata for " + ref, "/api/meta", { ref: ref },
					function(info) {
						meta = cache[ref] = info;
						$("#master_loading").addClass("hidden");
						updateBranchSpecific();
					});
			} else {
				meta = cache[ref];
				updateBranchSpecific();
			}
		});

	$("#next_button").click(function() {
		var values = validate.all(["submitter_surname", "submitter_givenname",
			"title", "description", "master", "version", ]);
		if (!values)
			return;
		var data = {};
		$.each(values, function(field, value) {
			data[field] = {
				user: value,
				update: $("#update_" + field).prop("checked")
			};
		});
		API.put("save fields", "/api/meta", data, function() {
			location.href = "/license.html";
		});
	});
	$("#next_button").removeClass("disabled");

	$("#main").removeClass("hidden");
	$("#main_loading").css("display", "none");
}

function loadLazyBranches(refs) {
	// update list of branches
	var select = $("#master");
	select.empty();
	$.each(refs, function(_, action) {
		var name = action.ref.type + " " + action.ref.name;
		var option = $("<option>").attr("value", action.ref.path)
			.text(name).data("refAction", action);
		select.append(option);
		branches.push(action.ref.path);
	});

	API.get("load field values", "/api/meta", {}, initFields);
}

$(function() {
	API.get("load branches and tags marked for publication",
		"/api/repo/actions", {}, loadLazyBranches);
});
