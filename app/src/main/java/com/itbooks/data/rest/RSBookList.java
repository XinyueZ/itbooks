package com.itbooks.data.rest;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public final class RSBookList implements Serializable {
	@SerializedName("status")
	private int          mStatus;
	@SerializedName("keyword")
	private String       mKeyword;
	@SerializedName("count")
	private int          mCount;
	@SerializedName("result")
	private List<RSBook> mBooks;

	public int getStatus() {
		return mStatus;
	}

	public String getKeyword() {
		return mKeyword;
	}

	public int getCount() {
		return mCount;
	}

	public List<RSBook> getBooks() {
		return mBooks;
	}
}
