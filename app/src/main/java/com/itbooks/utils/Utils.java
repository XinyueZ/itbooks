package com.itbooks.utils;


import android.content.Context;

import com.chopping.utils.NetworkUtils;
import com.itbooks.app.App;
import com.itbooks.bus.CellNetworkNoImageWarningEvent;

import de.greenrobot.event.EventBus;

public final class Utils {
	public static final boolean showImage(Context cxt) {
		boolean showImage;
		Prefs prefs = Prefs.getInstance(cxt);
		boolean isWifiOn = NetworkUtils.getCurrentNetworkType(App.Instance) == NetworkUtils.CONNECTION_WIFI;
		boolean neverImages = prefs.noImages();
		boolean onlyWifiImages = prefs.showImagesOnlyWifi();

		if(!isWifiOn) {//cell network
			if(neverImages) {
				showImage = false;
			} else {
				if(onlyWifiImages) {
					showImage = true;
				} else {
					showImage = false;
					EventBus.getDefault().postSticky(new CellNetworkNoImageWarningEvent());
				}
			}
		} else {//wifi
			if(neverImages) {
				showImage = false;
			} else {
				showImage = true;
			}
		}

		return showImage;
	}
}
