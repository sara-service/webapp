"use strict";

function saveAndContinue(data) {
	API.post("save license selection", "/api/licenses",
		{ license: data.all }, function() {
			location.href = "/overview.html";
		});
}

function initLicense(info) {
	if (info.multiple) { // multiple licenses
		$("#multi_existing").removeClass("hidden");
		// list all the various licenses
		$.each(info.detected, function(index, lic) {
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
	} else if (info.primary != null) { // a single license
		if (info.primary.id != "other") {
			// show label naming the detected license
			$("#single_existing").removeClass("hidden");
			$("#single_detected").text(info.primary.name);
			if (info.primary.url != null)
				$("#single_detected").attr("href", info.primary.url);
		} else
			$("#other_existing").removeClass("hidden");
	} else { // no licenses
		// show "missing license" label
		$("#none_existing").removeClass("hidden");
	} 
	
	if (!info.consistent) {
		// multiple effective licenses selected. user needs to clean up
		// that mess on the per-branch licenses page (or, even better,
		// in the git repo).
		// we do however allow the user to override differing detected
		// licenses if he already did so last time.
		if (info.missing)
			loadingFinished("confirm_edit", []);
		else
			loadingFinished("confirm_multi", []);
	} else {
		// this is silly here, but allows for easy code sharing with the 
		// multiple-licenses page...
		var form = {};
		$.each(["declare", "declare_keep", "declare_choose", "info"],
			function(_, id) {
				form[id] = $("#" + id);
			});
		initLicenseList("all", form, info.supported, info.primary,
			info.user);
		$("#declare_group").removeClass("hidden");

		// enable the right button
		if (info.missing)
			loadingFinished("confirm_missing", [form.declare]);
		else
			loadingFinished("confirm_single", [form.declare]);
	}
}

$(function() {
	API.get("autodetect licenses", "/api/licenses", {}, initLicense);
});
