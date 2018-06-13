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
}

function initLicenses(info) {
	$.each(info, function(ref, license) {
		var line = template("template_licenses");
		line.type.text(ref.type);
		line.name.text(ref.name);
		if (license == "keep")
			line.license.text("keep existing LICENSE file");
		else
			line.license.text(license);
		$("#licenses").append(line.root);
	});
}

function initMeta(info) {
	$.each(["submitter", "title", "description", "version"],
		function(_, name) {
			$("#" + name).text(info[name]);
		});
}

function init(info) {
	$("#project").text(info.sourceProject);
	initBranches(info.actions);
	initMeta(info.meta);
	initLicenses(info.licenses);
	$("#loading").remove();
	$("#content").removeClass("hidden");
	$("#next_button").click(function() {
		location.href = new URI("/api/push/trigger").search({
				token: info.token
			});
	});
	$("#next_button").removeClass("disabled");
}

$(function() {
	API.get("initialize page", "/api/push/overview", {}, init);
});
