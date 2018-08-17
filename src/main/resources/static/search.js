"use strict";

function initSearch(box, items, overflow, limit) {
	var group = box.parents(".form-group,.form-group-horizontal");
	limit = limit ? limit : 10;
	var maxMatched = limit;

	function updateSearch() {
		var keywords = box.val().trim().toLowerCase().split(/\s+/);
		var matched = 0;
		$.each(items, function(_, item) {
			var show = true;
			// show an item iff its text content contains all keywords
			var text = item.text().toLowerCase();
			for (var i = 0; i < keywords.length; i++)
				show &= text.indexOf(keywords[i]) >= 0;
			item.toggleClass("hidden", !show || matched >= maxMatched);
			if (show)
				matched++;
		});

		if (overflow)
			overflow.toggleClass("hidden", matched <= maxMatched);
	}

	// handling ENTER keypress in search box: click the single element if
	// exactly one is left, else color the box red until the user edits
	// something.
	box.on("select change keyup keydown paste input focusout", function(ev) {
		if ((ev.type == "keydown" || ev.type == "keyup") && ev.which == 13) {
			var visible = $.map(items, function(item) {
				return item.hasClass("hidden") ? undefined : item;
			});
			if (visible.length == 1)
				visible[0].get(0).click();
			else
				group.addClass("has-error");
			return false;
		} else
			group.removeClass("has-error");

		updateSearch();
	});

	if (overflow)
		overflow.click(function() {
			maxMatched += limit;
			updateSearch();
		});
	updateSearch();
}
