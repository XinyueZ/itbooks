package com.itbooks.adapters;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.chopping.net.TaskHelper;
import com.itbooks.R;
import com.itbooks.bus.OpenBookDetailEvent;
import com.itbooks.data.rest.RSBook;

import de.greenrobot.event.EventBus;


public final class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.ViewHolder> {
	/**
	 * Main layout for this component.
	 */
	private static final int ITEM_LAYOUT = R.layout.item_book_list;


	/**
	 * {@link java.util.List} of all books.
	 */
	private List<RSBook> mBooks;


	public BookListAdapter(List<RSBook> books) {
		setData(books);
	}

	public void setData(List<RSBook> books) {
		mBooks = books;
	}


	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		Context cxt = parent.getContext();
		//		boolean landscape = cxt.getResources().getBoolean(R.bool.landscape);
		View convertView = LayoutInflater.from(cxt).inflate(ITEM_LAYOUT, parent, false);
		return new BookListAdapter.ViewHolder(convertView);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		final RSBook book = mBooks.get(position);
		holder.mBookThumbIv.setImageUrl(book.getCoverUrl(), TaskHelper.getImageLoader());
		holder.mBookTitleTv.setText(book.getName());
		holder.mBookSubTitleTv.setText(book.getAuthor());
		holder.mISBNTv.setText(String.format("ISBN: %s", book.getISBN()));
		holder.itemView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EventBus.getDefault().post(new OpenBookDetailEvent(book));
			}
		});
	}

	@Override
	public int getItemCount() {
		return mBooks == null ? 0 : mBooks.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public NetworkImageView mBookThumbIv;
		private TextView mBookTitleTv;
		private TextView mBookSubTitleTv;
		private TextView mISBNTv;

		/**
		 * Constructor of {@link com.itbooks.adapters.BookListAdapter.ViewHolder}.
		 *
		 * @param convertView
		 * 		The root {@link android.view.View}.
		 */
		public ViewHolder(View convertView) {
			super(convertView);
			mBookThumbIv = (NetworkImageView) convertView.findViewById(R.id.book_thumb_iv);
			mBookTitleTv = (TextView) convertView.findViewById(R.id.book_title_tv);
			mBookSubTitleTv = (TextView) convertView.findViewById(R.id.book_subtitle_tv);
			mISBNTv = (TextView) convertView.findViewById(R.id.book_isbn_tv);
		}
	}
}
