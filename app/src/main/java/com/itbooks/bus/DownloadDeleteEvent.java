package com.itbooks.bus;


import com.itbooks.net.download.Download;

public final class DownloadDeleteEvent {
	private Download mDownload;

	public DownloadDeleteEvent( Download download ) {
		mDownload = download;
	}

	public Download getDownload() {
		return mDownload;
	}
}
