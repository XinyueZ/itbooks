package com.itbooks.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.chopping.net.GsonRequestTask;
import com.chopping.net.TaskHelper;
import com.chopping.utils.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.itbooks.R;
import com.itbooks.app.fragments.BookmarkInfoDialogFragment;
import com.itbooks.data.DSBook;
import com.itbooks.data.DSBookDetail;
import com.itbooks.data.DSBookmark;
import com.itbooks.db.DB;
import com.itbooks.utils.ParallelTask;
import com.itbooks.utils.Prefs;
import com.itbooks.views.OnViewAnimatedClickedListener;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Details of book.
 *
 * @author Xinyue Zhao
 */
public final class BookDetailActivity extends BaseActivity implements ImageListener {

	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_book_detail;
	/**
	 * Main menu.
	 */
	private static final int BOOK_DETAIL_MENU = R.menu.book_detail;
	/**
	 * Book id.
	 */
	public static final String EXTRAS_BOOK_ID = "com.itbooks.app.BookDetailActivity.book.id";

	/**
	 * Id of book.
	 */
	private long mBookId;


	private View mContent;
	private ImageView mThumbIv;
	private TextView mTitleTv;
	private TextView mSubTitleTv;
	private TextView mDescriptionTv;
	private TextView mAuthorTv;
	private TextView mISBNTv;
	private TextView mYearTv;
	private TextView mPageTv;
	private TextView mPublisherTv;


	private ImageLoader mImageLoader;
	private DSBookDetail mBookDetail;

	private Button mDownloadIBtn;
	private Button mDownloadIIBtn;
	private ImageButton mOpenBtn;

	/**
	 * The interstitial ad.
	 */
	private InterstitialAd mInterstitialAd;

	private boolean mBookmarked;
	private MenuItem mBookmarkItem;
	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.itbooks.data.DSBookDetail}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.data.DSBookDetail}.
	 */
	public void onEvent(DSBookDetail e) {
		mBookDetail = e;
		showBookDetail();
		mRefreshLayout.setRefreshing(false);
		setHasShownDataOnUI(true);
	}


	//------------------------------------------------

	/**
	 * Show single instance of {@link com.itbooks.app.BookDetailActivity}.
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 * @param bookId
	 * 		Book's id.
	 */
	public static void showInstance(Context cxt, long bookId) {
		Intent intent = new Intent(cxt, BookDetailActivity.class);
		intent.putExtra(EXTRAS_BOOK_ID, bookId);
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
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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


		if (savedInstanceState != null) {
			mBookId = savedInstanceState.getLong(EXTRAS_BOOK_ID);
		} else {
			mBookId = getIntent().getLongExtra(EXTRAS_BOOK_ID, -1);
		}

		setContentView(LAYOUT);


		mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_srl);
		mRefreshLayout.setColorSchemeResources(R.color.green_1, R.color.green_2, R.color.green_3, R.color.green_4);
		mRefreshLayout.setOnRefreshListener(this);
		mRefreshLayout.setRefreshing(true);


		mContent = findViewById(R.id.content_sv);
		mThumbIv = (ImageView) findViewById(R.id.detail_thumb_iv);
		mTitleTv = (TextView) findViewById(R.id.detail_title_tv);
		mSubTitleTv = (TextView) findViewById(R.id.detail_subtitle_tv);
		mDescriptionTv = (TextView) findViewById(R.id.detail_description_tv);
		mAuthorTv = (TextView) findViewById(R.id.detail_author_tv);
		mISBNTv = (TextView) findViewById(R.id.detail_isbn_tv);
		mYearTv = (TextView) findViewById(R.id.detail_year_tv);
		mPageTv = (TextView) findViewById(R.id.detail_page_tv);
		mPublisherTv = (TextView) findViewById(R.id.detail_publisher_tv);

		mImageLoader = TaskHelper.getImageLoader();
		loadBookDetail();


		mDownloadIBtn = (Button) findViewById(R.id.download_I_btn);
		mDownloadIBtn.setOnClickListener(new OnViewAnimatedClickedListener() {
			@Override
			public void onClick() {
				downloadInternal(mDownloadIBtn);
			}
		});
		mDownloadIIBtn = (Button) findViewById(R.id.download_II_btn);
		mDownloadIIBtn.setOnClickListener(new OnViewAnimatedClickedListener() {
			@Override
			public void onClick() {
				downloadBrowser(mDownloadIIBtn);
			}
		});
		mOpenBtn = (ImageButton) findViewById(R.id.download_btn);
		mOpenBtn.setOnClickListener(mOpenListener);

		if(!Prefs.getInstance(getApplication()).hasKnownBookmark()) {
			showDialogFragment(BookmarkInfoDialogFragment.newInstance(getApplication()), null);
		}
	}

	private OnViewAnimatedClickedListener mOpenListener = new OnViewAnimatedClickedListener() {
		@Override
		public void onClick() {
			mDownloadIIBtn.setVisibility(View.VISIBLE);
			AnimatorSet animatorSet = new AnimatorSet();
			ObjectAnimator iiBtnAnim = ObjectAnimator.ofFloat(mDownloadIIBtn, "translationY", 150f, 0).setDuration(100);
			iiBtnAnim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					mDownloadIBtn.setVisibility(View.VISIBLE);
				}
			});
			ObjectAnimator iBtnAnim = ObjectAnimator.ofFloat(mDownloadIBtn, "translationY", 200f, 0).setDuration(200);
			iBtnAnim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					mOpenBtn.setOnClickListener(mCloseListener);
				}
			});
			animatorSet.playSequentially(iiBtnAnim, iBtnAnim);
			animatorSet.start();
		}
	};

	private OnViewAnimatedClickedListener mCloseListener = new OnViewAnimatedClickedListener() {
		@Override
		public void onClick() {
			AnimatorSet animatorSet = new AnimatorSet();
			ObjectAnimator iiBtnAnim = ObjectAnimator.ofFloat(mDownloadIIBtn, "translationY", 0, 150f).setDuration(100);
			iiBtnAnim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					mDownloadIIBtn.setVisibility(View.GONE);
				}
			});
			ObjectAnimator iBtnAnim = ObjectAnimator.ofFloat(mDownloadIBtn, "translationY", 0, 200f).setDuration(200);
			iBtnAnim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					mDownloadIBtn.setVisibility(View.GONE);
					mOpenBtn.setOnClickListener(mOpenListener);

				}
			});
			animatorSet.playSequentially(iiBtnAnim, iBtnAnim);
			animatorSet.start();
		}
	};

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		mBookId = intent.getLongExtra(EXTRAS_BOOK_ID, -1);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(EXTRAS_BOOK_ID, mBookId);
	}

	@Override
	public void onRefresh() {
		loadBookDetail();
	}

	private void loadBookDetail() {
		String url = Prefs.getInstance(getApplication()).getApiBookDetail();
		url = String.format(url, mBookId + "");
		new GsonRequestTask<DSBookDetail>(getApplication(), Method.GET, url, DSBookDetail.class).execute();
	}

	private void showBookDetail() {
		mContent.setVisibility(View.VISIBLE);
		if (!TextUtils.isEmpty(mBookDetail.getImageUrl())) {
			mImageLoader.get(mBookDetail.getImageUrl(), this);
		}
		mTitleTv.setText(mBookDetail.getTitle());
		mSubTitleTv.setText(mBookDetail.getSubTitle());
		mDescriptionTv.setText(mBookDetail.getDescription());
		mAuthorTv.setText(mBookDetail.getAuthor());
		mISBNTv.setText(mBookDetail.getISBN());
		mYearTv.setText(mBookDetail.getYear());
		mPageTv.setText(mBookDetail.getPage());
		mPublisherTv.setText(mBookDetail.getPublisher());

		ActivityCompat.invalidateOptionsMenu(this);
	}

	@Override
	public void onResponse(ImageContainer response, boolean isImmediate) {
		if (response != null && response.getBitmap() != null) {
			mThumbIv.setImageBitmap(response.getBitmap());
		}
	}

	@Override
	public void onErrorResponse(VolleyError error) {

	}


	public void downloadInternal(View view) {
		DownloadWebViewActivity.showInstance(this, mBookDetail.getDownloadUrl());
	}

	public void downloadBrowser(View view) {
		if (mBookDetail != null && !TextUtils.isEmpty(mBookDetail.getDownloadUrl())) {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.setData(Uri.parse(mBookDetail.getDownloadUrl()));
			startActivity(i);

			String msg = getString(R.string.lbl_download_path, new StringBuilder().append(
							Environment.getExternalStorageDirectory()).append('/').append(
							Environment.DIRECTORY_DOWNLOADS));
			Utils.showLongToast(getApplicationContext(), msg);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(BOOK_DETAIL_MENU, menu);
		mBookmarkItem = menu.findItem(R.id.action_bookmark);
		new ParallelTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				mBookmarked = DB.getInstance(getApplication()).isBookmarked(mBookId);
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				mBookmarkItem.setIcon(mBookmarked ? R.drawable.ic_bookmarked : R.drawable.ic_not_bookmarked);
			}
		}.executeParallel();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_bookmark:
			new ParallelTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					DB db = DB.getInstance(getApplication());
					if (mBookmarked) {
						db.removeBookmark(new DSBook(mBookDetail.getId(), mBookDetail.getImageUrl()));
					} else {
						db.addBookmark(new DSBookmark(new DSBook(mBookDetail.getId(), mBookDetail.getImageUrl())));
					}
					mBookmarked = db.isBookmarked(mBookId);
					return null;
				}

				@Override
				protected void onPostExecute(Void aVoid) {
					super.onPostExecute(aVoid);
					mBookmarkItem.setIcon(mBookmarked ? R.drawable.ic_bookmarked : R.drawable.ic_not_bookmarked);
				}
			}.executeParallel();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem mMenuShare = menu.findItem(R.id.action_share_book);
		if (mBookDetail != null) {
			//Getting the actionprovider associated with the menu item whose id is share.
			android.support.v7.widget.ShareActionProvider provider =
					(android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(mMenuShare);
			//Setting a share intent.
			String subject = getString(R.string.lbl_share_book);
			String text = getString(R.string.lbl_share_book_content, mBookDetail.getTitle(), mBookDetail.getAuthor(),
					mBookDetail.getDownloadUrl());

			provider.setShareIntent(getDefaultShareIntent(provider, subject, text));
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onReload() {
		super.onReload();
		loadBookDetail();
	}


}
