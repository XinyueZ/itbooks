package com.itbooks.bus;

import com.itbooks.data.DSBookmark;
import com.itbooks.data.rest.RSBook;

/**
 * Event to remove a bookmark.
 *
 * @author Xinyue Zhao
 */
public final class DeleteBookmarkEvent {
	private DSBookmark mBookmark;


	public DeleteBookmarkEvent( DSBookmark bookmark ) {
		mBookmark = bookmark;
	}

	public DSBookmark getBookmark() {
		return mBookmark;
	}

	public RSBook getBook() {
		return mBookmark.getBook();
	}
}
