package com.itbooks.db;

/**
 * All "labels" for bookmarks.
 *
 * @author Xinyue Zhao
 * @deprecated For cloud bookmark.
 */
interface LabelsTbl {
	static final String ID = "_id";
	static final String NAME = "_name";
	static final String EDIT_TIME = "_edited_time";
	static final String TABLE_NAME = "labels";

	//We use rowId as key for each row.
	//See. http://www.sqlite.org/autoinc.html
	/**
	 * Init new table since {@link com.itbooks.db.DatabaseHelper#DATABASE_VERSION} = {@code 1}.
	 */
	static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY, " +
			NAME + " TEXT, " + EDIT_TIME + " INTEGER" +
			");";
}
