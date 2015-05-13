package com.itbooks.net.download;

import java.io.File;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.chopping.utils.Utils;
import com.itbooks.App;
import com.itbooks.bus.DownloadEndEvent;
import com.itbooks.bus.DownloadFailedEvent;
import com.itbooks.bus.DownloadOpenEvent;
import com.itbooks.bus.DownloadStartEvent;
import com.itbooks.data.rest.RSBook;
import com.itbooks.db.DB;

import de.greenrobot.event.EventBus;

/**
 * Download ebook.
 * <p/>
 * See events:
 * <p/>
 * {@link DownloadStartEvent}:Start downloading,
 * <p/>
 * {@link DownloadEndEvent}:End downloading,
 * <p/>
 * {@link DownloadOpenEvent}:Open downloaded file.
 * <p/>
 * @author Xinyue Zhao
 */
public final class Download {
	/**
	 * The file to load.
	 */
	private RSBook mBook;
	/**
	 * Time that loaded file. {@link com.itbooks.db.DownloadsTbl#EDIT_TIME}.
	 */
	private long mTimeStamp;
	/**
	 * The unique name when file saved.
	 */
	private String mTargetName;
	/**
	 * The ident given by android when start downloading.
	 */
	private long mDownloadId;

	/**
	 * Constructor of {@link Download}.
	 *
	 * @param book
	 * 		A book to download.
	 */
	public Download(RSBook book) {
		mBook = book;
		mTargetName = App.PREFIX + mBook.getName() + ".pdf";
	}

	/**
	 * Start downloading.
	 * <p/>
	 * When the file has been loaded, then directly to end.
	 *
	 * @param cxt
	 * 		{@link Context}.
	 */
	public void start(Context cxt) {
		//To check whether we've loaded.
		File to = new File(cxt.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), mTargetName);
		if (to.exists()) {
			EventBus.getDefault().post(new DownloadOpenEvent(to));
		} else {
			EventBus.getDefault().post(new DownloadStartEvent(this));
			DownloadManager downloadManager = (DownloadManager) cxt.getSystemService(Context.DOWNLOAD_SERVICE);
			mTimeStamp = System.currentTimeMillis();

			DownloadManager.Request request = new DownloadManager.Request(
					Uri.parse(Utils.uriStr2URI(mBook.getLink()).toASCIIString()));
			request.setDestinationInExternalFilesDir(cxt, Environment.DIRECTORY_DOWNLOADS, mTargetName);
			request.setVisibleInDownloadsUi(true);//Can see the downloaded file in "download" app.
//			if (Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
//				request.setNotificationVisibility(Request.VISIBILITY_HIDDEN);
//			}
			mDownloadId = downloadManager.enqueue(request);
			DB.getInstance(cxt).insertNewDownload(this);
		}
	}

	/**
	 * Test whether a book is already available local.
	 * @param cxt {@link Context}.
	 * @param book {@link RSBook} The book.
	 * @return {@code true} if already exist to read.
	 */
	public static boolean exists(Context cxt, RSBook book) {
		Download download  = new Download(book);
		File to = new File(cxt.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), download.getTargetName());
		return to.exists();
	}

	/**
	 * Test whether is being downloaded.
	 * @param cxt {@link Context}.
	 * @param book {@link RSBook} The book.
	 * @return {@code true} if the book is being downloaded.
	 */
	public static boolean downloading(Context cxt, RSBook book) {
		DownloadManager downloadManager = (DownloadManager) cxt.getSystemService(Context.DOWNLOAD_SERVICE);
		return false;
	}


	/**
	 * End of downloading.
	 *
	 * @param cxt
	 * 		{@link Context}.
	 */
	public void end(Context cxt) {
		EventBus.getDefault().post(new DownloadEndEvent(this));

	}

	/**
	 * Fail on downloading.
	 *
	 * @param cxt
	 * 		{@link Context}.
	 */
	public void failed(Context cxt) {
		EventBus.getDefault().post(new DownloadFailedEvent(this));

	}

	/**
	 * @return The unique name when file saved.
	 */
	public String getTargetName() {
		return mTargetName;
	}

	/**
	 * @return The ident given by android when start downloading.
	 */
	public long getDownloadId() {
		return mDownloadId;
	}

	/**
	 * @return Time that loaded file. {@link com.itbooks.db.DownloadsTbl#EDIT_TIME}.
	 */
	public long getTimeStamp() {
		return mTimeStamp;
	}

	/**
	 * @return The file to load.
	 */
	public RSBook getBook() {
		return mBook;
	}
}
