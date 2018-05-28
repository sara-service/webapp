"use strict";

var meta, cache = {};

function canWriteBack() {
	var action = $("#master :selected").data("refAction");
	// only allow updating branches that haven't been pushed back. tags
	// cannot be written to, and trying to update branches a few commits
	// back is impossible to do consistently.
	return action.ref.type == "branch" && action.firstCommit == "HEAD";
};

function initField(id, branchDependent, validator, speshul) {
	var elem = $("#" + id);
	$("#reset_" + id).click(function() {
		// don't bother asking the server because we already have all the
		// required info...
		elem.val(meta[id].autodetected);
		validate.check(id);
	});
	elem.val(meta[id].value);

	validate.init(id, meta[id].value, validator, function(value, valid) {
		var update = $("#update_" + id);
		var label = $("#update_" + id).parent("label");
		var changed = value != meta[id].autodetected;
		var allowWriteBack = branchDependent ? canWriteBack() : true;

		if (valid && changed && allowWriteBack) {
			update.prop("disabled", false);
			label.removeClass("text-muted");
		} else {
			update.prop("disabled", true);
			update.prop("checked", false);
			label.addClass("text-muted");
		}

		if (speshul)
			speshul();
		var reset = $("#reset_" + id);
		reset.prop("disabled", !changed);
	});
}

function updateBranchSpecific() {
	validate.check("version", true);
}

function initFields(info) {
	meta = cache[info.master.value] = info;

	initField("title", false, function(value) {
		if (value.trim() == "")
			return "What title do you want to use for your publication?";
		return true;
	});
	initField("description", false, function(value) {
		if (value.trim() != "")
			return true;
		return null;
	});
	initField("version", true, function(value) {
		if (value.trim() == "")
			return "What version of your software artefact are you publishing?";
		return true;
	});

	initField("master", false, function() { return true; }, function() {
			var action = $("#master :selected").data("refAction");
			$("[data-master]").text(action.ref.type + " " + action.ref.name);

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
		var data = validate.all("title", "description", "master", "version");
		if (data)
			API.put("save fields", "/api/meta", data, function() {
				location.href = "/overview.html";
			});
	});

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
	});

	API.get("load field values", "/api/meta", {}, initFields);
}

$(function() {
	API.get("load branches and tags marked for publication",
		"/api/repo/actions", {}, loadLazyBranches);
});
