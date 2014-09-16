package com.itbooks.adapters;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.chopping.net.TaskHelper;
import com.itbooks.R;
import com.itbooks.data.DSBook;

/**
 * {@link android.widget.Adapter} for all book {@link android.widget.ListView}
 *
 * @author Xinyue Zhao
 */
public final class BookListAdapter extends BaseAdapter {
	/**
	 * Main layout for this component.
	 */
	private static final int ITEM_LAYOUT = R.layout.item_book_list;


	/**
	 * {@link java.util.List} of all books.
	 */
	private List<DSBook> mBooks;


	public BookListAdapter(List<DSBook> books) {
		setData(books);
	}

	public void setData(List<DSBook> books) {
		mBooks = books;
	}

	@Override
	public int getCount() {
		return mBooks == null ? 0 : mBooks.size();
	}

	@Override
	public Object getItem(int position) {
		return mBooks.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(ITEM_LAYOUT, parent, false);
			viewHolder = new ViewHolder(convertView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		DSBook book = mBooks.get(position);
		viewHolder.mBookThumbIv.setImageUrl(book.getImageUrl(), TaskHelper.getImageLoader());
		viewHolder.mBookTitleTv.setText(book.getTitle());
		viewHolder.mBookSubTitleTv.setText(book.getSubTitle());
		viewHolder.mISBNTv.setText(String.format("ISBN: %s", book.getISBN()));
		return convertView;
	}

	public List<DSBook> getBooks() {
		return mBooks;
	}

	/**
	 * ViewHolder pattern.
	 *
	 * @author Xinyue Zhao.
	 */
	private static class ViewHolder {
		private NetworkImageView mBookThumbIv;
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
			mBookThumbIv = (NetworkImageView) convertView.findViewById(R.id.book_thumb_iv);
			mBookTitleTv = (TextView) convertView.findViewById(R.id.book_title_tv);
			mBookSubTitleTv = (TextView) convertView.findViewById(R.id.book_subtitle_tv);
			mISBNTv = (TextView) convertView.findViewById(R.id.book_isbn_tv);
		}
	}
}
