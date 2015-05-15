package com.itbooks.data.rest;


import com.google.gson.annotations.SerializedName;

public final class RSResult {
	@SerializedName("status")
	private int mStatus;

	public RSResult(int status) {
		mStatus = status;
	}

	public int getStatus() {
		return mStatus;
	}
}
