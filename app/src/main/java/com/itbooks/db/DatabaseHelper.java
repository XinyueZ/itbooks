package com.itbooks.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Classical helper pattern on Android DB ops.
 *
 * @author Xinyue Zhao
 */
public final class DatabaseHelper extends SQLiteOpenHelper {
	/**
	 * DB name.
	 */
	public static final String DATABASE_NAME = "itbooksDB";
	/**
	 * Init version of DB.
	 */
	private static final int DATABASE_VERSION = 1;

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
		db.execSQL(BookmarksTbl.SQL_CREATE);
		db.execSQL(LabelsTbl.SQL_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
