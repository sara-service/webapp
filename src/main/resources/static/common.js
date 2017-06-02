function template(name)  {
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

$(function() {
	API.get("/api/session-info", {}, function(info) {
		$("title").text(info.project + " – SARA software publishing");
		$("#ir_link").attr("href", info.ir.url);
		$("#ir_link img").attr("src", "/logos/" + info.ir.logo);
		initPage(info);
	});
});
