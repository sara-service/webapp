"use strict";

function fillMetaData(meta) {
	$.each(["title", "description", "version", "url", "submitter", "date"],
		function(_, id) {
			$('#' + id).text(meta[id]);
		});
	$('#url').attr("href", meta.url);
	if (meta.access == "PUBLIC")
		$('#access').text("public");
	else if (meta.access == "PRIVATE")
		$('#access').text("private");
	else
		$('#access').text(meta.access);

	$('#meta').removeClass("hidden");
}

$(function() {
	var item = new URI(location.href).search(true).item;
	if (!item)
		return;

	$("#next_button").attr("href",
		new URI("/api/auth/publish").search({ item: item }));
	$("#next_button").removeClass("disabled");
	$.ajax("/api/item/" + item, { method: "GET", success: fillMetaData,
		complete: function() {
			$("#content").removeClass("hidden");
			$("#loading").remove();
		}
	});
});
