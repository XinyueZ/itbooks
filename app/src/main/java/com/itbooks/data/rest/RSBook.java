package com.itbooks.data.rest;


import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public final class RSBook implements Serializable {
	@SerializedName("Name")
	private String mName;
	@SerializedName("Author")
	private String mAuthor;
	@SerializedName("Size")
	private String mSize;
	@SerializedName("Pages")
	private String mPages;
	@SerializedName("Link")
	private String mLink;
	@SerializedName("ISBN")
	private String mISBN;
	@SerializedName("Year")
	private String mYear;
	@SerializedName("Publisher")
	private String mPublisher;
	@SerializedName("Description")
	private String mDescription;
	@SerializedName("CoverUrl")
	private String mCoverUrl;

	public RSBook() {

	}

	public RSBook(String name, String author, String size, String pages, String link, String ISBN, String year,
			String publisher, String description, String coverUrl) {
		mName = name;
		mAuthor = author;
		mSize = size;
		mPages = pages;
		mLink = link;
		mISBN = ISBN;
		mYear = year;
		mPublisher = publisher;
		mDescription = description;
		mCoverUrl = coverUrl;
	}

	public String getName() {
		return mName;
	}

	public String getAuthor() {
		return mAuthor;
	}

	public String getSize() {
		return mSize;
	}

	public String getPages() {
		return mPages;
	}

	public String getLink() {
		return mLink;
	}

	public String getISBN() {
		return mISBN;
	}

	public String getYear() {
		return mYear;
	}

	public String getPublisher() {
		return mPublisher;
	}

	public String getDescription() {
		return mDescription;
	}

	public String getCoverUrl() {
		return mCoverUrl;
	}
}
