package com.itbooks.app.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.itbooks.R;
import com.itbooks.app.App;
import com.itbooks.databinding.ActivityConnectGoogleBinding;
import com.itbooks.utils.Prefs;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.squareup.picasso.Picasso;

/**
 * Login on Google.
 *
 * @author Xinyue Zhao
 */
public final class ConnectGoogleActivity extends BaseActivity {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_connect_google;
	/**
	 * Request-id of this  {@link Activity}.
	 */
	public static final int REQ = 0x91;
	/**
	 * SignIn request-code.
	 */
	private static final int RC_SIGN_IN = 0x92;
	/**
	 * Data-binding.
	 */
	private ActivityConnectGoogleBinding mBinding;
	/**
	 * The Google-API.
	 */
	private GoogleApiClient mGoogleApiClient;

	private volatile boolean mUIVisible;

	/**
	 * Show single instance of {@link ConnectGoogleActivity}
	 *
	 * @param cxt
	 * 		{@link Context}.
	 */
	public static void showInstance(Activity cxt) {
		Intent intent = new Intent(cxt, ConnectGoogleActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		ActivityCompat.startActivityForResult(cxt, intent, REQ, null);
	}


	private void handleGoogleLogin(GoogleSignInResult result) {
		if (result.isSuccess() && mUIVisible) {
			Prefs prefs = Prefs.getInstance(App.Instance);
			GoogleSignInAccount acct = result.getSignInAccount();
			if (acct != null) {
				prefs.setGoogleId(acct.getId());
				prefs.setGoogleDisplyName(acct.getDisplayName());

				Picasso picasso = Picasso.with(App.Instance);
				Uri imageLoc = acct.getPhotoUrl();
				if (imageLoc != null) {
					String s = imageLoc.toString();
					picasso.load(s).into(mBinding.thumbIv);
					prefs.setGoogleThumbUrl(s);
				}
				ViewPropertyAnimator.animate(mBinding.thumbIv).cancel();
				ViewPropertyAnimator.animate(mBinding.thumbIv).alpha(1).setDuration(500).start();
				mBinding.helloTv.setText(getString(R.string.lbl_hello, acct.getDisplayName()));
				mBinding.loginPb.setVisibility(View.GONE);
				mBinding.closeBtn.setVisibility(View.VISIBLE);
				Animation shake = AnimationUtils.loadAnimation(App.Instance, R.anim.shake);
				mBinding.closeBtn.startAnimation(shake);
			}
		} else {
			if (mUIVisible) {
				Snackbar.make(mBinding.loginContentLl, R.string.meta_load_error, Snackbar.LENGTH_LONG).setAction(
						R.string.btn_close, new OnClickListener() {
							@Override
							public void onClick(View v) {
								ActivityCompat.finishAffinity(ConnectGoogleActivity.this);
							}
						}).show();
			}
		}
	}


	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		super.onBackPressed();
	}

	/**
	 * Login Google+
	 */
	private void loginGoogle() {
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
		startActivityForResult(signInIntent, RC_SIGN_IN);
	}


	@Override
	public void onRefresh() {

	}


	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		if (requestCode == RC_SIGN_IN && mUIVisible) {
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
			handleGoogleLogin(result);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBinding = DataBindingUtil.setContentView(this, LAYOUT);
		setUpErrorHandling((ViewGroup) findViewById(R.id.error_content));
		mUIVisible = true;

		mBinding.googleLoginBtn.setSize(SignInButton.SIZE_WIDE);
		mBinding.helloTv.setText(getString(R.string.lbl_welcome, getString(R.string.application_name)));
		ViewCompat.setElevation(mBinding.sloganVg, getResources().getDimension(R.dimen.common_elevation));
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
				.build();
		mGoogleApiClient = new GoogleApiClient.Builder(App.Instance).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
		mBinding.googleLoginBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mBinding.googleLoginBtn.setVisibility(View.GONE);
				mBinding.loginPb.setVisibility(View.VISIBLE);
				mBinding.helloTv.setText(R.string.lbl_connect_google);
				ViewPropertyAnimator.animate(mBinding.thumbIv).cancel();
				ViewPropertyAnimator.animate(mBinding.thumbIv).alpha(0.3f).setDuration(500).start();
				loginGoogle();
			}
		});


		mBinding.closeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				ActivityCompat.finishAfterTransition(ConnectGoogleActivity.this);
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mUIVisible = false;
	}
}
