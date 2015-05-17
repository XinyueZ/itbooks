package com.itbooks.app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.chopping.application.BasicPrefs;
import com.chopping.fragments.BaseFragment;
import com.itbooks.R;
import com.itbooks.adapters.BookmarkListAdapter;
import com.itbooks.bus.BookmarksLoadedEvent;
import com.itbooks.bus.DeleteBookmarkEvent;
import com.itbooks.bus.RefreshBookmarksEvent;
import com.itbooks.data.DSBookmark;
import com.itbooks.net.bookmark.BookmarkManger;
import com.itbooks.utils.Prefs;
import com.nineoldandroids.animation.ObjectAnimator;

import de.greenrobot.event.EventBus;

/**
 * Show list of all bookmarks.
 *
 * @author Xinyue Zhao
 */
public final class BookmarkListFragment extends BaseFragment {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.fragment_bookmark_list;
	private RecyclerView mBookmarksRv;
	private BookmarkListAdapter mAdp;
	private View mEmptyV;
	private View mRefreshV;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.itbooks.bus.DeleteBookmarkEvent}.
	 *
	 * @param e
	 * 		Event {@link}.
	 */
	public void onEvent(DeleteBookmarkEvent e) {
		DSBookmark bookmark = e.getBookmark();
		BookmarkManger.getInstance().removeBookmark(bookmark.getBook());
		mAdp.notifyDataSetChanged();
		mEmptyV.setVisibility(BookmarkManger.getInstance().getBookmarksInCache().size() <= 0 ? View.VISIBLE : View.GONE);
	}

	/**
	 * Handler for {@link com.itbooks.bus.CleanBookmarkEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.CleanBookmarkEvent}.
	 */
	public void onEvent(com.itbooks.bus.CleanBookmarkEvent e) {
		mAdp.setData(null);
		mAdp.notifyDataSetChanged();
		mEmptyV.setVisibility(View.VISIBLE);
	}

	/**
	 * Handler for {@link com.itbooks.bus.BookmarksLoadedEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.BookmarksLoadedEvent}.
	 */
	public void onEvent(BookmarksLoadedEvent e) {
		if(mObjectAnimator != null) {
			mObjectAnimator.cancel();
		}
	}
	//------------------------------------------------

	public static Fragment newInstance(Context context) {
		return BookmarkListFragment.instantiate(context, BookmarkListFragment.class.getName());
	}


	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(LAYOUT, container, false);
	}

	private ObjectAnimator mObjectAnimator;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setErrorHandlerAvailable(false);
		mBookmarksRv = (RecyclerView) view.findViewById(R.id.bookmarks_rv);
		StaggeredGridLayoutManager llmgr = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
		mBookmarksRv.setLayoutManager(llmgr);
		mEmptyV = view.findViewById(R.id.empty_ll);

		mRefreshV = view.findViewById(R.id.refresh_btn);
		mRefreshV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mObjectAnimator = ObjectAnimator.ofFloat(mRefreshV, "rotation", 0, 360f);
				mObjectAnimator.setDuration(800);
				mObjectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
				mObjectAnimator.start();
				EventBus.getDefault().post(new RefreshBookmarksEvent());
			}
		});
		mRefreshV.setBackgroundColor(getResources().getColor(R.color.common_red));
	}

	@Override
	public void onResume() {
		super.onResume();
		loadBookmarks();
	}

	/**
	 * Get all bookmarks to show.
	 */
	private void loadBookmarks() {
		if (mAdp == null) {
			mAdp = new BookmarkListAdapter(BookmarkManger.getInstance().getBookmarksInCache());
			mBookmarksRv.setAdapter(mAdp);
		} else {
			mAdp.setData(BookmarkManger.getInstance().getBookmarksInCache());
			mAdp.notifyDataSetChanged();
		}
		mEmptyV.setVisibility(
				BookmarkManger.getInstance().getCount() <= 0 ? View.VISIBLE : View.GONE);
	}



	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance(getActivity().getApplication());
	}


}
