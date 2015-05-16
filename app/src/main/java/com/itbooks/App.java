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
import java.util.Properties;

import android.app.Application;
import android.support.v4.os.AsyncTaskCompat;
import android.text.TextUtils;

import com.chopping.net.TaskHelper;
import com.facebook.stetho.Stetho;
import com.itbooks.db.DB;
import com.itbooks.gcm.RegGCMTask;
import com.itbooks.net.bookmark.BookmarkManger;
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

	@Override
	public void onCreate() {
		super.onCreate();
		Stetho.initialize(Stetho.newInitializerBuilder(this).enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
				.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this)).build());

		BookmarkManger.createInstance(this);
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
}
