package com.itbooks.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigPictureStyle;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.text.TextUtils;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.chopping.net.TaskHelper;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.itbooks.R;
import com.itbooks.app.MainActivity;

public class GcmIntentService extends IntentService {
	private static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	private	NotificationCompat.Builder mNotifyBuilder;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
			/*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				//ignore.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				//ignore.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				sendNotification(extras);
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(final Bundle msg) {
		String bookId = msg.getString("book_id");
		final String title = msg.getString("title");
		final String desc = msg.getString("desc");
		final String image = msg.getString("image");

		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);


		if (!TextUtils.isEmpty(image)) {
			new Handler( Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					TaskHelper.getImageLoader().get(image, new ImageListener() {
						@Override
						public void onResponse(ImageContainer response, boolean isImmediate) {
							mNotifyBuilder = new NotificationCompat.Builder(GcmIntentService.this).setSmallIcon(
									R.drawable.ic_launcher).setTicker(title).setContentTitle(title).setContentText(desc)
									.setStyle(new BigPictureStyle().bigPicture(response.getBitmap()).setBigContentTitle(
													title).setSummaryText(desc)).setAutoCancel(true);

							mNotifyBuilder.setContentIntent(contentIntent);
							mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());

						}

						@Override
						public void onErrorResponse(VolleyError error) {
							mNotifyBuilder = new NotificationCompat.Builder(GcmIntentService.this).setSmallIcon(
									R.drawable.ic_launcher).setTicker(title).setContentTitle(title).setContentText(desc)
									.setStyle(new BigTextStyle().setBigContentTitle(title).setSummaryText(desc).bigText(
											desc)).setAutoCancel(true);

							mNotifyBuilder.setContentIntent(contentIntent);
							mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
						}
					});
				}
			});
		} else {
			mNotifyBuilder = new NotificationCompat.Builder(GcmIntentService.this).setSmallIcon(R.drawable.ic_launcher)
					.setTicker(title).setContentTitle(title).setContentText(desc).setStyle(
							new BigTextStyle().setBigContentTitle(title).setSummaryText(desc).bigText(desc)).setAutoCancel(true);

			mNotifyBuilder.setContentIntent(contentIntent);
			mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());

		}

	}
}
