package com.itbooks.app.fragments;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.os.AsyncTaskCompat;
import android.view.View;

import com.chopping.utils.Utils;
import com.itbooks.R;
import com.itbooks.app.App;
import com.itbooks.net.download.Download;
import com.turhanoz.android.reactivedirectorychooser.event.OnDirectoryChosenEvent;
import com.turhanoz.android.reactivedirectorychooser.ui.DirectoryChooserFragment;

import org.apache.commons.io.FileUtils;


public final class DownloadDirChooserDialogFragment extends DirectoryChooserFragment {
	public static final int COPY = 4;
	private static final String EXTRAS_DOWNLOAD = DownloadDirChooserDialogFragment.class.getName() + ".EXTRAS.download";
	private static final String EXTRAS_COPY_OR_MOVE =
			DownloadDirChooserDialogFragment.class.getName() + ".EXTRAS.copyOrMove";

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link OnDirectoryChosenEvent}.
	 *
	 * @param e
	 * 		Event {@link OnDirectoryChosenEvent}.
	 */
	public void onEvent(final OnDirectoryChosenEvent e) {
		Bundle args = getArguments();
		final Download download = (Download) args.getSerializable(EXTRAS_DOWNLOAD);
		int copyOrMove = args.getInt(EXTRAS_COPY_OR_MOVE);
		File from = new File(App.Instance.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
				download.getTargetName());
		File to = new File(e.getFile(), from.getName());
		if (from.exists() && !to.exists()) {
			switch (copyOrMove) {
			case COPY: //copy
				AsyncTaskCompat.executeParallel(new AsyncTask<File, Void, IOException>() {
					@Override
					protected IOException doInBackground(File... params) {
						try {
							FileUtils.copyFile(params[0], params[1]);
							return null;
						} catch (IOException e1) {
							return e1;
						}
					}

					@Override
					protected void onPostExecute(IOException e) {
						super.onPostExecute(e);
						if (e == null) {
							Utils.showLongToast(App.Instance, R.string.msg_file_copied);
						} else {
							Utils.showLongToast(App.Instance, R.string.lbl_status_failed);
						}
					}
				}, from, to);
				break;
			}
		} else {
			if(to.exists()) {
				Utils.showLongToast(App.Instance, R.string.msg_file_duplicated);
			}
		}
	}

	//------------------------------------------------

	public static DownloadDirChooserDialogFragment newInstance(Context cxt, int type, Download download,
			File externalFilesDir) {
		Bundle args = new Bundle();
		args.putInt(EXTRAS_COPY_OR_MOVE, type);
		args.putSerializable(EXTRAS_DOWNLOAD, download);
		args.putSerializable("rootDirectory", externalFilesDir);
		return (DownloadDirChooserDialogFragment) DownloadDirChooserDialogFragment.instantiate(cxt,
				DownloadDirChooserDialogFragment.class.getName(), args);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		int mCopyOrMove = getArguments().getInt(EXTRAS_COPY_OR_MOVE);
		switch (mCopyOrMove) {
		case COPY: //copy
			getDialog().setTitle(R.string.menu_book_copy);
			break;
		}
	}
}
