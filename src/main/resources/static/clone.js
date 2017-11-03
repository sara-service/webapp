"use strict";

function initPage(session) {
	API.post("initiate repository clone", "/api/clone/trigger", {},
		function() {
			initStatus("/api/clone/status", function() {
					location.href = "/license.html";
				}, function() {
					location.href = "/branches.html";
				});
		});
}
