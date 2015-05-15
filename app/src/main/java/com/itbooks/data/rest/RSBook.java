package com.itbooks.data.rest;


import java.io.Serializable;

import android.os.Bundle;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import cn.bmob.v3.BmobObject;

public   class RSBook extends BmobObject implements Serializable {
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

	public String[] toArray() {
		String[] array = new String[8];
		array[0] = mName;
		array[1] = mAuthor;
		array[2] = mSize;
		array[3] = mPages;
		array[4] = mISBN;
		array[5] = mYear;
		array[6] = mPublisher;
		array[7] = mDescription;
		return array;
	}

	/**
	 * RSBook(String name, String author, String size, String pages, String link, String ISBN, String year,
	 String publisher, String description, String coverUrl)
	 * @param bundle
	 * @return
	 */
	public static RSBook newInstance(Bundle bundle) {
		return new RSBook(
				bundle.getString("Name"),
				bundle.getString("Author"),
				bundle.getString("Size"),
				bundle.getString("Pages"),
				bundle.getString("Link"),
				bundle.getString("ISBN"),
				bundle.getString("Year"),
				bundle.getString("Publisher"),
				bundle.getString("Description"),
				bundle.getString("CoverUrl")
		);
	}

	@Override
	public boolean equals(Object o) {
		RSBook other = (RSBook) o;
		return TextUtils.equals(mName, other.mName) &&
				TextUtils.equals(mAuthor, other.mAuthor) &&
				TextUtils.equals(mSize, other.mSize) &&
				TextUtils.equals(mPages, other.mPages)&&
				TextUtils.equals(mISBN, other.mISBN) &&
				TextUtils.equals(mYear, other.mYear) &&
				TextUtils.equals(mPublisher, other.mPublisher) &&
				TextUtils.equals(mDescription, other.mDescription) ;
	}
}
