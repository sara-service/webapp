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

function updateStatusItem(id, step) {
	function setStatus(elem, name) {
		setStatusClass(elem, clone_status, name,
				clone_status[step.status]);
	}

	var line = elements[id];
	if (typeof line == "undefined") {
		line = template("template");
		elements[id] = line;
		$("#steps").append(line.root);
	}
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

var _nextURL, _errorURL, _endpoint;

function updateStatus(timeout) {
	API.get("check progress", _endpoint, {},
		function(status) {
			$.each(status.steps, updateStatusItem);

			// if task finished or failed, redirect to appropriate page
			if (status.status == "error") {
				if (status.error) {
					window.alert("clone failed: " +
						status.error.exception + ": " +
						status.error.message);
				}
				location.href = _errorURL;
			} else if (status.status == "success")
				location.href = _nextURL;
			else
				// schedule next status update. uses setTimeout instead
				// of setInterval so two requests can never be active at
				// the same time.
				setTimeout(update, timeout);
		});
}

function update() {
	// long timeout for later updates because the user is already
	// waiting anyway
	updateStatus(5000);
}

function initStatus(endpoint, nextURL, errorURL) {
	_endpoint = endpoint;
	_nextURL = nextURL;
	_errorURL = errorURL;

	// short timeout for first update so the user doesn't have to wait
	// excessively if the operations finish quickly
	updateStatus(2000);
}
