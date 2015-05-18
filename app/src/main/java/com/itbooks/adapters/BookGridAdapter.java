package com.itbooks.adapters;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.chopping.utils.DeviceUtils.ScreenSize;
import com.chopping.utils.Utils;
import com.itbooks.R;
import com.itbooks.bus.OpenBookDetailEvent;
import com.itbooks.data.rest.RSBook;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import de.greenrobot.event.EventBus;


public final class BookGridAdapter extends AbstractBookViewAdapter<BookGridAdapter.ViewHolder, RSBook> {
	/**
	 * Main layout for this component.
	 */
	private static final int ITEM_LAYOUT = R.layout.item_book_grid;
	private ScreenSize mScreenSize;
	private int mColCount;

	public BookGridAdapter(List<RSBook> books, int colCount, ScreenSize screenSize) {
		mColCount = colCount;
		mScreenSize = screenSize;
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
	public void onBindViewHolder(final ViewHolder holder, int position) {
		final RSBook book = getData().get(position);
		try {
			Picasso picasso = Picasso.with(holder.itemView.getContext());
			picasso.load(Utils.uriStr2URI(book.getCoverUrl()).toASCIIString()).transform(new Transformation() {

				public Bitmap getResizedBitmap(Bitmap bm, float newWidth, float newHeight) {
					int width = bm.getWidth();
					int height = bm.getHeight();
					float scaleWidth = newWidth / width;
					float scaleHeight = newHeight / height;
					// CREATE A MATRIX FOR THE MANIPULATION
					Matrix matrix = new Matrix();
					// RESIZE THE BIT MAP
					matrix.postScale(scaleWidth, scaleHeight);
					// "RECREATE" THE NEW BITMAP
					return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
				}

				@Override
				public Bitmap transform(Bitmap source) {
					float x = mScreenSize.Width / (mColCount + 0.f);
					float y = x * (source.getHeight() / (source.getWidth() + 0.f));
					Bitmap result = getResizedBitmap(source, x, y);
					if (result != source) {
						source.recycle();
					}
					return result;
				}

				@Override
				public String key() {
					return book.hashCode() + "";
				}
			}).placeholder(R.drawable.ic_launcher).tag(holder.itemView.getContext()).into(holder.mBookThumbIv);
		} catch (NullPointerException e) {
			holder.mBookThumbIv.setImageResource(R.drawable.ic_launcher);
		}
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
