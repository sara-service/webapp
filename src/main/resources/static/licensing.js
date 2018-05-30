"use strict";

function addLicense(form, license) {
	var option = $("<option>").attr("value", license.id);
	option.text(license.name);
	option.data("infourl", license.url);
	form.declare.append(option);
}

function initLicenseList(ref, form, supported, detected, user) {
	// create the "keep" entry separately. this automatically deals with
	// the situation where it isn't in the list (ie. "other" or a hidden
	// license).
	if (detected != null) {
		if (detected.id != "other") {
			form.declare_keep.text("keep " + detected.name);
			form.declare_keep.data("infourl", detected.url);
		} else
			form.declare_keep.text("keep existing LICENSE file");
		// if the user selected that license last time, then updated the
		// git repo to match, its entry will now be called "keep" in the
		// selection field.
		// we could just fix that here by setting user = "keep", but
		// then we'd have to write that back somehow. as this is a rare
		// situation, just force the user to pick a license. this will
		// trigger the required writeback, and having an explicit
		// confirmation here is probably not a bad idea anyway since
		// "replace with X" isn't exactly identical to "keep X" for
		// licenses that contain placeholders.
		if (user == detected.id)
			user = null;
	} else
		// "keep" makes no sense whatsoever if there is nothing to keep
		form.declare_keep.remove();

	$.each(supported, function(_, info) {
		if (detected == null)
			addLicense(form, info);
		else if (detected.id != info.id)
			addLicense(form, info);
	});

	form.declare.attr("name", ref);
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
	console.log(fields);
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
