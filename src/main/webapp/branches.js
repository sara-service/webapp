$(function() {
	function addBranch(name, value) {
		var el = $("#template").clone();
		el.removeAttr("style");
		var label = $("#template_label", el);
		label.text(name);
		label.attr("for", "branch_" + name);
		label.attr("id", "label_" + name);
		var form = $("#template_form", el);
		form.attr("id", "branch_" + name);
		form.val(value);
		if (form.val() != value)
			form.val("archive");
		$("#branches").append(el);
	}
	addBranch("master", "full");
	addBranch("test", "abbrev");
	addBranch("foo", "invalid");
	addBranch("bar", "ignore");
	addBranch("baz", "ignore");
});
