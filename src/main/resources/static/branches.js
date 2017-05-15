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

function addBranches(branches, actions) {
	branches.sort(function(a, b) {
		if (a == "master")
			return -1;
		if (b == "master")
			return +1;
		return a.localeCompare(b);
	});
	for (var i = 0; i < branches.length; i++) {
		var branch = branches[i];
		if (typeof actions[branch] == "undefined") {
			if (branch == "master")
				actions[branch] = "full";
			else
				actions[branch] = "ignore";
		}
		addBranch(branch, actions[branch]);
	}
	$("#loading").remove();	
}

$(function() {
	var data = JSON.parse(decodeURI(location.hash.substring(1)));
	
	$("title").text(data.project + " – SARA software publishing");

	$.ajax("/api/branches", {
		dataType: "json",
		success: function(branches) {
			$.ajax("/api/actions", {
				dataType: "json",
				success: function(actions) {
					addBranches(branches, actions);
				},
				error: function(xhr, status, error) {
					alert(status);
				}
			});
		},
		error: function(xhr, status, error) {
			alert(status);
		}
	});
});
