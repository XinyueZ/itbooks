package com.itbooks.bus;


import com.itbooks.net.download.Download;

public final class DownloadEndEvent {
	private Download mDownload;

	public DownloadEndEvent( Download download ) {
		mDownload = download;
	}

	public Download getDownload() {
		return mDownload;
	}
}
