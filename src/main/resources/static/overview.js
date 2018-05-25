"use strict";

var publish = {
	PUBLISH_FULL: {
		sara: "sara-green",
		text: "publish full history of" },
	PUBLISH_ABBREV: {
		sara: "sara-blue",
		text: "publish abbreviated history of" },
	PUBLISH_LATEST: {
		sara: "sara-red",
		text: "publish latest version of" },
	ARCHIVE_PUBLIC: {
		sara: "sara-orange",
		text: "archive with public record" },
	ARCHIVE_HIDDEN: {
		sara: "sara-cyan",
		text: "archive without record" }
};

var has_block = { meta: false, branches: false };

function blockLoaded(name) {
	has_block[name] = true;
	if (Object.values(has_block).indexOf(false) >= 0)
		return;

	$("#loading").remove();
	$("#content").removeClass("hidden");
	$("#next_button").click(function() {
		location.href = "/push.html";
	});
	$("#next_button").removeClass("disabled");
}

function initBranches(info) {
	$.each(info, function(_, action) {
		var line = template(action.firstCommit == "HEAD" ?
			"template_head" : "template_nonhead");
		line.publish.addClass(publish[action.publish].sara);
		line.publish.text(publish[action.publish].text);
		line.type.text(action.ref.type);
		line.name.text(action.ref.name);
		if (action.firstCommit != "HEAD")
			line.start.text(action.firstCommit);
		$("#branches").append(line.root);
	});
	blockLoaded("branches");
}

function initLicenses(info) {
	if (info.multiple) {
		$.each(info.branches, function(_, branch) {
			var line = template("template_licenses");
			line.type.text(branch.ref.type);
			line.name.text(branch.ref.name);
			if (branch.effective == "keep")
				line.license.text("keep " + branch.detected.id);
			else if (branch.detected != null)
				line.license.text("replace LICENSE file with " + branch.effective);
			else
				line.license.text("choose " + branch.effective);
			$("#licenses").append(line.root);
		});
	} else {
		if (info.user == "keep")
			// note: to get here, the user must have selected "keep" for
			// ALL branches, AND this must result in all branches having
			// the same license. IOW: only one license was detected
			$("#license").text("keep " + info.detected[0].id);
		else
			$("#license").text(info.user);
		$("#license").removeClass("hidden");
	}
	blockLoaded("licenses");
}

function initMeta(info) {
	$.each(["title", "description", "version"],
		function(_, name) {
			$("#" + name).text(info[name].value);
		});
	blockLoaded("meta");

}

$(function() {
	API.get("initialize page", "/api/session-info", {}, function(info) {
		$("#project").text(info.project);
	});
	API.get("load metadata fields", "/api/meta", {}, initMeta);
	API.get("load license choice", "/api/licenses", {}, initLicenses);
	API.get("load branch list", "/api/repo/actions", {}, initBranches);

	API.get("get selected IR", "/api/meta", {}, function (meta){
		// FIXME replace IR + collection with the more user-friendly names here
		$("#ir_name").text(meta.pubrepo.value);
		$("#collection_displayname").text(meta.collection.value);
		$("#login").text(meta.email.value);
		blockLoaded("publish");
	});
});
