"use strict";

var has_block = { meta: false };

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
	// FIXME replace IR + collection IDs with the more user-friendly names here
	$.each(["title", "description", "version", "pubrepo", "collection",
		"email"], function(_, name) {
			$("#" + name).text(info[name]);
		});
	blockLoaded("meta");
}

$(function() {
	API.get("initialize page", "/api/session-info", {}, function(info) {
		$("#project").text(info.project);
	});
	API.get("load metadata fields", "/api/publish/meta", {}, initMeta);
});
