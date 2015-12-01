package com.itbooks.app.noactivities;

import java.io.File;
import java.util.List;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.itbooks.app.App;
import com.itbooks.db.DB;
import com.itbooks.net.download.Download;

public class Updated3_0Service extends IntentService {

	public static void startFixPre3_0Bugs(Context context) {
		Intent intent = new Intent(context, Updated3_0Service.class);
		context.startService(intent);
	}

	public Updated3_0Service() {
		super("Updated3_0Service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			DB db = DB.getInstance(this);
			List<Download> downloads = db.getDownloads();
			for (Download download : downloads) {
				File localFile = new File(App.Instance.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
						download.getTargetName());
				if (download.getStatus() != DownloadManager.STATUS_SUCCESSFUL) {
					if (localFile.exists()) {
						//The download-status is not success but the file has been loaded.
						//It must be fixed to status in DB.
						download.setStatus(getApplication(), DownloadManager.STATUS_SUCCESSFUL);
					} else {
						//The download-status was set before, but there's no any files exists.
						//The best choice is to delete download-status.
						db.deleteDownload(download.getDownloadId());
					}
				} else {
					if (!localFile.exists()) {
						//The download-status was set successfully, but there's no any files exists.
						//The best choice is to delete download-status.
						db.deleteDownload(download.getDownloadId());
					}
				}
			}
		}
	}
}
