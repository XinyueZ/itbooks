package com.itbooks.app;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.chopping.utils.NetworkUtils;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.itbooks.R;
import com.itbooks.app.activities.BookDetailActivity;
import com.itbooks.net.SyncService;
import com.itbooks.utils.NotifyUtils;
import com.itbooks.utils.Prefs;

public final class AppGuardService extends GcmTaskService {
	private static final int NOTIFY_ID = 0x07;

	@Override
	public int onRunTask(TaskParams taskParams) {
		if (!TextUtils.isEmpty(Prefs.getInstance(getApplication()).getGoogleId())) {
			Prefs prefs = Prefs.getInstance(getApplicationContext());
			boolean syncWifi = prefs.syncWifi();
			boolean isWifiOn = NetworkUtils.getCurrentNetworkType(App.Instance) == NetworkUtils.CONNECTION_WIFI;
			if (syncWifi) {
				if (isWifiOn) {
					notifySync(this);
					SyncService.startSync(this);
				}
			} else {
				notifySync(this);
				SyncService.startSync(this);
			}
		}
		return GcmNetworkManager.RESULT_SUCCESS;
	}

	private static void notifySync(Context cxt) {
		Intent intent = new Intent(cxt, BookDetailActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(cxt, com.chopping.utils.Utils.randInt(1, 9999), intent,
				PendingIntent.FLAG_ONE_SHOT);
		NotifyUtils.notifyWithoutBigImage(cxt, NOTIFY_ID, cxt.getString(R.string.application_name), cxt.getString(
				R.string.msg_scheduled_sync_done), android.R.drawable.stat_notify_sync, contentIntent);
	}
}
