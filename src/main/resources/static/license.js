"use strict";

function addLicense(license, action) {
	var option = $("<option>").attr("value", license.id);
	option.text(action + " " + license.name);
	option.data("infourl", license.url);
	$("#declare").append(option);
}

function initLicenseList(supported, detected, user) {
	// create the "keep" entry separately. this automatically deals with
	// the situation where it isn't in the list (ie. "other" or a hidden
	// license).
	if (detected != null) {
		$("#declare_keep").text("keep " + detected.name);
		$("#declare_keep").data("infourl", detected.url);
	} else
		// "keep" makes no sense whatsoever if there is nothing to keep
		$("#declare_keep").remove();

	$.each(supported, function(_, info) {
		if (detected == null)
			addLicense(info, "choose");
		else if (detected.id != info.id)
			addLicense(info, "replace with");
	});

	// select whatever value the user selected last time. if there is no
	// "last time", or if the user has selected different licenses per
	// branch, keep the "choose a license" text selected.
	if (user != null)
		$("#declare").val(user);
	else
		$("#declare_choose").prop("selected", true);

	$("#declare").on("select change", updateInfoButton);
	updateInfoButton();
	$("#declare_group").removeAttr("style");
}

function updateInfoButton() {
	var lic = $("#declare :selected");
	var url = lic.data("infourl");
	if (url != null) {
		$("#info").removeClass("disabled");
		$("#info").attr("href", url);
	} else {
		$("#info").addClass("disabled");
		$("#info").removeAttr("href");
	}
}

function saveAndContinue() {
	var lic = $("#declare :selected");
	console.log($("#declare").val(), lic.val(), lic.attr("value"));
	API.post("save license selection", "/api/licenses/all",
		{ license: lic.val() }, function() {
			location.href = "/meta.html";
		});
}

function loadingFinished(nextButton) {
	var next = $("#" + nextButton);
	next.removeAttr("style");
	if (typeof next.attr("href") == "undefined")
		next.click(function() {
			// we don't care what the user actually selected as long as
			// we have a useful license, ie. anything but the "choose a
			// license" placeholder is valid.
			if ($("#declare").val() == null)
				$("#declare").focus(); // will the user notice?
			else
				saveAndContinue();
		});
	$("#license").removeAttr("style");
	$("#loading").remove();
}

function initSingleLicense(info) {
	var detected = info.detected.length > 0 ? info.detected[0] : null;
	initLicenseList(info.supported, detected, info.user);

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
		loadingFinished("confirm_missing");
	else
		loadingFinished("confirm_single");
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
		loadingFinished("confirm_multi");
	else
		loadingFinished("confirm_edit");
}

function initLicense(info) {
	if (info.detected.length > 1) {
		// multiple licenses detected. user needs to clean up that mess
		// on the per-branch licenses page (or, even better, in the git
		// repo).
		initMultiLicense(info.detected, info.missing);
	} else
		initSingleLicense(info);
	$("#declare_loading").remove();
}

function initPage(info) {
	API.get("autodetect licenses", "/api/licenses", {}, initLicense);
}
