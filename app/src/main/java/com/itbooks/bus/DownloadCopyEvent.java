package com.itbooks.bus;


import com.itbooks.net.download.Download;

public final class DownloadCopyEvent {
	private Download mDownload;

	public DownloadCopyEvent(Download download) {
		mDownload = download;
	}

	public Download getDownload() {
		return mDownload;
	}
}
