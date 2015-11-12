package com.itbooks.bus;


import com.itbooks.net.download.Download;

public final class DownloadMoveEvent {
	private Download mDownload;

	public DownloadMoveEvent(Download download) {
		mDownload = download;
	}

	public Download getDownload() {
		return mDownload;
	}
}
