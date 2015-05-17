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
import com.itbooks.bus.DownloadUnavailableEvent;
import com.itbooks.data.rest.RSBook;
import com.itbooks.net.bookmark.BookmarkManger;
import com.itbooks.net.download.Download;
import com.itbooks.utils.Prefs;
import com.itbooks.views.RevealLayout;
import com.itbooks.views.RevealLayout.OnRevealEndListener;
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
	private TextView mSizeTv;


	private ButtonFloat mOpenBtn;
	private View mLoadingPb;
	private RevealLayout mHeadV;
	private boolean mInProgress;


	/**
	 * The interstitial ad.
	 */
	private InterstitialAd mInterstitialAd;
	private MenuItem mBookmarkItem;
	private Toolbar mToolbar;

	private boolean mBookmarked;
	private static final int ANIM_DUR = 1000;
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
			showDialogFragment(new DialogFragment() {
				@Override
				public Dialog onCreateDialog(Bundle savedInstanceState) {
					// Use the Builder class for convenient dialog construction
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle(R.string.application_name).setMessage(R.string.msg_no_reader).setPositiveButton(
							R.string.btn_ok, new DialogInterface.OnClickListener() {
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
					}).setNegativeButton(R.string.btn_not_yet_load, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									// User cancelled the dialog
								}
							});
					// Create the AlertDialog object and return it
					return builder.create();
				}
			}, null);
		}
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
		mSizeTv = (TextView) findViewById(R.id.book_size_tv);

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
		childV.getLayoutParams().height = su.Height - getResources().getDimensionPixelSize(
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

		mDescriptionTv.setText(Html.fromHtml(mBook.getDescription()));
		mISBNTv.setText(mBook.getISBN());
		mYearTv.setText(mBook.getYear());
		mPageTv.setText(mBook.getPages());
		mPublisherTv.setText(mBook.getPublisher());
		mSizeTv.setText(mBook.getSize());

		mHeadV.setOnRevealEndListener(new OnRevealEndListener() {
			@Override
			public void onEnd() {
				mTitleTv.setText(mBook.getName());
				mAuthorTv.setText(mBook.getAuthor());
				mHeadV.setOnRevealEndListener(null);
			}
		});
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mHeadV.next();
			}
		}, ANIM_DUR);

		if (Download.exists(getApplicationContext(), mBook)  ) {
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
					if(isLoading != null) {
						if (isLoading) {
							mInProgress = true;
							uiLoading();
						} else {
							mInProgress = false;
							uiLoaded();
						}
					} else {
						mInProgress = false;
						uiFailDownloading();
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
					if(isLoading != null) {
						if (isLoading) {
							mInProgress = true;
							uiLoading();
						} else {
							mInProgress = false;
						}
					} else {
						mInProgress = false;
						uiFailDownloading();
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
		mBookmarkItem.setIcon(mBookmarked ? R.drawable.ic_bookmarked :
				R.drawable.ic_not_bookmarked);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_bookmark:
			if (mBook != null) {
				if(mBookmarked) {
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


			switch (mDownloadStatus ) {
			case 0:
				mHeadV.setBackgroundResource(R.drawable.bg_not_downloaded);
				break;
			case 1:
				mHeadV.setBackgroundResource(R.drawable.bg_downloading);
				break;
			case 2:
				mHeadV.setBackgroundResource(R.drawable.bg_downloaded);
				break;
			case 3:
				mHeadV.setBackgroundResource(R.drawable.bg_failed_downloaded);
				break;
			}
		}
	}

	private void hideFab() {
		if (mFabIsShown) {
			mOpenBtn.setEnabled(false);
			ViewPropertyAnimator.animate(mOpenBtn).cancel();
			ViewPropertyAnimator.animate(mOpenBtn).scaleX(0).scaleY(0).setDuration(200).start();
			mToolbar.setBackgroundResource(R.color.primary_color);
			mFabIsShown = false;

			switch (mDownloadStatus ) {
			case 0:
				mHeadV.setBackgroundResource(R.color.book_not_downloaded_full);
				break;
			case 1:
				mHeadV.setBackgroundResource(R.color.book_downloading_full);
				break;
			case 2:
				mHeadV.setBackgroundResource(R.color.book_downloaded_full);
				break;
			case 3:
				mHeadV.setBackgroundResource(R.color.book_failed_downloaded_full);
				break;
			}
		}
	}

	/**
	 * Color on head changed while being loaded.
	 */
	private void uiLoading() {
		mDownloadStatus =1;
		mLoadingPb.setVisibility(View.VISIBLE);
		mHeadV.setBackgroundResource(mFabIsShown ? R.drawable.bg_downloading :R.color.book_downloading_full );
		mOpenBtn.setDrawableIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_file_cloud_download, null));
		mOpenBtn.setBackgroundColor(getResources().getColor(R.color.book_btn_downloading));
		mHeadV.next(ANIM_DUR);
	}

	/**
	 * Color on head changed after being loaded.
	 */
	private void uiLoaded() {
		mDownloadStatus = 2;
		mLoadingPb.setVisibility(View.GONE);
		mHeadV.setBackgroundResource(mFabIsShown ?R.drawable.bg_downloaded : R.color.book_downloaded_full);
		mOpenBtn.setDrawableIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_reading, null));
		mOpenBtn.setBackgroundColor(getResources().getColor(R.color.book_btn_downloaded));
		mHeadV.next(ANIM_DUR);
	}

	/**
	 * Color on head changed if download failed.
	 */
	private void uiFailDownloading() {
		mDownloadStatus = 3;
		mLoadingPb.setVisibility(View.GONE);
		mOpenBtn.setDrawableIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_download, null));
		mHeadV.setBackgroundResource(mFabIsShown ? R.drawable.bg_failed_downloaded : R.color.book_failed_downloaded_full);
		mHeadV.next(ANIM_DUR);
	}

	private int mDownloadStatus = 0;
}
