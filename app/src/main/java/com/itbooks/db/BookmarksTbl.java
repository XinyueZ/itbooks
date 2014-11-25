package com.itbooks.db;

/**
 * All bookmarks.
 *
 * @author Xinyue Zhao
 */
public interface BookmarksTbl {
	public static final String DEF_LABEL_ID = "-1";
	static final String ID = "_id";
	static final String BOOK_ID = "_book_id";
	static final String BOOK_COVER_URL = "_cover_url";
	static final String LABEL_ID = "_label_id";
	static final String EDIT_TIME = "_edited_time";
	static final String TABLE_NAME = "bookmarks";

	//We use rowId as key for each row.
	//See. http://www.sqlite.org/autoinc.html
	/**
	 * Init new table since {@link DatabaseHelper#DATABASE_VERSION} = {@code 1}.
	 */
	static final String SQL_CREATE =
			"CREATE TABLE " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY, " +
					BOOK_ID + " INTEGER, " + BOOK_COVER_URL + " TEXT, " +  LABEL_ID + " INTEGER DEFAULT " + DEF_LABEL_ID + ", " + EDIT_TIME + " INTEGER" +
					");";
}
