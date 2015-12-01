package com.itbooks.bus;

import com.itbooks.data.DSBookmark;
import com.itbooks.data.rest.RSBook;

/**
 * Event to open a bookmark.
 *
 * @author Xinyue Zhao
 */
public final class OpenBookmarkEvent {
	private DSBookmark mBookmark;


	public OpenBookmarkEvent( DSBookmark bookmark ) {
		mBookmark = bookmark;
	}

	public DSBookmark getBookmark() {
		return mBookmark;
	}

	public RSBook getBook() {
		return mBookmark.getBook();
	}
}
