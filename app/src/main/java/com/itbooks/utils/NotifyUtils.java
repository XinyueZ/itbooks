package com.itbooks.utils;


import java.io.File;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat.BigPictureStyle;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.drive.DriveId;
import com.itbooks.R;
import com.itbooks.app.App;
import com.itbooks.app.activities.ConnectGoogleActivity;
import com.itbooks.app.activities.MainActivity;

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
		Builder builder = new Builder(cxt).setWhen(System.currentTimeMillis()).setSmallIcon(icon).setTicker(title)
				.setContentTitle(title).setContentText(desc).addAction(R.drawable.ic_rating, cxt.getString(
						R.string.btn_app_rating), getAppPlayStore(cxt)).setStyle(new BigPictureStyle().bigPicture(image)
						.setBigContentTitle(title)).setAutoCancel(true).setLargeIcon(image);
		builder.setContentIntent(contentIntent);
		ringWorks(cxt, builder);
		mgr.notify(id, builder.build());
	}

	public static void notifyWithoutBigImage(Context cxt, int id, String title, String desc, @DrawableRes int icon,
			PendingIntent contentIntent) {
		NotificationManager mgr = (NotificationManager) cxt.getSystemService(Context.NOTIFICATION_SERVICE);
		Builder builder = new Builder(cxt).setWhen(id).setSmallIcon(icon).setTicker(title).setContentTitle(title)
				.setContentText(desc).addAction(R.drawable.ic_rating, cxt.getString(R.string.btn_app_rating),
						getAppPlayStore(cxt)).setStyle(new BigTextStyle().bigText(desc).setBigContentTitle(title))
				.setAutoCancel(true);
		builder.setContentIntent(contentIntent);
		ringWorks(cxt, builder);
		mgr.notify(id, builder.build());
	}


	public static PendingIntent getAppPlayStore(Context cxt) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
				"https://play.google.com/store/apps/details?id=" + cxt.getPackageName()));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return PendingIntent.getActivity(cxt, com.chopping.utils.Utils.randInt(1, 9999), intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public static PendingIntent getAppHome(Context cxt) {
		Intent intent = new Intent(cxt, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return PendingIntent.getActivity(cxt, com.chopping.utils.Utils.randInt(1, 9999), intent, PendingIntent.FLAG_ONE_SHOT);
	}

	public static PendingIntent getDrive(Context cxt, DriveId id) {
		String to = new StringBuilder().append("https://drive.google.com/drive/folders/").append(id.getResourceId()).toString();
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(to));
		return PendingIntent.getActivity(cxt, com.chopping.utils.Utils.randInt(1, 9999), intent, PendingIntent.FLAG_ONE_SHOT);
	}

	public static PendingIntent getGoogleLogin(Context cxt) {
		Intent intent = new Intent(cxt, ConnectGoogleActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return PendingIntent.getActivity(cxt, com.chopping.utils.Utils.randInt(1, 9999), intent, PendingIntent.FLAG_ONE_SHOT);
	}

	public static PendingIntent getPDFReader(Context cxt, File pdf) {
		PendingIntent contentIntent;
		try {
			Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
			openFileIntent.setDataAndType(Uri.fromFile(pdf), "application/pdf");
			openFileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			contentIntent = PendingIntent.getActivity(cxt, com.chopping.utils.Utils.randInt(1, 9999), openFileIntent,
					PendingIntent.FLAG_ONE_SHOT);
		} catch (Exception ex) {
			//Download pdf-reader.
			String pdfReader = "com.adobe.reader";
			contentIntent = PendingIntent.getActivity(cxt, com.chopping.utils.Utils.randInt(1, 9999), new Intent(Intent.ACTION_VIEW,
							Uri.parse("https://play.google.com/store/apps/details?id=" + pdfReader)),
					PendingIntent.FLAG_ONE_SHOT);
		}
		return contentIntent;
	}
}