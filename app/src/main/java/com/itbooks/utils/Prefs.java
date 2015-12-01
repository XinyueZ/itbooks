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

	private static final String KEY_LAST_SEARCHED           = "key.last.searched";
	private static final String KEY_KNOWN_BOOKMARK          = "key.known.bookmark";
	private static final String KEY_KNOWN_PUSH              = "key.known.push";
	public static final  String KEY_PUSH_REG_ID             = "key.push.regid";
	public static final  String KEY_PUSH_SETTING            = "key.push.setting";
	public static final  String KEY_NO_IMAGES               = "key.no.images";
	public static final  String KEY_SHOW_IMAGES_WIFI        = "key.show.images.wifi";
	public static final  String KEY_SYNC_CHARGING           = "key.sync.charging";
	public static final  String KEY_SYNC_WIFI               = "key.sync.wifi";
	private static final String KEY_SHOWN_DETAILS_TIMES     = "key.details.shown.times";
	private static final String KEY_VIEW_STYLE              = "key.view.style";
	private static final String KEY_SHOWN_DETAILS_ADS_TIMES = "ads";
	private static final String KEY_DEVICE_IDENT            = "key.device.ident";
	private static final String KEY_NEW_API_UPDATED         = "key.new.api.updated";
	private static final String KEY_NEW_3_0_UPDATED         = "key.new.api.updated_3_0";
	/**
	 * Storage. Whether the "End User License Agreement" has been shown and agreed at application's first start.
	 * <p/>
	 * {@code true} if EULA has been shown and agreed.
	 */
	private static final String KEY_EULA_SHOWN              = "key_eula_shown";
	private static final String REST_API                    = "rest_api";
	private static final String PUSH_SENDER_ID              = "push_sender_id";

	/**
	 * Google's ID
	 */
	private static final String KEY_GOOGLE_ID           = "key.google.id";
	/**
	 * The display-name of Google's user.
	 */
	private static final String KEY_GOOGLE_DISPLAY_NAME = "key.google.display.name";
	/**
	 * Url to user's profile-image.
	 */
	private static final String KEY_GOOGLE_THUMB_URL    = "key.google.thumb.url";
	private static final String KEY_LAST_TIME_SYNC      = "key.last.time.sync";
	private static final String KEY_ASK_LOGIN           = "key.ask.login";
	/**
	 * Created a DeviceData storage.
	 *
	 * @param context
	 * 		A context object.
	 */
	private Prefs( Context context ) {
		super( context );
	}

	/**
	 * Get instance of  {@link com.itbooks.utils.Prefs} singleton.
	 *
	 * @param _context
	 * 		{@link android.app.Application}.
	 *
	 * @return The {@link com.itbooks.utils.Prefs} singleton.
	 */
	public static Prefs getInstance( Context _context ) {
		if( sInstance == null ) {
			sInstance = new Prefs( _context );
		}
		return sInstance;
	}


	/**
	 * Whether the "End User License Agreement" has been shown and agreed at application's first start.
	 * <p/>
	 *
	 * @return {@code true} if EULA has been shown and agreed.
	 */
	public boolean isEULAOnceConfirmed() {
		return getBoolean( KEY_EULA_SHOWN, false );
	}

	/**
	 * Set whether the "End User License Agreement" has been shown and agreed at application's first start.
	 * <p/>
	 *
	 * @param isConfirmed
	 * 		{@code true} if EULA has been shown and agreed.
	 */
	public void setEULAOnceConfirmed( boolean isConfirmed ) {
		setBoolean( KEY_EULA_SHOWN, isConfirmed );
	}
	public String getLastSearched() {
		return getString( KEY_LAST_SEARCHED, null );
	}
	public void setLastSearched( String searched ) {
		setString( KEY_LAST_SEARCHED, searched );
	}
	public void setKnownBookmark( boolean known ) {
		setBoolean( KEY_KNOWN_BOOKMARK, known );
	}

	public void setKnownPush( boolean known ) {
		setBoolean( KEY_KNOWN_PUSH, known );
	}

	public boolean hasKnownBookmark() {
		return getBoolean( KEY_KNOWN_BOOKMARK, false );
	}


	public boolean hasKnownPush() {
		return getBoolean( KEY_KNOWN_PUSH, false );
	}
	public String getPushRegId() {
		return getString( KEY_PUSH_REG_ID, null );
	}
	public void setPushRegId( String regId ) {
		setString( KEY_PUSH_REG_ID, regId );
	}
	public long getPushSenderId() {
		return getLong( PUSH_SENDER_ID, -1 );
	}

	public boolean isPushTurnedOn() {
		return getBoolean( KEY_PUSH_SETTING, false );
	}

	public void turnOnPush() {
		setBoolean( KEY_PUSH_SETTING, true );
	}

	public void turnOffPush() {
		setBoolean( KEY_PUSH_SETTING, false );
	}

	public boolean noImages() {
		return getBoolean( KEY_NO_IMAGES, false );
	}


	public boolean showImagesOnlyWifi() {
		return getBoolean( KEY_SHOW_IMAGES_WIFI, false );
	}

	public boolean syncCharging() {
		return getBoolean( KEY_SYNC_CHARGING, true );
	}


	public boolean syncWifi() {
		return getBoolean( KEY_SYNC_WIFI, true );
	}
	public int getShownDetailsTimes() {
		return getInt( KEY_SHOWN_DETAILS_TIMES, 1 );
	}
	public void setShownDetailsTimes( int times ) {
		setInt( KEY_SHOWN_DETAILS_TIMES, times );
	}
	public int getShownDetailsAdsTimes() {
		return getInt( KEY_SHOWN_DETAILS_ADS_TIMES, 5 );
	}

	public String getRESTApi() {
		return getString( REST_API, null );
	}
	/**
	 * @return The view style , default is list, {@code 1}: grid, {@code 2}:list.
	 */
	public int getViewStyle() {
		return getInt( KEY_VIEW_STYLE, 2 );
	}
	/**
	 * Set view style , default is list.
	 *
	 * @param style
	 * 		{@code 1}: grid, {@code 2}:list.
	 */
	public void setViewStyle( int style ) {
		setInt( KEY_VIEW_STYLE, style );
	}
	/**
	 * @return The device identifier for remote storage.
	 */
	public String getDeviceIdent() {
		return getString( KEY_DEVICE_IDENT, null );
	}
	/**
	 * Set device identifier for remote storage.
	 *
	 * @param ident
	 * 		An identifier.
	 */
	public void setDeviceIdent( String ident ) {
		setString( KEY_DEVICE_IDENT, ident );
	}
	/**
	 * @return {@code true} if user has ran the application with new-api before.
	 */
	public boolean isNewApiUpdated() {
		return getBoolean( KEY_NEW_API_UPDATED, true );
	}

	/**
	 * Set flag to indicate that user run and has ran the new-api version.
	 *
	 * @param updated
	 * 		{@code true} if new-api version started.
	 */
	public void setNewApiUpdated( boolean updated ) {
		setBoolean( KEY_NEW_API_UPDATED, updated );
	}


	/**
	 * @return {@code true} if user has ran the application with 3.0 before.
	 */
	public boolean isUpdated3_0() {
		return getBoolean( KEY_NEW_3_0_UPDATED, true );
	}

	/**
	 * Set flag to indicate that user run and has ran the 3.0 version
	 *
	 * @param updated
	 * 		{@code true} if new-api version started.
	 */
	public void setUpdated3_0( boolean updated ) {
		setBoolean( KEY_NEW_3_0_UPDATED, updated );
	}
	/**
	 * Google's ID
	 */
	public String getGoogleId() {
		return getString( KEY_GOOGLE_ID, null );
	}
	/**
	 * Google's ID
	 */
	public void setGoogleId( String id ) {
		setString( KEY_GOOGLE_ID, id );
	}
	/**
	 * The display-name of Google's user.
	 */
	public String getGoogleDisplyName() {
		return getString( KEY_GOOGLE_DISPLAY_NAME, null );
	}
	/**
	 * The display-name of Google's user.
	 */
	public void setGoogleDisplyName( String displayName ) {
		setString( KEY_GOOGLE_DISPLAY_NAME, displayName );
	}
	/**
	 * Url to user's profile-image.
	 */
	public String getGoogleThumbUrl() {
		return getString( KEY_GOOGLE_THUMB_URL, null );
	}
	/**
	 * The display-name of Google's user.
	 */
	public void setGoogleThumbUrl( String thumbUrl ) {
		setString( KEY_GOOGLE_THUMB_URL, thumbUrl );
	}
	/**
	 * Time to last sync-point.
	 */
	public long getLastTimeSync() {
		return getLong( KEY_LAST_TIME_SYNC, -1 );
	}
	/**
	 * Set the time to last sync-point.
	 */
	public void setLastTimeSync( long time ) {
		setLong( KEY_LAST_TIME_SYNC, time );
	}
	/**
	 * Set whether has asked login benefit.
	 */
	public void setAskLogin( boolean asked ) {
		setBoolean( KEY_ASK_LOGIN, asked );
	}

	/**
	 * Whether has asked login benefit.
	 */
	public boolean askLogin() {
		return getBoolean( KEY_ASK_LOGIN, false );
	}
}
