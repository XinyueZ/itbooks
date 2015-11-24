package com.itbooks.net;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ServiceCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.chopping.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.DriveResource.MetadataResult;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.itbooks.R;
import com.itbooks.app.App;
import com.itbooks.data.rest.RSBook;
import com.itbooks.db.DB;
import com.itbooks.net.download.Download;
import com.itbooks.utils.NotifyUtils;
import com.itbooks.utils.Prefs;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class SyncService extends Service implements ConnectionCallbacks, OnConnectionFailedListener {
	private static final String TAG = SyncService.class.getSimpleName();
	private volatile GoogleApiClient mGoogleApiClient;
	public static final String EXTRAS_ERROR_RESULT = SyncService.class.getName() + ".EXTRAS.error_result";
	private static final String EXTRAS_LIST_DELETE = SyncService.class.getName() + ".EXTRAS.list_delete";
	private static final String ACTION_SYNC_ONLY = SyncService.class.getName() + ".EXTRAS.sync_only";
	private static final String ACTION_SYNC_DEL = SyncService.class.getName() + ".EXTRAS.sync_del";
	public static final String ACTION_CONNECT_ERROR = SyncService.class.getName() + ".ACTION.SyncService.connect_error";
	public static final String ACTION_SYNC_BEGIN = SyncService.class.getName() + ".ACTION.SyncService.sync_begin";
	public static final String ACTION_SYNC_END = SyncService.class.getName() + ".ACTION.SyncService.sync_end";
	private static final String FOLDER_NAME = "itbooks";
	private static final String MIME_TYPE = "application/pdf";
	private static final String BOOK_NAME = "book_name";
	private static final String BOOK_AUTHOR = "book_author";
	private static final String BOOK_SIZE = "book_size";
	private static final String BOOK_PAGES = "book_pages";
	private static final String BOOK_LINK = "book_link";
	private static final String BOOK_ISBN = "book_isbn";
	private static final String BOOK_YEAR = "book_year";
	private static final String BOOK_PUBLISHER = "book_publisher";
	private static final String BOOK_COVER_1 = "book_cover_1";
	private static final String BOOK_COVER_2 = "book_cover_2";

	private static final int NOTIFY_ERROR_DEL_ID = 0x06;
	private static final int NOTIFY_ERROR_PUSH_ID = 0x16;
	private static final int NOTIFY_ERROR_PULL_ID = 0x26;
	public static final int NOTIFY_REQ_LOGIN = 0x27;

	private boolean mCmdSyncOnly = true;
	private long[] mDownloadDelList = null;


	/**
	 * Sync can be continued to use when limit's passed.
	 */
	//private static final long SYNC_LIMIT = 60000;
	private static final long SYNC_LIMIT =  AlarmManager.INTERVAL_FIFTEEN_MINUTES;

	public static void startSync(Context cxt) {
		Intent intent = new Intent(cxt, SyncService.class);
		intent.setAction(ACTION_SYNC_ONLY);
		cxt.startService(intent);
	}

	public static void startSyncDel(Context cxt, List<Download> downloadsList) {
		long[] downloadIds = new long[downloadsList.size()];
		int i = 0;
		for(Download download : downloadsList) {
			downloadIds[i++] = download.getDownloadId();
		}
		Intent intent = new Intent(cxt, SyncService.class);
		intent.setAction(ACTION_SYNC_DEL);
		intent.putExtra(EXTRAS_LIST_DELETE, downloadIds);
		cxt.startService(intent);
	}


	private static DriveId getITBooksFolderId(GoogleApiClient client) {
		DriveId folderId = null;
		// Create the file in the "itbooks" folder, again calling await() to
		// block until the request finishes.
		DriveFolder rootFolder = Drive.DriveApi.getRootFolder(client);
		Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TRASHED, false)).addFilter(Filters.eq(
				SearchableField.TITLE, FOLDER_NAME)).addFilter(Filters.eq(SearchableField.MIME_TYPE,
				DriveFolder.MIME_TYPE)).build();
		MetadataBufferResult folderResult = rootFolder.queryChildren(client, query).await();
		//			MetadataBufferResult folderResult = rootFolder.listChildren(mGoogleApiClient).await();
		if (folderResult.getStatus().isSuccess()) {
			MetadataBuffer metadataBuffer = folderResult.getMetadataBuffer();
			for (Metadata metaData : metadataBuffer) {
				if (metaData.isFolder() && TextUtils.equals(FOLDER_NAME, metaData.getTitle())) {
					folderId = metaData.getDriveId();
					break;
				}
			}
			metadataBuffer.close();
		}
		return folderId;
	}

	private static DriveFolder getITBooksFolder(GoogleApiClient client, DriveId folderId) {
		DriveFolder itBooksFolder = null;
		if (folderId == null) {
			Log.i(TAG, "Can not find folder and try to create new one: " + FOLDER_NAME);
			//Not success, then create a new one.
			DriveFolder rootFolder = Drive.DriveApi.getRootFolder(client);
			MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(FOLDER_NAME).build();
			DriveFolderResult folderCreatedResult = rootFolder.createFolder(client, changeSet).await();
			if (folderCreatedResult.getStatus().isSuccess()) {
				itBooksFolder = folderCreatedResult.getDriveFolder();
				DriveResource.MetadataResult checkFolderResult = itBooksFolder.getMetadata(client).await();
				if (!checkFolderResult.getStatus().isSuccess()) {
					//Error
					Log.e(TAG, "Can not get newly created folder: " + FOLDER_NAME);
				} else {
					Log.i(TAG, "Created successfully dir: " + FOLDER_NAME);
				}
			} else {
				//Error
				Log.e(TAG, "Can not create folder: " + FOLDER_NAME);
			}
		} else {
			Log.i(TAG, "Dir exist: " + FOLDER_NAME);
			itBooksFolder = folderId.asDriveFolder();
		}
		return itBooksFolder;
	}

	private static void pushFiles(GoogleApiClient client, DriveFolder itBooksFolder) {
		if (itBooksFolder != null) {
			File downloadsDir = App.Instance.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
			List<Download> downloads = DB.getInstance(App.Instance).getDownloads();
			int downloadTotal = downloads.size();
			if (downloadTotal > 0) {
				for (Download download : downloads) {
					boolean fileExist = false;
					MetadataBufferResult fileBufferResult = itBooksFolder.listChildren(client).await();
					if (fileBufferResult.getStatus().isSuccess()) {
						MetadataBuffer metadataBuffer = fileBufferResult.getMetadataBuffer();
						for (Metadata metaData : metadataBuffer) {
							if (!metaData.isFolder() && TextUtils.equals(download.getTargetName(),
									metaData.getTitle())) {
								fileExist = true;
								break;
							}
						}
						metadataBuffer.close();
					}

					if (fileExist) {
						Log.w(TAG, "File already pushed: " + download.getTargetName());
					} else {
						try {
							Log.i(TAG, "Start pushing: " + download.getTargetName());
							DriveContentsResult driveContentsResult = Drive.DriveApi.newDriveContents(client).await();
							if (driveContentsResult.getStatus().isSuccess()) {
								DriveContents contents = driveContentsResult.getDriveContents();
								OutputStream driverStream = contents.getOutputStream();//Write data on stream is fine.
								driverStream.write(IOUtils.toByteArray(FileUtils.openInputStream(new File(downloadsDir,
										download.getTargetName()))));


								CustomPropertyKey bookName = new CustomPropertyKey(BOOK_NAME, CustomPropertyKey.PUBLIC);
								CustomPropertyKey bookAuthor = new CustomPropertyKey(BOOK_AUTHOR,
										CustomPropertyKey.PUBLIC);
								CustomPropertyKey bookSize = new CustomPropertyKey(BOOK_SIZE, CustomPropertyKey.PUBLIC);
								CustomPropertyKey bookPages = new CustomPropertyKey(BOOK_PAGES,
										CustomPropertyKey.PUBLIC);
								CustomPropertyKey bookLink = new CustomPropertyKey(BOOK_LINK, CustomPropertyKey.PUBLIC);
								CustomPropertyKey bookISBN = new CustomPropertyKey(BOOK_ISBN, CustomPropertyKey.PUBLIC);
								CustomPropertyKey bookYear = new CustomPropertyKey(BOOK_YEAR, CustomPropertyKey.PUBLIC);
								CustomPropertyKey bookPublisher = new CustomPropertyKey(BOOK_PUBLISHER,
										CustomPropertyKey.PUBLIC);
								CustomPropertyKey bookCover1 = new CustomPropertyKey(BOOK_COVER_1,
										CustomPropertyKey.PUBLIC);
								CustomPropertyKey bookCover2 = new CustomPropertyKey(BOOK_COVER_2,
										CustomPropertyKey.PUBLIC);
								String coverUrl =  download.getCoverUrl();
								int len = coverUrl.length();
								String s1 = coverUrl.substring(0, len / 2);
								String s2 = coverUrl.substring(len / 2);

								MetadataChangeSet createdFileMeta = new MetadataChangeSet.Builder().setMimeType(
										MIME_TYPE).setTitle(download.getTargetName()).setCustomProperty(bookName,
										download.getName()).setCustomProperty(bookAuthor, download.getAuthor())
										.setCustomProperty(bookSize, download.getSize()).setCustomProperty(bookPages,
												download.getPages()).setCustomProperty(bookLink, download.getLink())
										.setCustomProperty(bookISBN, download.getISBN()).setCustomProperty(bookYear,
												download.getYear()).setCustomProperty(bookPublisher,
												download.getPublisher()).setCustomProperty(bookCover1,
												s1).setCustomProperty(bookCover2,
												s2).setDescription(download.getDescription())
										.build();
								DriveFileResult fileResult = itBooksFolder.createFile(client, createdFileMeta, contents)
										.await();
								if (fileResult.getStatus().isSuccess()) {
									// Finally, fetch the metadata for the newly created file, again
									// calling await to block until the request finishes.
									MetadataResult newFileMeta = fileResult.getDriveFile().getMetadata(client).await();
									if (newFileMeta.getStatus().isSuccess()) {
										try {
											Bitmap image =  Picasso.with(App.Instance).load(Utils.uriStr2URI(
													download.getCoverUrl()).toASCIIString()).get();
											NotifyUtils.notifyWithBigImage(App.Instance,
													(int) System.currentTimeMillis(), App.Instance.getString(
															R.string.application_name), App.Instance.getString(
															R.string.msg_push_success) + download.getTargetName(),
													android.R.drawable.stat_notify_sync, image, NotifyUtils.getDrive(
															App.Instance, itBooksFolder.getDriveId()));
										} catch (IOException e) {
											NotifyUtils.notifyWithoutBigImage(App.Instance,
													(int) System.currentTimeMillis(), App.Instance.getString(
															R.string.application_name), App.Instance.getString(
															R.string.msg_push_success) + download.getTargetName(),
													android.R.drawable.stat_notify_sync, NotifyUtils.getDrive(
															App.Instance, itBooksFolder.getDriveId()));
										}
									} else {
										NotifyUtils.notifyWithoutBigImage(App.Instance, NOTIFY_ERROR_PUSH_ID,
												App.Instance.getString(R.string.application_name),
												App.Instance.getString(R.string.msg_push_failed),
												android.R.drawable.stat_notify_error, NotifyUtils.getDrive(App.Instance,
														itBooksFolder.getDriveId()));
									}
								} else {
									NotifyUtils.notifyWithoutBigImage(App.Instance, NOTIFY_ERROR_PUSH_ID,
											App.Instance.getString(R.string.application_name), App.Instance.getString(
													R.string.msg_push_failed), android.R.drawable.stat_notify_error,
											NotifyUtils.getDrive(App.Instance, itBooksFolder.getDriveId()));
								}
							} else {
								NotifyUtils.notifyWithoutBigImage(App.Instance, NOTIFY_ERROR_PUSH_ID,
										App.Instance.getString(R.string.application_name), App.Instance.getString(
												R.string.msg_push_failed), android.R.drawable.stat_notify_error,
										NotifyUtils.getDrive(App.Instance, itBooksFolder.getDriveId()));
							}
						} catch (IOException e1) {
							NotifyUtils.notifyWithoutBigImage(App.Instance, NOTIFY_ERROR_PUSH_ID,
									App.Instance.getString(R.string.application_name), App.Instance.getString(
											R.string.msg_push_failed), android.R.drawable.stat_notify_error,
									NotifyUtils.getDrive(App.Instance, itBooksFolder.getDriveId()));
						}
					}
				}
			}
		}
	}

	//PUSH JOB FOR ALL DOWNLOADED PDF-FILES.
	private void push() {
		if (!TextUtils.isEmpty(Prefs.getInstance(App.Instance).getGoogleId()) && mGoogleApiClient != null) {
			DriveId folderId = getITBooksFolderId(mGoogleApiClient);
			DriveFolder itBooksFolder = getITBooksFolder(mGoogleApiClient, folderId);
			pushFiles(mGoogleApiClient, itBooksFolder);
		}
	}

	private static void pullFiles(GoogleApiClient client, DriveFolder itBooksFolder) {
		if (itBooksFolder != null) {
			Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.MIME_TYPE, MIME_TYPE)).addFilter(
					Filters.contains(SearchableField.TITLE, FOLDER_NAME + "_")).build();
			MetadataBufferResult filesBufferResult = itBooksFolder.queryChildren(client, query).await();
			if (filesBufferResult.getStatus().isSuccess()) {
				Log.i(TAG, "Download files.");
				MetadataBuffer metadataBuffer = filesBufferResult.getMetadataBuffer();
				for (Metadata metaData : metadataBuffer) {
					String description = metaData.getDescription();
					Map<CustomPropertyKey, String> propertyKeyStringMap = metaData.getCustomProperties();
					CustomPropertyKey bookName = new CustomPropertyKey(BOOK_NAME, CustomPropertyKey.PUBLIC);
					CustomPropertyKey bookAuthor = new CustomPropertyKey(BOOK_AUTHOR, CustomPropertyKey.PUBLIC);
					CustomPropertyKey bookSize = new CustomPropertyKey(BOOK_SIZE, CustomPropertyKey.PUBLIC);
					CustomPropertyKey bookPages = new CustomPropertyKey(BOOK_PAGES, CustomPropertyKey.PUBLIC);
					CustomPropertyKey bookLink = new CustomPropertyKey(BOOK_LINK, CustomPropertyKey.PUBLIC);
					CustomPropertyKey bookISBN = new CustomPropertyKey(BOOK_ISBN, CustomPropertyKey.PUBLIC);
					CustomPropertyKey bookYear = new CustomPropertyKey(BOOK_YEAR, CustomPropertyKey.PUBLIC);
					CustomPropertyKey bookPublisher = new CustomPropertyKey(BOOK_PUBLISHER, CustomPropertyKey.PUBLIC);
					CustomPropertyKey bookCover1 = new CustomPropertyKey(BOOK_COVER_1, CustomPropertyKey.PUBLIC);
					CustomPropertyKey bookCover2 = new CustomPropertyKey(BOOK_COVER_2, CustomPropertyKey.PUBLIC);

					String coverUrl = propertyKeyStringMap.get(bookCover1) + propertyKeyStringMap.get(bookCover2);
					//					String name, String author, String size, String pages, String link, String ISBN, String year,
					//							String publisher, String description, String coverUrl
					RSBook book = new RSBook(propertyKeyStringMap.get(bookName), propertyKeyStringMap.get(bookAuthor),
							propertyKeyStringMap.get(bookSize), propertyKeyStringMap.get(bookPages),
							propertyKeyStringMap.get(bookLink), propertyKeyStringMap.get(bookISBN),
							propertyKeyStringMap.get(bookYear), propertyKeyStringMap.get(bookPublisher), description,
							coverUrl);
					DB db = DB.getInstance(App.Instance);
					List<Download> downloads = db.getDownloads(book);
					if (downloads.size() == 0) {
						DriveId driveId = metaData.getDriveId();
						DriveFile file = driveId.asDriveFile();
						DriveContentsResult result = file.open(client, DriveFile.MODE_READ_ONLY, null).await();
						Log.i(TAG, "Start downloading book: " + book.getName());
						DriveContents contents = result.getDriveContents();
						File localFile = new File(App.Instance.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
								metaData.getTitle());
						try {
							FileUtils.copyInputStreamToFile(contents.getInputStream(), localFile);
							Download download = new Download(book);
							download.setTimeStamp(System.currentTimeMillis());
							download.setStatus(DownloadManager.STATUS_SUCCESSFUL);
							download.setDownloadId(System.currentTimeMillis());
							db.insertNewDownload(download);
							File to = new File(App.Instance.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
									download.getTargetName());
							try {
								Bitmap image =  Picasso.with(App.Instance).load(Utils.uriStr2URI(
										download.getCoverUrl()).toASCIIString()).get();


								NotifyUtils.notifyWithBigImage(App.Instance, (int) System.currentTimeMillis(),
										App.Instance.getString(R.string.application_name), App.Instance.getString(
												R.string.msg_pull_success) + download.getTargetName(),
										android.R.drawable.stat_notify_sync, image, NotifyUtils.getPDFReader(
												App.Instance, to));

							} catch (IOException e) {
								NotifyUtils.notifyWithoutBigImage(App.Instance, (int) System.currentTimeMillis(),
										App.Instance.getString(R.string.application_name), App.Instance.getString(
												R.string.msg_pull_success) + download.getTargetName(),
										android.R.drawable.stat_notify_sync, NotifyUtils.getPDFReader(App.Instance, to));
							}


						} catch (IOException e) {
							NotifyUtils.notifyWithoutBigImage(App.Instance, NOTIFY_ERROR_PULL_ID,
									App.Instance.getString(R.string.application_name), App.Instance.getString(
											R.string.msg_pull_failed), android.R.drawable.stat_notify_error,
									NotifyUtils.getDrive(App.Instance, itBooksFolder.getDriveId()));
						}
					}
				}
			}
		}
	}

	//PULL JOB FOR ALL DOWNLOADED PDF-FILES.
	private void pull() {
		if (!TextUtils.isEmpty(Prefs.getInstance(App.Instance).getGoogleId()) && mGoogleApiClient != null) {
			DriveId folderId = getITBooksFolderId(mGoogleApiClient);
			DriveFolder itBooksFolder = getITBooksFolder(mGoogleApiClient, folderId);
			pullFiles(mGoogleApiClient, itBooksFolder);
		}
	}

	private static void deleteFiles(GoogleApiClient client, long[] fileIDs, DriveFolder itBooksFolder) {
		if (itBooksFolder != null) {
			DB db = DB.getInstance(App.Instance);
			for (long downloadId : fileIDs) {
				Download download = db.getDownload(downloadId);
				db.deleteDownload(downloadId);
				try {
					File localFile = new File(App.Instance.getExternalFilesDir(
							Environment.DIRECTORY_DOWNLOADS), download.getTargetName());
					if (localFile.exists()) {
						FileUtils.forceDelete(localFile);
					}
				} catch (Exception e) {
					//Ignore...
				}
				Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.MIME_TYPE, MIME_TYPE))
						.addFilter(Filters.eq(SearchableField.TITLE, download.getTargetName())).build();
				MetadataBufferResult fileBufferResult = itBooksFolder.queryChildren(client, query)
						.await();
				MetadataBuffer fileMetadataBuffer = fileBufferResult.getMetadataBuffer();
				for (Metadata metaData : fileMetadataBuffer) {
					DriveId driveId = metaData.getDriveId();
					DriveFile file = driveId.asDriveFile();
					PendingResult<Status> pendingResult = file.delete(client);
					Status status = pendingResult.await();
					if (status.isSuccess()) {
						NotifyUtils.notifyWithoutBigImage(App.Instance, (int) System.currentTimeMillis(),
								App.Instance.getString(R.string.application_name), App.Instance.getString(
										R.string.msg_file_delete_success) + download.getTargetName(),
								android.R.drawable.stat_notify_sync, NotifyUtils.getDrive(App.Instance,
										itBooksFolder.getDriveId()));
					} else {
						NotifyUtils.notifyWithoutBigImage(App.Instance, NOTIFY_ERROR_DEL_ID, App.Instance.getString(
										R.string.application_name), App.Instance.getString(
										R.string.msg_file_delete_failed), android.R.drawable.stat_notify_error,
								NotifyUtils.getDrive(App.Instance, itBooksFolder.getDriveId()));
					}
				}
			}
		}
	}

	//REMOVE FILE(s).
	private void del() {
		if (!TextUtils.isEmpty(Prefs.getInstance(App.Instance).getGoogleId()) && mGoogleApiClient != null) {
			DriveId folderId = getITBooksFolderId(mGoogleApiClient);
			DriveFolder itBooksFolder = getITBooksFolder(mGoogleApiClient, folderId);
			deleteFiles(mGoogleApiClient, mDownloadDelList, itBooksFolder);
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
	private void disestablishGoogleDriver() {
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
		synchronized (TAG) {
			disestablishGoogleDriver();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if(!connectionResult.hasResolution()) {
			switch (connectionResult.getErrorCode()) {
			case ConnectionResult.SIGN_IN_FAILED:
			case ConnectionResult.SIGN_IN_REQUIRED:
			case ConnectionResult.INVALID_ACCOUNT:
				NotifyUtils.notifyWithoutBigImage(App.Instance, NOTIFY_REQ_LOGIN, App.Instance.getString(R.string.application_name), App.Instance.getString(R.string.msg_sync_need_login),
						android.R.drawable.stat_notify_error, NotifyUtils.getGoogleLogin(App.Instance));
				break;
			}
		}
		Intent intent = new Intent(ACTION_CONNECT_ERROR);
		intent.putExtra(EXTRAS_ERROR_RESULT, connectionResult);
		LocalBroadcastManager.getInstance(App.Instance).sendBroadcast(intent);
	}

	@Override
	public void onConnected(Bundle bundle) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (TAG) {
					LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(App.Instance);
					lbm.sendBroadcast(new Intent(ACTION_SYNC_BEGIN));
					if (mCmdSyncOnly) {
						Prefs prefs = Prefs.getInstance(App.Instance);
						long timeLastSync = Prefs.getInstance(App.Instance).getLastTimeSync();
						long thisSyncTime = System.currentTimeMillis();
						long gap = thisSyncTime - timeLastSync;
						if (timeLastSync < 0 || gap > SYNC_LIMIT) {
							push();
							pull();
							disestablishGoogleDriver();
							prefs.setLastTimeSync(thisSyncTime);
						} else {
							Log.w(TAG,
									"Abort sync because duration between last sync point and this sync point must be larger than 15 minutes, you tried still in elapsed range:" +
											gap);
						}
					} else {
						del();
					}
					lbm.sendBroadcast(new Intent(ACTION_SYNC_END));
					stopSelf();
				}
			}
		}).start();
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	public SyncService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null) {//Fixed: #69
			mCmdSyncOnly = TextUtils.equals(intent.getAction(), ACTION_SYNC_ONLY);
			if (!mCmdSyncOnly) {
				mDownloadDelList = intent.getLongArrayExtra(EXTRAS_LIST_DELETE);
			}
			establishGoogleDriver();
			return ServiceCompat.START_STICKY;
		}
		return super.onStartCommand(intent, flags, startId);
	}
}
