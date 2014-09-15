package com.itbooks.app;


import android.os.Bundle;

import com.chopping.application.BasicPrefs;
import com.itbooks.utils.Prefs;

public   class BaseActivity extends com.chopping.activities.BaseActivity{

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setErrorHandlerAvailable(false);
	}

	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance(getApplication());
	}
}
