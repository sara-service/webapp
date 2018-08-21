"use strict";

function updateStatusItem(id, step) {
	var line = template("template");
	line.text.text(step.text);
	line.status.text("(" + step.status + ")");

	// set icon and color
	if (step.status == "done") {
		line.icon.text("\u2714");
		line.root.addClass("text-success");
	} else if (step.status == "working") {
		line.loading.addClass("loading-icon");
		line.root.addClass("text-primary");
	} else if (step.status == "pending")
		line.root.addClass("text-muted");

	// hide or update progress bar
	if (step.status == "working") {
		// users don't like it when a progress bar is stuck at 100%, but the
		// task isn't finished yet. 98% is visibly not-yet-complete...
		var value = step.progress * 98;
		line.bar.attr("aria-valuenow", value);
		line.bar.css("width", value + "%");
	} else
		line.progress.remove();

	$("#steps").append(line.root);
}

var _success, _error, _endpoint;

function updateStatus(timeout) {
	API.get("check progress", _endpoint, {},
		function(status) {
			$("#steps > li").not("#template").remove();
			$.each(status.steps, updateStatusItem);

			// if task finished or failed, redirect to appropriate page
			if (status.status == "error") {
				if (status.error) {
					var operation = status.error.step ? status.error.step : "operation";
					window.alert(operation + " failed!\n\n" +
						status.error.exception + ": " + status.error.message);
				}
				location.replace(_error);
			} else if (status.status == "success")
				location.replace(_success);
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
