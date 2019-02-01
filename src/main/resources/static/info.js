"use strict";

function fillMetaData(meta) {
	$.each(["title", "description", "version", "url", "date"],
		function(_, id) {
			$('#' + id).text(meta[id]);
		});
	$.each(["surname", "givenname"], function(_, id) {
		$('#submitter_' + id).text(meta.submitter[id]);
	});
	$.each(meta.authors, function(_, author) {
		var row = $("<li>");
		row.text(author.surname + ", " + author.givenname);
		$("#authors").append(row);
	});

	if (meta.public_access) {
		$('#access').text("public");
		$('#url').attr("href", meta.url);
		$('#public').removeClass("hidden");
	} else
		$('#access').text("private");

	if (meta.token != null && !meta.public_access) {
		$('#token').text(meta.token);
		$('#tokeninfo').removeClass("hidden");
	}

	// note: this relies on the "token" parameter being omitted if the token
	// is null!
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
	// note: this also relies on "token" being omitted if null
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
