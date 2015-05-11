package com.itbooks.adapters;

import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.itbooks.R;
import com.itbooks.bus.DeleteBookmarkEvent;
import com.itbooks.bus.OpenBookmarkEvent;
import com.itbooks.data.DSBookmark;
import com.itbooks.data.rest.RSBook;
import com.itbooks.views.OnViewAnimatedClickedListener;
import com.squareup.picasso.Picasso;

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
	public void onBindViewHolder(final ViewHolder viewHolder, int position) {
		long id = mBookmarkList.keyAt(position);
		final DSBookmark bookmark = mBookmarkList.get(id);
		RSBook book = bookmark.getBook();



		Picasso.with(viewHolder.itemView.getContext())
				.load(book.getCoverUrl())
				.placeholder(R.drawable.ic_launcher)
				.tag(viewHolder.itemView.getContext())
				.into(viewHolder.mBookCoverIv);

		viewHolder.mBookCoverIv.setOnClickListener(new OnViewAnimatedClickedListener() {
			@Override
			public void onClick() {
				EventBus.getDefault().post(new OpenBookmarkEvent(bookmark ));
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
		return mBookmarkList == null ? 0 : mBookmarkList.size();
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		ImageView mBookCoverIv;
		Button mDeleteBtn;

		ViewHolder(View convertView) {
			super(convertView);
			mBookCoverIv = (ImageView) convertView.findViewById(R.id.bookmarked_book_cover_iv);
			mDeleteBtn = (Button) convertView.findViewById(R.id.delete_bookmark_btn);
		}
	}
}
