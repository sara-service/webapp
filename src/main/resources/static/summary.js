"use strict";

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
