"use strict";

var cache = {}, timer;

// do not call directly, ever. call queryHierarchy() instead!
function doQueryHierarchy(pubrepo, email) {
	cache[pubrepo][email] = "busy"; // abuse of untyped language...
	API.post("update list of collections", "/api/publish/query-hierarchy", {
		repo_uuid : pubrepo,
		user_email : email
	}, function(info) {
		cache[pubrepo][email] = info;
		validate.check("email"); // callback to validateEmail
	});
}

// query-hierarchy is slow and expensive. this method thus does two things:
// 1. it caches query results locally
// 2. it delays all queries by 500ms
// if the result isn't in cache, it returns null, starts the download in the
// background and calls email validation once the query is finished. it is
// expected that email validation then calls queryHierarchy() again, and this
// time the result will be in cache and available immediately.
function queryHierarchy(force) {
	var email = $("#email").val();
	var pubrepo = $("#pubrepo").val();
	if (pubrepo == null)
		return null;
	if (!cache[pubrepo])
		cache[pubrepo] = {};

	if (cache[pubrepo][email] === "busy") // download already in progress?
		return null;
	if (cache[pubrepo][email] != null)
		return cache[pubrepo][email];

	if (timer != null)
		clearTimeout(timer);
	timer = setTimeout(function() {
		doQueryHierarchy(pubrepo, email);
	}, 500);
	return null;
}

function validateEmail(email) {
	// validating emails is HARD. just delegate it to the browser.
	// if the browser doesn't support <input type=email>, everything will be
	// valid, which isn't too bad an approximation of RFC822.
	if (email == "" || $("#email").is(":invalid"))
		return "Please enter a valid email address";

	var userInfo = queryHierarchy();
	if (userInfo == null)
		return null; // no checkmark yet; AJAX will call us back with status
	if (!userInfo["user-valid"])
		return "Your email isn't registered there";
	if (userInfo.hierarchy == null)
		return "You don't have submit rights to any collection";
	return true;
}

function setCollectionList(select, collection_path, hierarchy) {
	if (hierarchy.children.length != 0) {
		$.each(hierarchy.children, function(_, child) {
			var cp;
			if (collection_path.trim().length == 0) {
				cp = child.name;
			} else {
				cp = collection_path + " \u2192 " + child.name;
			}
			setCollectionList(select, cp, child);
		});
	} else
		if (hierarchy.is_collection) {
			select.append($("<option>").attr("value", hierarchy.url)
					.text(collection_path).data("policy", hierarchy.policy));
		} else {
			select.append($("<option>").prop("disabled","true").attr("value", hierarchy.url)
					.text(collection_path));
		};
}

var initialCollection;

function initPolicy() {
	var policyOk = $("#policyOk");
	var acceptedPolicy = policyOk.prop('checked') ? $("#policy").val() : null;
	var policy = $("#collection").find('option:selected').data("policy");
	$("#policy").val(policy);
	$("#policy_group").toggleClass("hidden", policy == null);
	// check the box if
	// - there is no policy (and the box is hidden anyway)
	// - it was checked before and it didn't change
	// uncheck it in all other cases to get explicit user consent
	policyOk.prop('checked', policy == acceptedPolicy || policy == null);
};

function updateCollections(_, valid) {
	// remove status display on the collection field, otherwise it tends to
	// stick around forever.
	validate.feedback("collection", null);

	var collection = $("#collection");
	// if either field is invalid, don't even bother checking
	var pubrepoValid = $("#pubrepo").val() != null;
	if (!valid || !pubrepoValid) {
		$("#collections_list").removeClass("hidden");
		$("#collections_loading").addClass("hidden");
		collection.prop("disabled", true);
		return;
	}

	// query the server. this point is reached for every character typed, but
	// queryHierarchy() works around that by delaying the query until the user
	// has stopped typing.
	var userInfo = queryHierarchy();
	if (userInfo == null) { // still loading; show wait animation
		$("#collections_list").addClass("hidden");
		$("#collections_loading").removeClass("hidden");
		return;
	}
	$("#collections_list").removeClass("hidden");
	$("#collections_loading").addClass("hidden");

	// now update collections list
	if (userInfo.hierarchy != null) {
		collection.prop("disabled", false);
		var selectedCollection = collection.val();
		if (selectedCollection == null)
			selectedCollection = initialCollection;
		collection.empty();
		setCollectionList(collection, "", userInfo.hierarchy);

		// restore selection if one existed
		collection.val(selectedCollection);
		// add placeholder (because placeholder="foo" doesn't work on dropdowns)
		if (selectedCollection == null)
			collection.prepend($("<option>").prop('disabled', true)
				.prop('selected', true).attr("id", "collection_placeholder")
				.text("Please select a collection"));
	} else
		collection.prop("disabled", true);
	
	initPolicy();
}

function initPubRepos(info) {
	var select = $("#pubrepo");
	select.empty();
	$.each(info.repos, function(_, repo) {
		var option = $("<option>").attr("value", repo.uuid).text(
				repo.display_name).data("repo", repo);
		select.append(option);
	});

	validate.init("email", info.meta.email, validateEmail, updateCollections);
	validate.init("pubrepo", info.meta.pubrepo, function(value) {
		if (value == null)
			return "Please select your institutional repository";
		return true;
	}, function(value, valid, elem, disableFeedback) {
		var repo = $("#pubrepo :selected").data("repo");
		var logo = repo ? repo.logo_url : null;
		if (logo && !logo.match(/^(https:\/\/|data:)/))
			// insecure URL will cause a mixed content warning. give the IR
			// operator a very good reason to fix that:
			logo = "/insecure.svg";
		if (logo)
			$("#ir_logo").attr("src", logo);
		else
			$("#ir_logo").removeAttr("src");
		$("#ir_link,#ir_button").attr("href", repo.url);
		$("#user_hint").text(repo.user_hint);
		$("#user_hint_group").toggleClass("hidden", !repo.user_hint);
		// delegate to email validation. the two fields have basically the
		// same role (influencing the collection list), but we want the UI
		// messages below the email to which they refer.
		validate.check("email", disableFeedback);
	});
	
	validate.init("collection", null, function(value) {
		if (value == null || queryHierarchy() == null)
			return "Please select a collection";
		return true;
	}, function(value, valid, elem, disableFeedback) {
		// hide placeholder when something valid is selected
		$("#collection_placeholder").toggleClass("hidden", valid);
		if (valid)
			initPolicy();
	});
	validate.init("policyOk", false, function() {
		if (!$("#policyOk").prop("checked"))
			return "You have to accept the terms of service to continue";
		return true;
	});
	
	initialCollection = info.meta.collection;
	
	$("#next_button").click(function() {
		var values = validate.all([ "pubrepo", "email", "collection", "policyOk",
			{ name: "pubrepo_displayname", value: $("#pubrepo :selected") },
			{ name: "collection_displayname", value: $("#collection :selected") }]);
		if (values == null)
			return;
		// FIXME we should probably send the policy to server for checking!
		delete values["policyOk"];
		API.put("save fields", "/api/publish/meta", values, function() {
			location.href = "/summary.html";
		});
	});
	$("#next_button").removeClass("disabled");

	$("#loading").remove();
	$("#content").removeClass("hidden");
}

$(function() {
	API.get("load list of institutional repositories", "/api/publish", {},
			initPubRepos);
	$("form").submit(function() {
		return false;
	});
});
