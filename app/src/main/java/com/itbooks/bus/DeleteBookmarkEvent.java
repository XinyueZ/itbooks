package com.itbooks.bus;

import com.itbooks.data.DSBook;
import com.itbooks.data.DSBookmark;

/**
 * Event to remove a bookmark.
 *
 * @author Xinyue Zhao
 */
public final class DeleteBookmarkEvent {
	private DSBookmark mBookmark;


	public DeleteBookmarkEvent(DSBookmark bookmark) {
		mBookmark = bookmark;
	}

	public DSBookmark getBookmark() {
		return mBookmark;
	}

	public DSBook getBook() {
		return mBookmark.getBook();
	}
}
