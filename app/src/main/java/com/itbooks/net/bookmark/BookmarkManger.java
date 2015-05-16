package com.itbooks.net.bookmark;

import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import android.app.Application;
import android.support.annotation.Nullable;

import com.itbooks.bus.BookmarksLoadedEvent;
import com.itbooks.bus.CloseProgressDialogEvent;
import com.itbooks.bus.OpenProgressDialogEvent;
import com.itbooks.data.DSBookmark;
import com.itbooks.data.rest.RSBook;
import com.itbooks.utils.DeviceUniqueUtil;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import de.greenrobot.event.EventBus;

/**
 * Operation on bookmarks, including memory local and remote in cloud.
 *
 * @author Xinyue Zhao
 */
public final class BookmarkManger {
	/**
	 * All cached bookmarks.
	 */
	private List<DSBookmark> mBookmarksInCache = new LinkedList<>();
	/**
	 * The application context.
	 */
	private Application mContext;

	private   static  BookmarkManger sInstance;

	public static final void createInstance(Application context) {
		sInstance = new BookmarkManger(context);
	}

	public static final BookmarkManger getInstance() {
		return sInstance;
	}

	/**
	 * Constructor of {@link BookmarkManger}
	 * @param context The application context.
	 */
	private BookmarkManger(Application context) {
		mContext = context;
	}

	/**
	 * Initialize the bookmarks in cache.
	 *
	 * @param bookmarks
	 * 		The bookmarks remote.
	 */
	private void setBookmarksInCache(List<DSBookmark> bookmarks) {
		mBookmarksInCache = bookmarks;
	}

	/**
	 * To test whether the book has been added as bookmark, and get the instance of the object.
	 *
	 * @param book
	 * 		{@link RSBook}, the book to test.
	 *
	 * @return The object found in list, it might be {@code null}.
	 */
	public @Nullable
	DSBookmark getBookmarked(RSBook book) {
		for (DSBookmark bookmark : mBookmarksInCache) {
			if (bookmark.getBook().equals(book)) {
				return bookmark;
			}
		}
		return null;
	}

	/**
	 * Bookmark.
	 *
	 * @param newBookmark
	 * 		{@link DSBookmark}, the book to add.
	 */
	private void addBookmark(DSBookmark newBookmark) {
		mBookmarksInCache.add(newBookmark);
	}

	/**
	 * Remove from bookmark.
	 *
	 * @param book
	 * 		{@link RSBook}, the book to remove.
	 */
	private void removeFromBookmark(RSBook book) {
		for (DSBookmark bookmark : mBookmarksInCache) {
			if (bookmark.getBook().equals(book)) {
				mBookmarksInCache.remove(bookmark);
				return;
			}
		}
	}

	/**
	 *
	 * @return  All cached bookmarks.
	 */
	public List<DSBookmark> getBookmarksInCache() {
		return mBookmarksInCache;
	}

	/**
	 *
	 * @return Count of all bookmarks in cache.
	 */
	public int getCount(){
		return getBookmarksInCache().size();
	}

	/**
	 * Delete bookmarks in net.
	 */
	public void removeAllRemoteBookmarks() {
		getBookmarksInCache().clear();
		try {
			BmobQuery<DSBookmark> queryBookmarks = new BmobQuery<>();
			queryBookmarks.addWhereEqualTo("mUID", DeviceUniqueUtil.getDeviceIdent(mContext));
			EventBus.getDefault().post(new OpenProgressDialogEvent());
			queryBookmarks.findObjects(mContext, new FindListener<DSBookmark>() {
				@Override
				public void onSuccess(List<DSBookmark> list) {
					for(DSBookmark b : list) {
						DSBookmark delBookmark = new DSBookmark(b.getBook());
						delBookmark.setObjectId(b.getObjectId());
						delBookmark.delete(mContext);
					}
					EventBus.getDefault().post(new CloseProgressDialogEvent());
				}

				@Override
				public void onError(int i, String s) {
					EventBus.getDefault().post(new CloseProgressDialogEvent());
					removeAllRemoteBookmarks();
				}
			});
		} catch (NoSuchAlgorithmException e) {
			//TODO Error when can not get device id.
		}
	}

	/**
	 * Add bookmark in net.
	 *
	 * @param bookmark The bookmark to add.
	 */
	private void addRemoteBookmark( final DSBookmark bookmark) {
		EventBus.getDefault().post(new OpenProgressDialogEvent());
		bookmark.save(mContext, new SaveListener() {
			@Override
			public void onSuccess() {
				EventBus.getDefault().post(new CloseProgressDialogEvent());
			}

			@Override
			public void onFailure(int i, String s) {
				EventBus.getDefault().post(new CloseProgressDialogEvent());
				addRemoteBookmark(  bookmark);
			}
		});
	}

	/**
	 * Remove bookmark in net.
	 *
	 * @param bookmark The bookmark to remove.
	 */
	private void removeRemoteBookmark( final DSBookmark bookmark) {
		final DSBookmark delBookmark = new DSBookmark(bookmark.getBook());
		delBookmark.setObjectId(bookmark.getObjectId());
		EventBus.getDefault().post(new OpenProgressDialogEvent());
		delBookmark.delete(mContext, new DeleteListener() {
			@Override
			public void onSuccess() {
				EventBus.getDefault().post(new CloseProgressDialogEvent());
			}

			@Override
			public void onFailure(int i, String s) {
				EventBus.getDefault().post(new CloseProgressDialogEvent());
				removeRemoteBookmark(bookmark);
			}
		});
	}

	/**
	 * Make UI for loading all bookmarks
	 */
	public void loadAllBookmarks( ) {
		try {
			BmobQuery<DSBookmark> queryBookmarks = new BmobQuery<>();
			queryBookmarks.addWhereEqualTo("mUID", DeviceUniqueUtil.getDeviceIdent(mContext));
			queryBookmarks.findObjects(mContext, new FindListener<DSBookmark>() {
				@Override
				public void onSuccess(List<DSBookmark> list) {
					getInstance().setBookmarksInCache(list);
					EventBus.getDefault().post(new BookmarksLoadedEvent());
				}

				@Override
				public void onError(int i, String s) {
					EventBus.getDefault().post(new BookmarksLoadedEvent());
				}
			});
		} catch (NoSuchAlgorithmException e) {
			//TODO Error when can not get device id.
		}
	}


	/**
	 * The bookmark of the {@code book} will be removed.
	 * @param book The book whose bookmark will be removed.
	 */
	public void removeBookmark(RSBook book) {
		DSBookmark foundBookmark = BookmarkManger.getInstance().getBookmarked(book);
		BookmarkManger.getInstance().removeFromBookmark(book);
		BookmarkManger.getInstance().removeRemoteBookmark(foundBookmark);
	}

	/**
	 * Create bookmark of the {@code book}.
	 * @param book The book whose bookmark will be created..
	 */
	public void addBookmark(RSBook book) {
		try {
			DSBookmark bookmark = new DSBookmark(book, DeviceUniqueUtil.getDeviceIdent(
					mContext));
			BookmarkManger.getInstance().addBookmark(bookmark);
			BookmarkManger.getInstance().addRemoteBookmark(bookmark);
		} catch (NoSuchAlgorithmException e) {
			//TODO Error when can not get device id.
		}
	}
}
