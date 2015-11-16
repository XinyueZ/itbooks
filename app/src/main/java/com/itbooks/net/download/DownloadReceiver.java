package com.itbooks.net.download;

import java.io.File;
import java.io.IOException;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigPictureStyle;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.AsyncTaskCompat;

import com.chopping.utils.Utils;
import com.itbooks.R;
import com.itbooks.app.App;
import com.itbooks.bus.DownloadCompleteEvent;
import com.itbooks.db.DB;
import com.squareup.picasso.Picasso;

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
					AsyncTaskCompat.executeParallel(new AsyncTask<Download, Void, Bitmap>() {
						private Download mDownload;

						@Override
						protected Bitmap doInBackground(Download... params) {
							mDownload = params[0];
							Picasso picasso = Picasso.with(App.Instance);
							try {
								return picasso.load(Utils.uriStr2URI(mDownload.getCoverUrl()).toASCIIString()).get();
							} catch (IOException e) {
								return null;
							}
						}

						@Override
						protected void onPostExecute(Bitmap image) {
							super.onPostExecute(image);
							File to = new File(App.Instance.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
									mDownload.getTargetName());
							if (to.exists()) {
								PendingIntent pi = getIntent(App.Instance, to);
								if (image != null) {
									notifyDownloadCompleted(App.Instance, System.currentTimeMillis(),
											App.Instance.getString(R.string.application_name), App.Instance.getString(
													R.string.msg_one_book_downloaded), image, pi);
								} else {
									fallbackNotify(App.Instance, System.currentTimeMillis(), App.Instance.getString(
											R.string.application_name), App.Instance.getString(
											R.string.msg_one_book_downloaded), pi);
								}
							}
						}
					}, download);


					break;
				case DownloadManager.STATUS_FAILED:
					download.failed();
					break;
				}
			}
		}
		EventBus.getDefault().post(new DownloadCompleteEvent(download));
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
			contentIntent = PendingIntent.getActivity(cxt, (int) System.currentTimeMillis(), new Intent(
							Intent.ACTION_VIEW, Uri.parse(
							"https://play.google.com/store/apps/details?id=" + pdfReader)),
					PendingIntent.FLAG_ONE_SHOT);
		}
		return contentIntent;
	}

	private static void ringWorks(Context cxt, Builder builder) {
		AudioManager audioManager = (AudioManager) App.Instance.getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
			builder.setVibrate(new long[] { 1000, 1000, 1000, 1000 });
			builder.setSound(Uri.parse(String.format("android.resource://%s/%s", cxt.getPackageName(), R.raw.signal)));
		}
		builder.setLights(ContextCompat.getColor(App.Instance, R.color.primary_color), 1000, 1000);
	}

	private static void notifyDownloadCompleted(Context cxt, long id, String title, String desc, Bitmap image,
			PendingIntent contentIntent) {
		NotificationManager mgr = (NotificationManager) cxt.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(cxt).setWhen(id).setSmallIcon(
				R.drawable.ic_download).setTicker(title).setContentTitle(title).setContentText(desc).setStyle(
				new BigPictureStyle().bigPicture(image).setBigContentTitle(title)).setAutoCancel(true).setLargeIcon(
				image);
		builder.setContentIntent(contentIntent);
		ringWorks(cxt, builder);
		mgr.notify(Utils.randInt(1, 9999), builder.build());
	}


	private void fallbackNotify(Context cxt, long id, String title, String desc, PendingIntent contentIntent) {
		NotificationManager mgr = (NotificationManager) cxt.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(cxt).setWhen(id).setSmallIcon(
				R.drawable.ic_download).setTicker(title).setContentTitle(title).setContentText(desc).setStyle(
				new BigTextStyle().bigText(desc).setBigContentTitle(title)).setAutoCancel(true);
		builder.setContentIntent(contentIntent);
		ringWorks(cxt, builder);
		mgr.notify(Utils.randInt(1, 9999), builder.build());
	}
}
