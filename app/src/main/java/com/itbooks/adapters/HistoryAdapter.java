package com.itbooks.adapters;

import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itbooks.R;
import com.itbooks.data.rest.RSBook;
import com.itbooks.net.download.Download;

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
		viewHolder.mDiv.setVisibility(  position == getItemCount() - 1 ?  View.GONE : View.VISIBLE);
	}


	static class ViewHolder extends RecyclerView.ViewHolder {
		private View mDiv;
		private TextView mBookNameTv;
		private TextView mTimeTv;

		ViewHolder(View convertView) {
			super(convertView);
			mBookNameTv = (TextView) convertView.findViewById(R.id.book_name_tv);
			mTimeTv = (TextView) convertView.findViewById(R.id.time_tv);
			mDiv = convertView.findViewById(R.id.div);
		}
	}
}
