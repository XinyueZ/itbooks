package com.itbooks.gcm;

import java.io.IOException;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.text.TextUtils;

import com.chopping.utils.Utils;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.itbooks.R;
import com.itbooks.app.BookDetailActivity;
import com.itbooks.data.rest.RSBook;
import com.squareup.picasso.Picasso;

public class GcmIntentService extends IntentService {
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
				Utils.showLongToast(this, GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR );
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				Utils.showLongToast(this, GoogleCloudMessaging.MESSAGE_TYPE_DELETED );
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
		final String title = msg.getString("Name");
		final String desc = msg.getString("Description");
		final String image = msg.getString("CoverUrl");

		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(this, BookDetailActivity.class);
		intent.putExtra(BookDetailActivity.EXTRAS_BOOK, RSBook.newInstance(msg));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		final PendingIntent contentIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT);


		if (!TextUtils.isEmpty(image)) {
			Picasso picasso = Picasso.with(this);
			try {
				notify(title, desc, image, contentIntent, picasso);
			} catch(NullPointerException | IOException e) {
				fallbackNotify(title, desc, contentIntent);
			}

		} else {
			fallbackNotify(title, desc, contentIntent);
		}
	}

	private void notify(String title, String desc, String image, PendingIntent contentIntent, Picasso picasso) throws
			IOException {
		Bitmap bitmap = picasso.load(Utils.uriStr2URI(image).toASCIIString()).get();
		mNotifyBuilder = new NotificationCompat.Builder(GcmIntentService.this).setWhen(
				System.currentTimeMillis()).setSmallIcon(R.drawable.ic_launcher).setTicker(title)
				.setContentTitle(title).setContentText(desc).setStyle(new BigTextStyle().bigText(desc)
						.setBigContentTitle(title)).setAutoCancel(true).setLargeIcon(bitmap);
		mNotifyBuilder.setContentIntent(contentIntent);


		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
			mNotifyBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000 });
			mNotifyBuilder.setSound(Uri.parse(String.format("android.resource://%s/%s", getPackageName(), R.raw.signal)));
		}
		mNotifyBuilder.setLights(getResources().getColor(R.color.primary_color), 1000, 1000);

		mNotificationManager.notify((int) System.currentTimeMillis(), mNotifyBuilder.build());
	}

	private void fallbackNotify(String title, String desc, PendingIntent contentIntent) {
		mNotifyBuilder = new NotificationCompat.Builder(GcmIntentService.this).setWhen(System.currentTimeMillis()).setSmallIcon(
				R.drawable.ic_launcher).setTicker(title).setContentTitle(title).setContentText(desc).setStyle(
				new BigTextStyle().bigText(desc).setBigContentTitle(title)).setAutoCancel(true);
		mNotifyBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify((int) System.currentTimeMillis(), mNotifyBuilder.build());
	}
}
