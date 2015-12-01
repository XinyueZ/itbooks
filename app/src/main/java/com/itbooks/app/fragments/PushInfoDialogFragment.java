package com.itbooks.app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.os.AsyncTaskCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.itbooks.R;
import com.itbooks.bus.AskedPushEvent;
import com.itbooks.gcm.RegGCMTask;
import com.itbooks.utils.Prefs;

import de.greenrobot.event.EventBus;

/**
 * Dialog to show some information about push
 *
 * @author Xinyue Zhao
 */
public final class PushInfoDialogFragment extends DialogFragment implements OnClickListener {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.dialog_fragment_push_info;


	public static PushInfoDialogFragment newInstance( Context context ) {
		return (PushInfoDialogFragment) PushInfoDialogFragment.instantiate( context, PushInfoDialogFragment.class.getName() );
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
		Prefs prefs = Prefs.getInstance( getActivity().getApplication() );
		switch( v.getId() ) {
			case R.id.close_i_know_btn:
				prefs.setKnownPush( true );
				break;
			case R.id.close_confirm_btn:
				AsyncTaskCompat.executeParallel( new RegGCMTask( getActivity() ) );
				break;
		}
		EventBus.getDefault().post( new AskedPushEvent() );
		dismiss();
	}
}
