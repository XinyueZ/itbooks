package com.itbooks.data;

import com.itbooks.data.rest.RSBook;

/**
 * A bookmark.
 *
 * @author Xinyue Zhao
 */
public final class DSBookmark extends RSBook {
	private String mUID;


	public DSBookmark(RSBook book) {
		super(book.getName(), book.getAuthor(), book.getSize(), book.getPages(), book.getLink(), book.getISBN(),
				book.getYear(), book.getPublisher(), book.getDescription(), book.getCoverUrl());

	}

	public DSBookmark(RSBook book, String uid) {
		super(book.getName(), book.getAuthor(), book.getSize(), book.getPages(), book.getLink(), book.getISBN(),
				book.getYear(), book.getPublisher(), book.getDescription(), book.getCoverUrl());
		mUID = uid;
	}

	public String getUID() {
		return mUID;
	}

	public RSBook getBook() {
		return this;
	}
}
