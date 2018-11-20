"use strict";

function addLicense(form, license) {
	var option = $("<option>").attr("value", license.id);
	option.text(license.name);
	option.data("infourl", license.url);
	form.declare.append(option);
}

function initLicenseList(form, supported, keep, user) {
	// create the "keep" entry separately. this automatically deals with
	// the situation where it isn't in the list (ie. "other" or a hidden
	// license).
	if (keep != null) {
		if (keep.id != "other") {
			form.declare_keep.text("keep " + keep.name);
			form.declare_keep.data("infourl", keep.url);
		} else
			form.declare_keep.text("keep existing LICENSE file");
		// the backend uses null to keep the license, but we cannot use
		// that in the dropdown because null corresponds to "no selection"
		// in dropdowns.
		// boundary case: if the user selected that license last time and
		// then updated the git repo to match, its entry will now be called
		// "keep" in the selection field. so, make sure we select "keep"
		// now, because the other entry won't be present any more. "keep X"
		// isn't exactly identical to "replace with X" for licenses that
		// contain placeholders, but the user explicitly put that license
		// into the repo, so that's probably the form of the license he
		// want to use...
		if (user == null || user == keep.id)
			user = "keep";
	} else
		// "keep" makes no sense whatsoever if there is nothing to keep.
		// note that this leaves the selection at null, forcing him to
		// select something, if the user setting was "keep" last time.
		form.declare_keep.remove();

	$.each(supported, function(_, info) {
		// don't create both "replace with X" and "keep X" entries. there
		// *is* a small difference (placeholders), but that distinction is
		// almost impossible to communicate to the user.
		if (keep == null || keep.id != info.id)
			addLicense(form, info);
	});

	validate.init(form.declare, user, function(value) {
		// we don't care what the user actually selected as long as
		// we have a useful license, ie. anything but the "choose a
		// license" placeholder is valid.
		if (value == null)
			return "Please pick a license!";
		return true;
	}, function() {
		updateInfoButton(form);
	});
}

function updateInfoButton(form) {
	var lic = form.declare.find(":selected");
	var url = lic.data("infourl");
	if (url != null) {
		form.info.removeClass("disabled");
		form.info.attr("href", url);
	} else {
		form.info.addClass("disabled");
		form.info.removeAttr("href");
	}
}

function checkLicenses(fields) {
	var data = validate.all(fields);
	if (data)
		saveAndContinue(data);
}

function loadingFinished(nextButton, fields) {
	var next = $("#" + nextButton);
	next.removeClass("hidden");
	if (typeof next.attr("href") == "undefined")
		next.click(function() { checkLicenses(fields); });
	$("#license").removeClass("hidden");
	$("#loading").remove();
}
