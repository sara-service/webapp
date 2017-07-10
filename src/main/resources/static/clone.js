"use strict";

var clone_status = {
	pending: {
		glyphicon: "glyphicon-hourglass",
		status_text: "pending",
		text: "text-muted" },
	working: {
		glyphicon: "glyphicon-transfer",
		status_text: "working",
		text: "text-primary" },
	done: {
		glyphicon: "glyphicon-ok",
		status_text: "done",
		text: "text-success" },
};

var elements = {};

function updateStatusItem(step) {
	function setStatus(elem, name) {
		setStatusClass(elem, clone_status, name,
				clone_status[step.status]);
	}

	var line = elements[step.id];
	setStatus(line.root, "text");
	setStatus(line.icon, "glyphicon");
	line.status.text("(" + clone_status[step.status].status_text + ")");
	line.text.text(step.text);

	if (step.status == "working") {
		line.progress.css("display", "block");
		var value = step.progress * 100;
		line.bar.attr("aria-valuenow", value);
		line.bar.css("width", value + "%");
	} else
		line.progress.css("display", "none");
}

function addStatusItem(step) {
	var line = template("template");
	elements[step.id] = line;
	updateStatusItem(step);
	$("#steps").append(line.root);
}

function initStatus(steps) {
	$.each(steps, function(_, step) {
		addStatusItem(step);
	});
}

function updateStatus(handleItem) {
	API.get("check clone status", "/api/clone/status", {},
		function(steps) {
			var done = true;
			$.each(steps, function(_, step) {
				handleItem(step);
				if (step.status != "done")
					done = false;
			});
			if (done)
				location.href = "/done.html";
		});
}

function update() {
	updateStatus(updateStatusItem);
	// long timeout for later updates because the user is already
	// annoyed anyway
	// uses setTimeout instead of setInterval so two requests can never
	// be active at the same time
	setTimeout(update, 15000);
}

function initPage(session) {
	updateStatus(addStatusItem);
	// short timeout for first update so the user doesn't have to wait
	// excessively if the operations finish quickly
	setTimeout(update, 5000);
}
