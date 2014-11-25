package com.itbooks.utils;

import android.app.Application;
import android.content.Context;

import com.chopping.application.BasicPrefs;

/**
 *
 */
public final class Prefs extends BasicPrefs {
	public static final String NA = "N/A";
	public static final String API_LIMIT = "API limit exceeded!";
	/**
	 * Impl singleton pattern.
	 */
	private static Prefs sInstance;

	private static final String API_SEARCH_BOOKS = "api_search_books";
	private static final String API_BOOK_DETAIL = "api_book_detail";
	private static final String API_DEFAULT_BOOKS = "api_default_books";
	private static final String KEY_LAST_SEARCHED = "key.last.searched";
	private static final String KEY_KNOWN_BOOKMARK = "key.known.bookmark";
	/**
	 * Storage. Whether the "End User License Agreement" has been shown and agreed at application's first start.
	 * <p/>
	 * {@code true} if EULA has been shown and agreed.
	 */
	private static final String KEY_EULA_SHOWN = "key_eula_shown";

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

	/**
	 * Whether the "End User License Agreement" has been shown and agreed at application's first start.
	 * <p/>
	 *
	 * @return {@code true} if EULA has been shown and agreed.
	 */
	public boolean isEULAOnceConfirmed() {
		return getBoolean(KEY_EULA_SHOWN, false);
	}

	/**
	 * Set whether the "End User License Agreement" has been shown and agreed at application's first start.
	 * <p/>
	 *
	 * @param isConfirmed
	 * 		{@code true} if EULA has been shown and agreed.
	 */
	public void setEULAOnceConfirmed(boolean isConfirmed) {
		setBoolean(KEY_EULA_SHOWN, isConfirmed);
	}

	public void setLastSearched(String searched) {
		setString(KEY_LAST_SEARCHED, searched);
	}

	public String getLastSearched(){
		return getString(KEY_LAST_SEARCHED, null);
	}

	public void setKnownBookmark(boolean known) {
		setBoolean(KEY_KNOWN_BOOKMARK, known);
	}

	public boolean hasKnownBookmark(){
		return getBoolean(KEY_KNOWN_BOOKMARK, false);
	}

}
