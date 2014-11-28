package com.itbooks.gcm;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.chopping.net.TaskHelper;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.itbooks.utils.ParallelTask;
import com.itbooks.utils.Prefs;

/**
 * Register GCM.
 *
 * @author Xinyue Zhao
 */
public   class UnregGCMTask extends ParallelTask<Void, Void, String> {
	private GoogleCloudMessaging mGCM;
	private Prefs mPrefs;


	public UnregGCMTask(Context context) {
		mGCM = GoogleCloudMessaging.getInstance(context);
		mPrefs = Prefs.getInstance(context.getApplicationContext());
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
			StringRequest req = new StringRequest(Request.Method.POST, mPrefs.getPushBackendUnregUrl(),
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						mPrefs.setPushRegId(null);
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						mPrefs.setPushRegId(null);
					}
			}) {
				@Override
				public Map<String, String> getHeaders() throws AuthFailureError {
					Map<String, String> headers = super.getHeaders();
					if (headers == null || headers.equals(Collections.emptyMap())) {
						headers = new HashMap<String, String>();
					}
					headers.put("Cookie","pushID=" + regId);
					return headers;
				}
			};
			TaskHelper.getRequestQueue().add(req);
		}
	}
}

