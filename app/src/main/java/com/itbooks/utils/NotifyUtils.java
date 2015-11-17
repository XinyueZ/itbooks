package com.itbooks.utils;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat.BigPictureStyle;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.ContextCompat;

import com.itbooks.R;
import com.itbooks.app.App;

public final class NotifyUtils {
	private static void ringWorks(Context cxt, Builder builder) {
		AudioManager audioManager = (AudioManager) App.Instance.getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
			builder.setVibrate(new long[] { 1000, 1000, 1000, 1000 });
			builder.setSound(Uri.parse(String.format("android.resource://%s/%s", cxt.getPackageName(), R.raw.signal)));
		}
		builder.setLights(ContextCompat.getColor(App.Instance, R.color.primary_color), 1000, 1000);
	}

	public static void notifyWithBigImage(Context cxt, int id, String title, String desc, @DrawableRes int icon,
			Bitmap image, PendingIntent contentIntent) {
		NotificationManager mgr = (NotificationManager) cxt.getSystemService(Context.NOTIFICATION_SERVICE);
		Builder builder = new Builder(cxt).setWhen(System.currentTimeMillis())
				.setSmallIcon(icon).setTicker(title).setContentTitle(title).setContentText(
						desc).setStyle(new BigPictureStyle().bigPicture(image).setBigContentTitle(title)).setAutoCancel(
						true).setLargeIcon(image);
		builder.setContentIntent(contentIntent);
		ringWorks(cxt, builder);
		mgr.notify(id, builder.build());
	}

	public static void notifyWithoutBitImage(Context cxt, int id, String title, String desc, @DrawableRes int icon,
			PendingIntent contentIntent) {
		NotificationManager mgr = (NotificationManager) cxt.getSystemService(Context.NOTIFICATION_SERVICE);
		Builder builder = new Builder(cxt).setWhen(id).setSmallIcon(
				icon	).setTicker(title).setContentTitle(title).setContentText(desc).setStyle(
				new BigTextStyle().bigText(desc).setBigContentTitle(title)).setAutoCancel(true);
		builder.setContentIntent(contentIntent);
		ringWorks(cxt, builder);
		mgr.notify(id, builder.build());
	}
}
