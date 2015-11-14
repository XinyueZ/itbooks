package com.itbooks.net;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ServiceCompat;
import android.text.TextUtils;
import android.util.Log;

import com.chopping.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveResource.MetadataResult;
import com.google.android.gms.drive.MetadataChangeSet;
import com.itbooks.R;
import com.itbooks.app.App;
import com.itbooks.utils.Prefs;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class SyncService extends Service implements ConnectionCallbacks, OnConnectionFailedListener {
	private static final String TAG = SyncService.class.getSimpleName();
	/**
	 * Google Driver access client.
	 */
	private volatile GoogleApiClient mGoogleApiClient;

	public SyncService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		establishGoogleDriver();
		return ServiceCompat.START_STICKY;
	}

	//SYNC JOB FOR ALL DOWNLOADED PDF-FILES.
	private void sync() {
		if (!TextUtils.isEmpty(Prefs.getInstance(App.Instance).getGoogleId()) && mGoogleApiClient != null) {

			File downloadsDir = App.Instance.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
			String[] pdfs = downloadsDir.list();
			if (pdfs != null && pdfs.length > 0) {
				for (String file : pdfs) {
					try {
						DriveContentsResult driveContentsResult = Drive.DriveApi.newDriveContents(mGoogleApiClient)
								.await();
						if (driveContentsResult.getStatus().isSuccess()) {
							DriveContents contents = driveContentsResult.getDriveContents();
							OutputStream driverStream = contents.getOutputStream();//Write data on stream is fine.
							driverStream.write(IOUtils.toByteArray(FileUtils.openInputStream(new File(downloadsDir,
									file))));
							MetadataChangeSet createdFileMeta = new MetadataChangeSet.Builder().setMimeType(
									"application/pdf").setTitle(file).build();
							// Create the file in the root folder, again calling await() to
							// block until the request finishes.
							DriveFolder rootFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
							DriveFileResult fileResult = rootFolder.createFile(mGoogleApiClient, createdFileMeta,
									contents).await();
							if (fileResult.getStatus().isSuccess()) {
								// Finally, fetch the metadata for the newly created file, again
								// calling await to block until the request finishes.
								MetadataResult newFileMeta = fileResult.getDriveFile().getMetadata(mGoogleApiClient)
										.await();
								if (newFileMeta.getStatus().isSuccess()) {
									Log.i(TAG, "File has been sync: " + file);
								} else {
									//Error.
									Log.e(TAG, "File can not be found: " + file);
								}
							} else {
								//Error.
								Log.e(TAG, "File can not be created: " + file);
							}
						} else {
							//Error.
							Log.i(TAG, "Google Driver can not be found.");
						}
					} catch (IOException e1) {
						//Error.
						Log.e(TAG, "File can not be sync: " + file);
					}
				}
			}
		}
	}


	/**
	 * When user logined, user can access Google Driver and save downloaded files. Here to establish connection.
	 */
	private void establishGoogleDriver() {
		if (!TextUtils.isEmpty(Prefs.getInstance(App.Instance).getGoogleId()) && mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Drive.API).addScope(Drive.SCOPE_FILE)
					.addOnConnectionFailedListener(this).addConnectionCallbacks(this).build();
		}
		// Connect the client. Once connected, the camera is launched.
		if (!TextUtils.isEmpty(Prefs.getInstance(App.Instance).getGoogleId()) && mGoogleApiClient != null) {
			mGoogleApiClient.connect();
		}
	}

	/**
	 * When user logined, user can access Google Driver and save downloaded files. Here to release connection.
	 */
	private synchronized void disestablishGoogleDriver() {
		if (!TextUtils.isEmpty(Prefs.getInstance(App.Instance).getGoogleId()) &&
				mGoogleApiClient != null &&
				mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
			mGoogleApiClient = null;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		disestablishGoogleDriver();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Utils.showLongToast(App.Instance, R.string.msg_access_driver_failed);
	}

	@Override
	public void onConnected(Bundle bundle) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				sync();
				disestablishGoogleDriver();
			}
		}).start();
	}

	@Override
	public void onConnectionSuspended(int i) {

	}
}
