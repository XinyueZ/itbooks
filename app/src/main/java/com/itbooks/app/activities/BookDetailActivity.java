package com.itbooks.app.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.chopping.utils.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.itbooks.R;
import com.itbooks.app.fragments.BookmarkInfoDialogFragment;
import com.itbooks.bus.DownloadEndEvent;
import com.itbooks.bus.DownloadFailedEvent;
import com.itbooks.bus.DownloadStartEvent;
import com.itbooks.bus.DownloadUnavailableEvent;
import com.itbooks.data.rest.RSBook;
import com.itbooks.net.bookmark.BookmarkManger;
import com.itbooks.net.download.Download;
import com.itbooks.utils.Prefs;
import com.itbooks.views.RevealLayout;
import com.squareup.picasso.Picasso;

/**
 * Details of book.
 *
 * @author Xinyue Zhao
 */
public final class BookDetailActivity extends BaseActivity {

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
	private TextView mDescriptionTv;
	private TextView mISBNTv;
	private TextView mYearTv;
	private TextView mPageTv;
	private TextView mPublisherTv;
	private TextView mSizeTv;


	private FloatingActionButton mOpenBtn;
	private RevealLayout mHeadV;
	private boolean mInProgress;


	/**
	 * The interstitial ad.
	 */
	private InterstitialAd mInterstitialAd;
	private MenuItem mBookmarkItem;

	private boolean mBookmarked;
	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.itbooks.bus.DownloadStartEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.DownloadStartEvent}.
	 */
	public void onEvent(DownloadStartEvent e) {
		mInProgress = true;
	}


	/**
	 * Handler for {@link com.itbooks.bus.DownloadEndEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.DownloadEndEvent}.
	 */
	public void onEvent(DownloadEndEvent e) {
		mInProgress = false;
	}


	/**
	 * Handler for {@link com.itbooks.bus.DownloadFailedEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.DownloadFailedEvent}.
	 */
	public void onEvent(DownloadFailedEvent e) {
		if (e.getDownload().getBook().equals(mBook)) {
			showInfoToast(getString(R.string.msg_downloading_fail));
		}
		mInProgress = false;
	}


	/**
	 * Handler for {@link DownloadUnavailableEvent}.
	 *
	 * @param e
	 * 		Event {@link DownloadUnavailableEvent}.
	 */
	public void onEvent(DownloadUnavailableEvent e) {
		showDialogFragment(new DialogFragment() {
			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
				// Use the Builder class for convenient dialog construction
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.application_name).setMessage(R.string.msg_require_external_storage)
						.setPositiveButton(R.string.btn_ok, null);
				return builder.create();
			}
		}, null);
	}

	//------------------------------------------------

	/**
	 * Show single instance of {@link BookDetailActivity}.
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
			closeTroubleUI();
			mInterstitialAd.show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
		collapsingToolbar.setTitle(mBook.getName());

		collapsingToolbar.setExpandedTitleTextAppearance(R.style.BookNameTitle);

		mThumbIv = (ImageView) findViewById(R.id.detail_thumb_iv);
		mDescriptionTv = (TextView) findViewById(R.id.detail_description_tv);
		mISBNTv = (TextView) findViewById(R.id.detail_isbn_tv);
		mYearTv = (TextView) findViewById(R.id.detail_year_tv);
		mPageTv = (TextView) findViewById(R.id.detail_page_tv);
		mPublisherTv = (TextView) findViewById(R.id.detail_publisher_tv);
		mSizeTv = (TextView) findViewById(R.id.book_size_tv);



		mOpenBtn = (FloatingActionButton) findViewById(R.id.download_btn);
		mOpenBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mInProgress) {
					Download download = new Download(mBook);
					download.start(getApplicationContext());
				}
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
			try {
				Picasso.with(this).load(Utils.uriStr2URI(mBook.getCoverUrl()).toASCIIString()).placeholder(R.drawable.ic_launcher).into(mThumbIv);
			} catch (NullPointerException e) {
				Picasso.with(this).load(mBook.getCoverUrl()).placeholder(R.drawable.ic_launcher).into(mThumbIv);
			}
		}

		mDescriptionTv.setText(Html.fromHtml(mBook.getDescription()));
		mISBNTv.setText(mBook.getISBN());
		mYearTv.setText(mBook.getYear());
		mPageTv.setText(mBook.getPages());
		mPublisherTv.setText(mBook.getPublisher());
		if (!TextUtils.isEmpty(mBook.getSize())) {
			mSizeTv.setText(mBook.getSize());
		}

		mHeadV = (RevealLayout) findViewById(R.id.thumb_rl);
		mHeadV.hide();
		mHeadV.post(new Runnable() {
			@Override
			public void run() {
				mHeadV.show(1500);
			}
		});


		if (Download.exists(getApplicationContext(), mBook)) {
			AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Boolean>() {
				@Override
				protected Boolean doInBackground(Void... params) {
					try {
						return Download.downloading(getApplicationContext(), mBook);
					} catch (IllegalStateException e) {
						return null;
					}
				}

				@Override
				protected void onPostExecute(Boolean isLoading) {
					super.onPostExecute(isLoading);
					if (isLoading != null) {
						if (isLoading) {
							mInProgress = true;
						} else {
							mInProgress = false;
						}
					} else {
						mInProgress = false;
					}
				}
			});
		} else {
			AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Boolean>() {
				@Override
				protected Boolean doInBackground(Void... params) {
					try {
						return Download.downloading(getApplicationContext(), mBook);
					} catch (IllegalStateException e) {
						return null;
					}
				}

				@Override
				protected void onPostExecute(Boolean isLoading) {
					super.onPostExecute(isLoading);
					mInProgress = false;
					if (isLoading != null) {
						if (isLoading) {
							mInProgress = true;
						} else {
							mInProgress = false;
						}
					} else {
						mInProgress = false;
					}
				}
			});
		}
		ActivityCompat.invalidateOptionsMenu(this);
	}


	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(BOOK_DETAIL_MENU, menu);
		mBookmarkItem = menu.findItem(R.id.action_bookmark);
		mBookmarked = BookmarkManger.getInstance().getBookmarked(mBook) != null;
		mBookmarkItem.setIcon(mBookmarked ? R.drawable.ic_bookmarked : R.drawable.ic_not_bookmarked);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_bookmark:
			if (mBook != null) {
				if (mBookmarked) {
					BookmarkManger.getInstance().removeBookmark(mBook);
					Utils.showShortToast(getApplicationContext(), R.string.msg_unbookmark_the_book);
					mBookmarkItem.setIcon(R.drawable.ic_not_bookmarked);
					mBookmarked = false;
				} else {
					BookmarkManger.getInstance().addBookmark(mBook);
					Utils.showShortToast(getApplicationContext(), R.string.msg_bookmark_the_book);
					mBookmarkItem.setIcon(R.drawable.ic_bookmarked);
					mBookmarked = true;
				}
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
			if(provider != null) {
				String subject = getString(R.string.lbl_share_book);
				String text = getString(R.string.lbl_share_book_content, mBook.getName(), mBook.getAuthor(), mBook.getLink());
				Intent intent = getDefaultShareIntent(provider, subject, text);
				if (intent != null) {
					provider.setShareIntent(intent);
				}
			}
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
