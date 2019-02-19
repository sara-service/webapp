"use strict";

var publish = {
	FULL: {
		sara: "sara-green",
		text: "full history of" },
	ABBREV: {
		sara: "sara-blue",
		text: "abbreviated history of" },
	LATEST: {
		sara: "sara-red",
		text: "latest version of" },
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
	$.each(info, function(_, branch) {
		var line = template("template_licenses");
		line.type.text(branch.ref.type);
		line.name.text(branch.ref.name);
		if (branch.user != null)
			line.keep.remove();
		if (branch.effective.id != "other") {
			line.license.text(branch.effective.name);
			if (branch.effective.url)
				line.license.attr("href", branch.effective.url);
			line.other.remove();
		} else {
			line.other.text(branch.effective.name);
			line.license.remove();
		}
		$("#licenses").append(line.root);
	});
}

function initMeta(info) {
	$.each(["title", "description", "version", "master"], function(_, name) {
		$("#" + name).text(info[name]);
	});
	$.each(["surname", "givenname"], function(_, name) {
		$("#submitter_" + name).text(info.submitter[name]);
	});
	$.each(info.authors, function(_, author) {
		var row = template("author");
		row.surname.text(author.surname);
		row.givenname.text(author.givenname);
		$("#authors").append(row.root);
	});
}

function init(info) {
	$("#project").text(info.sourceProject);
	initBranches(info.actions);
	initMeta(info.meta);
	initLicenses(info.licenses);
	$("#" + info.access).removeClass("hidden");
	if (info.archive.license) {
		$("#archive_license").html(info.archive.license);
		$("#archive_license_block").removeClass("hidden");
		validate.init("agree", false, function() {
			if (!$("#agree").prop("checked"))
				return "You have to accept the terms of service to continue";
			return true;
		});
	} else
		validate.init("agree", true, function() { return true; });
	$("#loading").remove();
	$("#content").removeClass("hidden");
	$("#next_button").click(function() {
		if (!validate.all(["agree"]))
			return;
		location.href = new URI("/api/push/trigger").search({
				token: info.token
			});
	});
	$("#next_button").removeClass("disabled");
}

$(function() {
	API.get("initialize page", "/api/push/overview", {}, init);
});
