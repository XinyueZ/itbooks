package com.itbooks.app.noactivities;


import android.content.Context;
import android.text.TextUtils;

import com.chopping.utils.NetworkUtils;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.itbooks.R;
import com.itbooks.app.App;
import com.itbooks.net.SyncService;
import com.itbooks.utils.NotifyUtils;
import com.itbooks.utils.Prefs;

public final class AppGuardService extends GcmTaskService {
	private static final int NOTIFY_ID = 0x07;

	@Override
	public int onRunTask( TaskParams taskParams ) {
		if( !TextUtils.isEmpty( Prefs.getInstance( getApplication() ).getGoogleId() ) ) {
			Prefs   prefs    = Prefs.getInstance( getApplicationContext() );
			boolean syncWifi = prefs.syncWifi();
			if( syncWifi ) {
				boolean isWifiOn = NetworkUtils.getCurrentNetworkType( App.Instance ) == NetworkUtils.CONNECTION_WIFI;
				if( isWifiOn ) {
					notifySync( this );
					SyncService.startSync( this );
				}
			} else {
				notifySync( this );
				SyncService.startSync( this );
			}
		}
		return GcmNetworkManager.RESULT_SUCCESS;
	}

	private static void notifySync( Context cxt ) {
		NotifyUtils.notifyWithoutBigImage( cxt, NOTIFY_ID, cxt.getString( R.string.application_name ),
										   cxt.getString( R.string.msg_scheduled_sync_done ), android.R.drawable.stat_notify_sync,
										   com.itbooks.utils.NotifyUtils.getAppHome( cxt )
		);
	}
}
