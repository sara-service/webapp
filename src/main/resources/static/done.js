"use strict";

$(function() {
	var item = new URI(location.href).search(true).item;
	$('#item').text(item);
	$("#loading").remove();
	$("#content").removeClass("hidden");
	$("#next_button").attr("href",
		new URI("/api/auth/publish").search({ item: item }));
	$("#next_button").removeClass("disabled");
});
