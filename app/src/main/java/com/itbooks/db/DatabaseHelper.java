package com.itbooks.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.itbooks.bus.NewAPIVersionUpdateEvent;

import de.greenrobot.event.EventBus;

/**
 * Classical helper pattern on Android DB ops.
 *
 * @author Xinyue Zhao
 */
final class DatabaseHelper extends SQLiteOpenHelper {
	/**
	 * DB name.
	 */
	public static final String DATABASE_NAME = "itbooksDB";
	private static final int DATABASE_VERSION = 2;
	/**
	 * Init version of DB.
	 */
	//private static final int DATABASE_VERSION = 1;

	/**
	 * Constructor of {@link DatabaseHelper}.
	 *
	 * @param context
	 * 		{@link android.content.Context}.
	 */
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DownloadsTbl.SQL_CREATE);//New table from v2, if user had never created bookmark-table or label-table, Android creates download-table for initialization.
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1 && newVersion == 2) {
			//Have to delete old version because of new API.
			db.execSQL(String.format("DROP TABLE IF EXISTS %s", BookmarksTbl.TABLE_NAME));
			db.execSQL(String.format("DROP TABLE IF EXISTS %s", BookmarksTbl.TABLE_NAME));
			db.execSQL(DownloadsTbl.SQL_CREATE);//New table for v2, if user had created bookmark-table or label-table, Android creates download-table for update.
			EventBus.getDefault().postSticky(new NewAPIVersionUpdateEvent());
		}
	}
}
