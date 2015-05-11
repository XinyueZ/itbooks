package com.itbooks.data;

import com.itbooks.data.rest.RSBook;
import com.itbooks.db.BookmarksTbl;

/**
 * A bookmark.
 *
 * @author Xinyue Zhao
 */
public final class DSBookmark {
	private  long mId = -1;
	private RSBook mBook;
	private long mLabelId = Long.parseLong(BookmarksTbl.DEF_LABEL_ID);
	private long mEditTime = -1;

	public DSBookmark(RSBook book, long labelId, long editTime) {
		mBook = book;
		mLabelId = labelId;
		mEditTime = editTime;
	}

	public DSBookmark(RSBook book, long labelId) {
		mBook = book;
		mLabelId = labelId;
	}

	public DSBookmark(RSBook book ) {
		mBook = book;
	}

	public DSBookmark(long id, RSBook book, long labelId, long editTime) {
		mId = id;
		mBook = book;
		mLabelId = labelId;
		mEditTime = editTime;
	}

	public DSBookmark(long id, RSBook book, long labelId) {
		mId = id;
		mBook = book;
		mLabelId = labelId;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	public RSBook getBook() {
		return mBook;
	}

	public void setBook(RSBook book) {
		mBook = book;
	}

	public long getLabelId() {
		return mLabelId;
	}

	public void setLabelId(long labelId) {
		mLabelId = labelId;
	}

	public long getEditTime() {
		return mEditTime;
	}

	public void setEditTime(long editTime) {
		mEditTime = editTime;
	}
}
