package com.itbooks.data;

import android.text.TextUtils;
import android.view.View;

import com.google.gson.annotations.SerializedName;
import com.itbooks.utils.Prefs;

/**
 * Details of book.
 *
 * @author Xinyue Zhao
 */
public final class DSBookDetail {
	@SerializedName("Error")
	private String mError;//"Error": "0",
	@SerializedName("Time")
	private double mTime; //"Time": 0.00049,
	@SerializedName("ID")
	private long mId; //"ID": 1539580363,
	@SerializedName("Title")
	private String mTitle;//"Title": "Developing Android Applications with Flex 4.5",
	@SerializedName("SubTitle")
	private String mSubTitle;//"SubTitle": "Building Android Applications with ActionScript",
	@SerializedName("Description")
	private String mDescription;//"Description": "Ready to put your ActionScript 3 skills to work on mobile apps? This
	@SerializedName("Author")
	private String mAuthor;//"Author": "Rich Tretola",
	@SerializedName("ISBN")
	private String mISBN; //"ISBN": "9781449305376",
	@SerializedName("Year")
	private String mYear;//"Year": "2011",
	@SerializedName("Page")
	private String mPage;//"Page": "112",
	@SerializedName("Publisher")
	private String mPublisher;//"Publisher": "O'Reilly Media",
	@SerializedName("Image")
	private String mImageUrl;//"Image": "http://s.it-ebooks-api.info/3/developing_android_applications_with_flex_4.5
	@SerializedName("Download")
	private String mDownloadUrl;//"Download": "http://filepi.com/i/8wlDiuB"

	public DSBookDetail(String _error, double _time, long _id, String _title, String _subTitle, String _description,
			String _author, String _ISBN, String _year, String _page, String _publisher, String _imageUrl,
			String _downloadUrl) {
		mError = _error;
		mTime = _time;
		mId = _id;
		mTitle = _title;
		mSubTitle = _subTitle;
		mDescription = _description;
		mAuthor = _author;
		mISBN = _ISBN;
		mYear = _year;
		mPage = _page;
		mPublisher = _publisher;
		mImageUrl = _imageUrl;
		mDownloadUrl = _downloadUrl;
	}

	public String getError() {
		return mError;
	}

	public double getTime() {
		return mTime;
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

	public String getAuthor() {
		return TextUtils.isEmpty(mAuthor) ? Prefs.NA : mAuthor;
	}

	public String getISBN() {
		return TextUtils.isEmpty(mISBN) ? Prefs.NA : mISBN;
	}

	public String getYear() {
		return TextUtils.isEmpty(mYear) ? Prefs.NA : mYear;
	}

	public String getPage() {
		return TextUtils.isEmpty(mPage) ? Prefs.NA : mPage;
	}

	public String getPublisher() {
		return TextUtils.isEmpty(mPublisher) ? Prefs.NA : mPublisher;
	}

	public String getImageUrl() {
		return mImageUrl;
	}

	public String getDownloadUrl() {
		return mDownloadUrl;
	}

}
