package com.itbooks.bus;


import com.itbooks.net.download.Download;

public final class DownloadStartEvent {
	private Download mDownload;

	public DownloadStartEvent( Download download ) {
		mDownload = download;
	}

	public Download getDownload() {
		return mDownload;
	}
}
