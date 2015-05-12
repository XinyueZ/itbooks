package com.itbooks.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.view.MenuItemCompat;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.chopping.utils.Utils;
import com.crashlytics.android.Crashlytics;
import com.gc.materialdesign.views.ButtonFloat;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.itbooks.R;
import com.itbooks.app.fragments.BookmarkInfoDialogFragment;
import com.itbooks.data.DSBookmark;
import com.itbooks.data.rest.RSBook;
import com.itbooks.db.DB;
import com.itbooks.utils.Prefs;
import com.squareup.picasso.Picasso;

/**
 * Details of book.
 *
 * @author Xinyue Zhao
 */
public final class BookDetailActivity extends BaseActivity   {

	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_book_detail;


	/**
	 * Main menu.
	 */
	private static final int BOOK_DETAIL_MENU = R.menu.book_detail;
	/**
	 * Book
	 */
	public static final String EXTRAS_BOOK = "com.itbooks.app.BookDetailActivity.book";

	/**
	 * The book to show.
	 */
	private RSBook mBook;


	private ImageView mThumbIv;
	private TextView mTitleTv;
	private TextView mDescriptionTv;
	private TextView mAuthorTv;
	private TextView mISBNTv;
	private TextView mYearTv;
	private TextView mPageTv;
	private TextView mPublisherTv;


	private ButtonFloat mOpenBtn;

	/**
	 * The interstitial ad.
	 */
	private InterstitialAd mInterstitialAd;

	private boolean mBookmarked;
	private MenuItem mBookmarkItem;


	/**
	 * Show single instance of {@link com.itbooks.app.BookDetailActivity}.
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 * @param book
	 * 		{@link com.itbooks.data.rest.RSBook}.
	 */
	public static void showInstance(Activity cxt, RSBook book) {
		Intent intent = new Intent(cxt, BookDetailActivity.class);
		intent.putExtra(EXTRAS_BOOK, book);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		cxt.startActivity(intent);
	}

	/**
	 * Invoke displayInterstitial() when you are ready to display an interstitial.
	 */
	public void displayInterstitial() {
		if (mInterstitialAd.isLoaded()) {
			mInterstitialAd.show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Prefs prefs = Prefs.getInstance(getApplication());
		int curTime = prefs.getShownDetailsTimes();
		int adsTimes = prefs.getShownDetailsAdsTimes();
		if (curTime % adsTimes == 0) {
			// Create an ad.
			mInterstitialAd = new InterstitialAd(this);
			mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
			// Create ad request.
			AdRequest adRequest = new AdRequest.Builder().build();
			// Begin loading your interstitial.
			mInterstitialAd.setAdListener(new AdListener() {
				@Override
				public void onAdLoaded() {
					super.onAdLoaded();
					displayInterstitial();
				}
			});
			mInterstitialAd.loadAd(adRequest);
		}
		curTime++;
		prefs.setShownDetailsTimes(curTime);


		if (savedInstanceState != null) {
			mBook = (RSBook) savedInstanceState.getSerializable(EXTRAS_BOOK);
		} else {
			mBook = (RSBook) getIntent().getSerializableExtra(EXTRAS_BOOK);
		}

		setContentView(LAYOUT);

		mThumbIv = (ImageView) findViewById(R.id.detail_thumb_iv);
		mTitleTv = (TextView) findViewById(R.id.detail_title_tv);
		mDescriptionTv = (TextView) findViewById(R.id.detail_description_tv);
		mAuthorTv = (TextView) findViewById(R.id.detail_author_tv);
		mISBNTv = (TextView) findViewById(R.id.detail_isbn_tv);
		mYearTv = (TextView) findViewById(R.id.detail_year_tv);
		mPageTv = (TextView) findViewById(R.id.detail_page_tv);
		mPublisherTv = (TextView) findViewById(R.id.detail_publisher_tv);

		mOpenBtn = (ButtonFloat) findViewById(R.id.download_btn);
		mOpenBtn.setBackgroundColor(getResources().getColor(R.color.common_pink));


		mOpenBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialogFragment(new DialogFragment() {
					@Override
					public Dialog onCreateDialog(Bundle savedInstanceState) {
						com.gc.materialdesign.widgets.Dialog dialog = new com.gc.materialdesign.widgets.Dialog(
								getActivity(), getString(R.string.application_name), getString(
								R.string.msg_ask_download));
						//						dialog.getButtonAccept().setText(getString(R.string.btn_confirm));
						dialog.setOnAcceptButtonClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								downloadBrowser();
							}
						});
						return dialog;
					}
				}, "download ask");
			}
		});

		if (!prefs.hasKnownBookmark()) {
			showDialogFragment(BookmarkInfoDialogFragment.newInstance(getApplication()), null);
		}
		showBookDetail();
	}


	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		mBook = (RSBook) intent.getSerializableExtra(EXTRAS_BOOK);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(EXTRAS_BOOK, mBook);
	}


	/**
	 * Show the content of a book.
	 */
	private void showBookDetail() {
		if (!TextUtils.isEmpty(mBook.getCoverUrl())) {
			Picasso.with(this)
					.load(mBook.getCoverUrl())
					.placeholder(R.drawable.ic_launcher)
					.into(mThumbIv);
		}
		mTitleTv.setText(mBook.getName());
		mDescriptionTv.setText(Html.fromHtml(mBook.getDescription()));
		mAuthorTv.setText(mBook.getAuthor());
		mISBNTv.setText(mBook.getISBN());
		mYearTv.setText(mBook.getYear());
		mPageTv.setText(mBook.getPages());
		mPublisherTv.setText(mBook.getPublisher());

		ActivityCompat.invalidateOptionsMenu(this);
	}


	public void downloadBrowser() {
		if (mBook != null && !TextUtils.isEmpty(mBook.getLink())) {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.setData(Uri.parse(mBook.getLink()));
			startActivity(i);

			String msg = getString(R.string.lbl_download_path, new StringBuilder().append(
					Environment.getExternalStorageDirectory()).append('/').append(Environment.DIRECTORY_DOWNLOADS));
			Utils.showLongToast(getApplicationContext(), msg);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(BOOK_DETAIL_MENU, menu);
		mBookmarkItem = menu.findItem(R.id.action_bookmark);
		AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						mBookmarked = DB.getInstance(getApplication()).isBookmarked(mBook);
						return null;
					}

					@Override
					protected void onPostExecute(Void aVoid) {
						super.onPostExecute(aVoid);
						mBookmarkItem.setIcon(mBookmarked ? R.drawable.ic_bookmarked : R.drawable.ic_not_bookmarked);
					}
				});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_bookmark:
			if (mBook != null) {
				AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						DB db = DB.getInstance(getApplication());
						if (mBookmarked) {
							db.removeBookmark(mBook);
						} else {
							db.addBookmark(new DSBookmark(mBook));
						}
						mBookmarked = db.isBookmarked(mBook);
						return null;
					}

					@Override
					protected void onPostExecute(Void aVoid) {
						super.onPostExecute(aVoid);
						mBookmarkItem.setIcon(mBookmarked ? R.drawable.ic_bookmarked : R.drawable.ic_not_bookmarked);
						Utils.showShortToast(getApplicationContext(), getString(
								mBookmarked ? R.string.msg_bookmark_the_book : R.string.msg_unbookmark_the_book));
					}
				});
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem mMenuShare = menu.findItem(R.id.action_share_book);
		if (mBook != null) {
			//Getting the actionprovider associated with the menu item whose id is share.
			android.support.v7.widget.ShareActionProvider provider =
					(android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(mMenuShare);
			//Setting a share intent.
			String subject = getString(R.string.lbl_share_book);
			String text = getString(R.string.lbl_share_book_content, mBook.getName(), mBook.getAuthor(),
					mBook.getLink());

			provider.setShareIntent(getDefaultShareIntent(provider, subject, text));
		}
		return super.onPrepareOptionsMenu(menu);
	}


	@Override
	public void onRefresh() {

	}

	@Override
	public void onBackPressed() {
		ActivityCompat.finishAfterTransition(this);
	}
}
