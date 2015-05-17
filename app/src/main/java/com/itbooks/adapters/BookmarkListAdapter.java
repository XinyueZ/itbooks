package com.itbooks.adapters;

import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.chopping.utils.Utils;
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
public final class BookmarkListAdapter extends AbstractBookViewAdapter<BookmarkListAdapter.ViewHolder, DSBookmark> {
	/**
	 * Main layout for this component.
	 */
	private static final int ITEM_LAYOUT = R.layout.item_bookmark_list;
	public BookmarkListAdapter(List<DSBookmark> bookmarkList) {
		setData(bookmarkList);
	}


	@Override
	public BookmarkListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View convertView = LayoutInflater.from(parent.getContext()).inflate(ITEM_LAYOUT, null);
		BookmarkListAdapter.ViewHolder viewHolder = new BookmarkListAdapter.ViewHolder(convertView);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(final ViewHolder viewHolder, int position) {
		final DSBookmark bookmark = getData().get(position);
		RSBook book = bookmark.getBook();
		Picasso picasso = Picasso.with(viewHolder.itemView.getContext());
		picasso.load(Utils.uriStr2URI(book.getCoverUrl()).toASCIIString())
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


	static class ViewHolder extends RecyclerView.ViewHolder {

		private ImageView mBookCoverIv;
		private Button mDeleteBtn;

		ViewHolder(View convertView) {
			super(convertView);
			mBookCoverIv = (ImageView) convertView.findViewById(R.id.bookmarked_book_cover_iv);
			mDeleteBtn = (Button) convertView.findViewById(R.id.delete_bookmark_btn);
		}
	}
}
