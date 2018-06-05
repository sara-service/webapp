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

var has_block = { meta: false, publish: false };

function blockLoaded(name) {
	has_block[name] = true;
	if (Object.values(has_block).indexOf(false) >= 0)
		return;

	$("#loading").remove();
	$("#content").removeClass("hidden");
	$("#next_button").click(function() {
		// FIXME ?id=deadbeef
		location.href = "/api/publish/trigger";
	});
	$("#next_button").removeClass("disabled");
}

function initMeta(info) {
	$.each(["title", "description", "version"],
		function(_, name) {
			$("#" + name).text(info[name].value);
		});
	blockLoaded("meta");
}

function initPublish(meta){
	// FIXME replace IR + collection IDs with the more user-friendly names here
	$("#ir_name").text(meta.pubrepo.value);
	$("#coll").text(meta.collection.value);
	$("#login").text(meta.email.value);
	blockLoaded("publish");
}

$(function() {
	API.get("initialize page", "/api/session-info", {}, function(info) {
		$("#project").text(info.project);
	});
	API.get("load metadata fields", "/api/meta", {}, initMeta);
	API.get("get selected IR", "/api/meta", {}, initPublish);
});
