"use strict";

function initPage(session) {
	API.post("initiate repository clone", "/api/clone/trigger", {},
		function() {
			initStatus("/api/clone/status", "/meta.html",
				"/branches.html");
		});
}
