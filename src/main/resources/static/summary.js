"use strict";

var has_block = { meta: false };

function blockLoaded(name) {
	has_block[name] = true;
	if (Object.values(has_block).indexOf(false) >= 0)
		return;

	$("#loading").remove();
	$("#content").removeClass("hidden");
	$("#next_button1").click(function() {
		// FIXME ?id=deadbeef
		location.href = "/api/publish/trigger";
	});
	$("#next_button2").click(function() {
		// FIXME ?id=deadbeef
		location.href = "/api/publish/trigger";
	});
	
	$('#vcode').on('input', function() {
	    API.post("checking verification code", "/api/publish/verify", { vcode: $("#vcode").val().trim() }, setEnabledNextButton);
	});
	
	$("#send_verification_button").on('click', function() {
		API.get("sending verification code", "/api/publish/sendVerification", {}, {});
	});
}

function setEnabledNextButton(enable) {
	if (enable) {
		$("#next_button1").removeClass("disabled");
		$("#next_button2").removeClass("disabled");
	} else {
		$("#next_button1").addClass("disabled");
		$("#next_button2").addClass("disabled");
	}
}

function initMeta(info) {
	$.each(["title", "description", "version", "pubrepo_displayname", "collection_displayname",
		"email", "submitter_givenname", "submitter_surname"], function(_, name) {
			$("#" + name).text(info[name]);
		});
	// display some info tooltip on mouse hover
	$('#pubrepo_displayname').prop('title',info["pubrepo"]);
	$('#collection_displayname').prop('title',info["collection"]);
	if (info["verify_user"]=="true") {
		$("#verify").removeClass("hidden");
		setEnabledNextButton(false);
	} else {
		$("#noverify").removeClass("hidden");
		setEnabledNextButton(true);
	}
	blockLoaded("meta");
}

function initPubID(pubid) {
	$("#pubid").text(pubid);
}

$(function() {
	API.get("load metadata fields", "/api/publish/meta", {}, initMeta);
	API.get("initialize publication ID", "/api/publish/getpubid", {}, initPubID);
});
