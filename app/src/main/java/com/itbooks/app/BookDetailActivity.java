package com.itbooks.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.chopping.utils.DeviceUtils;
import com.chopping.utils.DeviceUtils.ScreenSize;
import com.chopping.utils.Utils;
import com.gc.materialdesign.views.ButtonFloat;
import com.gc.materialdesign.widgets.SnackBar;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.itbooks.R;
import com.itbooks.app.fragments.BookmarkInfoDialogFragment;
import com.itbooks.bus.DownloadEndEvent;
import com.itbooks.bus.DownloadFailedEvent;
import com.itbooks.bus.DownloadOpenEvent;
import com.itbooks.bus.DownloadStartEvent;
import com.itbooks.data.rest.RSBook;
import com.itbooks.net.download.Download;
import com.itbooks.utils.Prefs;
import com.itbooks.views.RevealLayout;
import com.nineoldandroids.view.ViewPropertyAnimator;
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
	private TextView mTitleTv;
	private TextView mDescriptionTv;
	private TextView mAuthorTv;
	private TextView mISBNTv;
	private TextView mYearTv;
	private TextView mPageTv;
	private TextView mPublisherTv;


	private ButtonFloat mOpenBtn;
	private View mLoadingPb;
	private RevealLayout mHeadV;
	private boolean mInProgress;


	/**
	 * The interstitial ad.
	 */
	private InterstitialAd mInterstitialAd;

	private boolean mBookmarked;
	private MenuItem mBookmarkItem;

	private Toolbar mToolbar;

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
		uiLoading();
		mHeadV.show();
	}



	/**
	 * Handler for {@link com.itbooks.bus.DownloadEndEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.DownloadEndEvent}.
	 */
	public void onEvent(DownloadEndEvent e) {
		if (e.getDownload().getBook().equals(mBook)) {
			uiLoaded();
			mHeadV.show();
		}
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
			uiFailDownloading();
			new SnackBar(this, getString(R.string.msg_downloading_fail)).show();
		}
		mInProgress = false;
	}



	/**
	 * Handler for {@link com.itbooks.bus.DownloadOpenEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.DownloadOpenEvent}.
	 */
	public void onEvent(DownloadOpenEvent e) {
		try {
			Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
			openFileIntent.setDataAndType(Uri.fromFile(e.getFile()), "application/pdf");
			openFileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(openFileIntent);
		} catch (Exception ex) {
			//Download pdf-reader.
			showDialogFragment(
					new DialogFragment() {
						@Override
						public Dialog onCreateDialog(Bundle savedInstanceState) {
							// Use the Builder class for convenient dialog construction
							AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
							builder.setMessage(R.string.msg_no_reader)
									.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											String pdfReader = "com.adobe.reader";
											try {
												startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
														"market://details?id=" + pdfReader)));
											} catch (android.content.ActivityNotFoundException exx) {
												startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
														"https://play.google.com/store/apps/details?id=" + pdfReader)));
											}
										}
									})
									.setNegativeButton(R.string.btn_not_yet_load, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											// User cancelled the dialog
										}
									});
							// Create the AlertDialog object and return it
							return builder.create();
						}}, null);
		}
	}

	//------------------------------------------------

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

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setBackgroundResource(R.color.indigo_500_25);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);


		mThumbIv = (ImageView) findViewById(R.id.detail_thumb_iv);
		mTitleTv = (TextView) findViewById(R.id.detail_title_tv);
		mDescriptionTv = (TextView) findViewById(R.id.detail_description_tv);
		mAuthorTv = (TextView) findViewById(R.id.detail_author_tv);
		mISBNTv = (TextView) findViewById(R.id.detail_isbn_tv);
		mYearTv = (TextView) findViewById(R.id.detail_year_tv);
		mPageTv = (TextView) findViewById(R.id.detail_page_tv);
		mPublisherTv = (TextView) findViewById(R.id.detail_publisher_tv);


		mLoadingPb = findViewById(R.id.loading_pb);
		mHeadV = (RevealLayout) findViewById(R.id.child_head_ll);
		ViewCompat.setElevation(mHeadV, getResources().getDimensionPixelSize(R.dimen.detail_head_elevation));
		mOpenBtn = (ButtonFloat) findViewById(R.id.download_btn);
		mOpenBtn.setBackgroundColor(getResources().getColor(R.color.book_btn_not_downloaded));
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

		mParentV = (NestedScrollView) findViewById(R.id.parent_sv);
		mParentV.setOnTouchListener(touchParent);
		NestedScrollView childV = (NestedScrollView) findViewById(R.id.child_sv);
		childV.setOnTouchListener(touchParent);
		ScreenSize su = DeviceUtils.getScreenSize(this);
		childV.getLayoutParams().height =
				su.Height  - getResources().getDimensionPixelSize(
						R.dimen.detail_head_to_top_distance);

		showBookDetail();
	}

	//For sticky affect.
	private NestedScrollView mParentV;
	private OnTouchListener touchParent = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int y = mParentV.getScrollY();
			if (y >= 0 && y <= 150) {
				showFab();
			} else {
				hideFab();
			}
			return false;
		}
	};

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
			Picasso.with(this).load(Utils.uriStr2URI(mBook.getCoverUrl()).toASCIIString()).placeholder(
					R.drawable.ic_launcher).into(mThumbIv);
		}
		mTitleTv.setText(mBook.getName());
		mDescriptionTv.setText(Html.fromHtml(mBook.getDescription()));
		mAuthorTv.setText(mBook.getAuthor());
		mISBNTv.setText(mBook.getISBN());
		mYearTv.setText(mBook.getYear());
		mPageTv.setText(mBook.getPages());
		mPublisherTv.setText(mBook.getPublisher());

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mHeadV.show();
			}
		}, 500);

		//Try to find whether local has this book or not.
		if(Download.exists(getApplicationContext(), mBook)) {
			uiLoaded();
		} else {
			//Whether is being downloaded.
			if(Download.downloading(getApplicationContext(), mBook)) {
				uiLoading();
			}
		}
		ActivityCompat.invalidateOptionsMenu(this);
	}


	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(BOOK_DETAIL_MENU, menu);
		mBookmarkItem = menu.findItem(R.id.action_bookmark);
		AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				//TODO Check if bookmarked in detail
				//mBookmarked = DB.getInstance(getApplication()).isBookmarked(mBook);
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
						//TODO Do bookmark
//						DB db = DB.getInstance(getApplication());
//						if (mBookmarked) {
//							db.removeBookmark(mBook);
//						} else {
//							db.addBookmark(new DSBookmark(mBook));
//						}
//						mBookmarked = db.isBookmarked(mBook);
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

	private boolean mFabIsShown;

	private void showFab() {
		if (!mFabIsShown) {
			mOpenBtn.setEnabled(true);
			ViewPropertyAnimator.animate(mOpenBtn).cancel();
			ViewPropertyAnimator.animate(mOpenBtn).scaleX(1).scaleY(1).setDuration(200).start();
			mToolbar.setBackgroundResource(R.color.indigo_500_25);
			mFabIsShown = true;
		}
	}

	private void hideFab() {
		if (mFabIsShown) {
			mOpenBtn.setEnabled(false);
			ViewPropertyAnimator.animate(mOpenBtn).cancel();
			ViewPropertyAnimator.animate(mOpenBtn).scaleX(0).scaleY(0).setDuration(200).start();
			mToolbar.setBackgroundResource(R.color.primary_color);
			mFabIsShown = false;
		}
	}

	/**
	 * Color on head changed while being loaded.
	 */
	private void uiLoading() {
		mLoadingPb.setVisibility(View.VISIBLE);
		mHeadV.setBackgroundResource(R.color.book_downloading);
		mOpenBtn.setDrawableIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_file_cloud_download, null));
		mOpenBtn.setBackgroundColor(getResources().getColor(R.color.book_btn_downloading));
	}

	/**
	 * Color on head changed after being loaded..
	 */
	private void uiLoaded() {
		mLoadingPb.setVisibility(View.GONE);
		mHeadV.setBackgroundResource(R.color.book_downloaded);
		mOpenBtn.setDrawableIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_reading, null));
		mOpenBtn.setBackgroundColor(getResources().getColor(R.color.book_btn_downloaded));
	}

	/**
	 * Color on head changed if download failed.
	 */
	private void uiFailDownloading() {
		mLoadingPb.setVisibility(View.GONE);
		mHeadV.setBackgroundResource(R.color.book_failed_downloaded);
		mHeadV.show();
	}

}
