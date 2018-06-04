"use strict";

var cache = {}, timer;

// do not call directly, ever. call queryHierarchy() instead!
function doQueryHierarchy(pubrepo, email) {
	cache[pubrepo][email] = "busy"; // abuse of untyped language...
	API.post("update list of collections", "/api/publish/query-hierarchy",
		{ repo_uuid: pubrepo, user_email: email }, function(info) {
			cache[pubrepo][email] = info;
			validate.check("email"); // callback to validateEmail
		});
}

// query-hierarchy is slow and expensive. this method thus does two things:
//   1. it caches query results locally
//   2. it delays all queries by 1.5s
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
	}, 1500);
	return null;
}

function validateEmail(email) {
	// validating emails is HARD. just delegate it to the browser.
	// if the browser doesn't support <input type=email>, everything will be
	// valid, which isn't too bad an approximation of RFC822.
	if (email == "" || $("#email").is(":invalid"))
		return "Please enter a valid email address!";

	var userInfo = queryHierarchy();
	if (userInfo == null)
		return null; // no checkmark yet; AJAX will call us back with status
	if (!userInfo["user-valid"])
		return "Your email isn't registered there!";
	if (userInfo.hierarchy == null)
		return "You don't have submit rights anywhere!";
	return true;
}

function setCollectionList(select, collection_path, hierarchy) {
	if (hierarchy.children.length != 0) {
		$.each(hierarchy.children, function(_, child) {
			var cp = collection_path + " \u2192 " + child.name;
			setCollectionList(select, cp, child);
		});
	} else {
		// TODO shouldn't we add this even if there are sub-collections?
		var option = $("<option>").attr("value", hierarchy.url)
				.text(collection_path);
		select.append(option);
	}
}

var initialCollection;

function updateCollections(_, valid) {
	// remove status display on the collection field, otherwise it tends to
	// stick around forever.
	validate.feedback("collection", null);

	var collection = $("#collection");
	// if either field is invalid, don't even bother checking
	var pubrepoValid = $("#pubrepo").val() != null;
	if (!valid || !pubrepoValid) {
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
		collection.val(selectedCollection); // restore selection
	} else
		collection.prop("disabled", true);
}

function initPubRepos(info) {
	var select = $("#pubrepo");
	select.empty();
	$.each(info.repos, function(_, repo) {
		var option = $("<option>").attr("value", repo.uuid)
				.text(repo.display_name).data("repo", repo);
		select.append(option);
	});

	validate.init("email", info.meta.email.value, validateEmail,
		updateCollections);
	validate.init("pubrepo", info.meta.pubrepo.value, function(value) {
		if (value == null)
			return "Please select your institutional repository!";
		return true;
	}, function() {
		// delegate to email validation. the two fields have basically the
		// same role (influencing the collection list), but we want the UI
		// messages below the email to which they refer.
		validate.check("email");
	});

	validate.init("collection", null, function(value) {
		if (value == null || queryHierarchy() == null)
			return "Please select a collection (usually, your department)!";
		return true;
	});
	initialCollection = info.meta.collection.value;

	$("#next_button").click(function() {
		var values = validate.all(["pubrepo", "email", "collection"]);
		if (values == null)
			return;
		var data = {};
		$.each(values, function(field, value) {
			data[field] = {
				user: value,
				update: false
			};
		});
		API.put("save fields", "/api/meta", data, function() {
			location.href = "/pubmeta.html";
		});
	});
	$("#next_button").removeClass("disabled");

	$("#loading").remove();
	$("#content").removeClass("hidden");
}

$(function() {
	API.get("load list of institutional repositories", "/api/publish", {},
			initPubRepos);
});
