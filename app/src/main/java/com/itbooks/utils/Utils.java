package com.itbooks.utils;


import android.content.Context;

import com.chopping.utils.NetworkUtils;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.itbooks.app.App;
import com.itbooks.app.AppGuardService;
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

	/**
	 * A background service that for automatically sync.
	 */
	public static void startAppGuardService(Context cxt) {
		GcmNetworkManager mgr = GcmNetworkManager.getInstance(cxt);
		try {
			mgr.cancelAllTasks(AppGuardService.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Prefs prefs = Prefs.getInstance(cxt.getApplicationContext());
		long periodSecs = 18000; // the task should be executed every 18 000 seconds (5 hours).
		long flexSecs = 300; // the task can run as early as 5 minutes seconds from the scheduled time
		String tag = System.currentTimeMillis() + "";
		PeriodicTask periodic = new PeriodicTask.Builder()
				.setService(AppGuardService.class)
				.setPeriod(periodSecs)
				.setFlex(flexSecs)
				.setTag(tag)
				.setPersisted(true)
				.setRequiredNetwork(com.google.android.gms.gcm.Task.NETWORK_STATE_ANY)
				.setRequiresCharging(prefs.syncCharging())
				.build();
		mgr.schedule(periodic);
	}
}
