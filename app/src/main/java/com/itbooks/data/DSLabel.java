package com.itbooks.data;

/**
 * Label
 *
 * @author Xinyue Zhao
 */
public final class DSLabel {
	private long mId = -1;
	private String mName;
	private long mEditTime;

	public DSLabel(long id, String name, long editTime) {
		mId = id;
		mName = name;
		mEditTime = editTime;
	}

	public DSLabel(String name, long editTime) {
		mName = name;
		mEditTime = editTime;
	}

	public DSLabel(String name) {
		mName = name;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public long getEditTime() {
		return mEditTime;
	}

	public void setEditTime(long editTime) {
		mEditTime = editTime;
	}
}
