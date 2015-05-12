package com.itbooks.adapters;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.itbooks.R;
import com.itbooks.bus.OpenBookDetailEvent;
import com.itbooks.data.rest.RSBook;
import com.squareup.picasso.Picasso;

import de.greenrobot.event.EventBus;


public final class BookGridAdapter extends AbstractBookViewAdapter<BookGridAdapter.ViewHolder> {
	/**
	 * Main layout for this component.
	 */
	private static final int ITEM_LAYOUT = R.layout.item_book_grid;


	public BookGridAdapter(List<RSBook> books) {
		setData(books);
	}


	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		Context cxt = parent.getContext();
		//		boolean landscape = cxt.getResources().getBoolean(R.bool.landscape);
		View convertView = LayoutInflater.from(cxt).inflate(ITEM_LAYOUT, parent, false);
		return new BookGridAdapter.ViewHolder(convertView);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		final RSBook book = getData().get(position);
		Picasso picasso = Picasso.with(holder.itemView.getContext());
//		picasso.setIndicatorsEnabled(true);
		picasso.load(book.getCoverUrl()).placeholder(R.drawable.ic_launcher) .tag(
				holder.itemView.getContext()).into(holder.mBookThumbIv);
		 holder.itemView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EventBus.getDefault().post(new OpenBookDetailEvent(book));
			}
		});
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private ImageView mBookThumbIv;
		/**
		 * Constructor of {@link BookGridAdapter.ViewHolder}.
		 *
		 * @param convertView
		 * 		The root {@link View}.
		 */
		public ViewHolder(View convertView) {
			super(convertView);
			mBookThumbIv = (ImageView) convertView.findViewById(R.id.book_thumb_iv);
		}
	}
}
