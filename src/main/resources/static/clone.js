"use strict";

$(function() {
	API.post("initiate repository clone", "/api/clone/trigger", {},
		function() {
			initStatus("/api/clone/status", function() {
					location.href = "/license.html";
				}, function() {
					location.href = "/branches.html";
				});
		});
});
