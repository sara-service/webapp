"use strict";

var clone_status = {
	pending: {
		icon: "\u231A",
		status_text: "pending",
		text: "text-muted" },
	working: {
		icon: "\u21C4",
		status_text: "working",
		text: "text-primary" },
	done: {
		icon: "\u2714",
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
	line.icon.text(clone_status[step.status].icon);
	line.status.text("(" + clone_status[step.status].status_text + ")");
	line.text.text(step.text);

	if (step.status == "working") {
		line.progress.removeClass("hidden");
		var value = step.progress * 100;
		line.bar.attr("aria-valuenow", value);
		line.bar.css("width", value + "%");
	} else
		line.progress.addClass("hidden");
}

var _success, _error, _endpoint;

function updateStatus(timeout) {
	API.get("check progress", _endpoint, {},
		function(status) {
			$.each(status.steps, updateStatusItem);

			// if task finished or failed, redirect to appropriate page
			if (status.status == "error") {
				if (status.error) {
					window.alert("operation failed: " +
						status.error.exception + ": " +
						status.error.message);
				}
				_error(status);
			} else if (status.status == "success")
				_success(status);
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
	updateStatus(2000);
}

function initStatus(endpoint, cancel, success, error) {
	_endpoint = endpoint;
	_success = success;
	_error = error;

	// short timeout for first update so the user doesn't have to wait
	// excessively if the operations finish quickly
	updateStatus(1000);

	$("#cancel").click(function() {
		location.replace(cancel);
	});
}
