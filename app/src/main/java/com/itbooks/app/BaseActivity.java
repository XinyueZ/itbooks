package com.itbooks.app;


import java.lang.reflect.Field;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.ViewConfiguration;

import com.chopping.application.BasicPrefs;
import com.github.johnpersano.supertoasts.SuperCardToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.SuperToast.Animations;
import com.github.johnpersano.supertoasts.SuperToast.Background;
import com.github.johnpersano.supertoasts.SuperToast.IconPosition;
import com.github.johnpersano.supertoasts.SuperToast.Type;
import com.github.johnpersano.supertoasts.util.OnClickWrapper;
import com.github.johnpersano.supertoasts.util.Wrappers;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.itbooks.R;
import com.itbooks.app.fragments.AboutDialogFragment;
import com.itbooks.bus.CloseProgressDialogEvent;
import com.itbooks.bus.DownloadOpenEvent;
import com.itbooks.bus.OpenProgressDialogEvent;
import com.itbooks.net.download.DownloadReceiver;
import com.itbooks.utils.Prefs;

public abstract class BaseActivity extends com.chopping.activities.BaseActivity implements OnRefreshListener {
	protected SwipeRefreshLayout mRefreshLayout;
	/**
	 * Receiver for downloading reports.
	 */
	private DownloadReceiver mDownloadReceiver;

	private ProgressDialog mProgressDialog;


	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.itbooks.bus.OpenProgressDialogEvent}.
	 *
	 * @param e
	 * 		Event {@link  com.itbooks.bus.OpenProgressDialogEvent}.
	 */
	public void onEvent(OpenProgressDialogEvent e) {
		mProgressDialog = ProgressDialog.show(this, null, getString(R.string.msg_op));
		mProgressDialog.setCancelable(true);
	}

	/**
	 * Handler for {@link com.itbooks.bus.CloseProgressDialogEvent}.
	 *
	 * @param e
	 * 		Event {@link  com.itbooks.bus.CloseProgressDialogEvent}.
	 */
	public void onEvent(CloseProgressDialogEvent e) {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	/**
	 * Handler for {@link com.itbooks.bus.DownloadOpenEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.DownloadOpenEvent}.
	 */
	public void onEvent(DownloadOpenEvent e) {
		try {
			Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
			openFileIntent.setDataAndType(Uri.fromFile(e.getFile()), "application/pdf");
			openFileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(openFileIntent);
		} catch (Exception ex) {
			//Download pdf-reader.
			showDialogFragment(new DialogFragment() {
				@Override
				public Dialog onCreateDialog(Bundle savedInstanceState) {
					// Use the Builder class for convenient dialog construction
					android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
					builder.setTitle(R.string.application_name).setMessage(R.string.msg_no_reader).setPositiveButton(
							R.string.btn_ok, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									String pdfReader = "com.adobe.reader";
									try {
										startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
												"market://details?id=" + pdfReader)));
									} catch (android.content.ActivityNotFoundException exx) {
										startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
												"https://play.google.com/store/apps/details?id=" + pdfReader)));
									}
								}
							}).setNegativeButton(R.string.btn_not_yet_load, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// User cancelled the dialog
						}
					});
					// Create the AlertDialog object and return it
					return builder.create();
				}
			}, null);
		}
	}

	//------------------------------------------------


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception _e) {
			_e.printStackTrace();
		}

		final Wrappers wrappers = new Wrappers();
		//		wrappers.add(onClickWrapper);
		//		wrappers.add(onDismissWrapper);
		SuperCardToast.onRestoreState(savedInstanceState, this, wrappers);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setErrorHandlerAvailable(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mDownloadReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		registerReceiver(mDownloadReceiver = new DownloadReceiver(), intentFilter);
		final int isFound = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (isFound == ConnectionResult.SUCCESS ||
				isFound == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {//Ignore update.
			//The "End User License Agreement" must be confirmed before you use this application.
			if (!Prefs.getInstance(getApplication()).isEULAOnceConfirmed()) {
				showDialogFragment(AboutDialogFragment.EulaConfirmationDialog.newInstance(this), null);
			}
		} else {
			new AlertDialog.Builder(this).setTitle(R.string.application_name).setMessage(R.string.lbl_play_service)
					.setCancelable(false).setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(getString(R.string.play_service_url)));
					startActivity(intent);
					finish();
				}
			}).create().show();
		}
	}

	/**
	 * Show  {@link android.support.v4.app.DialogFragment}.
	 *
	 * @param _dlgFrg
	 * 		An instance of {@link android.support.v4.app.DialogFragment}.
	 * @param _tagName
	 * 		Tag name for dialog, default is "dlg". To grantee that only one instance of {@link
	 * 		android.support.v4.app.DialogFragment} can been seen.
	 */
	protected void showDialogFragment(DialogFragment _dlgFrg, String _tagName) {
		try {
			if (_dlgFrg != null) {
				DialogFragment dialogFragment = _dlgFrg;
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				// Ensure that there's only one dialog to the user.
				Fragment prev = getSupportFragmentManager().findFragmentByTag("dlg");
				if (prev != null) {
					ft.remove(prev);
				}
				try {
					if (TextUtils.isEmpty(_tagName)) {
						dialogFragment.show(ft, "dlg");
					} else {
						dialogFragment.show(ft, _tagName);
					}
				} catch (Exception _e) {
				}
			}
		} catch (Exception _e) {
		}
	}


	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance(getApplication());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Standard sharing app for sharing on actionbar.
	 */
	protected static Intent getDefaultShareIntent(android.support.v7.widget.ShareActionProvider provider,
			String subject, String body) {
		if (provider != null) {
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("text/plain");
			i.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			i.putExtra(android.content.Intent.EXTRA_TEXT, body);
			provider.setShareIntent(i);
			return i;
		}
		return null;
	}

	/**
	 * Close UI that should be closed for some reasons.
	 */
	protected void closeTroubleUI() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	protected void showWarningToast(String text, SuperToast.OnClickListener clickListener) {
		SuperCardToast toast = new SuperCardToast(this, Type.BUTTON);
		toast.setAnimations(Animations.POPUP);
		toast.setBackground(Background.BLUE);
		toast.setText(text);
		toast.setDuration(5000);
		toast.setButtonText(getString(R.string.btn_confirm));
		toast.setOnClickWrapper(new OnClickWrapper("showWarningToast", clickListener));
		toast.setTextColor(getResources().getColor(R.color.common_white));
		toast.setIcon(SuperToast.Icon.Dark.INFO, IconPosition.LEFT);
		toast.show();
	}

	protected void showErrorToast(String text, SuperToast.OnClickListener clickListener) {
		SuperCardToast toast = new SuperCardToast(this, Type.BUTTON);
		toast.setAnimations(Animations.FADE);
		toast.setBackground(Background.RED);
		toast.setText(text);
		toast.setIndeterminate(true);
		toast.setButtonText(getString(R.string.btn_retry));
		toast.setTextColor(getResources().getColor(R.color.common_white));
		toast.setIcon(SuperToast.Icon.Dark.INFO, IconPosition.LEFT);
		toast.setOnClickWrapper(new OnClickWrapper("showErrorToast", clickListener));
		toast.show();
	}

	protected void showInfoToast(String text) {
		SuperCardToast toast = new SuperCardToast(this, Type.STANDARD);
		toast.setAnimations(Animations.FLYIN);
		toast.setBackground(Background.GREEN);
		toast.setText(text);
		toast.setDuration(5000);
		toast.setTextColor(getResources().getColor(R.color.common_white));
		toast.setIcon(SuperToast.Icon.Dark.INFO, IconPosition.LEFT);
		toast.show();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		SuperCardToast.onSaveState(outState);

	}
}
