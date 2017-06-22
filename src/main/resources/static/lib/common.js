"use strict";

function template(name) {
	// clone whole subtree and change ID to be unique
	var root = $("#" + name).clone();
	root.removeAttr("style");
	template.index++;
	root.attr("id", "form_" + template.index);
	var form = { root: root };
	// callect all child elements that have a name attribute
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

function reportAPIError(xhr, status, error) {
	alert(status + " " + error);
	// fix the error for the user; it's not like he will do anything
	// else
	if (!location.href.match("^file:"))
		location.reload();
}

var API = {};
API.get = function(path, data, callback) {
	$.ajax(path, {
		data: data,
		success: callback,
		error: reportAPIError,
	});
};
API.post = function(path, data, callback) {
	$.ajax(path, {
		method: "POST",
		data: data,
		success: callback,
		error: reportAPIError,
	});
}
API.put = function(path, data, callback) {
	$.ajax(path, {
		method: "PUT",
		data: data,
		success: callback,
		error: reportAPIError,
	});
}
API.delete = function(path, callback) {
	$.ajax(path, {
		method: "DELETE",
		success: callback,
		error: reportAPIError,
	});
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
// sets the control value, saving with autoset=true
autosave.reset = function(id, value) {
	autosave.value(id, value);
	var elem = $("#" + id);
	var save = elem.data("autosave");
	save.saver(value, id, true);
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

var reftype_names = {
	BRANCH: "branch ",
	TAG: "tag ",
};

$(function() {
	API.get("/api/session-info", {}, function(info) {
		$("title").text(info.project + " – SARA software publishing");
		$("#ir_link").attr("href", info.ir.url);
		$("#ir_link img").attr("src", "/logos/" + info.ir.logo);
		initPage(info);
	});
});
