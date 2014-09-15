package com.itbooks.utils;

import android.app.Application;
import android.content.Context;

import com.chopping.application.BasicPrefs;

/**
 *
 */
public final class Prefs extends BasicPrefs{
	/**
	 * Impl singleton pattern.
	 */
	private static Prefs sInstance;

	private static final String API_SEARCH_BOOKS ="api_search_books";
	private static final String API_BOOK_DETAIL = "api_book_detail";
	private static final String API_DEFAULT_BOOKS = "api_default_books";

	/**
	 * Created a DeviceData storage.
	 *
	 * @param context
	 * 		A context object.
	 */
	private Prefs(Context context) {
		super(context);
	}

	/**
	 * Get instance of  {@link com.itbooks.utils.Prefs} singleton.
	 *
	 * @param _context
	 * 		{@link android.app.Application}.
	 *
	 * @return The {@link com.itbooks.utils.Prefs} singleton.
	 */
	public static Prefs getInstance(Application _context) {
		if (sInstance == null) {
			sInstance = new Prefs(_context);
		}
		return sInstance;
	}

	public String getApiSearchBooks() {
		return getString(API_SEARCH_BOOKS, null);
	}

	public String getApiBookDetail() {
		return getString(API_BOOK_DETAIL, null);
	}

	public String getApiDefaultBooks() {
		return getString(API_DEFAULT_BOOKS, null);
	}
}
