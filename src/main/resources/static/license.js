"use strict";

function saveAndContinue() {
	var lic = $("#declare :selected");
	API.post("save license selection", "/api/licenses/all",
		{ license: lic.val() }, function() {
			location.href = "/meta.html";
		});
}

function initSingleLicense(info) {
	// this is silly here, but allows for easy code sharing with the 
	// multiple-licenses page...
	var form = {};
	$.each(["declare", "declare_keep", "declare_choose", "info"],
		function(_, id) {
			form[id] = $("#" + id);
		});

	var detected = info.detected.length > 0 ? info.detected[0] : null;
	initLicenseList(form, info.supported, detected, info.user);
	$("#declare_group").removeAttr("style");

	if (detected != null) {
		// show label naming the detected license
		$("#single_existing").removeAttr("style");
		$("#single_detected").text(detected.name);
		if (detected.url != null)
			$("#single_detected").attr("href", detected.url);
	} else
		// show "missing license" label
		$("#none_existing").removeAttr("style");

	// enable the right button
	if (info.missing)
		loadingFinished("confirm_missing", [form]);
	else
		loadingFinished("confirm_single", [form]);
}

function initMultiLicense(licenses, missing) {
	$("#multi_existing").removeAttr("style");
	// list all the various licenses
	$.each(licenses, function(index, lic) {
		if (index > 0)
			$("#multi_detected").append(", ");
		var link;
		if (lic.url !== null) {
			link = $("<a>").attr("href", lic.url);
			link.attr("target", "_blank");
		} else
			link = $("<b>");
		$("#multi_detected").append(link.text(lic.name));
	});
	// only allow user to continue if all branches have licenses.
	// (both buttons just go to the "per-license branch" page; the only
	// difference is the label. neither actually commits anything to the
	// server.)
	if (!missing)
		loadingFinished("confirm_multi", []);
	else
		loadingFinished("confirm_edit", []);
}

function initLicense(info) {
	if (info.detected.length > 1) {
		// multiple licenses detected. user needs to clean up that mess
		// on the per-branch licenses page (or, even better, in the git
		// repo).
		initMultiLicense(info.detected, info.missing);
	} else
		initSingleLicense(info);
}

function initPage(info) {
	API.get("autodetect licenses", "/api/licenses", {}, initLicense);
}
