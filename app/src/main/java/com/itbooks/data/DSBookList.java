package com.itbooks.data;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * List of books
 */
public final class DSBookList {
	@SerializedName("Error")
	private String mError;//"Error": "0",
	@SerializedName("Time")
	private double mTime;//"Time": 0.00094,
	@SerializedName("Total")
	private String mTotal;//"Total": "147",
	@SerializedName("Page")
	private int mPage;//"Page": 2,
	@SerializedName("Books")
	private List<DSBook> mBooks;//"Books":


	public DSBookList(String _error, double _time, String _total, int _page, List<DSBook> _books) {
		mError = _error;
		mTime = _time;
		mTotal = _total;
		mPage = _page;
		mBooks = _books;
	}

	public String getError() {
		return mError;
	}

	public double getTime() {
		return mTime;
	}

	public String getTotal() {
		return mTotal;
	}

	public int getPage() {
		return mPage;
	}

	public List<DSBook> getBooks() {
		return mBooks;
	}
}
