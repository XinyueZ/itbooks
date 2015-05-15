/*
                   _ooOoo_
                  o8888888o
                  88" . "88
                  (| -_- |)
                  O\  =  /O
               ____/`---'\____
             .'  \\|     |//  `.
            /  \\|||  :  |||//  \
           /  _||||| -:- |||||-  \
           |   | \\\  -  /// |   |
           | \_|  ''\---/''  |   |
           \  .-\__  `-`  ___/-. /
         ___`. .'  /--.--\  `. . __
      ."" '<  `.___\_<|>_/___.'  >'"".
     | | :  `- \`.;`\ _ /`;.`/ - ` : | |
     \  \ `-.   \_ __\ /__ _/   .-` /  /
======`-.____`-.___\_____/___.-`____.-'======
                   `=---='
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
         佛祖保佑       永无BUG
*/
//          佛曰:
//                  写字楼里写字间，写字间里程序员；
//                  程序人员写程序，又拿程序换酒钱。
//                  酒醒只在网上坐，酒醉还来网下眠；
//                  酒醉酒醒日复日，网上网下年复年。
//                  但愿老死电脑间，不愿鞠躬老板前；
//                  奔驰宝马贵者趣，公交自行程序员。
//                  别人笑我忒疯癫，我笑自己命太贱。

package com.itbooks;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import android.app.Application;
import android.support.annotation.Nullable;
import android.support.v4.os.AsyncTaskCompat;
import android.text.TextUtils;

import com.chopping.net.TaskHelper;
import com.facebook.stetho.Stetho;
import com.itbooks.data.DSBookmark;
import com.itbooks.data.rest.RSBook;
import com.itbooks.db.DB;
import com.itbooks.gcm.RegGCMTask;
import com.itbooks.utils.Prefs;

import cn.bmob.v3.Bmob;


/**
 * The app-object of the project.
 *
 * @author Xinyue Zhao
 */
public final class App extends Application {
	/**
	 * Our downloaded file is unique to other apps.
	 */
	public static final String PREFIX = "itbooks_";//Prefix of a downloaded file.
	/**
	 * All cached bookmarks.
	 */
	private List<DSBookmark> mBookmarksInCache = new LinkedList<>();

	@Override
	public void onCreate() {
		super.onCreate();
		Stetho.initialize(Stetho.newInitializerBuilder(this).enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
				.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this)).build());

		Properties prop = new Properties();
		try {
			prop.load(getClassLoader().getResourceAsStream("app.properties"));
			Bmob.initialize(this, prop.getProperty("bmob_app"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		TaskHelper.init(getApplicationContext());
		DB.getInstance(this).open();

		//Refresh push-id if user has used.
		if(!TextUtils.isEmpty(Prefs.getInstance(this).getPushRegId())) {
			AsyncTaskCompat.executeParallel(new RegGCMTask(this));
		}
	}

	/**
	 * Initialize the bookmarks in cache.
	 *
	 * @param bookmarks
	 * 		The bookmarks remote.
	 */
	public void setBookmarksInCache(List<DSBookmark> bookmarks) {
		mBookmarksInCache = bookmarks;
	}

	/**
	 * To test whether the book has been added as bookmark, and get the instance of the object.
	 *
	 * @param book
	 * 		{@link RSBook}, the book to test.
	 *
	 * @return The object found in list, it might be {@code null}.
	 */
	public @Nullable DSBookmark getBookmarked(RSBook book) {
		for (DSBookmark bookmark : mBookmarksInCache) {
			if (bookmark.getBook().equals(book)) {
				return bookmark;
			}
		}
		return null;
	}

	/**
	 * Bookmark.
	 *
	 * @param mNewBookmark
	 * 		{@link DSBookmark}, the book to add.
	 */
	public void addToBookmark(DSBookmark mNewBookmark) {
		mBookmarksInCache.add(mNewBookmark);
	}

	/**
	 * Remove from bookmark.
	 *
	 * @param book
	 * 		{@link RSBook}, the book to remove.
	 */
	public void removeFromBookmark(RSBook book) {
		for (DSBookmark bookmark : mBookmarksInCache) {
			if (bookmark.getBook().equals(book)) {
				mBookmarksInCache.remove(bookmark);
				return;
			}
		}
	}

	/**
	 *
	 * @return  All cached bookmarks.
	 */
	public List<DSBookmark> getBookmarksInCache() {
		return mBookmarksInCache;
	}
}
