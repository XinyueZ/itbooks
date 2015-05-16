package com.itbooks.net.download;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.itbooks.db.DB;


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
					break;
				case DownloadManager.STATUS_FAILED:
					download.failed(context);
					break;
				}
			}
		}
	}
}
