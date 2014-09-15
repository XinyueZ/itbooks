package com.itbooks.app;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.widget.ListView;

import com.android.volley.Request.Method;
import com.chopping.net.GsonRequestTask;
import com.chopping.utils.Utils;
import com.itbooks.R;
import com.itbooks.adapters.BookListAdapter;
import com.itbooks.data.DSBookList;
import com.itbooks.utils.Prefs;


public class MainActivity extends BaseActivity implements OnRefreshListener {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_main;

	private SwipeRefreshLayout mRefreshLayout;
	private ListView mLv;
	private BookListAdapter mAdp;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.itbooks.data.DSBookList}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.data.DSBookList}.
	 */
	public void onEvent(DSBookList e) {
		Utils.showShortToast(this, "get books: " + e.getBooks().size());
		mRefreshLayout.setRefreshing(false);

		if (mAdp == null) {
			mAdp = new BookListAdapter(e.getBooks());
			mLv.setAdapter(mAdp);
		} else {
			mAdp.setData(e.getBooks());
			mAdp.notifyDataSetChanged();
		}
	}


	//------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(LAYOUT);
		mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_srl);
		mRefreshLayout.setColorSchemeResources(R.color.green_1, R.color.green_2, R.color.green_3, R.color.green_4);
		mRefreshLayout.setOnRefreshListener(this);
		mRefreshLayout.setRefreshing(true);

		mLv = (ListView) findViewById(R.id.books_lv);
	}

	@Override
	public void onRefresh() {
		loadDefaultPage();
	}

	/**
	 * Default page when nothing wanna be searched.
	 */
	private void loadDefaultPage() {
		String url = Prefs.getInstance(getApplication()).getApiDefaultBooks();
		new GsonRequestTask<DSBookList>(getApplicationContext(), Method.GET, url, DSBookList.class).execute();
	}

	@Override
	protected void onAppConfigLoaded() {
		super.onAppConfigLoaded();
		loadDefaultPage();
	}
}
