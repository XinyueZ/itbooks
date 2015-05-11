package com.itbooks.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.LongSparseArray;

import com.itbooks.data.DSBookmark;
import com.itbooks.data.DSLabel;
import com.itbooks.data.rest.RSBook;


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
	 * Add a label.
	 *
	 * @param item
	 * 		{@link  com.itbooks.data.DSLabel} to insert.
	 *
	 * @return {@code true} if insert is success.
	 */
	public synchronized boolean addLabel(DSLabel item) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		boolean success = false;
		try {
			long rowId = -1;
			ContentValues v = new ContentValues();
			v.put(LabelsTbl.NAME, item.getName());
			v.put(LabelsTbl.EDIT_TIME, System.currentTimeMillis());
			rowId = mDB.insert(LabelsTbl.TABLE_NAME, null, v);
			item.setId(rowId);
			success = rowId != -1;
		} finally {
			close();
		}
		return success;
	}

	/**
	 * Add a bookmark.
	 *
	 * @param item
	 * 		{@link  com.itbooks.data.DSBookmark} to insert.
	 *
	 * @return {@code true} if insert is success.
	 */
	public synchronized boolean addBookmark(DSBookmark item) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		boolean success = false;
		try {
			long rowId = -1;
			RSBook book = item.getBook();
			ContentValues v = new ContentValues();

			v.put(BookmarksTbl.BOOK_NAME, book.getName());
			v.put(BookmarksTbl.BOOK_AUTH, book.getAuthor());
			v.put(BookmarksTbl.BOOK_SIZE, book.getSize());
			v.put(BookmarksTbl.BOOK_PAGES, book.getPages());
			v.put(BookmarksTbl.BOOK_LINK, book.getLink());
			v.put(BookmarksTbl.BOOK_ISBN, book.getISBN());
			v.put(BookmarksTbl.BOOK_YEAR, book.getYear());
			v.put(BookmarksTbl.BOOK_PUB, book.getPublisher());
			v.put(BookmarksTbl.BOOK_DESC, book.getDescription());
			v.put(BookmarksTbl.BOOK_COVER_URL, book.getCoverUrl());
			v.put(BookmarksTbl.LABEL_ID, item.getLabelId());
			v.put(BookmarksTbl.EDIT_TIME, System.currentTimeMillis());

			rowId = mDB.insert(BookmarksTbl.TABLE_NAME, null, v);
			item.setId(rowId);
			success = rowId != -1;
		} finally {
			close();
		}
		return success;
	}


	/**
	 * Remove one label from DB.
	 *
	 * @param item
	 * 		The label to remove.
	 *
	 * @return The count of rows remain in DB after removed item.
	 * <p/>
	 * Return -1 if there's error when removed data.
	 */
	public synchronized int removeLabel(DSLabel item) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		int rowsRemain = -1;
		boolean success;
		try {
			long rowId;
			String whereClause = LabelsTbl.ID + "=?";
			String[] whereArgs = new String[] { String.valueOf(item.getId()) };
			rowId = mDB.delete(LabelsTbl.TABLE_NAME, whereClause, whereArgs);
			success = rowId > 0;
			if (success) {
				Cursor c = mDB.query(LabelsTbl.TABLE_NAME, new String[] { LabelsTbl.ID }, null, null, null, null, null);
				rowsRemain = c.getCount();
			} else {
				rowsRemain = -1;
			}
			//Set bookmarks to no labels.
			ContentValues v = new ContentValues();
			v.put(BookmarksTbl.LABEL_ID, Long.parseLong(BookmarksTbl.DEF_LABEL_ID));
			v.put(BookmarksTbl.EDIT_TIME, System.currentTimeMillis());
			String[] args = new String[] { item.getId() + "" };
			mDB.update(BookmarksTbl.TABLE_NAME, v, BookmarksTbl.LABEL_ID + " = ?", args);
		} finally {
			close();
		}
		return rowsRemain;
	}


	/**
	 * Remove one bookmark from DB.
	 *
	 *
	 * @return The count of rows remain in DB after removed item.
	 * <p/>
	 * Return {@code true} if success.
	 */
	public synchronized boolean removeBookmarks() {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		boolean success;
		try {
			long rowId;
			rowId = mDB.delete(BookmarksTbl.TABLE_NAME, null, null);
			success = rowId > 0;
		} finally {
			close();
		}
		return success;
	}


	/**
	 * Remove one bookmark from DB.
	 *
	 * @param item
	 * 		The bookmark to remove. If {@code null} then remove all bookmarks.
	 *
	 * @return The count of rows remain in DB after removed item.
	 * <p/>
	 * Return -1 if there's error when removed data.
	 */
	public synchronized int removeBookmark(DSBookmark item) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		int rowsRemain = -1;
		boolean success;
		try {
			long rowId;
			if (item != null) {
				String whereClause = BookmarksTbl.ID + "=?";
				String[] whereArgs = new String[] { String.valueOf(item.getId()) };
				rowId = mDB.delete(BookmarksTbl.TABLE_NAME, whereClause, whereArgs);
			} else {
				rowId = mDB.delete(BookmarksTbl.TABLE_NAME, null, null);
			}
			success = rowId > 0;
			if (success) {
				Cursor c = mDB.query(BookmarksTbl.TABLE_NAME, new String[] { BookmarksTbl.ID }, null, null, null, null,
						null);
				rowsRemain = c.getCount();
			} else {
				rowsRemain = -1;
			}
		} finally {
			close();
		}
		return rowsRemain;
	}

	/**
	 * Remove one bookmark from DB.
	 *
	 * @param book
	 * 		The book that associates with the bookmark to remove. If {@code null} then remove all bookmarks.
	 *
	 * @return The count of rows remain in DB after removed item.
	 * <p/>
	 * Return -1 if there's error when removed data.
	 */
	public synchronized int removeBookmark(RSBook book) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		int rowsRemain = -1;
		boolean success;
		try {
			long rowId;
			if (book != null) {
				String whereClause = BookmarksTbl.BOOK_NAME + "=? AND " +
						BookmarksTbl.BOOK_AUTH + "=? AND " +
						BookmarksTbl.BOOK_SIZE + "=? AND " +
						BookmarksTbl.BOOK_PAGES + "=? AND " +
						BookmarksTbl.BOOK_LINK + "=? AND " +
						BookmarksTbl.BOOK_ISBN + "=? AND " +
						BookmarksTbl.BOOK_YEAR + "=? AND " +
						BookmarksTbl.BOOK_PUB + "=? AND " +
						BookmarksTbl.BOOK_DESC + "=? AND "+
						BookmarksTbl.BOOK_COVER_URL + "=? "  ;
				String[] whereArgs = book.toArray();
				rowId = mDB.delete(BookmarksTbl.TABLE_NAME, whereClause, whereArgs);
			} else {
				rowId = mDB.delete(BookmarksTbl.TABLE_NAME, null, null);
			}
			success = rowId > 0;
			if (success) {
				Cursor c = mDB.query(BookmarksTbl.TABLE_NAME, new String[] { BookmarksTbl.ID }, null, null, null, null,
						null);
				rowsRemain = c.getCount();
			} else {
				rowsRemain = -1;
			}
		} finally {
			close();
		}
		return rowsRemain;
	}

	/**
	 * Sort direction.
	 */
	public enum Sort {
		DESC("DESC"), ASC("ASC");
		/**
		 * Text represents this enum.
		 */
		private String nm;

		/**
		 * Init {@link com.itbooks.db.DB.Sort}.
		 *
		 * @param nm
		 * 		{@code DESC or ASC}.
		 */
		Sort(String nm) {
			this.nm = nm;
		}

		@Override
		public String toString() {
			return nm;
		}
	}

	/**
	 * Returns all labels from DB order by the time of edition.
	 *
	 * @param sort
	 * 		"DESC" or "ASC".
	 *
	 * @return All labels from DB order by the time of edition.
	 */
	public synchronized LongSparseArray<DSLabel> getLabels(Sort sort) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		Cursor c = mDB.query(LabelsTbl.TABLE_NAME, null, null, null, null, null,
				LabelsTbl.EDIT_TIME + " " + sort.toString());
		DSLabel item;
		LongSparseArray<DSLabel> list = new LongSparseArray<DSLabel>();
		try {

			while (c.moveToNext()) {
				item = new DSLabel(c.getLong(c.getColumnIndex(LabelsTbl.ID)), c.getString(c.getColumnIndex(
						LabelsTbl.NAME)), c.getLong(c.getColumnIndex(LabelsTbl.EDIT_TIME)));
				list.put(item.getId(), item);
			}
		} finally {
			if (c != null) {
				c.close();
			}
			close();
		}
		return list;
	}

	/**
	 * Returns all bookmarks from DB order by the time of edition.
	 *
	 * @param sort
	 * 		"DESC" or "ASC".
	 * @param label
	 * 		The label associated with bookmark.
	 *
	 * @return All labels from DB order by the time of edition.
	 */
	public synchronized LongSparseArray<DSBookmark> getBookmarks(Sort sort, DSLabel label) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		Cursor c;
		if (label == null) {
			c = mDB.query(BookmarksTbl.TABLE_NAME, null, null, null, null, null,
					BookmarksTbl.EDIT_TIME + " " + sort.toString());
		} else {
			String whereClause = BookmarksTbl.LABEL_ID + "=?";
			String[] whereArgs = new String[] { String.valueOf(label.getId()) };
			c = mDB.query(BookmarksTbl.TABLE_NAME, null, whereClause, whereArgs, null, null, null,
					BookmarksTbl.EDIT_TIME + " " + sort.toString());
		}
		DSBookmark item;
		LongSparseArray<DSBookmark> list = new LongSparseArray<DSBookmark>();
		try {
			RSBook book;
			while (c.moveToNext()) {
				book = new RSBook(
						c.getString(c.getColumnIndex(BookmarksTbl.BOOK_NAME)),
						c.getString(c.getColumnIndex(BookmarksTbl.BOOK_AUTH)),
						c.getString(c.getColumnIndex(BookmarksTbl.BOOK_SIZE)),
						c.getString(c.getColumnIndex(BookmarksTbl.BOOK_PAGES)),
						c.getString(c.getColumnIndex(BookmarksTbl.BOOK_LINK)),
						c.getString(c.getColumnIndex(BookmarksTbl.BOOK_ISBN)),
						c.getString(c.getColumnIndex(BookmarksTbl.BOOK_YEAR)),
						c.getString(c.getColumnIndex(BookmarksTbl.BOOK_PUB)),
						c.getString(c.getColumnIndex(BookmarksTbl.BOOK_DESC)),
						c.getString(c.getColumnIndex(BookmarksTbl.BOOK_COVER_URL))
				);

				item = new DSBookmark(
						c.getLong(c.getColumnIndex(BookmarksTbl.ID)),
						book,
						c.getLong(c.getColumnIndex( BookmarksTbl.LABEL_ID)),
						c.getLong(c.getColumnIndex(LabelsTbl.EDIT_TIME))
				);
				list.put(item.getId(), item);
			}
		} finally {
			if (c != null) {
				c.close();
			}
			close();
		}
		return list;
	}

	/**
	 * To test whether the book has been bookmarked or not.
	 *
	 * @param book {@link RSBook}
	 *
	 * @return {@code true} if bookmarked.
	 */
	public synchronized boolean isBookmarked(RSBook book) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		boolean success;
		try {
			String whereClause = BookmarksTbl.BOOK_NAME + "=? AND " +
					BookmarksTbl.BOOK_AUTH + "=? AND " +
					BookmarksTbl.BOOK_SIZE + "=? AND " +
					BookmarksTbl.BOOK_PAGES + "=? AND " +
					BookmarksTbl.BOOK_LINK + "=? AND " +
					BookmarksTbl.BOOK_ISBN + "=? AND " +
					BookmarksTbl.BOOK_YEAR + "=? AND " +
					BookmarksTbl.BOOK_PUB + "=? AND " +
					BookmarksTbl.BOOK_DESC + "=? AND "+
					BookmarksTbl.BOOK_COVER_URL + "=? "  ;
			String[] whereArgs = book.toArray();
			Cursor c = mDB.query(BookmarksTbl.TABLE_NAME, new String[] { BookmarksTbl.BOOK_NAME }, whereClause, whereArgs,
					null, null, null);
			success = c.getCount() >= 1;
		} finally {
			close();
		}
		return success;
	}
}
