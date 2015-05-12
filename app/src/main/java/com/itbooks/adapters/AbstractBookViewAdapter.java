package com.itbooks.adapters;


import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;

import com.itbooks.data.rest.RSBook;

public abstract class AbstractBookViewAdapter<T extends ViewHolder> extends RecyclerView.Adapter<T>{

	private List<RSBook> mBooks;

	public void setData(List<RSBook> books) {
		mBooks = books;
	}

	public List<RSBook> getData() {
		return mBooks;
	}

	@Override
	public int getItemCount() {
		return mBooks == null ? 0 : mBooks.size();
	}
}
