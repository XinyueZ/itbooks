package com.itbooks.data.rest;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public final class RSBookQuery implements Serializable {
	@SerializedName("query")
	private String mQuery;


	public RSBookQuery( String query ) {
		mQuery = query;
	}

	public String getQuery() {
		return mQuery;
	}
}
