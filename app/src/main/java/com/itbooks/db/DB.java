package com.itbooks.db;


import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.itbooks.data.rest.RSBook;
import com.itbooks.net.download.Download;

import javax.annotation.Nullable;


/**
 * Defines methods that operate on database.
 * <p/>
 * <b>Singleton pattern.</b>
 * <p/>
 * <p/>
 *
 * @author Xinyue Zhao
 */
public final class DB {
	/**
	 * {@link android.content.Context}.
	 */
	private Context mContext;
	/**
	 * Impl singleton pattern.
	 */
	private static DB sInstance;
	/**
	 * Helper class that create, delete, update tables of database.
	 */
	private DatabaseHelper mDatabaseHelper;
	/**
	 * The database object.
	 */
	private SQLiteDatabase mDB;

	/**
	 * Constructor of {@link DB}. Impl singleton pattern so that it is private.
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 */
	private DB(Context cxt) {
		mContext = cxt;
	}

	/**
	 * Get instance of  {@link  DB} singleton.
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 *
	 * @return The {@link DB} singleton.
	 */
	public static DB getInstance(Context cxt) {
		if (sInstance == null) {
			sInstance = new DB(cxt);
		}
		return sInstance;
	}

	/**
	 * Open database.
	 */
	public synchronized void open() {
		mDatabaseHelper = new DatabaseHelper(mContext);
		mDB = mDatabaseHelper.getWritableDatabase();
	}

	/**
	 * Close database.
	 */
	public synchronized void close() {
		mDatabaseHelper.close();
	}

	/**
	 * @return The count of downloaded instance.
	 */
	public synchronized int getDownloadsCount() {
		return getObjectDbCount(DownloadsTbl.TABLE_NAME);
	}

	/**
	 * Get tables rows count.
	 *
	 * @param tableName
	 * 		Table name.
	 *
	 * @return Count of all rows.
	 */
	private synchronized int getObjectDbCount(String tableName) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		String countQuery = "SELECT " + DownloadsTbl.ID + " FROM " + tableName;
		Cursor cursor = mDB.rawQuery(countQuery, null);
		int cnt = cursor.getCount();
		cursor.close();
		return cnt;
	}

	/**
	 * Insert a new download instance.
	 *
	 * @param download
	 * 		{@link Download} A download instance.
	 *
	 * @return {@code true} if insert successed.
	 */
	public synchronized boolean insertNewDownload(Download download) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		boolean success = false;
		try {
			long rowId = -1;
			ContentValues v = new ContentValues();

			RSBook book = download.getBook();

			v.put(DownloadsTbl.BOOK_NAME, book.getName());
			v.put(DownloadsTbl.BOOK_AUTH, book.getAuthor());
			v.put(DownloadsTbl.BOOK_SIZE, book.getSize());
			v.put(DownloadsTbl.BOOK_PAGES, book.getPages());
			v.put(DownloadsTbl.BOOK_LINK, book.getLink());
			v.put(DownloadsTbl.BOOK_ISBN, book.getISBN());
			v.put(DownloadsTbl.BOOK_YEAR, book.getYear());
			v.put(DownloadsTbl.BOOK_PUB, book.getPublisher());
			v.put(DownloadsTbl.BOOK_DESC, book.getDescription());
			v.put(DownloadsTbl.BOOK_COVER_URL, book.getCoverUrl());
			v.put(DownloadsTbl.DOWNLOAD_ID, download.getDownloadId());
			v.put(DownloadsTbl.DOWNLOAD_STATUS, download.getStatus());
			v.put(DownloadsTbl.EDIT_TIME, download.getTimeStamp());

			rowId = mDB.insert(DownloadsTbl.TABLE_NAME, null, v);
			success = rowId != -1;
		} finally {
			close();
		}
		return success;
	}

	/**
	 * Update the status of download instance.
	 * @param download An instance of {@link Download}.
	 * @return {@code  true} if success.
	 */
	public synchronized boolean updateDownload(Download download) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		boolean success = false;
		try {
			long rowId;
			ContentValues v = new ContentValues();

			v.put(DownloadsTbl.DOWNLOAD_STATUS, download.getStatus());
			String whereClause =
					DownloadsTbl.BOOK_NAME + "=? AND " +
					DownloadsTbl.BOOK_AUTH + "=? AND " +
					DownloadsTbl.BOOK_SIZE + "=? AND " +
					DownloadsTbl.BOOK_PAGES + "=? AND " +
					DownloadsTbl.BOOK_ISBN + "=? AND " +
					DownloadsTbl.BOOK_YEAR + "=? AND " +
					DownloadsTbl.BOOK_PUB + "=? AND " +
					DownloadsTbl.BOOK_DESC + "=?  ";
			String[] whereArgs = download.getBook().toArray();
			rowId = mDB.update(DownloadsTbl.TABLE_NAME, v, whereClause, whereArgs);
			success = rowId != -1;
		} finally {
			close();
		}
		return success;
	}

	/**
	 * Get an instance of download associates with the download-id.
	 *
	 * @param downloadId
	 * 		The id provided by Android when started downloading.
	 *
	 * @return {@link Download}.
	 */
	public synchronized
	@Nullable
	Download getDownload(long downloadId) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		Cursor c;
		String whereClause = DownloadsTbl.DOWNLOAD_ID + "=?";
		String[] whereArgs = new String[] { String.valueOf(downloadId) };
		c = mDB.query(DownloadsTbl.TABLE_NAME, null, whereClause, whereArgs, null, null, null, null);
		Download item = null;
		try {
			RSBook book;
			while (c.moveToNext()) {
				book = new RSBook(c.getString(c.getColumnIndex(DownloadsTbl.BOOK_NAME)), c.getString(c.getColumnIndex(
						DownloadsTbl.BOOK_AUTH)), c.getString(c.getColumnIndex(DownloadsTbl.BOOK_SIZE)), c.getString(
						c.getColumnIndex(DownloadsTbl.BOOK_PAGES)), c.getString(c.getColumnIndex(
						DownloadsTbl.BOOK_LINK)), c.getString(c.getColumnIndex(DownloadsTbl.BOOK_ISBN)), c.getString(
						c.getColumnIndex(DownloadsTbl.BOOK_YEAR)), c.getString(c.getColumnIndex(DownloadsTbl.BOOK_PUB)),
						c.getString(c.getColumnIndex(DownloadsTbl.BOOK_DESC)), c.getString(c.getColumnIndex(
						DownloadsTbl.BOOK_COVER_URL)));

				item = new Download(book);
				item.setDownloadId(c.getLong(c.getColumnIndex(DownloadsTbl.DOWNLOAD_ID)));
				item.setStatus(c.getInt(c.getColumnIndex(DownloadsTbl.DOWNLOAD_STATUS)));
				item.setTimeStamp(c.getLong(c.getColumnIndex(DownloadsTbl.EDIT_TIME)));
			}
		} finally {
			if (c != null) {
				c.close();
			}
			close();
		}
		return item;
	}


	/**
	 * Get instances of download.
	 *
	 * @param book A book that might have being downloaded.
	 *
	 * @return A list of  {@link Download}s that associates with the {@code book}.
	 */
	public synchronized List<Download> getDownloads(RSBook book) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		Cursor c = null;
		List<Download>  downloads = new ArrayList<>();
		try {
			String whereClause = DownloadsTbl.BOOK_NAME + "=? AND " +
					DownloadsTbl.BOOK_AUTH + "=? AND " +
					DownloadsTbl.BOOK_SIZE + "=? AND " +
					DownloadsTbl.BOOK_PAGES + "=? AND " +
					DownloadsTbl.BOOK_ISBN + "=? AND " +
					DownloadsTbl.BOOK_YEAR + "=? AND " +
					DownloadsTbl.BOOK_PUB + "=? AND " +
					DownloadsTbl.BOOK_DESC + "=?  ";
			String[] whereArgs = book.toArray();
			c = mDB.query(DownloadsTbl.TABLE_NAME,null, whereClause,
					whereArgs, null, null, DownloadsTbl.EDIT_TIME +  " DESC");

			while (c.moveToNext()) {
				Download item = new Download(new RSBook(c.getString(c.getColumnIndex(DownloadsTbl.BOOK_NAME)), c.getString(c.getColumnIndex(
						DownloadsTbl.BOOK_AUTH)), c.getString(c.getColumnIndex(DownloadsTbl.BOOK_SIZE)), c.getString(
						c.getColumnIndex(DownloadsTbl.BOOK_PAGES)), c.getString(c.getColumnIndex(
						DownloadsTbl.BOOK_LINK)), c.getString(c.getColumnIndex(DownloadsTbl.BOOK_ISBN)), c.getString(
						c.getColumnIndex(DownloadsTbl.BOOK_YEAR)), c.getString(c.getColumnIndex(DownloadsTbl.BOOK_PUB)),
						c.getString(c.getColumnIndex(DownloadsTbl.BOOK_DESC)), c.getString(c.getColumnIndex(
						DownloadsTbl.BOOK_COVER_URL))));
				item.setDownloadId(c.getLong(c.getColumnIndex(DownloadsTbl.DOWNLOAD_ID)));
				item.setStatus(c.getInt(c.getColumnIndex(DownloadsTbl.DOWNLOAD_STATUS)));
				item.setTimeStamp(c.getLong(c.getColumnIndex(DownloadsTbl.EDIT_TIME)));
				downloads.add(item);
			}
		} finally {
			if (c != null) {
				c.close();
			}
			close();
		}
		return downloads;
	}

	/**
	 * Get all instances of download.
	 *
	 * @return A list of all {@link Download}s.
	 */
	public synchronized List<Download> getDownloads() {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		Cursor c = null;
		List<Download>  downloads = new ArrayList<>();
		try {
			c = mDB.query(DownloadsTbl.TABLE_NAME,null, null,
					null, null, null, DownloadsTbl.EDIT_TIME +  " DESC");

			while (c.moveToNext()) {
				Download item = new Download(new RSBook(c.getString(c.getColumnIndex(DownloadsTbl.BOOK_NAME)), c.getString(c.getColumnIndex(
						DownloadsTbl.BOOK_AUTH)), c.getString(c.getColumnIndex(DownloadsTbl.BOOK_SIZE)), c.getString(
						c.getColumnIndex(DownloadsTbl.BOOK_PAGES)), c.getString(c.getColumnIndex(
						DownloadsTbl.BOOK_LINK)), c.getString(c.getColumnIndex(DownloadsTbl.BOOK_ISBN)), c.getString(
						c.getColumnIndex(DownloadsTbl.BOOK_YEAR)), c.getString(c.getColumnIndex(DownloadsTbl.BOOK_PUB)),
						c.getString(c.getColumnIndex(DownloadsTbl.BOOK_DESC)), c.getString(c.getColumnIndex(
						DownloadsTbl.BOOK_COVER_URL))));
				item.setDownloadId(c.getLong(c.getColumnIndex(DownloadsTbl.DOWNLOAD_ID)));
				item.setStatus(c.getInt(c.getColumnIndex(DownloadsTbl.DOWNLOAD_STATUS)));
				item.setTimeStamp(c.getLong(c.getColumnIndex(DownloadsTbl.EDIT_TIME)));
				downloads.add(item);
			}
		} finally {
			if (c != null) {
				c.close();
			}
			close();
		}
		return downloads;
	}


	/**
	 * Delete a download history.
	 * @param id The ident of a download entry. If id<0, delete all.
	 * @return {@code true} if delete, otherwise {@code false}.
	 */
	public synchronized boolean deleteDownload(long id) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		boolean success;
		try {
			long rowId;
			if (id < 0) {
				rowId = mDB.delete(DownloadsTbl.TABLE_NAME, null, null);
				success = rowId > 0;
			} else {
				String whereClause = DownloadsTbl.DOWNLOAD_ID + "=?";
				String[] whereArgs = new String[] { String.valueOf(id) };
				rowId = mDB.delete(DownloadsTbl.TABLE_NAME, whereClause, whereArgs);
				success = rowId > 0;
			}
		} finally {
			close();
		}
		return success;
	}
}
