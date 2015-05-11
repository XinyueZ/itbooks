package com.itbooks.bus;


import com.itbooks.data.rest.RSBook;

public final class OpenBookDetailEvent {
	private RSBook mBook;

	public OpenBookDetailEvent(RSBook book) {
		mBook = book;
	}

	public RSBook getBook() {
		return mBook;
	}
}
