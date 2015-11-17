package com.itbooks.net.download;

import java.io.File;
import java.io.IOException;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.os.AsyncTaskCompat;

import com.chopping.utils.Utils;
import com.itbooks.R;
import com.itbooks.app.App;
import com.itbooks.bus.DownloadCompleteEvent;
import com.itbooks.db.DB;
import com.itbooks.utils.NotifyUtils;
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
									NotifyUtils.notifyWithBigImage(App.Instance, (int)System.currentTimeMillis(),
											App.Instance.getString(R.string.application_name), App.Instance.getString(
											R.string.msg_one_book_downloaded), R.drawable.ic_download_notify, image, pi);
								} else {
									NotifyUtils.notifyWithoutBitImage(App.Instance, (int)System.currentTimeMillis(),
											App.Instance.getString(R.string.application_name), App.Instance.getString(
													R.string.msg_one_book_downloaded), R.drawable.ic_download_notify,
											pi);
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


	private static PendingIntent getIntent(Context cxt, File pdf) {
		PendingIntent contentIntent;
		try {
			Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
			openFileIntent.setDataAndType(Uri.fromFile(pdf), "application/pdf");
			openFileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			contentIntent = PendingIntent.getActivity(cxt, Utils.randInt(1, 9999), openFileIntent,
					PendingIntent.FLAG_ONE_SHOT);
		} catch (Exception ex) {
			//Download pdf-reader.
			String pdfReader = "com.adobe.reader";
			contentIntent = PendingIntent.getActivity(cxt, Utils.randInt(1, 9999), new Intent(Intent.ACTION_VIEW,
							Uri.parse("https://play.google.com/store/apps/details?id=" + pdfReader)),
					PendingIntent.FLAG_ONE_SHOT);
		}
		return contentIntent;
	}


}
