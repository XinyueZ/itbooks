package com.itbooks.db;

/**
 * All bookmarks.
 *
 * @author Xinyue Zhao
 * @deprecated For cloud bookmark.
 */
public interface BookmarksTbl {
	public static final String DEF_LABEL_ID = "-1";
	static final String ID = "_id";
	static final String BOOK_NAME = "_name";
	static final String BOOK_AUTH = "_auth";
	static final String BOOK_SIZE = "_size";
	static final String BOOK_PAGES = "_pages";
	static final String BOOK_LINK = "_link";
	static final String BOOK_ISBN = "_isbn";
	static final String BOOK_YEAR = "_year";
	static final String BOOK_PUB = "_pub";
	static final String BOOK_DESC = "_desc";
	static final String BOOK_COVER_URL = "_cover_url";


	static final String LABEL_ID = "_label_id";
	static final String EDIT_TIME = "_edited_time";
	static final String TABLE_NAME = "bookmarks";

	//We use rowId as key for each row.
	//See. http://www.sqlite.org/autoinc.html
	/**
	 * Init new table since {@link DatabaseHelper#DATABASE_VERSION} = {@code 1}.
	 */
	static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
			ID + " INTEGER PRIMARY KEY, " +


			BOOK_NAME + " TEXT DEFAULT \"\", " +
			BOOK_AUTH + " TEXT DEFAULT \"\", " +
			BOOK_SIZE + " TEXT DEFAULT \"\", " +
			BOOK_PAGES + " TEXT DEFAULT \"\", " +
			BOOK_LINK + " TEXT DEFAULT \"\", " +
			BOOK_ISBN + " TEXT DEFAULT \"\", " +
			BOOK_YEAR + " TEXT DEFAULT \"\", " +
			BOOK_PUB + " TEXT DEFAULT \"\", " +
			BOOK_DESC + " TEXT DEFAULT \"\", " +
			BOOK_COVER_URL + " TEXT DEFAULT \"\", " +


			LABEL_ID + " INTEGER DEFAULT " + DEF_LABEL_ID + ", " +
			EDIT_TIME + " INTEGER" +
			");";
}
