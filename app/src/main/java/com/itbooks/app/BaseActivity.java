package com.itbooks.app;


import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;

import com.chopping.application.BasicPrefs;
import com.itbooks.utils.Prefs;

public abstract  class BaseActivity extends com.chopping.activities.BaseActivity  implements OnRefreshListener{
	protected View mInitLl;
	protected SwipeRefreshLayout mRefreshLayout;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setErrorHandlerAvailable(true);
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
}
