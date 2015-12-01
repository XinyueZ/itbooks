package com.itbooks.data.rest;


import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public final class RSPushClient implements Serializable {
	@SerializedName("uid")
	private String mUID;
	@SerializedName("pushId")
	private String mRegId;


	public RSPushClient( String UID, String regId ) {
		mUID = UID;
		mRegId = regId;
	}


	public String getUID() {
		return mUID;
	}

	public String getRegId() {
		return mRegId;
	}
}
