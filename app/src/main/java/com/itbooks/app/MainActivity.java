package com.itbooks.app;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.android.volley.Request.Method;
import com.chopping.bus.CloseDrawerEvent;
import com.chopping.net.GsonRequestTask;
import com.chopping.net.TaskHelper;
import com.chopping.utils.Utils;
import com.crashlytics.android.Crashlytics;
import com.itbooks.R;
import com.itbooks.adapters.BookListAdapter;
import com.itbooks.app.fragments.AboutDialogFragment;
import com.itbooks.app.fragments.AppListImpFragment;
import com.itbooks.bus.OpenBookmarkEvent;
import com.itbooks.data.DSBook;
import com.itbooks.data.DSBookList;
import com.itbooks.utils.Prefs;


public class MainActivity extends BaseActivity implements OnQueryTextListener, OnItemClickListener,
	 OnScrollListener {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_main;
	/**
	 * Main menu.
	 */
	private static final int MAIN_MENU = R.menu.main_menu;
	/**
	 * Foot view for loading more.
	 */
	private static final int LAYOUT_LOAD_MORE = R.layout.inc_load_more;

	private static final int MAX_PAGER = 100;

	private ListView mLv;
	private BookListAdapter mAdp;

	private SearchRecentSuggestions mSuggestions;
	private String mKeyword;
	private SearchView mSearchView;

	private int mCurrentPage = 1;
	private boolean mLoadedMore;
	private View mLoadMoreIndicatorV;

	private int mPreItemOnLast;

	private boolean mDetailOpened;
	/**
	 * Use navigation-drawer for this fork.
	 */
	private ActionBarDrawerToggle mDrawerToggle;
	/**
	 * Navigation drawer.
	 */
	private DrawerLayout mDrawerLayout;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

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
	 * Handler for {@link com.itbooks.data.DSBookList}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.data.DSBookList}.
	 */
	public void onEvent(DSBookList e) {
		mRefreshLayout.setRefreshing(false);
		if (TextUtils.equals(e.getError(), Prefs.API_LIMIT)) {
			new AlertDialog.Builder(this).setTitle(R.string.application_name).setMessage(R.string.lbl_api_limit).setCancelable(
					false).setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					resetPaging();
					finish();
				}
			}).create().show();
			setHasShownDataOnUI(false);
		} else {
			int total = Integer.parseInt(e.getTotal());
			if (total == 0) {
				mLv.setVisibility(View.GONE);
				Utils.showShortToast(this, R.string.lbl_no_data);
			} else {
				mCurrentPage = e.getPage();
				mLv.setVisibility(View.VISIBLE);
				if (mAdp == null) {
					mAdp = new BookListAdapter(e.getBooks());
					mLv.setAdapter(mAdp);
				} else {
					if (!mLoadedMore) {
						mAdp.setData(e.getBooks());
					} else {
						mAdp.getBooks().addAll(e.getBooks());
						mLoadedMore = false;
					}
					mAdp.notifyDataSetChanged();
				}
				if (total > 10) {
					mCurrentPage++;
				}
			}
			setHasShownDataOnUI(true);
		}
	}


	//------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);
		setContentView(LAYOUT);


		mSuggestions = new SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY,
				SearchSuggestionProvider.MODE);

		mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_srl);
		mRefreshLayout.setColorSchemeResources(R.color.green_1, R.color.green_2, R.color.green_3, R.color.green_4);
		mRefreshLayout.setOnRefreshListener(this);
		mRefreshLayout.setRefreshing(true);

		mLv = (ListView) findViewById(R.id.books_lv);
		mLv.setOnItemClickListener(this);

		mLoadMoreIndicatorV = getLayoutInflater().inflate(LAYOUT_LOAD_MORE, mLv, false);
		mLoadMoreIndicatorV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
		mLv.addFooterView(mLoadMoreIndicatorV);

		handleIntent(getIntent());

		mLv.setOnScrollListener(this);

		mKeyword = Prefs.getInstance(getApplication()).getLastSearched();
		initDrawer();
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

		mSearchView.setOnQueryTextListener(this);
		SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
		if (searchManager != null) {
			SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
			mSearchView.setSearchableInfo(info);
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
		if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		switch (item.getItemId()) {
		case R.id.action_about:
			showDialogFragment(AboutDialogFragment.newInstance(this), null);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void handleIntent(Intent intent) {
		mRefreshLayout.setRefreshing(true);
		mKeyword = intent.getStringExtra(SearchManager.QUERY);
		if (!TextUtils.isEmpty(mKeyword)) {
			if(mSearchView != null) {
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
		resetPaging();
		loadBooks();
	}

	private void loadBooks() {
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
		String url = Prefs.getInstance(getApplication()).getApiSearchBooks();
		url = String.format(url, Utils.encode("Android"), mCurrentPage + "");
		//		LL.d("load: " + url);
		new GsonRequestTask<DSBookList>(getApplicationContext(), Method.GET, url, DSBookList.class).execute();
	}


	/**
	 * Page when nothing wanna be searched.
	 */
	private void loadByKeyword() {
		String url = Prefs.getInstance(getApplication()).getApiSearchBooks();
		url = String.format(url, Utils.encode(mKeyword), mCurrentPage + "");
		//		LL.d("load: " + url);
		new GsonRequestTask<DSBookList>(getApplicationContext(), Method.GET, url, DSBookList.class).execute();
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		DSBook book = (DSBook) mAdp.getItem(position);
		openBookDetail(book);
	}

	private void openBookDetail(DSBook book) {
		mDetailOpened = true;
		BookDetailActivity.showInstance(this, book.getId());
	}

	public void search(View view) {
		resetPaging();
		mKeyword =mSearchView.getQuery().toString();
		loadBooks();

		mLoadMoreIndicatorV.setVisibility(View.VISIBLE);
	}

	private void resetPaging() {
		mCurrentPage = 1;
		mPreItemOnLast = 0;
	}

	@Override
	protected void onAppConfigLoaded() {
		super.onAppConfigLoaded();
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
		if (!mDetailOpened) {
			loadBooks();
		} else {
			mDetailOpened = false;
		}

		showAppList();
	}

	private Handler mDelayLoadBooksHandler = new Handler();
	private Runnable mDelayLoadBooksTask = new Runnable() {
		@Override
		public void run() {
			loadBooks();
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDelayLoadBooksHandler != null && mDelayLoadBooksTask != null) {
			mDelayLoadBooksHandler.removeCallbacks(mDelayLoadBooksTask);
		}
		TaskHelper.getRequestQueue().cancelAll(GsonRequestTask.TAG);
	}

	private void loadMore() {
		if (mCurrentPage < MAX_PAGER) {
			mLoadedMore = true;
			mRefreshLayout.setRefreshing(true);
			mDelayLoadBooksHandler.postDelayed(mDelayLoadBooksTask, 5500);
		} else {
			Utils.showLongToast(getApplicationContext(), R.string.lbl_no_more);
			mLoadMoreIndicatorV.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onReload() {
		super.onReload();
		loadBooks();
	}


	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}


	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {


		// Make your calculation stuff here. You have all your
		// needed info from the parameters of this function.

		// Sample calculation to determine if the last
		// item is fully visible.
		final int lastItem = firstVisibleItem + visibleItemCount;
		if (lastItem == totalItemCount) {
			if (mPreItemOnLast != lastItem) { //to avoid multiple calls for last item
				loadMore();
				mPreItemOnLast = lastItem;
			}
		}

	}


	/**
	 * Show all external applications links.
	 */
	private void showAppList() {
		getSupportFragmentManager().beginTransaction().replace(R.id.app_list_fl, AppListImpFragment.newInstance(this))
				.commit();
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
			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,   R.string.application_name,
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
					mDrawerLayout.openDrawer(Gravity.RIGHT);
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
	}
}
