package com.itbooks.views;

import android.view.View;
import android.view.View.OnClickListener;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * Click listener for some animations.
 *
 * @author Xinyue Zhao
 */
public   abstract class OnButtonAnimatedClickedListener implements OnClickListener {
	/**
	 * Impl. Event what user clicks.
	 */
	public abstract void onClick();

	@Override
	public final void onClick(final View v) {
		v.setEnabled(false);
		final float initX = ViewHelper.getScaleX(v);
		final float initY = ViewHelper.getScaleY(v);
		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playTogether(ObjectAnimator.ofFloat(v, "scaleX", initX, 0.5f, initX).setDuration(100),
				ObjectAnimator.ofFloat(v, "scaleY", initY, 0.5f, initY).setDuration(100));
		animatorSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				onClick();
				v.setEnabled(true);
			}
		});
		animatorSet.start();
	}
}
