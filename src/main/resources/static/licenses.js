"use strict";

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

function initBranch(form, ref, file) {
	var edit;
	if (file != null) {
		form.file.text(file);
		edit = form.edit_branch;
	} else {
		form.file.replaceWith("&#8213;");
		edit = form.create_branch;
		file = "LICENSE";
	}

	form.type.text(ref.type);
	form.name.text(ref.name);
	if (ref.type == 'branch') {
		// FIXME use the url library
		edit.attr("href", new URI("/api/repo/edit-file")
			.addSearch("branch", ref.name)
			.addSearch("path", file));
		edit.removeAttr("style");
	} else
		form.edit_tag.removeAttr("style");

}

function initDetected(form, detected) {
	var text;
	if (detected != null) {
		if (detected.url != null) {
			text = form.detected;
			text.attr("href", detected.url);
		} else
			text = form.noinfo;
		text.text(detected.name);
		text.removeAttr("style");
	} else
		text = form.missing;
	text.removeAttr("style");
}

function initLicense(info) {
	$.each(info.branches, function(_, branch) {
		var form = template("template");
		initLicenseList(form, info.supported, branch.detected, branch.user);
		initBranch(form, branch.ref, branch.file);
		initDetected(form, branch.detected);
		$("#branch_table").append(form.root);
	});
	$("#licenses").removeAttr("style");
	$("#loading").remove();

	// FIXME actually save licenses and continue
	$("#confirm").removeAttr("style");
}

function initPage(info) {
	API.get("autodetect licenses", "/api/licenses", {}, initLicense);
}
