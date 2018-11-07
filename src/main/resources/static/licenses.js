"use strict";

function saveAndContinue(data) {
	API.put("set licenses", "/api/licenses", data, function(info) {
		location.href = "/access.html";
	});
}

function initBranch(form, ref, file) {
	if (file != null)
		form.file.text(file);
	else
		form.file.replaceWith("&#8213;");

	form.type.text(ref.type);
	form.name.text(ref.name);
}

function initDetected(form, detected) {
	var text;
	if (detected != null) {
		if (detected.id != "other") {
			if (detected.url != null) {
				text = form.detected;
				text.attr("href", detected.url);
			} else
				text = form.noinfo;
			text.text(detected.name);
		} else
			text = form.other;
		text.removeClass("hidden");
	} else
		text = form.missing;
	text.removeClass("hidden");
}

function initLicense(info) {
	var forms = [];
	$.each(info.branches, function(_, branch) {
		var form = template("template");
		initLicenseList(branch.ref.path, form, info.supported, branch.keep,
			branch.user);
		initBranch(form, branch.ref, branch.file);
		initDetected(form, branch.detected);
		$("#branch_table").append(form.root);
		form.declare.on("select change", function() {
			save(branch.ref, $(this).val());
		});
		forms.push(form.declare);
	});

	loadingFinished("confirm", forms);
}

$(function() {
	API.get("autodetect licenses", "/api/licenses", {}, initLicense);
});
