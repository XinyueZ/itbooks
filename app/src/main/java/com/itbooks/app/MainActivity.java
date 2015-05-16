package com.itbooks.app;

import android.app.Dialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

import com.chopping.bus.CloseDrawerEvent;
import com.chopping.net.GsonRequestTask;
import com.chopping.net.TaskHelper;
import com.chopping.utils.DeviceUtils;
import com.chopping.utils.DeviceUtils.ScreenSize;
import com.crashlytics.android.Crashlytics;
import com.gc.materialdesign.widgets.SnackBar;
import com.itbooks.R;
import com.itbooks.adapters.AbstractBookViewAdapter;
import com.itbooks.adapters.BookGridAdapter;
import com.itbooks.adapters.BookListAdapter;
import com.itbooks.app.fragments.AboutDialogFragment;
import com.itbooks.app.fragments.AppListImpFragment;
import com.itbooks.app.fragments.BookmarkListFragment;
import com.itbooks.app.fragments.PushInfoDialogFragment;
import com.itbooks.bus.BookmarksLoadedEvent;
import com.itbooks.bus.CleanBookmarkEvent;
import com.itbooks.bus.EULAConfirmedEvent;
import com.itbooks.bus.EULARejectEvent;
import com.itbooks.bus.NewAPIVersionUpdateEvent;
import com.itbooks.bus.OpenBookDetailEvent;
import com.itbooks.bus.OpenBookmarkEvent;
import com.itbooks.bus.RefreshBookmarksEvent;
import com.itbooks.data.rest.RSBook;
import com.itbooks.data.rest.RSBookList;
import com.itbooks.data.rest.RSBookQuery;
import com.itbooks.net.api.Api;
import com.itbooks.net.api.ApiNotInitializedException;
import com.itbooks.net.bookmark.BookmarkManger;
import com.itbooks.utils.Prefs;

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

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

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
		getSupportFragmentManager().beginTransaction().replace(R.id.bookmark_list_container_fl, BookmarkListFragment
				.newInstance(getApplicationContext()))
				.commit();
	}
	//------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(LAYOUT);
		Crashlytics.start(this);

		mScreenSize = DeviceUtils.getScreenSize(getApplicationContext());
		mSuggestions = new SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY,
				SearchSuggestionProvider.MODE);

		mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_srl);
		mRefreshLayout.setColorSchemeResources(R.color.green_1, R.color.green_2, R.color.green_3, R.color.green_4);
		mRefreshLayout.setOnRefreshListener(this);
		mRefreshLayout.setRefreshing(true);

		mRv = (RecyclerView) findViewById(R.id.books_rv);
		if (Prefs.getInstance(getApplicationContext()).getViewStyle() == 2) {
			mRv.setLayoutManager(mLayoutManager = new LinearLayoutManager(this));
			mRv.setAdapter(new BookListAdapter(null));
		} else {
			mRv.setLayoutManager(mLayoutManager = new GridLayoutManager(this, GRID_COL_COUNT));
			mRv.setAdapter(new BookGridAdapter(null,GRID_COL_COUNT, mScreenSize));
		}

		handleIntent(getIntent());

		mKeyword = Prefs.getInstance(getApplication()).getLastSearched();
		initDrawer();
		initSlidingPanel();

		View topV = findViewById(R.id.to_top_btn);
		topV.setBackgroundColor(getResources().getColor(R.color.common_green));
		topV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mLayoutManager.scrollToPositionWithOffset(0, 0);
				getSupportActionBar().show();
			}
		});

		getBookmarks();
	}

	/**
	 * Get and load all bookmarks.
	 */
	private void getBookmarks() {
		BookmarkManger.getInstance().loadAllBookmarks( );
	}


	@Override
	protected void onPause() {
		super.onPause();
		Prefs.getInstance(getApplication()).setLastSearched(mKeyword);
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

		if(Prefs.getInstance(getApplicationContext()).getViewStyle() == 2) {
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
		String subject = getString(R.string.lbl_share_app);
		String text = getString(R.string.lbl_share_app_content);
		provider.setShareIntent(getDefaultShareIntent(provider, subject, text));
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

		case R.id.action_setting:
			SettingActivity.showInstance(this);
			break;
		case R.id.action_view_style_list:
			mRv.setLayoutManager(mLayoutManager = new LinearLayoutManager(this));
			mRv.setAdapter(new BookListAdapter(((AbstractBookViewAdapter) mRv.getAdapter()).getData()));
			Prefs.getInstance(getApplicationContext()).setViewStyle(2);
			supportInvalidateOptionsMenu();
			break;
		case R.id.action_view_style_grid:
			mRv.setLayoutManager(mLayoutManager = new GridLayoutManager(this, GRID_COL_COUNT));
			mRv.setAdapter(new BookGridAdapter(((AbstractBookViewAdapter) mRv.getAdapter()).getData(), GRID_COL_COUNT, mScreenSize)  );
			Prefs.getInstance(getApplicationContext()).setViewStyle(1);
			supportInvalidateOptionsMenu();
			break;
		}
		return super.onOptionsItemSelected(item);
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
		if (((AbstractBookViewAdapter)mRv.getAdapter()).getItemCount() == 0) {
			findViewById(R.id.loading_pb).setVisibility(View.VISIBLE);
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
		loadBooks();
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
		showAppList();
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
		showAppList();
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		TaskHelper.getRequestQueue().cancelAll(GsonRequestTask.TAG);
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
		getSupportFragmentManager().beginTransaction().replace(R.id.app_list_fl, AppListImpFragment.newInstance(this))
				.commit();
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
			findViewById(R.id.open_bookmarks_ll).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					openBookmarkList();
					mDrawerLayout.closeDrawer(Gravity.LEFT);
				}
			});
			findViewById(R.id.open_setting_ll).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SettingActivity.showInstance(MainActivity.this);
					mDrawerLayout.closeDrawer(Gravity.LEFT);
				}
			});
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
		showPushInfo();
	}

	private void showPushInfo() {
		Prefs prefs = Prefs.getInstance(getApplication());
		if (prefs.isEULAOnceConfirmed() && !prefs.hasKnownPush()) {
			showDialogFragment(PushInfoDialogFragment.newInstance(getApplication()), null);
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

	/**
	 * Show feeds.
	 *
	 * @param bookList
	 * 		The result of REST call.
	 */
	public void showBookList(RSBookList bookList) {
		if (bookList != null && bookList.getStatus() == 200 && bookList.getBooks() != null &&
				bookList.getBooks().size() > 0) {
			((AbstractBookViewAdapter)mRv.getAdapter()).setData(bookList.getBooks());
			mRv.getAdapter().notifyDataSetChanged();
			setHasShownDataOnUI(true);
			new SnackBar(this, String.format(getString(R.string.msg_items_count),
					bookList.getBooks().size())).show();
		} else {
			 new SnackBar(this, getString(R.string.msg_refresh_fail), getString(R.string.btn_retry),
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							loadBooks();
						}
					}) .show();
		}
		findViewById(R.id.loading_pb).setVisibility(View.GONE);
		mRefreshLayout.setRefreshing(false);
	}
}
