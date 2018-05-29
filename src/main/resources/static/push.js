"use strict";

$(function() {
	initStatus("/api/push/status", "/api/push/cancel", "/api/push/redirect",
		"/overview.html");
});
