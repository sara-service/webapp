"use strict";

$(function() {
	initStatus("/api/clone/status", function() {
		location.href = "/license.html";
	}, function() {
		location.reload();
	});
});
