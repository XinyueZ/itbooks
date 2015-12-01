package com.itbooks.app.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

import com.chopping.utils.Utils;
import com.itbooks.R;
import com.itbooks.app.App;
import com.itbooks.gcm.RegGCMTask;
import com.itbooks.gcm.UnregGCMTask;
import com.itbooks.utils.Prefs;


/**
 * Setting .
 */
public final class SettingActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

	/**
	 * The "ActionBar".
	 */
	private Toolbar mToolbar;
	/**
	 * Request-id of this  {@link Activity}.
	 */
	public static final int REQ = 0x95;

	/**
	 * Show an instance of SettingsActivity.
	 *
	 * @param context
	 * 		A context object.
	 */
	public static void showInstance( Activity context ) {
		Intent intent = new Intent( context, SettingActivity.class );
		intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP );
		ActivityCompat.startActivityForResult( context, intent, REQ, null );
	}


	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		addPreferencesFromResource( R.xml.settings );
		mToolbar = (Toolbar) getLayoutInflater().inflate( R.layout.toolbar, null, false );
		addContentView( mToolbar, new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT ) );
		mToolbar.setTitle( R.string.menu_setting );
		mToolbar.setTitleTextColor( ActivityCompat.getColor( App.Instance, R.color.text_common_white ) );
		mToolbar.setNavigationIcon( R.drawable.ic_arrow_back_white_24dp );
		mToolbar.setNavigationOnClickListener( new OnClickListener() {
			@Override
			public void onClick( View v ) {
				finish();
			}
		} );

		CheckBoxPreference push = (CheckBoxPreference) findPreference( Prefs.KEY_PUSH_SETTING );
		push.setOnPreferenceChangeListener( this );
		CheckBoxPreference syncCharging = (CheckBoxPreference) findPreference( Prefs.KEY_SYNC_CHARGING );
		syncCharging.setOnPreferenceChangeListener( this );
		CheckBoxPreference syncWifi = (CheckBoxPreference) findPreference( Prefs.KEY_SYNC_WIFI );
		syncWifi.setOnPreferenceChangeListener( this );
		( (MarginLayoutParams) findViewById( android.R.id.list ).getLayoutParams() ).topMargin = Utils.getActionBarHeight( this );
		( (MarginLayoutParams) findViewById( android.R.id.list ).getLayoutParams() ).bottomMargin = getResources().getDimensionPixelSize(
				R.dimen.common_padding );
	}


	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		switch( item.getItemId() ) {
			case android.R.id.home:
				finish();
				break;
		}
		return super.onOptionsItemSelected( item );
	}


	@Override
	public boolean onPreferenceChange( Preference preference, Object newValue ) {
		if( preference.getKey().equals( Prefs.KEY_PUSH_SETTING ) ) {
			if( !Boolean.valueOf( newValue.toString() ) ) {
				AsyncTaskCompat.executeParallel( new UnregGCMTask( getApplicationContext() ) {
					ProgressDialog dlg;

					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						dlg = ProgressDialog.show( SettingActivity.this, null, getString( R.string.msg_push_unregistering ) );
						dlg.setCancelable( false );
					}

					@Override
					protected void onPostExecute( String regId ) {
						super.onPostExecute( regId );
						dlg.dismiss();
					}
				} );
			} else {
				AsyncTaskCompat.executeParallel( new RegGCMTask( getApplicationContext() ) {
					ProgressDialog dlg;

					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						dlg = ProgressDialog.show( SettingActivity.this, null, getString( R.string.msg_push_registering ) );
						dlg.setCancelable( false );
					}

					@Override
					protected void onPostExecute( String regId ) {
						super.onPostExecute( regId );
						dlg.dismiss();
					}
				} );
			}
		}
		if( preference.getKey().equals( Prefs.KEY_SYNC_CHARGING ) || preference.getKey().equals( Prefs.KEY_SYNC_WIFI ) ) {
			com.itbooks.utils.Utils.startAppGuardService( App.Instance );
		}
		return true;
	}

}
