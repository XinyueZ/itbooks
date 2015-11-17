package com.itbooks.app.activities;

import java.io.File;
import java.util.List;

import android.app.Dialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.chopping.bus.CloseDrawerEvent;
import com.chopping.net.GsonRequestTask;
import com.chopping.net.TaskHelper;
import com.chopping.utils.DeviceUtils;
import com.chopping.utils.DeviceUtils.ScreenSize;
import com.chopping.utils.Utils;
import com.github.johnpersano.supertoasts.SuperToast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.itbooks.R;
import com.itbooks.app.App;
import com.itbooks.app.Updated3_0Service;
import com.itbooks.app.adapters.AbstractBookViewAdapter;
import com.itbooks.app.adapters.BookGridAdapter;
import com.itbooks.app.adapters.BookListAdapter;
import com.itbooks.app.fragments.AboutDialogFragment;
import com.itbooks.app.fragments.AppListImpFragment;
import com.itbooks.app.fragments.BookmarkListFragment;
import com.itbooks.app.fragments.PushInfoDialogFragment;
import com.itbooks.bus.AskedPushEvent;
import com.itbooks.bus.BookmarksLoadedEvent;
import com.itbooks.bus.CleanBookmarkEvent;
import com.itbooks.bus.DownloadCompleteEvent;
import com.itbooks.bus.DownloadOpenEvent;
import com.itbooks.bus.EULAConfirmedEvent;
import com.itbooks.bus.EULARejectEvent;
import com.itbooks.bus.LoginRequestEvent;
import com.itbooks.bus.NewAPIVersionUpdateEvent;
import com.itbooks.bus.OpenBookDetailEvent;
import com.itbooks.bus.OpenBookmarkEvent;
import com.itbooks.bus.RefreshBookmarksEvent;
import com.itbooks.data.rest.RSBook;
import com.itbooks.data.rest.RSBookList;
import com.itbooks.data.rest.RSBookQuery;
import com.itbooks.gcm.RegGCMTask;
import com.itbooks.net.SyncService;
import com.itbooks.net.api.Api;
import com.itbooks.net.api.ApiNotInitializedException;
import com.itbooks.net.bookmark.BookmarkManger;
import com.itbooks.utils.Prefs;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.squareup.picasso.Picasso;

import net.steamcrafted.loadtoast.LoadToast;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends BaseActivity implements OnQueryTextListener {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_main;
	/**
	 * Main menu.
	 */
	private static final int MAIN_MENU = R.menu.main_menu;

	private RecyclerView mRv;

	private SearchRecentSuggestions mSuggestions;
	private String mKeyword;
	private SearchView mSearchView;

	private boolean mDetailOpened;
	/**
	 * Use navigation-drawer for this fork.
	 */
	private ActionBarDrawerToggle mDrawerToggle;
	/**
	 * Navigation drawer.
	 */
	private DrawerLayout mDrawerLayout;

	private SlidingPaneLayout mBookmarkSpl;

	private ActionBarHelper mActionBarHelper;

	private LinearLayoutManager mLayoutManager;

	private static final int GRID_COL_COUNT = 2;

	private ScreenSize mScreenSize;


	private FloatingActionButton mTopFab;

	private View mLoginBtn;
	private View mLogoutBtn;
	private TextView mLoginNameTv;
	private ImageView mUserIv;
	private View mAppListV;
	private volatile boolean mUIVisible;


	/**
	 * Request code for auto Google Play Services error resolution for Google Driver.
	 */
	private static final int REQUEST_CODE_RESOLUTION = 0x88;
	/**
	 * Handler filter for error of Google Client when connects Google Driver.
	 */
	private IntentFilter mConnectErrorHandlerFilter = new IntentFilter(SyncService.ACTION_CONNECT_ERROR);
	/**
	 * Handler for error of Google Client when connects Google Driver.
	 */
	private BroadcastReceiver mConnectErrorHandler = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectionResult connectionResult = intent.getParcelableExtra(SyncService.EXTRAS_ERROR_RESULT);
			if (!connectionResult.hasResolution()) {
				// show the localized error dialog.
				GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, connectionResult.getErrorCode(),
						0).show();
				return;
			}
			try {
				connectionResult.startResolutionForResult(MainActivity.this, REQUEST_CODE_RESOLUTION);
			} catch (SendIntentException e) {
				Log.e(MainActivity.class.getSimpleName(),
						"Exception while starting resolution activity for Google Driver connect.", e);
			}
		}
	};
	/**
	 * Indicator for sync progress.
	 */
	private LoadToast mLoadToast;
	/**
	 * Handler filter begin sync.
	 */
	private IntentFilter mSyncBeginHandlerFilter = new IntentFilter(SyncService.ACTION_SYNC_BEGIN);
	/**
	 * Handler   begin sync.
	 */
	private BroadcastReceiver mSyncBeginHandler = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mLoadToast == null) {
				mLoadToast = new LoadToast(MainActivity.this).setText(getString(R.string.msg_sync_in_progress))
						.setBackgroundColor(ActivityCompat.getColor(App.Instance, R.color.green_mid)).setProgressColor(
								ActivityCompat.getColor(App.Instance, R.color.primary_color)).setTextColor(
								ActivityCompat.getColor(App.Instance, R.color.text_common_white)).setTranslationY(
								Utils.getActionBarHeight(App.Instance)).show();
			}
		}
	};
	/**
	 * Handler filter end sync.
	 */
	private IntentFilter mSyncEndHandlerFilter = new IntentFilter(SyncService.ACTION_SYNC_END);
	/**
	 * Handler   end sync.
	 */
	private BroadcastReceiver mSyncEndHandler = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mLoadToast != null) {
				mLoadToast.success();
			}
		}
	};
	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.itbooks.bus.AskedPushEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.AskedPushEvent}.
	 */
	public void onEvent(AskedPushEvent e) {
		showAskLogin();
	}

	/**
	 * Handler for {@link com.itbooks.bus.LoginRequestEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.LoginRequestEvent}.
	 */
	public void onEvent(LoginRequestEvent e) {
		if (mRefreshLayout != null) {
			Snackbar.make(mRefreshLayout, R.string.msg_sync_req_for_driver, Snackbar.LENGTH_LONG).setAction(
					R.string.btn_login, new OnClickListener() {
						@Override
						public void onClick(View v) {
							ConnectGoogleActivity.showInstance(MainActivity.this);
						}
					}).show();
			mDrawerLayout.closeDrawers();
		}
	}



	/**
	 * Handler for {@link com.itbooks.bus.DownloadCompleteEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.DownloadCompleteEvent}.
	 */
	public void onEventMainThread(final DownloadCompleteEvent e) {
		showWarningToast(getString(R.string.msg_one_book_downloaded), new SuperToast.OnClickListener() {
			@Override
			public void onClick(View view, Parcelable parcelable) {
				File to = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
						e.getDownload().getTargetName());
				if (to.exists()) {
					EventBus.getDefault().post(new DownloadOpenEvent(to));
				}
			}
		});
	}

	/**
	 * Handler for {@link com.itbooks.bus.NewAPIVersionUpdateEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.NewAPIVersionUpdateEvent}.
	 */
	public void onEventMainThread(NewAPIVersionUpdateEvent e) {
		EventBus.getDefault().removeAllStickyEvents();
		showDialogFragment(new DialogFragment() {
			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
				// Use the Builder class for convenient dialog construction
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.application_name).setMessage(R.string.msg_new_api_version_update)
						.setPositiveButton(R.string.btn_ok, null);
				// Create the AlertDialog object and return it
				return builder.create();
			}
		}, null);
	}

	/**
	 * Handler for {@link com.itbooks.bus.OpenBookmarkEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.OpenBookmarkEvent}.
	 */
	public void onEvent(OpenBookmarkEvent e) {
		openBookDetail(e.getBook());
	}

	/**
	 * Handler for {@link com.chopping.bus.CloseDrawerEvent}.
	 *
	 * @param e
	 * 		Event {@link com.chopping.bus.CloseDrawerEvent}.
	 */
	public void onEvent(CloseDrawerEvent e) {
		mDrawerLayout.closeDrawers();
	}


	/**
	 * Handler for {@link com.itbooks.bus.EULARejectEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.EULARejectEvent}.
	 */
	public void onEvent(EULARejectEvent e) {
		finish();
	}

	/**
	 * Handler for {@link com.itbooks.bus.EULAConfirmedEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.EULAConfirmedEvent}.
	 */
	public void onEvent(EULAConfirmedEvent e) {
		showPushInfo();
		mDrawerLayout.openDrawer(Gravity.RIGHT);
	}

	/**
	 * Handler for {@link OpenBookDetailEvent}.
	 *
	 * @param e
	 * 		Event {@link OpenBookDetailEvent}.
	 */
	public void onEvent(OpenBookDetailEvent e) {
		openBookDetail(e.getBook());
	}

	/**
	 * Handler for {@link com.itbooks.bus.RefreshBookmarksEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.RefreshBookmarksEvent}.
	 */
	public void onEvent(RefreshBookmarksEvent e) {
		getBookmarks();
	}


	/**
	 * Handler for {@link com.itbooks.bus.BookmarksLoadedEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.BookmarksLoadedEvent}.
	 */
	public void onEvent(BookmarksLoadedEvent e) {
		if (mUIVisible) {
			getSupportFragmentManager().beginTransaction().replace(R.id.bookmark_list_container_fl,
					BookmarkListFragment.newInstance(getApplicationContext())).commit();
		}
	}


	//------------------------------------------------


	/**
	 * Get and load all bookmarks.
	 */
	private void getBookmarks() {
		BookmarkManger.getInstance().loadAllBookmarks();
	}


	/**
	 * Open the bookmark-list.
	 */
	private void openBookmarkList() {
		mBookmarkSpl.openPane();
	}

	protected void handleIntent(Intent intent) {
		mRefreshLayout.setRefreshing(true);
		mKeyword = intent.getStringExtra(SearchManager.QUERY);
		if (!TextUtils.isEmpty(mKeyword)) {
			if (mSearchView != null) {
				mSearchView.setQuery(mKeyword, false);
			}
			mKeyword = mKeyword.trim();
			resetSearchView();
			mSuggestions.saveRecentQuery(mKeyword, null);
		}
	}


	/**
	 * Reset the UI status of searchview.
	 */
	protected void resetSearchView() {
		if (mSearchView != null) {
			mSearchView.clearFocus();
		}
	}


	@Override
	protected void onNewIntent(Intent _intent) {
		super.onNewIntent(_intent);
		setIntent(_intent);
		handleIntent(_intent);
	}

	@Override
	public void onRefresh() {
		loadBooks();
	}

	/**
	 * Load feed of books.
	 */
	private void loadBooks() {
		if ((mRv.getAdapter()).getItemCount() == 0) {
			mRefreshLayout.setRefreshing(true);
		}
		if (!TextUtils.isEmpty(mKeyword)) {
			loadByKeyword();
		} else {
			loadDefaultPage();
		}
	}

	/**
	 * Default page when nothing wanna be searched.
	 */
	private void loadDefaultPage() {
		RSBookQuery query = new RSBookQuery("Android");
		try {
			Api.queryBooks(query, new Callback<RSBookList>() {
				@Override
				public void success(RSBookList rsBookList, Response response) {
					showBookList(rsBookList);
				}

				@Override
				public void failure(RetrofitError error) {
				}
			});
		} catch (ApiNotInitializedException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Page when nothing wanna be searched.
	 */
	private void loadByKeyword() {
		RSBookQuery query = new RSBookQuery(mKeyword);
		try {
			Api.queryBooks(query, new Callback<RSBookList>() {
				@Override
				public void success(RSBookList rsBookList, Response response) {
					showBookList(rsBookList);
				}

				@Override
				public void failure(RetrofitError error) {
				}
			});
		} catch (ApiNotInitializedException e) {
			e.printStackTrace();
		}
	}


	@Override
	public boolean onQueryTextSubmit(String s) {
		InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
		search(null);
		return false;
	}


	@Override
	public boolean onQueryTextChange(String s) {
		return false;
	}


	/**
	 * Open detail of a book.
	 *
	 * @param book
	 * 		{@link RSBook}
	 */
	private void openBookDetail(RSBook book) {
		mDetailOpened = true;
		BookDetailActivity.showInstance(this, book); //book.getId() );
	}

	public void search(View view) {
		mKeyword = mSearchView.getQuery().toString();
		//loadBooks();
	}

	@Override
	protected void onAppConfigLoaded() {
		super.onAppConfigLoaded();
		Api.initialize(this, Prefs.getInstance(getApplicationContext()).getRESTApi(), 1024 * 10);
		if (!mDetailOpened) {
			loadBooks();
		} else {
			mDetailOpened = false;
		}
	}

	@Override
	protected void onAppConfigIgnored() {
		super.onAppConfigIgnored();
		Api.initialize(this, Prefs.getInstance(getApplicationContext()).getRESTApi(), 1024 * 10);
		if (!mDetailOpened) {
			loadBooks();
		} else {
			mDetailOpened = false;
		}
	}


	@Override
	protected void onReload() {
		super.onReload();
		loadBooks();
	}


	/**
	 * Show all external applications links.
	 */
	private void showAppList() {
		String clsName = AppListImpFragment.class.getSimpleName();
		if (getSupportFragmentManager().getBackStackEntryCount() <= 0) {
			mAppListV.setVisibility(View.VISIBLE);
			getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_out_from_top_to_down_fast,
					R.anim.slide_in_from_down_to_top_fast, R.anim.slide_out_from_top_to_down_fast,
					R.anim.slide_in_from_down_to_top_fast).add(R.id.app_list_fl, AppListImpFragment.newInstance(this),
					clsName).addToBackStack(clsName).commit();
		}
	}

	/**
	 * Init the sliding-panel for bookmarks.
	 */
	private void initSlidingPanel() {
		mActionBarHelper = new ActionBarHelper(getSupportActionBar());
		mBookmarkSpl = (SlidingPaneLayout) findViewById(R.id.sliding_pane_layout);
		mBookmarkSpl.setPanelSlideListener(new SliderListener(mActionBarHelper));
		mActionBarHelper.init();
	}

	/**
	 * Initialize the navigation drawer.
	 */
	private void initDrawer() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
			mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.application_name,
					R.string.app_name) {
				@Override
				public void onDrawerSlide(View drawerView, float slideOffset) {
					super.onDrawerSlide(drawerView, slideOffset);
					if (!getSupportActionBar().isShowing()) {
						getSupportActionBar().show();
					}
				}
			};
			mDrawerLayout.setDrawerListener(mDrawerToggle);
		}
	}

	private void showPushInfo() {
		Prefs prefs = Prefs.getInstance(getApplication());
		if (prefs.isEULAOnceConfirmed() && !prefs.hasKnownPush()) {
			showDialogFragment(PushInfoDialogFragment.newInstance(getApplication()), null);
		} else {
				showAskLogin();
		}
	}

	private void showAskLogin() {
		Prefs prefs = Prefs.getInstance(getApplication());
		if (prefs.isEULAOnceConfirmed() && !prefs.askLogin()) {
			if (mDrawerLayout != null) {
				Prefs.getInstance(App.Instance).setAskLogin(true);
				mDrawerLayout.closeDrawer(GravityCompat.END);
				mDrawerLayout.openDrawer(GravityCompat.START);
				showDialogFragment(new DialogFragment() {
					@Override
					public Dialog onCreateDialog(Bundle savedInstanceState) {
						// Use the Builder class for convenient dialog construction
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setTitle(R.string.application_name).setMessage(R.string.lbl_login_benefit)
								.setNegativeButton(R.string.btn_not_yet, null).setPositiveButton(R.string.btn_login, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										ConnectGoogleActivity.showInstance(MainActivity.this);
									}
								});
						// Create the AlertDialog object and return it
						return builder.create();
					}
				}, null);
			}
		}
	}


	/**
	 * Action bar helper for use on ICS and newer devices.
	 */
	private static class ActionBarHelper {
		ActionBar mActionBar;

		ActionBarHelper(ActionBar actionBar) {
			mActionBar = actionBar;
		}

		public void init() {
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setHomeButtonEnabled(true);
		}

		public void onPanelClosed() {
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setHomeButtonEnabled(true);
			mActionBar.setTitle(R.string.application_name);
		}

		public void onPanelOpened() {
			mActionBar.setHomeButtonEnabled(true);
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setTitle(R.string.lbl_bookmark);
			if (!mActionBar.isShowing()) {
				mActionBar.show();
			}
		}

	}

	/**
	 * This panel slide listener updates the action bar accordingly for each panel state.
	 */
	private static class SliderListener extends SlidingPaneLayout.SimplePanelSlideListener {
		ActionBarHelper mActionBarHelper;

		SliderListener(ActionBarHelper actionBarHelper) {
			mActionBarHelper = actionBarHelper;
		}

		@Override
		public void onPanelOpened(View panel) {
			mActionBarHelper.onPanelOpened();
		}

		@Override
		public void onPanelClosed(View panel) {
			mActionBarHelper.onPanelClosed();
		}
	}

	private void initAdapters(@Nullable List<RSBook> books ) {
		Prefs prefs = Prefs.getInstance(App.Instance);
		if (prefs.getViewStyle() == 2) {
			mRv.setLayoutManager(mLayoutManager = new LinearLayoutManager(this));
			mRv.setAdapter(new BookListAdapter(books, com.itbooks.utils.Utils.showImage(App.Instance)));
		} else {
			mRv.setLayoutManager(mLayoutManager = new GridLayoutManager(this, GRID_COL_COUNT));
			mRv.setAdapter(new BookGridAdapter(books, GRID_COL_COUNT, mScreenSize, com.itbooks.utils.Utils.showImage(
					App.Instance)));
		}
	}

	/**
	 * Show feeds.
	 *
	 * @param bookList
	 * 		The result of REST call.
	 */
	public void showBookList(RSBookList bookList) {
		if (mUIVisible) {
			if (bookList != null && bookList.getStatus() == 200 && bookList.getBooks() != null &&
					bookList.getBooks().size() > 0) {
				AbstractBookViewAdapter adp = (AbstractBookViewAdapter) mRv.getAdapter();

				boolean shouldShowImages = com.itbooks.utils.Utils.showImage(App.Instance);
				boolean showImageCurrently = adp.showImages();

				if(showImageCurrently == shouldShowImages) {
					adp.setShowImages(com.itbooks.utils.Utils.showImage(App.Instance));
					adp.setData(bookList.getBooks());
					mRv.getAdapter().notifyDataSetChanged();
				} else {
					initAdapters(bookList.getBooks());
				}

				setHasShownDataOnUI(true);
				showInfoToast(String.format(getString(R.string.msg_items_count), bookList.getBooks().size()));
			} else {
				showErrorToast(getString(R.string.msg_refresh_fail), new SuperToast.OnClickListener() {
					@Override
					public void onClick(View view, Parcelable parcelable) {
						loadBooks();
					}
				});
			}
			mRefreshLayout.setRefreshing(false);
		}
	}


	/**
	 * Set-up of navi-bar left.
	 */
	private void setupDrawerContent(NavigationView navigationView) {
		View header = getLayoutInflater().inflate(R.layout.nav_header, navigationView, false);
		mUserIv = (ImageView) header.findViewById(R.id.user_iv);
		mLoginBtn = header.findViewById(R.id.google_login_btn);
		mLoginBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ConnectGoogleActivity.showInstance(MainActivity.this);
			}
		});
		mLogoutBtn = header.findViewById(R.id.logout_btn);
		mLogoutBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				exitAccount();
			}
		});
		mLoginNameTv = (TextView) header.findViewById(R.id.login_name_tv);
		navigationView.addHeaderView(header);
		navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(MenuItem menuItem) {
				menuItem.setChecked(true);
				mDrawerLayout.closeDrawer(GravityCompat.START);

				switch (menuItem.getItemId()) {
				case R.id.action_bookmarks:
					openBookmarkList();
					break;
				case R.id.action_history:
					mDrawerLayout.openDrawer(GravityCompat.END);
					break;
				case R.id.action_more_apps:
					showAppList();
					break;
				case R.id.action_setting:
					SettingActivity.showInstance(MainActivity.this);
					break;
				}
				return true;
			}
		});

	}

	@Override
	public void onBackPressed() {
		if (mDrawerLayout.isDrawerOpen(GravityCompat.START) || mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
			mDrawerLayout.closeDrawers();
		} else {
			if (mBookmarkSpl.isOpen()) {
				mBookmarkSpl.closePane();
			} else {
				super.onBackPressed();
			}
		}

	}

	private AnimatorListenerAdapter aniAdp = new AnimatorListenerAdapter() {
		@Override
		public void onAnimationEnd(Animator animation) {
			super.onAnimationEnd(animation);
			if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
				mDrawerLayout.closeDrawer(GravityCompat.START);
			}
		}
	};

	private void showUserInfo(Prefs prefs) {
		if (!TextUtils.isEmpty(prefs.getGoogleId())) {
			ViewPropertyAnimator.animate(mLoginBtn).alpha(0).setDuration(800).setListener(aniAdp).start();
			mLoginBtn.setEnabled(false);
			ViewPropertyAnimator.animate(mLogoutBtn).alpha(1).setDuration(800).start();
			mLogoutBtn.setEnabled(true);
		} else {
			ViewPropertyAnimator.animate(mLoginBtn).alpha(1).setDuration(800).setListener(aniAdp).start();
			mLoginBtn.setEnabled(true);
			ViewPropertyAnimator.animate(mLogoutBtn).alpha(0).setDuration(800).start();
			mLogoutBtn.setEnabled(false);
		}
		if (!TextUtils.isEmpty(prefs.getGoogleDisplyName())) {
			mLoginNameTv.setText(prefs.getGoogleDisplyName());
			ViewPropertyAnimator.animate(mLoginNameTv).translationY(Utils.convertPixelsToDp(App.Instance, 55))
					.setDuration(800).start();
		}
		Picasso picasso = Picasso.with(App.Instance);
		if (!TextUtils.isEmpty(prefs.getGoogleThumbUrl())) {
			picasso.load(Utils.uriStr2URI(prefs.getGoogleThumbUrl()).toASCIIString()).into(mUserIv);
		}
	}

	private void startSync(int resultCode) {
		if (resultCode == RESULT_OK) {
			SyncService.startSync(App.Instance);
		}
	}

	/**
	 * Exit current account, here unregister all push-elements etc.
	 */
	public void exitAccount() {
		Prefs prefs = Prefs.getInstance(App.Instance);
		prefs.setGoogleId(null);
		prefs.setGoogleThumbUrl(null);
		prefs.setGoogleDisplyName(null);
		mLoginNameTv.setText("");
		ViewPropertyAnimator.animate(mLoginNameTv).translationY(Utils.convertPixelsToDp(App.Instance, -55)).setDuration(
				800).start();
		ViewPropertyAnimator.animate(mLoginBtn).alpha(1).setDuration(800).start();
		mLoginBtn.setEnabled(true);
		ViewPropertyAnimator.animate(mLogoutBtn).alpha(0).setDuration(800).start();
		mLogoutBtn.setEnabled(false);
		mUserIv.setImageResource(R.drawable.ic_person);

		getBookmarks();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(MAIN_MENU, menu);
		final MenuItem searchMenu = menu.findItem(R.id.action_search);
		mSearchView = (SearchView) MenuItemCompat.getActionView(searchMenu);
		if (!TextUtils.isEmpty(mKeyword)) {
			mSearchView.setQuery(mKeyword, false);
		}
		mSearchView.setQueryHint(Html.fromHtml("<font color = #ffffff>" + mKeyword + "</font>"));
		mSearchView.setOnQueryTextListener(this);
		SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
		if (searchManager != null) {
			SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
			mSearchView.setSearchableInfo(info);
		}

		if (Prefs.getInstance(getApplicationContext()).getViewStyle() == 2) {
			menu.findItem(R.id.action_view_style_list).setVisible(false);
			menu.findItem(R.id.action_view_style_grid).setVisible(true);
		} else {
			menu.findItem(R.id.action_view_style_list).setVisible(true);
			menu.findItem(R.id.action_view_style_grid).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem menuShare = menu.findItem(R.id.action_share_app);
		//Getting the actionprovider associated with the menu item whose id is share.
		android.support.v7.widget.ShareActionProvider provider =
				(android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(menuShare);
		//Setting a share intent.
		if (provider != null) {
			String subject = getString(R.string.lbl_share_app);
			String text = getString(R.string.lbl_share_app_content);
			Intent intent = getDefaultShareIntent(provider, subject, text);
			if (intent != null) {
				provider.setShareIntent(intent);
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == android.R.id.home && mBookmarkSpl.isOpen()) {
			mBookmarkSpl.closePane();
			return true;
		}

		if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		switch (item.getItemId()) {
		case R.id.action_about:
			showDialogFragment(AboutDialogFragment.newInstance(this), null);
			break;
		case R.id.action_clear_bookmarks:
			BookmarkManger.getInstance().removeAllRemoteBookmarks();
			openBookmarkList();
			EventBus.getDefault().post(new CleanBookmarkEvent());
			break;
		case R.id.action_view_style_list:
			mRv.setLayoutManager(mLayoutManager = new LinearLayoutManager(this));
			mRv.setAdapter(new BookListAdapter(((AbstractBookViewAdapter) mRv.getAdapter()).getData(),
					com.itbooks.utils.Utils.showImage(App.Instance)));
			Prefs.getInstance(getApplicationContext()).setViewStyle(2);
			supportInvalidateOptionsMenu();
			break;
		case R.id.action_view_style_grid:
			mRv.setLayoutManager(mLayoutManager = new GridLayoutManager(this, GRID_COL_COUNT));
			mRv.setAdapter(new BookGridAdapter(((AbstractBookViewAdapter) mRv.getAdapter()).getData(), GRID_COL_COUNT,
					mScreenSize, com.itbooks.utils.Utils.showImage(App.Instance)));
			Prefs.getInstance(getApplicationContext()).setViewStyle(1);
			supportInvalidateOptionsMenu();
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ConnectGoogleActivity.REQ:
			getBookmarks();
			startSync(resultCode);
			break;
		case REQUEST_CODE_RESOLUTION:
			startSync(resultCode);
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(LAYOUT);
		LocalBroadcastManager.getInstance(App.Instance).registerReceiver(mConnectErrorHandler, mConnectErrorHandlerFilter);
		LocalBroadcastManager.getInstance(App.Instance).registerReceiver(mSyncBeginHandler, mSyncBeginHandlerFilter);
		LocalBroadcastManager.getInstance(App.Instance).registerReceiver(mSyncEndHandler, mSyncEndHandlerFilter);

		mAppListV = findViewById(R.id.app_list_sv);
		setupDrawerContent((NavigationView) findViewById(R.id.nav_view));

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		int actionBarHeight = Utils.getActionBarHeight(this);

		mScreenSize = DeviceUtils.getScreenSize(getApplicationContext());
		mSuggestions = new SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY,
				SearchSuggestionProvider.MODE);

		mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_srl);
		mRefreshLayout.setColorSchemeResources(R.color.indigo_1, R.color.indigo_2, R.color.indigo_3, R.color.indigo_4);
		mRefreshLayout.setProgressViewEndTarget(true, actionBarHeight * 2);
		mRefreshLayout.setProgressViewOffset(false, 0, actionBarHeight * 2);
		mRefreshLayout.setOnRefreshListener(this);
		mRefreshLayout.setRefreshing(true);

		mRv = (RecyclerView) findViewById(R.id.books_rv);
		initAdapters(null);

		mRv.addOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				//Scrolling up and down can hidden and show the FAB.
				float y = ViewCompat.getY(recyclerView);
				if (y < dy) {
					if (mTopFab.isShown()) {
						mTopFab.hide();
					}
				} else {
					if (!mTopFab.isShown()) {
						mTopFab.show();
					}
				}
			}
		});


		handleIntent(getIntent());


		Prefs prefs = Prefs.getInstance(getApplicationContext());
		mKeyword = prefs.getLastSearched();
		initDrawer();
		initSlidingPanel();

		mTopFab = (FloatingActionButton) findViewById(R.id.to_top_btn);
		mTopFab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mLayoutManager.scrollToPositionWithOffset(0, 0);
				getSupportActionBar().show();
			}
		});
		getSupportFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {
			@Override
			public void onBackStackChanged() {
				if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
					mTopFab.hide();
					mAppListV.setVisibility(View.VISIBLE);
				} else {
					mTopFab.show();
					mAppListV.setVisibility(View.GONE);
				}
			}
		});

		getBookmarks();
		mUIVisible = true;
	}


	@Override
	public void onResume() {
		Prefs prefs = Prefs.getInstance(getApplicationContext());
		if (prefs.isNewApiUpdated()) {
			if (prefs.isPushTurnedOn()) {
				prefs.turnOffPush();
				prefs.setPushRegId(null);
				AsyncTaskCompat.executeParallel(new RegGCMTask(getApplicationContext()));
				Utils.showLongToast(getApplicationContext(), R.string.msg_welcome_2_0);
			} else {
				Utils.showLongToast(getApplicationContext(), R.string.msg_welcome);
			}
			prefs.setNewApiUpdated(false);
		}
		if (!prefs.isNewApiUpdated() && prefs.isUpdated3_0()) {
			Utils.showLongToast(getApplicationContext(), R.string.msg_welcome_3_0);
			Updated3_0Service.startFixPre3_0Bugs(App.Instance);
			prefs.setUpdated3_0(false);
		}

		super.onResume();
		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
		showPushInfo();
		showUserInfo(prefs);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Prefs.getInstance(getApplication()).setLastSearched(mKeyword);
	}

	@Override
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(App.Instance).unregisterReceiver(mConnectErrorHandler);
		LocalBroadcastManager.getInstance(App.Instance).unregisterReceiver(mSyncBeginHandler);
		LocalBroadcastManager.getInstance(App.Instance).unregisterReceiver(mSyncEndHandler);

		super.onDestroy();
		mUIVisible = false;
		TaskHelper.getRequestQueue().cancelAll(GsonRequestTask.TAG);
	}
}
