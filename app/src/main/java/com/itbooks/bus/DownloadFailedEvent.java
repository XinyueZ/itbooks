package com.itbooks.bus;


import com.itbooks.net.download.Download;

public final class DownloadFailedEvent {
	private Download mDownload;

	public DownloadFailedEvent(Download download) {
		mDownload = download;
	}

	public Download getDownload() {
		return mDownload;
	}
}
