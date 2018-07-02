"use strict";

function template(name) {
	// clone whole subtree and change ID to be unique
	var root = $("#" + name).clone();
	root.removeClass("hidden");
	template.index++;
	root.attr("id", "form_" + template.index);
	var form = { root: root };
	// collect all child elements that have a name attribute
	var elements = $("[name]", root);
	elements.each(function() {
		var id = $(this).attr("name");
		form[id] = $(this);
		// rename element to be unique
		var name = id + "_" + template.index;
		$(this).attr("id", name)
		// if it has a label, make sure it's associated with its control
		var label = $(this).siblings("label");
		if (label)
			label.attr("for", name);
	});
	return form;
}
template.index = 0;

var APIERR = {};

APIERR.getmsg = function(step, exception, message) {
	return "Failed to " + step + ": " + exception + ": " + message;
}
APIERR.report = function(step, exception, message) {
	window.alert(APIERR.getmsg(step, exception, message));
};
// handles all unidentified exceptions
APIERR.handleOther = function(step, exception, message) {
	var msg = APIERR.getmsg(step, exception, message)
		+ "\n\nReload the page and hope the error will go away?";
	if (window.confirm(msg))
		// fix the error for the user; it's not like he will do anything
		// else
		location.reload();
};
// handles specific "well-known" exceptions returned by the API
APIERR.handleJSON = function(step, info) {
	if (info.exception == "NoSessionException") {
		window.alert("Session expired!"
			+ " (You didn't leave the tab open overnight, did you?)"
			+ "\n\nYour progress has been saved. Please go through"
			+ " the workflow once again and check that all fields"
			+ " are the way you left them!");
		// user not logged in. redirect to login page.
		location.href = "/";
		return;
	}
	if (info.exception == "NoProjectException") {
		window.alert("Please select a project first!");
		// no project set. redirect to project selection page.
		location.href = "/projects.html";
		return;
	}
	if (info.exception == "NeedCloneException") {
		window.alert("Repository needs to be cloned first!");
		// we need a local repo but don't have one. let's change that.
		// unfortunately this resets the workflow back to right after
		// the clone...
		location.href = "/api/clone/trigger";
		return;
	}
	if (info.exception == "ProjectCompletedException") {
		window.alert("Your software artefact has already been archived!");
		// redirect the user to "done" page, where he can start a publication.
		// safe to just concatenate query string here; the item UUID cannot
		// contain anything dangerous because it's only hex digits and dashes.
		location.href = "/done.html?item=" + info.itemID;
		return;
	}
	APIERR.handleOther(step, info.exception, info.message);
};
APIERR.handle = function(step, status, http, body) {
	console.log("API error: " + step + ": " + status + "/" + http, body);
	if (body) try {
		var info = JSON.parse(body);
		if (info.exception)
			// JSON error response from API
			return APIERR.handleJSON(step, info);
		if (info.error && info.status)
			// JSON error response from Spring
			return APIERR.handleOther(step, "Error " + info.status,
				info.error);
		return APIERR.handleOther(step, "Unknown Error", body);
	} catch (e) {}

	if (http)
		return APIERR.handleOther(step, "HTTP Error", http);
	if (status == "timeout")
		return APIERR.handleOther(step, "HTTP Timeout", null);
	return APIERR.handleOther(step, "Unknown Error", body);
}

var API = {};
API.ajax = function(step, method, path, data, callback) {
	$.ajax(path, {
		method: method, data: data, success: callback,
		error: function(xhr, status, http) {
			APIERR.handle(step, status, http, xhr.responseText);
		}
	});
}
API.get = function(step, path, data, callback) {
	API.ajax(step, "GET", path, data, callback);
};
API.post = function(step, path, data, callback) {
	API.ajax(step, "POST", path, data, callback);
}
API.putEmpty = function(step, path, data, callback) {
	API.ajax(step, "PUT", path, data, callback);
}
API.put = function(step, path, data, callback) {
	$.ajax(path, {
		method: "PUT", data: JSON.stringify(data), success: callback,
		error: function(xhr, status, http) {
			APIERR.handle(step, status, http, xhr.responseText);
		},
		contentType: "application/json; charset=UTF-8"
	});
}
API.delete = function(step, path, callback) {
	API.ajax(step, "DELETE", path, null, callback);
}

function setStatusClass(elem, status_list, name, status) {
	$.each(status_list, function(_, st) {
		if (st[name])
			elem.removeClass(st[name]);
	});
	if (status[name])
		elem.addClass(status[name]);
}

var validate = {};

validate.init = function(field, value, validator, updateHook) {
	var elem = typeof field == "string" ? $("#" + field) : field;
	elem.data("validator", validator);
	elem.data("updateHook", updateHook);
	elem.on("select change keyup paste input focusout", function() {
		validate.check(elem);
	});
	if (value !== null)
		elem.val(value);

	// no feedback on initial validation so the user doesn't start with a red
	// page. but call it anyway so the update hook gets called.
	validate.check(elem, true);
}

validate.check = function(field, disableFeedback) {
	var elem = typeof field == "string" ? $("#" + field) : field;
	var validator = elem.data("validator");
	var res = validator ? validator(elem.val()) : null;
	if (!disableFeedback)
		validate.feedback(elem, res);

	var valid = res === true || res === null;
	var updateHook = elem.data("updateHook");
	if (updateHook)
		updateHook(elem.val(), valid, elem, disableFeedback);
	return valid;
}

validate.feedback = function(field, status) {
	var elem = typeof field == "string" ? $("#" + field) : field;
	var group = elem.parents(".form-group,.form-group-horizontal");
	var text = elem.siblings(".form-control-feedback-text");
	var icon = elem.siblings(".form-control-feedback");;

	group.removeClass("has-error");
	icon.removeClass("text-success");
	text.addClass("hidden");
	icon.text("");
	if (status === true) {
		icon.addClass("text-success");
		icon.text("\u2714");
	} else if (status !== null) {
		group.addClass("has-error");
		icon.text("\u2716");
		text.removeClass("hidden");
		text.addClass("text-danger");
		if (status === false)
			text.text("please fill in this field");
		else
			text.text(status);
	}
}

validate.all = function(fields) {
	var valid = true;
	var data = {};
	$.each(fields, function(_, field) {
		var elem = typeof field == "string" ? $("#" + field) : field;
		if (!validate.check(elem)) {
			valid = false;
			elem.focus(); // let's hope the user notices
			return false;
		}
		var id = elem.attr("name");
		if (!id)
			id = elem.attr("id");
		data[id] = elem.val();
	});
	if (!valid)
		return null;
	return data;
}
