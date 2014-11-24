package com.itbooks.app;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.chopping.utils.Utils;
import com.itbooks.R;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

public final class DownloadWebViewActivity extends BaseActivity implements DownloadListener {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_download_webview;
	/**
	 * There is different between android pre 3.0 and 3.x, 4.x on this wording.
	 */
	private static final String ALPHA =
			(android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) ? "alpha" : "Alpha";
	private static final String EXTRAS_URL = "com.itbooks.app.WebViewActivity.url";

	private WebView mWebView;
	private String mUrl;



	/**
	 * The menu to this view.
	 */
	private static final int MENU = R.menu.webview;

	/**
	 * Show single instance of {@link com.itbooks.app.DownloadWebViewActivity}
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 * @param url
	 * 		Url that we can load file.
	 */
	public static void showInstance(Context cxt, String url) {
		Intent intent = new Intent(cxt, DownloadWebViewActivity.class);
		intent.putExtra(EXTRAS_URL, url);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		cxt.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);


		String downloadPath = getString(R.string.lbl_download_path, new StringBuilder().append(
				Environment.getExternalStorageDirectory()).append('/').append(Environment.DIRECTORY_DOWNLOADS));
		if (savedInstanceState != null) {
			mUrl = savedInstanceState.getString(EXTRAS_URL);
		} else {
			mUrl = getIntent().getStringExtra(EXTRAS_URL);
		}
		setContentView(LAYOUT);


		mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_srl);
		mRefreshLayout.setColorSchemeResources(R.color.green_1, R.color.green_2, R.color.green_3, R.color.green_4);
		mRefreshLayout.setOnRefreshListener(this);
		mRefreshLayout.setRefreshing(true);

		mWebView = (WebView) findViewById(R.id.download_wv);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
				mRefreshLayout.setRefreshing(true);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				mRefreshLayout.setRefreshing(false);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		mWebView.loadUrl(mUrl);
		mWebView.setDownloadListener(this);
		WebSettings settings = mWebView.getSettings();
		settings.setLoadWithOverviewMode(true);
		settings.setJavaScriptEnabled(true);
		settings.setLoadsImagesAutomatically(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setCacheMode(WebSettings.LOAD_NORMAL);
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(false);
		settings.setDomStorageEnabled(true);



		TextView downloadTv = (TextView) findViewById(R.id.download_path_tv);
		downloadTv.setText(getString(R.string.lbl_download_path, new StringBuilder().append(
				Environment.getExternalStorageDirectory()).append('/').append(Environment.DIRECTORY_DOWNLOADS)));
		float initAplha = ViewHelper.getAlpha(downloadTv);
		ObjectAnimator.ofFloat(downloadTv, ALPHA, 0, initAplha).setDuration(2000).start();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		mUrl = intent.getStringExtra(EXTRAS_URL);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(EXTRAS_URL, mUrl);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(MENU, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_forward:
			if (mWebView.canGoForward()) {
				mWebView.goForward();
			}
			break;
		case R.id.action_backward:
			if (mWebView.canGoBack()) {
				mWebView.goBack();
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onRefresh() {
		mWebView.loadUrl(mUrl);
	}

	@Override
	public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
			long contentLength) {
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
		TextView downloadTv = (TextView) findViewById(R.id.download_path_tv);
		Utils.showLongToast(getApplicationContext(), downloadTv.getText().toString());
	}
}
