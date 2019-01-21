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
	$.each(["title", "description", "version", "pubrepo_displayname", "collection_displayname",
		"email", "submitter"], function(_, name) {
			$("#" + name).text(info[name]);
		});

	$('#pubrepo_displayname').prop('title',info["pubrepo_displayname"]);
	$('#collection_displayname').prop('title',info["collection_displayname"]);
	blockLoaded("meta");
}

$(function() {
	API.get("load metadata fields", "/api/publish/meta", {}, initMeta);
});
