"use strict";

var clone_status = {
	pending: {
		glyphicon: "glyphicon-hourglass",
		status_text: "pending",
		text: "text-muted" },
	working: {
		glyphicon: "glyphicon-transfer",
		status_text: "working",
		text: "text-primary" },
	done: {
		glyphicon: "glyphicon-ok",
		status_text: "done",
		text: "text-success" },
};

var elements = {};

function updateStatus(step) {
	function setStatus(elem, name) {
		setStatusClass(elem, clone_status, name,
				clone_status[step.status]);
	}

	var line = elements[step.id];
	setStatus(line.root, "text");
	setStatus(line.icon, "glyphicon");
	line.status.text("(" + clone_status[step.status].status_text + ")");
	line.text.text(step.text);
}

function addStatus(step) {
	var line = template("template");
	elements[step.id] = line;
	updateStatus(step);
	$("#steps").append(line.root);
}

function initStatus(steps) {
	$.each(steps, function(_, step) {
		addStatus(step);
	});
}

function initPage(session) {
}

var demo = [
	{ id: "init", status: "done", text: "initializing temporary repository" },
	{ id: "branch_master", status: "done", text: "cloning branch master" },
	{ id: "branch_webapp", status: "working", text: "cloning branch webapp" },
	{ id: "tag_test", status: "pending", text: "cloning branch test" },
	{ id: "tag_foo", status: "pending", text: "cloning tag foo" },
	{ id: "tag_bar", status: "pending", text: "cloning tag bar" },
	{ id: "tag_baz", status: "pending", text: "cloning tag baz" }
];
$(function() { initStatus(demo); });
