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

package com.itbooks.app;

import java.io.IOException;
import java.util.Properties;

import android.support.multidex.MultiDexApplication;

import com.chopping.net.TaskHelper;
import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.itbooks.db.DB;
import com.itbooks.net.bookmark.BookmarkManger;
import com.itbooks.utils.Utils;

import cn.bmob.v3.Bmob;
import io.fabric.sdk.android.Fabric;


/**
 * The app-object of the project.
 *
 * @author Xinyue Zhao
 */
public final class App extends MultiDexApplication {
	/**
	 * Our downloaded file is unique to other apps.
	 */
	public static final String PREFIX = "itbooks_";//Prefix of a downloaded file.
	/**
	 * Application's instance.
	 */
	public static App Instance;
	{
		Instance = this;
	}

	private boolean mShow3GWarning;

	@Override
	public void onCreate() {
		super.onCreate();
		setShow3GWarning( false );

		Fabric.with( this, new Crashlytics() );
		Stetho.initialize( Stetho.newInitializerBuilder( this ).enableDumpapp( Stetho.defaultDumperPluginsProvider( this ) )
								   .enableWebKitInspector( Stetho.defaultInspectorModulesProvider( this ) ).build() );

		BookmarkManger.createInstance( this );
		Properties prop = new Properties();
		try {
			prop.load( getClassLoader().getResourceAsStream( "app.properties" ) );
			Bmob.initialize( this, prop.getProperty( "bmob_app" ) );
		} catch( IOException e ) {
			e.printStackTrace();
		}
		TaskHelper.init( getApplicationContext() );
		DB.getInstance( this ).open();

		Utils.startAppGuardService( App.Instance );
	}


	public boolean isShow3GWarning() {
		return mShow3GWarning;
	}

	public void setShow3GWarning( boolean show3GWarning ) {
		mShow3GWarning = show3GWarning;
	}
}
