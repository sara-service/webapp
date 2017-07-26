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
	if (!has_block.meta || !has_block.branches)
		return;

	$("#loading").remove();
	$("#content").removeAttr("style");
	$("#next_button").click(function() {
		location.href = "/push.html";
	});
	$("#next_button").removeClass("disabled");
}

function initBranches(info) {
	console.log(info);
	$.each(info, function(_, action) {
		console.log(action);
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

function initMeta(info) {
	$.each(["title", "description", "version", "license"],
		function(_, name) {
			$("#" + name).text(info[name].value);
		});
	blockLoaded("meta");
}

function initPage(session) {
	$("#project").text(session.project);
	API.get("load metadata fields", "/api/meta", {}, initMeta);
	API.get("load branch list", "/api/repo/actions", {},
			initBranches);
}
