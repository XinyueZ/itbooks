package com.itbooks.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

import com.itbooks.R;
import com.itbooks.gcm.RegGCMTask;
import com.itbooks.gcm.UnregGCMTask;


/**
 * Setting .
 */
public final class SettingActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

	/**
	 * The "ActionBar".
	 */
	private Toolbar mToolbar;


	private static final String KEY_PUSH_SETTING = "key.push.setting";

	/**
	 * Show an instance of SettingsActivity.
	 *
	 * @param context
	 * 		A context object.
	 */
	public static void showInstance(Context context) {
		Intent intent = new Intent(context, SettingActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		context.startActivity(intent);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		mToolbar = (Toolbar) getLayoutInflater().inflate(R.layout.toolbar, null, false);
		addContentView(mToolbar, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		mToolbar.setTitle(R.string.menu_setting);
		mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		mToolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		CheckBoxPreference push = (CheckBoxPreference) findPreference(KEY_PUSH_SETTING);
		push.setOnPreferenceChangeListener(this);
		((MarginLayoutParams) findViewById(android.R.id.list).getLayoutParams()).topMargin = getActionBarHeight(this);
	}


	/**
	 * Get height of {@link android.support.v7.app.ActionBar}.
	 *
	 * @param activity
	 * 		{@link android.app.Activity} that hosts an  {@link android.support.v7.app.ActionBar}.
	 *
	 * @return Height of bar.
	 */
	public static int getActionBarHeight(Activity activity) {
		int[] abSzAttr;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			abSzAttr = new int[] { android.R.attr.actionBarSize };
		} else {
			abSzAttr = new int[] { R.attr.actionBarSize };
		}
		TypedArray a = activity.obtainStyledAttributes(abSzAttr);
		return a.getDimensionPixelSize(0, -1);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals(KEY_PUSH_SETTING)) {
			if (!Boolean.valueOf(newValue.toString())) {
				new UnregGCMTask(getApplication()) {
					ProgressDialog dlg;
					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						dlg = ProgressDialog.show(SettingActivity.this, null, getString(R.string.msg_push_unregistering));
						dlg.setCancelable(false);
					}

					@Override
					protected void onPostExecute(String regId) {
						super.onPostExecute(regId);
						dlg.dismiss();
					}
				}.executeParallel();
			} else {
				new RegGCMTask(getApplication()) {
					ProgressDialog dlg;
					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						dlg = ProgressDialog.show(SettingActivity.this, null, getString(R.string.msg_push_registering));
						dlg.setCancelable(false);
					}

					@Override
					protected void onPostExecute(String regId) {
						super.onPostExecute(regId);
						dlg.dismiss();
					}
				}.executeParallel();
			}
		}
		return true;
	}

}
