package com.itbooks.gcm;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.itbooks.data.rest.RSPushClient;
import com.itbooks.data.rest.RSResult;
import com.itbooks.net.api.Api;
import com.itbooks.net.api.ApiNotInitializedException;
import com.itbooks.utils.DeviceUniqueUtil;
import com.itbooks.utils.Prefs;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Register GCM.
 *
 * @author Xinyue Zhao
 */
public class RegGCMTask extends AsyncTask<Void, Void, String> {
	private GoogleCloudMessaging mGCM;
	private Prefs                mPrefs;
	private String               mDeviceId;

	public RegGCMTask( Context context ) {
		mGCM = GoogleCloudMessaging.getInstance( context );
		mPrefs = Prefs.getInstance( context.getApplicationContext() );
		try {
			mDeviceId = DeviceUniqueUtil.getDeviceIdent( context );
		} catch( NoSuchAlgorithmException e ) {
			//TODO Error when can not get device id.
		}
	}

	@Override
	protected String doInBackground( Void... params ) {
		String regId;
		try {
			regId = mGCM.register( mPrefs.getPushSenderId() + "" );
		} catch( IOException ex ) {
			ex.printStackTrace();
			regId = null;
		}
		return regId;
	}

	@Override
	protected void onPostExecute( final String regId ) {
		mPrefs.setKnownPush( true );
		if( !TextUtils.isEmpty( regId ) ) {
			regOnRemote( regId );
		} else {
			//Keep going
			mPrefs.setPushRegId( null );
			mPrefs.turnOffPush();
		}
	}
	/**
	 * Refresh on server.
	 *
	 * @param regId
	 * 		The registered-id.
	 */
	private void regOnRemote( final String regId ) {
		try {
			Api.regPush( new RSPushClient( mDeviceId, regId ), new Callback<RSResult>() {
				@Override
				public void success( RSResult rsBookList, retrofit.client.Response response ) {
					mPrefs.setPushRegId( regId );
					mPrefs.turnOnPush();
				}

				@Override
				public void failure( RetrofitError error ) {
					regOnRemote( regId );
				}
			} );
		} catch( ApiNotInitializedException e ) {
			//Ignore.
		}
	}
}

