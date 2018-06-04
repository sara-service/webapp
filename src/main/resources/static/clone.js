"use strict";

$(function() {
	initStatus("/api/clone/status", "/api/clone/cancel", "/meta.html",
		"/branches.html");
});
