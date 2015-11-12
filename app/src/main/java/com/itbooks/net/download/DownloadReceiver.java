package com.itbooks.net.download;

import java.io.File;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;

import com.itbooks.R;
import com.itbooks.app.App;
import com.itbooks.bus.DownloadCompleteEvent;
import com.itbooks.db.DB;

import de.greenrobot.event.EventBus;


/**
 * Listener for downloading finished.
 *
 * @author Xinyue Zhao
 */
public final class DownloadReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
		Download download = DB.getInstance(context).getDownload(downloadId);
		if (download != null) {
			DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			DownloadManager.Query query = new DownloadManager.Query();
			query.setFilterById(downloadId);
			Cursor cursor = downloadManager.query(query);
			if (cursor.moveToFirst()) {
				int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
				int status = cursor.getInt(columnIndex);
				download.setStatus(context, status);
				switch (status) {
				case DownloadManager.STATUS_SUCCESSFUL:
					download.end(context);

					File to = new File(App.Instance.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
							download.getTargetName());
					if (to.exists()) {
						notify(context, context.getString(R.string.application_name), context.getString(
								R.string.msg_one_book_downloaded),
								getIntent(context, to));
					}

					break;
				case DownloadManager.STATUS_FAILED:
					download.failed();
					break;
				}
			}
		}
		EventBus.getDefault().post(new DownloadCompleteEvent(download));
	}


	private static void notify(Context cxt, String title, String desc, PendingIntent contentIntent) {
		NotificationManager mgr = (NotificationManager) cxt.getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(cxt).setWhen(System.currentTimeMillis())
				.setSmallIcon(
				R.drawable.ic_download).setTicker(title).setContentTitle(title).setContentText(desc).setStyle(
				new BigTextStyle().bigText(desc).setBigContentTitle(title)).setAutoCancel(true);
		builder.setContentIntent(contentIntent);
		mgr.notify((int) System.currentTimeMillis(), builder.build());
	}


	public static PendingIntent getIntent(Context cxt, File pdf) {
		PendingIntent contentIntent;
		try {
			Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
			openFileIntent.setDataAndType(Uri.fromFile(pdf), "application/pdf");
			openFileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			contentIntent = PendingIntent.getActivity(cxt, (int) System.currentTimeMillis(), openFileIntent,
					PendingIntent.FLAG_ONE_SHOT);
		} catch (Exception ex) {
			//Download pdf-reader.
			String pdfReader = "com.adobe.reader";
			contentIntent = PendingIntent.getActivity(cxt, (int) System.currentTimeMillis(), new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + pdfReader)),
					PendingIntent.FLAG_ONE_SHOT);
		}
		return contentIntent;
	}
}
