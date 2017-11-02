"use strict";

function addLicense(license, existing) {
	var option = $("<option>").attr("value", license.id);
	if (existing != null)
		option.text("replace with " + license.name);
	else
		option.text(license.name);
	console.log(license);
	option.data("infourl", license.url);
	console.log(option.data("infourl"));
	$("#declare").append(option);
}

function loadLicenses(existing) {
	$("#declare_group").removeAttr("style");
	API.get("load supported licenses", "licenses.json", {},
		function(list) {
			$.each(list, function(_, info) {
				if (existing == null || info.id != existing.id)
					addLicense(info, existing);
			});
			$("#declare_loading").remove();
			updateInfoButton();
		});
	$("#declare").on("select change", updateInfoButton);
}

function updateInfoButton() {
	var url = $("#declare :selected").data("infourl");
	if (url != null) {
		$("#info").removeClass("disabled");
		$("#info").attr("href", url);
	} else {
		$("#info").addClass("disabled");
		$("#info").removeAttr("href");
	}
	// TODO save to backend!
}

function loadinigFinished() {
	$("#license").removeAttr("style");
	$("#loading").remove();
}

function initNoLicense() {
	$("#none_existing").removeAttr("style");
	// allow user to choose a license
	loadLicenses(null);
	$("#confirm_missing").removeAttr("style");
	// "keep" makes no sense whatsoever; there is nothing to keep
	$("#declare_keep").remove();

	loadinigFinished();
}

function initSingleLicense(license, missing) {
	$("#single_existing").removeAttr("style");
	// show declared license in a few places
	$("#declare_keep").text("keep " + license.name);
	$("#declare_keep").data("infourl", license.url);
	$("#single_detected").text(license.name);
	if (license.url != null)
		$("#single_detected").attr("href", license.url);
	updateInfoButton();
	// remove the stupid "choose" text. the default is simply to keep
	// whatever license is there.
	$("#declare_choose").remove();
	// allow user to choose a license
	loadLicenses(license);
	// enable the right button
	if (missing)
		$("#confirm_missing").removeAttr("style");
	else
		$("#confirm_single").removeAttr("style");

	loadinigFinished();
}

function initMultiLicense(licenses, missing) {
	$("#multi_existing").removeAttr("style");
	// list all the various licenses
	$.each(licenses, function(index, lic) {
		if (index > 0)
			$("#multi_detected").append(", ");
		var link;
		if (lic.url !== null)
			link = $("<a>").attr("href", lic.url);
		else
			link = $("<b>");
		$("#multi_detected").append(link.text(lic.name));
	});
	// only allow user to continue if all branches have licenses.
	// never allow the user to choose a (single) replacement license.
	if (!missing)
		$("#confirm_multi").removeAttr("style");
	else
		$("#confirm_edit").removeAttr("style");

	loadinigFinished();
}

function initPage(info) {
	// TODO
	//initNoLicense();
	initSingleLicense({"id":"GPL-3.0","name":"GNU General Public License v3.0","url":"https://choosealicense.com/licenses/gpl-3.0/"}, false);
	//initMultiLicense([{"id":"GPL-3.0","name":"GNU General Public License v3.0","url":"https://choosealicense.com/licenses/gpl-3.0/"}, {"id":"LGPL-3.0","name":"GNU Lesser General Public License v3.0","url":null}], false);
}

$(initPage);
