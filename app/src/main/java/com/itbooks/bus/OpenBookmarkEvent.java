package com.itbooks.bus;

import android.view.View;

import com.itbooks.data.DSBook;
import com.itbooks.data.DSBookmark;

/**
 * Event to open a bookmark.
 *
 * @author Xinyue Zhao
 */
public final class OpenBookmarkEvent {
	private DSBookmark mBookmark;
	private View mBookCoverV;


	public OpenBookmarkEvent(DSBookmark bookmark, View bookCoverV) {
		mBookmark = bookmark;
		mBookCoverV = bookCoverV;
	}

	public DSBookmark getBookmark() {
		return mBookmark;
	}

	public DSBook getBook() {
		return mBookmark.getBook();
	}

	public View getBookCoverV() {
		return mBookCoverV;
	}
}
