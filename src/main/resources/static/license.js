"use strict";

function saveAndContinue(data) {
	API.post("save license selection", "/api/licenses",
		{ license: data.all }, function() {
			location.href = "/overview.html";
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
	initLicenseList("all", form, info.supported, detected, info.user);
	$("#declare_group").removeClass("hidden");

	if (detected != null) {
		if (detected.id != "other") {
			// show label naming the detected license
			$("#single_existing").removeClass("hidden");
			$("#single_detected").text(detected.name);
			if (detected.url != null)
				$("#single_detected").attr("href", detected.url);
		} else
			$("#other_existing").removeClass("hidden");
	} else
		// show "missing license" label
		$("#none_existing").removeClass("hidden");

	// enable the right button
	if (info.missing)
		loadingFinished("confirm_missing", [form.declare]);
	else
		loadingFinished("confirm_single", [form.declare]);
}

function initMultiLicense(licenses, missing) {
	$("#multi_existing").removeClass("hidden");
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
	if (info.multiple || info.detected.length > 1)
		// multiple licenses detected, or multiple effective licenses
		// selected. user needs to clean up that mess on the per-branch
		// licenses page (or, even better, in the git repo).
		initMultiLicense(info.detected, info.missing);
	else
		initSingleLicense(info);
}

$(function() {
	API.get("autodetect licenses", "/api/licenses", {}, initLicense);
});
