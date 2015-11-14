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
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.DriveResource.MetadataResult;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
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
	public static final String EXTRAS_ERROR_RESULT = SyncService.class.getName() + ".EXTRAS.error_result";
	public static final String ACTION_CONNECT_ERROR = SyncService.class.getName() + ".ACTION.SyncService.connect_error";

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
			DriveId folderId = null;
			DriveFolder itBooksFolder;
			String folderName = "itbooks";

			// Create the file in the "itbooks" folder, again calling await() to
			// block until the request finishes.
			DriveFolder rootFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
			Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TRASHED, false)).addFilter(
					Filters.eq(SearchableField.TITLE, folderName)).addFilter(Filters.eq(SearchableField.MIME_TYPE,
					DriveFolder.MIME_TYPE)).build();
			MetadataBufferResult folderResult = rootFolder.queryChildren(mGoogleApiClient, query).await();
//			MetadataBufferResult folderResult = rootFolder.listChildren(mGoogleApiClient).await();
			if (folderResult.getStatus().isSuccess()) {
				MetadataBuffer metadataBuffer = folderResult.getMetadataBuffer();
				for (Metadata metaData : metadataBuffer) {
					if (metaData.isFolder() && TextUtils.equals(folderName, metaData.getTitle())) {
						folderId = metaData.getDriveId();
						break;
					}
				}
				metadataBuffer.close();
			}

			if (folderId == null) {
				Log.i(TAG, "Can not find folder and try to create new one: " + folderName);
				//Not success, then create a new one.
				MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(folderName).build();
				DriveFolderResult folderCreatedResult = rootFolder.createFolder(mGoogleApiClient, changeSet).await();
				if (folderCreatedResult.getStatus().isSuccess()) {
					itBooksFolder = folderCreatedResult.getDriveFolder();
					DriveResource.MetadataResult checkFolderResult = itBooksFolder.getMetadata(mGoogleApiClient)
							.await();
					if (!checkFolderResult.getStatus().isSuccess()) {
						//Error
						Log.e(TAG, "Can not get newly created folder: " + folderName);
						return;
					} else {
						Log.i(TAG, "Created successfully dir: " + folderName);
					}
				} else {
					//Error
					Log.e(TAG, "Can not create folder: " + folderName);
					return;
				}
			} else {
				Log.i(TAG, "Dir exist: " + folderName);
				itBooksFolder = folderId.asDriveFolder();
			}


			String mimeType = "application/pdf";
			File downloadsDir = App.Instance.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
			String[] pdfs = downloadsDir.list();
			if (pdfs != null && pdfs.length > 0) {
				for (String file : pdfs) {
					boolean fileExist = false;
					MetadataBufferResult fileBufferResult = itBooksFolder.listChildren(mGoogleApiClient).await();
					if (fileBufferResult.getStatus().isSuccess()) {
						MetadataBuffer metadataBuffer = fileBufferResult.getMetadataBuffer();
						for (Metadata metaData : metadataBuffer) {
							if (!metaData.isFolder() && TextUtils.equals(file, metaData.getTitle())) {
								fileExist = true;
								break;
							}
						}
						metadataBuffer.close();
					}

					if (fileExist) {
						Log.i(TAG, "File already sync: " + file);
					} else {
						try {
							Log.i(TAG, "Start sync: " + file);
							DriveContentsResult driveContentsResult = Drive.DriveApi.newDriveContents(mGoogleApiClient)
									.await();
							if (driveContentsResult.getStatus().isSuccess()) {
								DriveContents contents = driveContentsResult.getDriveContents();
								OutputStream driverStream = contents.getOutputStream();//Write data on stream is fine.
								driverStream.write(IOUtils.toByteArray(FileUtils.openInputStream(new File(downloadsDir,
										file))));
								MetadataChangeSet createdFileMeta = new MetadataChangeSet.Builder().setMimeType(
										mimeType).setTitle(file).build();
								DriveFileResult fileResult = itBooksFolder.createFile(mGoogleApiClient, createdFileMeta,
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
		Intent intent = new Intent(ACTION_CONNECT_ERROR);
		intent.putExtra(EXTRAS_ERROR_RESULT, connectionResult);
		LocalBroadcastManager.getInstance(App.Instance).sendBroadcast(intent);
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
