package com.itbooks.bus;

import com.itbooks.data.DSBook;
import com.itbooks.data.DSBookmark;

/**
 * Event to open a bookmark.
 *
 * @author Xinyue Zhao
 */
public final class OpenBookmarkEvent {
	private DSBookmark mBookmark;


	public OpenBookmarkEvent(DSBookmark bookmark) {
		mBookmark = bookmark;
	}

	public DSBookmark getBookmark() {
		return mBookmark;
	}

	public DSBook getBook() {
		return mBookmark.getBook();
	}
}
