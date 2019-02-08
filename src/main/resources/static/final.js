"use strict";

var repo_uuid;

function initRepoInfo(list) {
	$.each(list, function(_, r) {
		if (r.uuid === repo_uuid) {
			$('#contact_mail').text(r.contact_email);
			$('#contact_mail').attr('href', "mailto:"+r.contact_email+"?subject=SARA%20Service");
			$('#help_text').text(r.help);
			$('#next_button_workspace').text("log in to " + r.display_name + " and complete submission →");
			$('#next_button_workflow').text("visit " + r.display_name + " and browse latest publications →");
			$('#next_button_workflow').attr('href',r.url);
			return false;
		}
	});
}

function initMeta(info) {
	repo_uuid = info["pubrepo"];

	API.get("retrieve repo info", "/api/pubrepo-list", {}, initRepoInfo );
	
	$('#next_button_workspace').attr('href',info["repository_url"]);
	$('#next_button_workspace').removeClass("disabled");
	$('#next_button_workflow').removeClass("disabled");
	
	$("#loading").remove();
	$("#content").removeClass("hidden");
}

$(function() {
	API.get("load metadata fields", "/api/publish/meta", {}, initMeta);
});
