package com.itbooks.utils;

import android.content.Context;

import com.chopping.application.BasicPrefs;

/**
 *
 */
public final class Prefs extends BasicPrefs {
	/**
	 * Impl singleton pattern.
	 */
	private static Prefs sInstance;

	private static final String KEY_LAST_SEARCHED = "key.last.searched";
	private static final String KEY_KNOWN_BOOKMARK = "key.known.bookmark";
	private static final String KEY_KNOWN_PUSH = "key.known.push";
	public static final String KEY_PUSH_REG_ID = "key.push.regid";
	private static final String KEY_APP_VERSION_FOR_PUSH ="key.app.version.push";
	public static final String KEY_PUSH_SETTING = "key.push.setting";
	private static final String KEY_SHOWN_DETAILS_TIMES = "key.details.shown.times";
	private static final String KEY_VIEW_STYLE = "key.view.style";
	private static final String KEY_SHOWN_DETAILS_ADS_TIMES = "ads";
	private static final String KEY_DEVICE_IDENT = "key.device.ident";
	/**
	 * Storage. Whether the "End User License Agreement" has been shown and agreed at application's first start.
	 * <p/>
	 * {@code true} if EULA has been shown and agreed.
	 */
	private static final String KEY_EULA_SHOWN = "key_eula_shown";


	private static final String API_SEARCH_BOOKS = "api_search_books";
	private static final String API_BOOK_DETAIL = "api_book_detail";
	private static final String API_DEFAULT_BOOKS = "api_default_books";
	private static final String REST_API = "rest_api";
	private static final String PUSH_HOST  = "push_host";
	private static final String PUSH_SENDER_ID  = "push_sender_id";

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
	public static Prefs getInstance(Context _context) {
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

	public void setKnownPush(boolean known) {
		setBoolean(KEY_KNOWN_PUSH, known);
	}

	public boolean hasKnownBookmark(){
		return getBoolean(KEY_KNOWN_BOOKMARK, false);
	}


	public boolean hasKnownPush(){
		return getBoolean(KEY_KNOWN_PUSH, false);
	}


	public void setPushRegId(String regId) {
		setString(KEY_PUSH_REG_ID, regId);
	}

	public String getPushRegId() {
		return getString(KEY_PUSH_REG_ID, null);
	}

	public void setAppVerionForPush(String appVersion) {
		setString(KEY_APP_VERSION_FOR_PUSH, appVersion);
	}

	public String getAppVersionForPush() {
		return getString(KEY_APP_VERSION_FOR_PUSH, null);
	}

	private String getPushHost() {
		return getString(PUSH_HOST, null);
	}

	public long getPushSenderId() {
		return getLong(PUSH_SENDER_ID, -1);
	}



	public void turnOnPush() {
		setBoolean(KEY_PUSH_SETTING, true);
	}

	public void setShownDetailsTimes(int times) {
		setInt(KEY_SHOWN_DETAILS_TIMES, times);
	}
	public int getShownDetailsTimes() {
		return getInt(KEY_SHOWN_DETAILS_TIMES, 1);
	}
	public int getShownDetailsAdsTimes() {
		return getInt(KEY_SHOWN_DETAILS_ADS_TIMES, 5);
	}

	public String getRESTApi() {
		return getString(REST_API, null);
	}

	/**
	 * Set view style , default is list.
	 * @param style {@code 1}: grid, {@code 2}:list.
	 */
	public void setViewStyle(int style) {
		setInt(KEY_VIEW_STYLE, style);
	}

	/**
	 *
	 * @return The view style , default is list, {@code 1}: grid, {@code 2}:list.
	 */
	public int getViewStyle() {
		return getInt(KEY_VIEW_STYLE, 2);
	}
	/**
	 * Set device identifier for remote storage.
	 *
	 * @param ident
	 * 		An identifier.
	 */
	public void setDeviceIdent(String ident) {
		setString(KEY_DEVICE_IDENT, ident);
	}

	/**
	 * @return The device identifier for remote storage.
	 */
	public String getDeviceIdent() {
		return getString(KEY_DEVICE_IDENT, null);
	}
}
