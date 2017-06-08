function template(name) {
	// clone whole subtree and change ID to be unique
	var root = $("#" + name).clone();
	root.removeAttr("style");
	template.index++;
	root.attr("id", "form_" + template.index);
	var form = { root: root };
	// for all .form-control's and named labels, change their ID to be
	// unique, but also collect them in the object that gets returned.
	var elements = $(".form-control, label[name]", root);
	elements.each(function() {
		var id = $(this).attr("name");
		var name = id + "_" + template.index;
		$(this).attr("id", name).siblings("label").attr("for", name);
		form[id] = $(this);
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

API = {};
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

autosave = {};
autosave.msg = {
	saving: {
		glyphicon: "glyphicon-refresh",
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
		$.each(autosave.msg, function(_, st) {
			if (st[name])
				elem.removeClass(st[name]);
		});
		if (value[name])
			elem.addClass(value[name]);
	}

	setClass(id, "group", "feedback", st);
	setClass(id, "status", "glyphicon", st);
	if (st.text)
		$("#" + id + "_status_text").text("(" + st.text + ")");
	else
		$("#" + id + "_status_text").text("");
}

autosave.init = function(id, saver, validator) {
	var save = {
		saver: saver, validator: validator, value: null, timer: null };
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

	if (!validator || validator(elem.val(), id))
		autosave.feedback(id, autosave.msg.none);
	else
		autosave.feedback(id, autosave.msg.invalid);
};

// sets the control value, without saving
autosave.value = function(id, value) {
	var elem = $("#" + id);
	var save = elem.data("autosave");
	autosave._cancelTimeout(save);
	elem.val(value);
	save.value = value;
};
// sets the control value while saving null, ie. deletes the saved
// value.
autosave.reset = function(id, value) {
	autosave.value(id, value);
	var elem = $("#" + id);
	var save = elem.data("autosave");
	save.saver(null, id);
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
	if (save.validator && !save.validator(elem.val(), id)) {
		autosave.feedback(id, autosave.msg.invalid);
		return;
	}
	if (elem.val() == save.value) {
		autosave.feedback(id, autosave.msg.none);
		return; // not changed, nothing to do
	}
	autosave.feedback(id, autosave.msg.saving);
	save.value = elem.val();
	save.saver(elem.val(), id);
};
autosave.success = function(id) {
	var elem = $("#" + id);
	elem.data("autosave").value = elem.val();
	autosave.feedback(id, autosave.msg.success);
};

$(function() {
	API.get("/api/session-info", {}, function(info) {
		$("title").text(info.project + " – SARA software publishing");
		$("#ir_link").attr("href", info.ir.url);
		$("#ir_link img").attr("src", "/logos/" + info.ir.logo);
		initPage(info);
	});
});
