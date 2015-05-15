package com.itbooks.app.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.chopping.application.BasicPrefs;
import com.chopping.fragments.BaseFragment;
import com.itbooks.App;
import com.itbooks.R;
import com.itbooks.adapters.BookmarkListAdapter;
import com.itbooks.app.BaseActivity;
import com.itbooks.bus.DeleteBookmarkEvent;
import com.itbooks.data.DSBookmark;
import com.itbooks.utils.Prefs;

import cn.bmob.v3.listener.DeleteListener;

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
		deleteBookmark(bookmark);
	}

	/**
	 * Handler for {@link com.itbooks.bus.CleanBookmarkEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.CleanBookmarkEvent}.
	 */
	public void onEvent(com.itbooks.bus.CleanBookmarkEvent e) {
		mAdp.setBookmarkList(null);
		mAdp.notifyDataSetChanged();
		mEmptyV.setVisibility(View.VISIBLE);
	}

	//------------------------------------------------

	public static Fragment newInstance(Context context ) {
		return  BookmarkListFragment.instantiate(context, BookmarkListFragment.class.getName()  );
	}



	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(LAYOUT, container, false);
	}

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
				loadBookmarks();
			}
		});


	}

	@Override
	public void onResume() {
		super.onResume();
		loadBookmarks();
	}

	private void loadBookmarks() {
		App app = (App) getActivity().getApplication();
		if(mAdp == null) {
			mAdp = new BookmarkListAdapter(app.getBookmarksInCache());
			mBookmarksRv.setAdapter(mAdp);
		} else {
			mAdp.setBookmarkList(app.getBookmarksInCache());
			mAdp.notifyDataSetChanged();
		}
		mEmptyV.setVisibility(app.getBookmarksInCache().size() <= 0 ? View.VISIBLE : View.GONE);
	}


	private void deleteBookmark(DSBookmark bookmark) {
		App app = (App) getActivity().getApplication();
		removeBookmarkInNet(app, bookmark );
		app.removeFromBookmark(bookmark.getBook());
		mAdp.notifyDataSetChanged();
		mEmptyV.setVisibility(app.getBookmarksInCache().size() <= 0 ? View.VISIBLE : View.GONE);
	}
	/**
	 * Remove bookmark in net.
	 * @param app
	 * @param bookmark
	 */
	private void removeBookmarkInNet( App app , final DSBookmark bookmark ) {
		DSBookmark delBookmark = new DSBookmark(bookmark.getBook());
		delBookmark.setObjectId(bookmark.getObjectId());
		BaseActivity baseActivity = (BaseActivity) getActivity();
		baseActivity.openPb();
		delBookmark.delete(app, new DeleteListener() {
			@Override
			public void onSuccess() {
				BaseActivity baseActivity = (BaseActivity) getActivity();
				baseActivity.closePb();
			}

			@Override
			public void onFailure(int i, String s) {
				BaseActivity baseActivity = (BaseActivity) getActivity();
				baseActivity.closePb();
				((BaseActivity) getActivity()).showDialogFragment(new DialogFragment() {
					@Override
					public Dialog onCreateDialog(Bundle savedInstanceState) {
						// Use the Builder class for convenient dialog construction
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setCancelable(false).setTitle(R.string.application_name).setMessage(
								R.string.msg_op_fail).setPositiveButton(R.string.btn_retry,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										removeBookmarkInNet((App) getActivity().getApplication(), bookmark);
									}
								});
						// Create the AlertDialog object and return it
						return builder.create();
					}
				}, null);
			}
		});
	}


	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance(getActivity().getApplication());
	}


}
