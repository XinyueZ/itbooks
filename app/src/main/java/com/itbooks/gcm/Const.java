package com.itbooks.gcm;


interface Const {
	static final String HOST = "http://itbooks-live-release-surge-778.appspot.com";
	/**
	 * Substitute you own sender ID here. This is the project number you got from the API Console, as described in
	 * "Getting Started."
	 */
	static final String SENDER_ID = "1086395265343";
	static final String URL_INFO_BACKEND_REG = HOST + "/insert";
	static final String URL_INFO_BACKEND_UNREG = HOST + "/del";
}
