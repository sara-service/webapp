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
	$("#send_verification_button").click(function() {
		location.href = "/api/publish/verify";
	});
}

function initMeta(info) {
	$.each(["title", "description", "version", "pubrepo_displayname", "collection_displayname",
		"email", "submitter", "pubid"], function(_, name) {
			$("#" + name).text(info[name]);
		});
	// display some info tooltip on mouse hover
	$('#pubrepo_displayname').prop('title',info["pubrepo"]);
	$('#collection_displayname').prop('title',info["collection"]);
	if (info["verify_user"]=="true") {
		$("#verify").removeClass("hidden");
	} else {
		$("#noverify").removeClass("hidden");
	}
	blockLoaded("meta");
}

$(function() {
	API.get("load metadata fields", "/api/publish/meta", {}, initMeta);
});
