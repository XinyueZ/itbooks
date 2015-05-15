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
public   class UnregGCMTask extends AsyncTask<Void, Void, String> {
	private GoogleCloudMessaging mGCM;
	private Prefs mPrefs;
	private String mDeviceId;

	public UnregGCMTask(Context context) {
		mGCM = GoogleCloudMessaging.getInstance(context);
		mPrefs = Prefs.getInstance(context.getApplicationContext());
		try {
			mDeviceId = DeviceUniqueUtil.getDeviceIdent(context);
		} catch (NoSuchAlgorithmException e) {
			//TODO Error when can not get device id.
		}
	}

	@Override
	protected String doInBackground(Void... params) {
		String regId;
		try {
			mGCM.unregister();
			regId = mPrefs.getPushRegId();
		} catch (IOException ex) {
			regId = null;
		}
		return regId;
	}

	@Override
	protected void onPostExecute(final String regId) {
		if (!TextUtils.isEmpty(regId)) {
			unregOnRemote(regId);
		}
	}

	/**
	 * Refresh on server.
	 * @param regId The registered-id.
	 */
	private void unregOnRemote(final String regId) {
		try {
			Api.unregPush(new RSPushClient(mDeviceId, regId), new Callback<RSResult>() {
				@Override
				public void success(RSResult rsBookList, retrofit.client.Response response) {
					mPrefs.setPushRegId(null);
				}

				@Override
				public void failure(RetrofitError error) {
					unregOnRemote(regId);
				}
			});
		} catch (ApiNotInitializedException e) {
			//Ignore.
		}
	}
}

