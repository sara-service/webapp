"use strict";

function fillMetaData(meta) {
	$.each(["title", "description", "version", "url", "submitter", "date"],
		function(_, id) {
			$('#' + id).text(meta[id]);
		});

	if (meta.access == "PUBLIC") {
		$('#access').text("public");
		$('#url').attr("href", meta.url);
		$('#public').removeClass("hidden");
	} else if (meta.access == "PRIVATE") {
		$('#access').text("private");
		$('#token').text(meta.token);
		$('#private').removeClass("hidden");
	} else {
		$('#access').text(meta.access);
		$("#error").removeClass("hidden");
	}
	$("#next_button").attr("href",
		new URI("/api/auth/publish").search({
			item: meta.item, token: meta.token }));

	$('#meta').removeClass("hidden");
}

$(function() {
	var args = new URI(location.href).search(true);
	var item = args.item;
	if (!item)
		return;

	$("#item").text(item);
	// note: this relies on token not being sent if the variable is null!
	$.ajax("/api/item/" + item, { method: "GET", data: { token: args.token },
		success: fillMetaData, error: function() {
			$("#error").removeClass("hidden");
			// best guess: this will at least work if the user still has
			// login credentials
			$("#next_button").attr("href",
				new URI("/api/auth/publish").search({ item: item }));
		}, complete: function() {
			$("#next_button").removeClass("disabled");
			$("#loading").remove();
		}
	});
});
