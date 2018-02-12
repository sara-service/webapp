"use strict";

function saveAndContinue(forms) {
	var lic = $("#declare :selected");
	API.get("check licenses", "/api/licenses", {}, function(info) {
			if (!info.undefined)
				location.href = "/publish.html";
		});
}

function save(ref, license) {
	API.post("save " + ref.type + " " + ref.name, "/api/licenses", {
			ref: ref.path,
			license: license,
		});
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
		edit.attr("href", new URI("/api/repo/edit-file")
			.addSearch("branch", ref.name)
			.addSearch("path", file));
		edit.removeClass("hidden");
	} else
		form.edit_tag.removeClass("hidden");

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
		text.removeClass("hidden");
	} else
		text = form.missing;
	text.removeClass("hidden");
}

function initLicense(info) {
	var forms = [];
	$.each(info.branches, function(_, branch) {
		var form = template("template");
		initLicenseList(form, info.supported, branch.detected,
			branch.user);
		initBranch(form, branch.ref, branch.file);
		initDetected(form, branch.detected);
		$("#branch_table").append(form.root);
		form.declare.on("select change", function() {
			save(branch.ref, $(this).val());
		});
		forms.push(form);
	});

	loadingFinished("confirm", forms);
}

function initPage(info) {
	API.get("autodetect licenses", "/api/licenses", {}, initLicense);
}
