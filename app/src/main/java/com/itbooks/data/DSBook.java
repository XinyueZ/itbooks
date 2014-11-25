package com.itbooks.data;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.itbooks.utils.Prefs;

/**
 * Book
 *
 * @author Xinyue Zhao
 */
public final class DSBook {
	@SerializedName("ID")
	private long mId;//"ID": 2163100151,
	@SerializedName("Title")
	private String mTitle;//"Title": "Android Native Development Kit Cookbook",
	@SerializedName("SubTitle")
	private String mSubTitle;//"SubTitle": "A step-by-step tutorial with more than 60 concise recipes on Android NDK
	// development skills",
	@SerializedName("Description")
	private String mDescription;//"Description": "Building Android applications would usually mean that you spend all
	@SerializedName("Image")
	private String mImageUrl;//"Image": "http://s.it-ebooks-api.info/14/android_native_development_kit_cookbook.jpg",
	@SerializedName("isbn")
	private String mISBN;//"isbn": "9781849691505"

	public DSBook(long _id, String _title, String _subTitle, String _description, String _imageUrl, String _ISBN) {
		mId = _id;
		mTitle = _title;
		mSubTitle = _subTitle;
		mDescription = _description;
		mImageUrl = _imageUrl;
		mISBN = _ISBN;
	}

	public DSBook(long id,   String imageUrl) {
		mId = id;
		mImageUrl = imageUrl;
	}

	public long getId() {
		return mId;
	}

	public String getTitle() {
		return TextUtils.isEmpty(mTitle) ? Prefs.NA : mTitle;
	}

	public String getSubTitle() {
		return TextUtils.isEmpty(mSubTitle) ? Prefs.NA : mSubTitle;
	}

	public String getDescription() {
		return TextUtils.isEmpty(mDescription) ? Prefs.NA : mDescription;
	}

	public String getImageUrl() {
		return mImageUrl;
	}

	public String getISBN() {
		return TextUtils.isEmpty(mISBN) ? Prefs.NA : mISBN;
	}
}
