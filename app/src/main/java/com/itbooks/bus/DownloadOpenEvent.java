package com.itbooks.bus;


import java.io.File;

public final class DownloadOpenEvent {
	private File mFile;

	public DownloadOpenEvent( File file ) {
		mFile = file;
	}

	public File getFile() {
		return mFile;
	}
}
