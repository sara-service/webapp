"use strict";

var repo_uuid;

function initRepoInfo(list) {
	$.each(list, function(_, r) {
		if (r.uuid === repo_uuid) {
			$('#contact_mail').text(r.contact_email);
			$('#contact_mail').attr('href', "mailto:"+r.contact_email+"?subject=SARA%20Service");
			$('#help_text').text(r.help);
			}
		} );
}

function initMeta(info) {
	repo_uuid = info["pubrepo"];

	API.get("retrieve repo info", "/api/pubrepo-list", {}, initRepoInfo );
	
	$('#next_button').attr('href',info["repository_url"]);
	$('#next_button').removeClass("disabled");
	
	$("#loading").remove();
	$("#content").removeClass("hidden");
}

$(function() {
	API.get("load metadata fields", "/api/publish/meta", {}, initMeta);
});
