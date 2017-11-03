"use strict";

function template(name) {
	// clone whole subtree and change ID to be unique
	var root = $("#" + name).clone();
	root.removeAttr("style");
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
		location.href = "/clone.html";
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
API.put = function(step, path, data, callback) {
	API.ajax(step, "PUT", path, data, callback);
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

var autosave = {};
autosave.msg = {
	saving: {
		glyphicon: "glyphicon-transfer",
		feedback: null,
		text: null },
	success: {
		glyphicon: "glyphicon-ok",
		feedback: "has-success",
		text: "saved" },
	invalid: {
		glyphicon: "glyphicon-remove",
		feedback: "has-error",
		text: "field contents are invalid" },
	none: {
		glyphicon: null,
		feedback: null,
		text: null },
};

autosave.feedback = function(id, st) {
	function setClass(id, part, name, value) {
		var elem = $("#" + id + "_" + part);
		setStatusClass(elem, autosave.msg, name, value);
	}

	setClass(id, "group", "feedback", st);
	setClass(id, "status", "glyphicon", st);
	if (st.text)
		$("#" + id + "_status_text").text("(" + st.text + ")");
	else
		$("#" + id + "_status_text").text("");
}

autosave.init = function(id, saver, validator) {
	var save = { saver: saver, validator: validator, value: null,
		timer: null, update: null, updateEnabled: false };
	var elem = $("#" + id);
	elem.data("autosave", save);

	elem.on("change keyup paste", function() {
		autosave._cancelTimeout(save);
		save.timer = setTimeout(function() {
			autosave.save(id);
		}, 1500);
	});
	elem.on("focusout", function() {
		autosave.save(id);
	});
	elem.on("focus", function() {
		autosave.feedback(id, autosave.msg.none);
	});
	autosave.validate(id);
};

autosave.isValid = function(id) {
	var elem = $("#" + id);
	var save = elem.data("autosave");
	return !save.validator || save.validator(elem.val(), id);
};
autosave.validate = function(id) {
	var elem = $("#" + id);
	var save = elem.data("autosave");
	var valid = autosave.isValid(id);
	autosave.feedback(id, valid ? autosave.msg.none :
		autosave.msg.invalid);
	if (save.update)
		save.update.prop("disabled", !valid || !save.updateEnabled);
	return valid;
};

// sets the control value, without saving
autosave.value = function(id, value) {
	var elem = $("#" + id);
	var save = elem.data("autosave");
	autosave._cancelTimeout(save);
	elem.val(value);
	save.value = value;
	autosave.validate(id);
};

autosave.cancelTimeout = function(id) {
	autosave._cancelTimeout($("#" + id).data("autosave"));
};
autosave._cancelTimeout = function(save) {
	if (save.timer != null)
		clearTimeout(save.timer);
	save.timer = null;
};
autosave.save = function(id) {
	var elem = $("#" + id);
	var save = elem.data("autosave");
	autosave._cancelTimeout(save);
	if (!autosave.validate(id))
		return;
	if (elem.val() == save.value) {
		autosave.feedback(id, autosave.msg.none);
		return; // not changed, nothing to do
	}
	autosave.feedback(id, autosave.msg.saving);
	save.value = elem.val();
	save.saver(elem.val(), id, false);
};
autosave.success = function(id) {
	var elem = $("#" + id);
	elem.data("autosave").value = elem.val();
	autosave.feedback(id, autosave.msg.success);
};

autosave.configureUpdateButton = function(id, updater) {
	var elem = $("#" + id);
	var save = elem.data("autosave");
	save.update = $("#update_" + id);
	save.update.off("click");
	save.updateEnabled = !!updater;
	if (save.updateEnabled) {
		save.update.click(function() {
			autosave.cancelTimeout(id);
			if (autosave.validate(id))
				updater(elem.val(), id);
		});
		autosave.validate(id); // to enable / disable button initially
	} else
		save.update.prop("disabled", true);
}

var pageInfo;

$(function() {
	API.get("initialize page", "/api/session-info", {}, function(info) {
		var title = "SARA software publishing";
		if (info.project !== null)
			title = info.project + " – " + title;
		$("title").text(title);
		$("#ir_link").attr("href", info.ir.url);
		$("#ir_link img").attr("src", "/logos/" + info.ir.logo);
		pageInfo = info;
		initPage(info);
	});
});
