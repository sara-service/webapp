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

var project = new URI(location).search(true).project;

API = {};
API.get = function(path, data, callback) {
	data.project = project;
	$.ajax(path, {
		data: data,
		success: callback,
		error: reportAPIError,
	});
};
API.post = function(path, data, callback) {
	data.project = project;
	$.ajax(path, {
		method: "POST",
		data: data,
		success: callback,
		error: reportAPIError,
	});
}

$(function() {
	$("title").text(project + " – SARA software publishing");
	API.get("/api/ir-meta", {}, function(meta) {
		$("#ir_link").attr("href", meta.url);
		$("#ir_link img").attr("src", "/logos/" + meta.logo);
	});
	initPage();
});
