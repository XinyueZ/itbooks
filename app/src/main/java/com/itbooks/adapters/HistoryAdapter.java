package com.itbooks.adapters;

import java.io.File;
import java.util.List;

import android.app.DownloadManager;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itbooks.R;
import com.itbooks.bus.DownloadOpenEvent;
import com.itbooks.bus.OpenAllDownloadingsEvent;
import com.itbooks.data.rest.RSBook;
import com.itbooks.net.download.Download;

import de.greenrobot.event.EventBus;

/**
 * Adapter for history-list
 *
 * @author Xinyue Zhao
 */
public final class HistoryAdapter extends AbstractBookViewAdapter<HistoryAdapter.ViewHolder, Download> {
	/**
	 * Main layout for this component.
	 */
	private static final int ITEM_LAYOUT = R.layout.item_history;

	public HistoryAdapter(List<Download> downloadHistory) {
		setData(downloadHistory);
	}


	@Override
	public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View convertView = LayoutInflater.from(parent.getContext()).inflate(ITEM_LAYOUT, null);
		HistoryAdapter.ViewHolder viewHolder = new HistoryAdapter.ViewHolder(convertView);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(final ViewHolder viewHolder, int position) {
		final Download download = getData().get(position);
		RSBook book = download.getBook();
		viewHolder.mBookNameTv.setText(book.getName());CharSequence elapsedSeconds = DateUtils
				.getRelativeTimeSpanString(download.getTimeStamp(), System.currentTimeMillis(),
						DateUtils.MINUTE_IN_MILLIS);
		viewHolder.mTimeTv.setText(elapsedSeconds);
		setStatus(viewHolder, download);
		viewHolder.mDiv.setVisibility(position == getItemCount() - 1 ? View.GONE : View.VISIBLE);
	}

	private static void setStatus(final ViewHolder viewHolder, final Download download) {
		Resources resources = viewHolder.itemView.getContext().getResources();
		switch (download.getStatus()) {
		case DownloadManager.STATUS_PENDING:
			viewHolder.mStatusTv.setText(R.string.lbl_status_pending);
			viewHolder.mStatusTv.setTextColor(resources.getColor(R.color.book_downloading_full));
			viewHolder.mContentV.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
						EventBus.getDefault().post(new OpenAllDownloadingsEvent());
				}
			});
			break;
		case DownloadManager.STATUS_RUNNING:
			viewHolder.mStatusTv.setText(R.string.lbl_status_running);
			viewHolder.mStatusTv.setTextColor(resources.getColor(R.color.book_downloading_full));
			viewHolder.mContentV.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					EventBus.getDefault().post(new OpenAllDownloadingsEvent());
				}
			});
			break;
		case DownloadManager.STATUS_FAILED:
			viewHolder.mStatusTv.setText(R.string.lbl_status_failed);
			viewHolder.mStatusTv.setTextColor(resources.getColor(R.color.book_failed_downloaded_full));
			viewHolder.mContentV.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					EventBus.getDefault().post(new OpenAllDownloadingsEvent());
				}
			});
			break;
		case DownloadManager.STATUS_SUCCESSFUL:
			viewHolder.mStatusTv.setText(R.string.lbl_status_successfully);
			viewHolder.mStatusTv.setTextColor(resources.getColor(R.color.book_downloaded_full));

			viewHolder.mContentV.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					File to = new File(viewHolder.itemView.getContext().getExternalFilesDir(
							Environment.DIRECTORY_DOWNLOADS), download.getTargetName());
					if (to.exists()) {
						EventBus.getDefault().post(new DownloadOpenEvent(to));
					}
				}
			});
			break;
		}
	}

	static class ViewHolder extends RecyclerView.ViewHolder {
		private View mDiv;
		private View mContentV;
		private TextView mBookNameTv;
		private TextView mTimeTv;
		private TextView mStatusTv;

		ViewHolder(View convertView) {
			super(convertView);
			mContentV = convertView.findViewById(R.id.content_v);
			mBookNameTv = (TextView) convertView.findViewById(R.id.book_name_tv);
			mTimeTv = (TextView) convertView.findViewById(R.id.time_tv);
			mStatusTv = (TextView) convertView.findViewById(R.id.status_tv);
			mDiv = convertView.findViewById(R.id.div);
		}
	}
}
