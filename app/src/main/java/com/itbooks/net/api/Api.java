package com.itbooks.net.api;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.util.Log;

import com.itbooks.data.rest.RSBookList;
import com.itbooks.data.rest.RSBookQuery;
import com.itbooks.data.rest.RSPushClient;
import com.itbooks.data.rest.RSResult;
import com.squareup.okhttp.OkHttpClient;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * APIs defining.
 *
 * @author Xinyue Zhao
 */
public final class Api {
	private static final String TAG = Api.class.getSimpleName();

	/**
	 * Response-cache.
	 */
	private static com.squareup.okhttp.Cache sCache;
	/**
	 * The host of API.
	 */
	private static String sHost = null;
	/**
	 * Response-cache size with default value.
	 */
	private static long sCacheSize = 1024 * 10;

	/**
	 * Http-client.
	 */
	private static OkClient sClient = null;


	/**
	 * Init the http-client and cache.
	 *
	 * @param cxt
	 * 		{@link Context}.
	 */
	private static void initClient(Context cxt) {
		// Create an HTTP client that uses a cache on the file system. Android applications should use
		// their Context to get a cache directory.
		OkHttpClient okHttpClient = new OkHttpClient();

		File cacheDir = new File(cxt != null ? cxt.getCacheDir().getAbsolutePath() : System.getProperty(
				"java.io.tmpdir"), UUID.randomUUID().toString());

		sCache = new com.squareup.okhttp.Cache(cacheDir, sCacheSize);

		okHttpClient.setCache(sCache);
		okHttpClient.setReadTimeout(3600, TimeUnit.SECONDS);
		okHttpClient.setConnectTimeout(3600, TimeUnit.SECONDS);
		sClient = new OkClient(okHttpClient);
	}

	/**
	 * To initialize API.
	 *  @param host
	 * 		The host of API.
	 * @param cacheSz
	 */
	public static void initialize(Context cxt, String host,  long cacheSz) {
		sHost = host;
		sCacheSize = cacheSz;
		initClient(cxt);
		initInterfaces();
	}

	/**
	 * Initialize all API interfaces.
	 */
	private static void initInterfaces() {
		RestAdapter adp = new RestAdapter.Builder().setClient(sClient).setEndpoint(sHost).build();
		sBooksApi = adp.create(BooksApi.class);
		sPushApi = adp.create(PushApi.class);
	}

	/**
	 * Assert calling for each method.
	 *
	 * @throws ApiNotInitializedException
	 * 		The {@link Api} must call {@link #initialize(Context, String,    long)} first before any using.
	 */
	private static final void assertCall() throws ApiNotInitializedException {
		if (sClient == null) {//Create http-client when needs.
			throw new ApiNotInitializedException();
		}
		if (sHost == null) {//Default when needs.
			throw new ApiNotInitializedException();
		}
		Log.i(TAG, String.format("Host:%s, Cache:%d", sHost, sCacheSize));
		if (sCache != null) {
			Log.i(TAG, String.format("RequestCount:%d", sCache.getRequestCount()));
			Log.i(TAG, String.format("NetworkCount:%d", sCache.getNetworkCount()));
			Log.i(TAG, String.format("HitCount:%d", sCache.getHitCount()));
		} else {
			throw new ApiNotInitializedException();
		}
	}





	//-----------------------------------------------------------------------------------------
	//Book list in rest.
	//-----------------------------------------------------------------------------------------
	interface BooksApi {
		@POST("/download")
		void getBooks(@Body RSBookQuery query,  Callback<RSBookList> callback);
	}

	private static BooksApi sBooksApi;


	public static final void queryBooks(RSBookQuery query, Callback<RSBookList> callback) throws ApiNotInitializedException {
		assertCall();
		sBooksApi.getBooks(query, callback);
	}

	//-----------------------------------------------------------------------------------------
	//Push register and unregister.
	//-----------------------------------------------------------------------------------------
	interface PushApi {
		@POST("/insert")
		void reg(@Body RSPushClient client,  Callback<RSResult> callback);
		@POST("/del")
		void unreg(@Body RSPushClient client,  Callback<RSResult> callback);
	}

	private static PushApi sPushApi;

	public static final void regPush(RSPushClient client, Callback<RSResult> callback) throws ApiNotInitializedException {
		assertCall();
		sPushApi.reg(client, callback);
	}

	public static final void unregPush(RSPushClient client, Callback<RSResult> callback) throws ApiNotInitializedException {
		assertCall();
		sPushApi.unreg(client, callback);
	}
}
