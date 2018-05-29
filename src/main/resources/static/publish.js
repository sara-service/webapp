"use strict";

function save(value, id, success) {
	API.putEmpty("update " + id, "/api/meta/" + id, { value: value }, success);
}

function validateEmail(email) {
	if (email == "")
		return false;
	// validating emails is HARD. just delegate it to the browser.
	// if the browser doesn't support type=email, everything will be valid.
	// (which isn't too bad an approximation.)
	return !$("#email").is(":invalid");
}

function setCollectionList(select, collection_path, hierarchy) {
	if (hierarchy.children.length != 0) {
		$.each(hierarchy.children, function(_, child) {
			var cp = collection_path + " \u2192 " + child.name;
			setCollectionList(select, cp, child);
		});
	} else {
		// TODO shouldn't we add this even if there are sub-colelctions?
		var option = $("<option>").attr("value", hierarchy.url)
				.text(collection_path);
		select.append(option);
	}
}

var selectedCollection = null;

function processHierarchy(info) {
	$("#collections_loading").addClass("hidden");

	if (info.hierarchy == null) {
		$("#collections_group").addClass("has-error");
		$("#collections_list").addClass("hidden");
		$("#collections_text").removeClass("hidden");

		if (!info["user-valid"])
			$("#user_status").text("you aren't registered there!");
		else
			$("#user_status").text("you don't have submit rights anywhere!");
		// FIXME add some instructions for the user what to do about that...
		// (in publish.html, though)
	} else {
		$("#collections_text").addClass("hidden");
		$("#collections_list").removeClass("hidden");
		setCollectionList($("#collections"), "", info.hierarchy);
		// re-select the previousoly selected collection
		// TODO check whether this is harmless when not in the list
		$("#collections").val(selectedCollection);
	}
}

function updateCollections() {
	$("#collections").empty(); // this is what blocks the next button!
	$("#collections_group").removeClass("has-error");

	var repo = $("#irs").val();
	var email = $("#email").val();
	if (repo != null && email != "") {
		$("#user_status").text("checking user...");
		$("#collections_loading").removeClass("hidden");
		API.post("update list of collections", "/api/publish/query-hierarchy",
				{ repo_uuid: repo, user_email: email }, processHierarchy);
	} else
		$("#user_status").text("");
}

function initPubRepos(info) {
	var select = $("#irs");
	select.empty();
	$.each(info.repos, function(_, repo) {
		var option = $("<option>").attr("value", repo.uuid)
				.text(repo.display_name).data("repo", repo);
		select.append(option);
	});
	// select the previously selected IR, if any
	if (info.meta.pubrepo.value != null)
		select.val(info.meta.pubrepo.value);

	// event listeners to save on change
	$("#irs").on("select change", function() {
		save($(this).val(), "pubrepo");
		updateCollections();
	});
	var collections = $("#collections");
	collections.on('select change', function () {
		selectedCollection = $(this).val();
		save(selectedCollection, "collection");
	});

	// pre-fill email address and collection
	selectedCollection = info.meta.collection.value;
	if (info.meta.email.value != null) {
		autosave.value("email", info.meta.email.value);
		updateCollections();
	}

	$("#loading").remove();
	$("#content").removeClass("hidden");
}

$(function() {
	autosave.init("email", function(value) {
			save(value, "email", function() {
					autosave.success("email");
				});
			updateCollections();
		}, validateEmail);
	API.get("load list of institutional repositories", "/api/publish", {},
			initPubRepos);

	$("#next_button").click(function() {
		if ($("#irs").val() == null) {
			$("#irs").focus();
			return;
		}

		if (!autosave.validate("email")) {
			console.log("invalid email");
			$("#email").focus(); // let's hope the user notices
			return;
		}

		if ($("#collections").val() == null) {
			if ($("#collections option").length > 0)
				// there are options, but user didn't select any
				$("#collections").focus();
			else
				// not registered or no rights. user must change email to fix.
				$("#email").focus();
			return;
		}

		location.href = "/pubmeta.html";
	});
	$("#next_button").removeClass("disabled");
});
