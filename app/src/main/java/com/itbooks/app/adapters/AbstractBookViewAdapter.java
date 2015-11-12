package com.itbooks.app.adapters;


import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;

import com.itbooks.data.rest.RSBook;

public abstract class AbstractBookViewAdapter<T extends ViewHolder, B extends RSBook> extends RecyclerView.Adapter<T>{

	private List<B> mBooks;

	public void setData(List<B> books) {
		mBooks = books;
	}

	public List<B> getData() {
		return mBooks;
	}

	@Override
	public int getItemCount() {
		return mBooks == null ? 0 : mBooks.size();
	}
}
