package com.itbooks.app;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.chopping.application.BasicPrefs;
import com.itbooks.app.fragments.AboutDialogFragment;
import com.itbooks.utils.Prefs;

public abstract class BaseActivity extends com.chopping.activities.BaseActivity implements OnRefreshListener {
	protected View mInitLl;
	protected SwipeRefreshLayout mRefreshLayout;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setErrorHandlerAvailable(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		//The "End User License Agreement" must be confirmed before you use this application.
		if (!Prefs.getInstance(getApplication()).isEULAOnceConfirmed()) {
			showDialogFragment(AboutDialogFragment.EulaConfirmationDialog.newInstance(this), null);
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


	protected void dismissInitView() {
		mInitLl.setVisibility(View.GONE);
	}

	protected void showInitView() {
		mInitLl.setVisibility(View.VISIBLE);
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
}
