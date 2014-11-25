package com.itbooks.adapters;

import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.chopping.net.TaskHelper;
import com.itbooks.R;
import com.itbooks.bus.DeleteBookmarkEvent;
import com.itbooks.bus.OpenBookmarkEvent;
import com.itbooks.data.DSBook;
import com.itbooks.data.DSBookmark;
import com.itbooks.views.OnViewAnimatedClickedListener;

import de.greenrobot.event.EventBus;

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

	public void setBookmarkList(LongSparseArray<DSBookmark> bookmarkList) {
		mBookmarkList = bookmarkList;
	}

	@Override
	public BookmarkListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View convertView = LayoutInflater.from(parent.getContext()).inflate(ITEM_LAYOUT, null);
		BookmarkListAdapter.ViewHolder viewHolder = new BookmarkListAdapter.ViewHolder(convertView);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position) {
		long id = mBookmarkList.keyAt(position);
		final DSBookmark bookmark = mBookmarkList.get(id);
		DSBook book = bookmark.getBook();
		viewHolder.mBookIdTv.setText(book.getId() + "");
		viewHolder.mBookCoverIv.setImageUrl(book.getImageUrl(), TaskHelper.getImageLoader());
		viewHolder.mBookCoverIv.setOnClickListener(new OnViewAnimatedClickedListener() {
			@Override
			public void onClick() {
				EventBus.getDefault().post(new OpenBookmarkEvent(bookmark));
			}
		});
		viewHolder.mDeleteBtn.setOnClickListener(new OnViewAnimatedClickedListener() {
			@Override
			public void onClick() {
				EventBus.getDefault().post(new DeleteBookmarkEvent(bookmark));
			}
		});
	}

	@Override
	public int getItemCount() {
		return mBookmarkList.size();
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		TextView mBookIdTv;
		NetworkImageView mBookCoverIv;
		Button mDeleteBtn;

		ViewHolder(View convertView) {
			super(convertView);
			mBookIdTv = (TextView) convertView.findViewById(R.id.book_id_tv);
			mBookCoverIv = (NetworkImageView) convertView.findViewById(R.id.bookmarked_book_cover_iv);
			mBookCoverIv.setDefaultImageResId(R.drawable.ic_launcher);
			mDeleteBtn = (Button) convertView.findViewById(R.id.delete_bookmark_btn);
		}
	}
}
