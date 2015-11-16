package com.itbooks.app;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.text.TextUtils;

import com.chopping.utils.NetworkUtils;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.itbooks.R;
import com.itbooks.app.activities.BookDetailActivity;
import com.itbooks.net.SyncService;
import com.itbooks.utils.Prefs;

public final class AppGuardService extends GcmTaskService {

	@Override
	public int onRunTask(TaskParams taskParams) {
		if (!TextUtils.isEmpty(Prefs.getInstance(getApplication()).getGoogleId())) {
			Prefs prefs = Prefs.getInstance(getApplicationContext());
			boolean syncWifi = prefs.syncWifi();
			boolean isWifiOn = NetworkUtils.getCurrentNetworkType(App.Instance) == NetworkUtils.CONNECTION_WIFI;
			if (syncWifi) {
				if (isWifiOn) {
					SyncService.startSync(this);
					notifySync(this);
				}
			} else {
				SyncService.startSync(this);
				notifySync(this);
			}
		}
		return GcmNetworkManager.RESULT_SUCCESS;
	}

	public static void notifySync(Context cxt) {
		Intent intent = new Intent(cxt, BookDetailActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		 PendingIntent contentIntent = PendingIntent.getActivity(cxt, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT);
		notify(cxt, cxt.getString(R.string.application_name), cxt.getString(R.string.msg_scheduled_sync_done), contentIntent);
	}

	private static void notify(Context cxt, String title, String desc, PendingIntent contentIntent) {
		NotificationManager mgr = (NotificationManager) cxt.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(cxt).setWhen(System.currentTimeMillis())
				.setSmallIcon(
						R.drawable.ic_background_sync).setTicker(title).setContentTitle(title).setContentText(desc).setStyle(
						new BigTextStyle().bigText(desc).setBigContentTitle(title)).setAutoCancel(true);
		builder.setContentIntent(contentIntent);
		mgr.notify((int) System.currentTimeMillis(), builder.build());
	}
}
