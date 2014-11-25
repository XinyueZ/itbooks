package com.itbooks.adapters;

import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.chopping.net.TaskHelper;
import com.itbooks.R;
import com.itbooks.data.DSBook;
import com.itbooks.data.DSBookmark;

/**
 * Adapter for bookmark-list.
 *
 * @author Xinyue Zhao
 */
public final class BookmarkListAdapter extends RecyclerView.Adapter<BookmarkListAdapter.ViewHolder> {
	/**
	 * Main layout for this component.
	 */
	private static final int ITEM_LAYOUT = R.layout.item_bookmark_list;
	private LongSparseArray<DSBookmark> mBookmarkList;

	public BookmarkListAdapter(LongSparseArray<DSBookmark> bookmarkList) {
		mBookmarkList = bookmarkList;
	}

	@Override
	public BookmarkListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View convertView = LayoutInflater.from(parent.getContext()).inflate(ITEM_LAYOUT, null);
		ViewHolder viewHolder = new ViewHolder(convertView);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position) {
		long id = mBookmarkList.keyAt(position);
		DSBookmark bookmark = mBookmarkList.get(id);
		DSBook book = bookmark.getBook();
		viewHolder.mBookIdTv.setText(book.getId() + "");
		viewHolder.mBookCoverIv.setImageUrl(book.getImageUrl(), TaskHelper.getImageLoader());
	}

	@Override
	public int getItemCount() {
		return mBookmarkList.size();
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		public TextView mBookIdTv;
		public NetworkImageView mBookCoverIv;

		public ViewHolder(View convertView) {
			super(convertView);
			mBookIdTv = (TextView) convertView.findViewById(R.id.book_id_tv);
			mBookCoverIv = (NetworkImageView) convertView.findViewById(R.id.bookmarked_book_cover_iv);
		}
	}
}
