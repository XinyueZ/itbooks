package com.itbooks.bus;


import com.itbooks.net.download.Download;

public final class DownloadCompleteEvent {
	private Download mDownload;

	public DownloadCompleteEvent( Download download ) {
		mDownload = download;
	}


	public Download getDownload() {
		return mDownload;
	}
}
