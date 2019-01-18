"use strict";

var meta, cache = {}, branches = [], authors = [];

function initField(id, reset, branchDependent, validator, speshul) {
	var elem = $("#" + id);
	$("#" + reset).click(function() {
		// don't bother asking the server because we already have all the
		// required info...
		elem.val(meta.autodetected[id]);
		validate.check(id);
	});
	validate.init(id, meta.value[id], validator, speshul);
}

function updateBranchSpecific() {
	validate.check("version", true);
}

function removeAuthor(row) {
	// don't allow user to remove the last entry
	if (authors.length == 1)
		return;

	row.root.remove();
	authors.splice(authors.indexOf(row), 1);

	// also disable the button to avoid confusion
	if (authors.length == 1)
		authors[0].remove.addClass("disabled");
}

function addAuthor(info, predecessor) {
	if (info === null)
		info = { surname: "", givenname: "" };

	var row = template("author");
	validate.init(row.surname, info.surname, function(value) {
			if (value.trim() == "")
				return "Please provide the author's surname";
			return true;
		});
	validate.init(row.givenname, info.givenname, function(value) {
			if (value.trim() == "")
				return "Please provide the author's given name";
			return true;
		});
	row.add.click(function() {
		addAuthor(null, row);
	});
	row.remove.click(function() {
		removeAuthor(row);
	});

	if (predecessor) {
		predecessor.root.after(row.root);
		authors.splice(authors.indexOf(predecessor) + 1, 0, row);
	} else {
		$("#authors").append(row.root);
		authors.push(row);
	}

	// enable delete buttons if there is more than a single author field
	if (authors.length > 1)
		row.remove.removeClass("disabled");
	// if there were ≥2 fields before, all buttons are already enabled. but
	// if we're inserting the second field, the existing author field's button
	// is disabled and we need to enable it here
	if (authors.length == 2)
		authors[0].remove.removeClass("disabled");
	return row;
}

function initFields(info) {
	meta = cache[info.value.master] = info;

	validate.init("surname", meta.value.submitter.surname, function(value) {
			if (value.trim() == "")
				return "Please provide your surname";
			return true;
		});
	validate.init("givenname", meta.value.submitter.givenname,
		function(value) {
			if (value.trim() == "")
				return "Please provide your given name";
			return true;
		});
	$("#reset_submitter").click(function() {
		// don't bother asking the server because we already have all the
		// required info...
		$("#surname").val(meta.autodetected.submitter.surname);
		$("#givenname").val(meta.autodetected.submitter.givenname);
		validate.check("surname");
		validate.check("givenname");
	});

	initField("title", "reset_title", false, function(value) {
		if (value.trim() == "")
			return "What title do you want to use for your publication?";
		return true;
	});
	initField("description", "reset_description", false, function(value) {
		if (value.trim() != "")
			return true;
		return null;
	});
	initField("version", "reset_version", true, function(value) {
		if (value.trim() == "")
			return "What version of your software artefact are you publishing?";
		return true;
	});

	// special case: the saved user selection isn't in the list.
	// → just replace with autodetected best guess...
	if (!branches.includes(meta.value.master))
		meta.value.master = meta.autodetected.master;
	initField("master", "reset_master", false, function() { return true; },
		function() {
			var action = $("#master :selected").data("refAction");
			if (!action)
				return;

			var ref = action.ref.path;
			if (!cache[ref]) {
				$("#master_loading").removeClass("hidden");
				API.get("get metadata for " + ref, "/api/meta", { ref: ref },
					function(info) {
						meta = cache[ref] = info;
						$("#master_loading").addClass("hidden");
						updateBranchSpecific();
					});
			} else {
				meta = cache[ref];
				updateBranchSpecific();
			}
		});

	if (meta.value.authors.length > 0)
		$.each(meta.value.authors, function (seq, author) {
			addAuthor(author);
		});
	else
		addAuthor(null); // make sure there always is at least one field

	$("#next_button").click(function() {
		var authorList = [];
		$.each(authors, function(_, author) {
			authorList.push([
				{ name: "surname", value: author.surname },
				{ name: "givenname", value: author.givenname }]);
		});
		// note: these should be in page order because the first field to be
		// invalid will be focused. this becomes very confusing if several are
		// invalid, but it jumps to one somewhere in the middle of the page.
		var values = validate.all([
			{ name: "submitter", value: [ "surname", "givenname" ]},
			'title', 'description', 'master', 'version',
			{ name: "authors", array: authorList }]);
		if (!values)
			return;
		API.put("save fields", "/api/meta", values, function() {
			location.href = "/license.html";
		});
	});
	$("#next_button").removeClass("disabled");

	$("#main").removeClass("hidden");
	$("#main_loading").css("display", "none");
}

function loadLazyBranches(refs) {
	// update list of branches
	var select = $("#master");
	select.empty();
	$.each(refs, function(_, action) {
		var name = action.ref.type + " " + action.ref.name;
		var option = $("<option>").attr("value", action.ref.path)
			.text(name).data("refAction", action);
		select.append(option);
		branches.push(action.ref.path);
	});

	API.get("load field values", "/api/meta", {}, initFields);
}

$(function() {
	API.get("load branches and tags marked for publication",
		"/api/repo/actions", {}, loadLazyBranches);
});
