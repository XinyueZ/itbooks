package com.itbooks.app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.itbooks.R;
import com.itbooks.utils.Prefs;

/**
 * Dialog to show some information about bookmark-list.
 *
 * @author Xinyue Zhao
 */
public final class BookmarkInfoDialogFragment extends DialogFragment implements OnClickListener {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.dialog_fragment_bookmark_info;


	public static BookmarkInfoDialogFragment newInstance( Context context ) {
		return (BookmarkInfoDialogFragment) BookmarkInfoDialogFragment.instantiate( context, BookmarkInfoDialogFragment.class.getName() );
	}

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setCancelable( false );
		setStyle( DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_Light_Dialog );
	}

	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		return inflater.inflate( LAYOUT, container, false );
	}

	@Override
	public void onViewCreated( View view, @Nullable Bundle savedInstanceState ) {
		super.onViewCreated( view, savedInstanceState );
		view.findViewById( R.id.close_confirm_btn ).setOnClickListener( this );
		view.findViewById( R.id.close_i_know_btn ).setOnClickListener( this );
	}

	@Override
	public void onClick( View v ) {
		switch( v.getId() ) {
			case R.id.close_i_know_btn:
				Prefs.getInstance( getActivity().getApplication() ).setKnownBookmark( true );
				break;
		}
		dismiss();
	}
}
