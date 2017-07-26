"use strict";

function initPage(session) {
	API.post("initiate repository push", "/api/push/trigger", {},
		function() {
			initStatus("/api/push/status", function() {
					API.get("get persisntent URL", "/api/push/web-url",
						{}, function(url) {
							location.href = url;
						});
				}, function() {
					location.href = "/overview.html";
				});
		});
}
