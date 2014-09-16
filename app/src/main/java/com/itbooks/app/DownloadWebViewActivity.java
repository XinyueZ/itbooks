package com.itbooks.app;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.itbooks.R;

public final class DownloadWebViewActivity extends BaseActivity {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_webview;

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
		if (savedInstanceState != null) {
			mUrl = savedInstanceState.getString(EXTRAS_URL);
		} else {
			mUrl = getIntent().getStringExtra(EXTRAS_URL);
		}
		setContentView(LAYOUT);

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
		WebSettings settings = mWebView.getSettings();
		settings.setLoadWithOverviewMode(true);
		settings.setJavaScriptEnabled(true);
		settings.setLoadsImagesAutomatically(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setCacheMode(WebSettings.LOAD_NORMAL);
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(false);
		settings.setDomStorageEnabled(true);


		mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_srl);
		mRefreshLayout.setColorSchemeResources(R.color.green_1, R.color.green_2, R.color.green_3, R.color.green_4);
		mRefreshLayout.setOnRefreshListener(this);
		mRefreshLayout.setRefreshing(true);
		mWebView.loadUrl(mUrl);

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
}
