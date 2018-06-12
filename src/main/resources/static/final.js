"use strict";

function initMeta(info) {
	$('#archive_url').text(info["archive_url"]);
	$('#archive_url').attr('href',info["archive_url"]);
	$('#publish_url').text(info["repository_url"]);
	$('#publish_url').attr('href',info["repository_url"]);
	
	$('#next_button').removeClass("disabled");
	$('#next_button').attr('href','index.html');
	
	$("#loading").remove();
	$("#content").removeClass("hidden");
}

$(function() {
	API.get("load metadata fields", "/api/publish/meta", {}, initMeta);
});
