"use strict";

$(function() {
	initStatus("/api/clone/status", "/api/clone/cancel", function() {
		location.replace("/license.html");
	}, function() {
		location.replace("/branches.html");
	});
});
