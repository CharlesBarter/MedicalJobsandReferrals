package cbartersolutions.medicalreferralapp.Animations;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import cbartersolutions.medicalreferralapp.Adapters.RecyclerViewAdapter;

/**
 * Created by Charles on 29/09/2016.
 */

public class Animations {

    public static int DEFAULT_ANIMATION_DURATION = 200;

    public static void setDefaultAnimationDuration(int duration){
        DEFAULT_ANIMATION_DURATION = duration;
    }

    public static void animateIconClick(final ImageView imageView, final int secondIcon) {
        ObjectAnimator rotateYanimation = ObjectAnimator
                .ofFloat(imageView, "rotationY", 0.0f, 90f);
        rotateYanimation.setDuration(DEFAULT_ANIMATION_DURATION);
        rotateYanimation.setInterpolator(new AccelerateInterpolator());
        rotateYanimation.start();

        final ObjectAnimator rotateYanimation2 = ObjectAnimator
                .ofFloat(imageView, "rotationY", 270f, 360f);
        rotateYanimation2.setDuration(DEFAULT_ANIMATION_DURATION);
        rotateYanimation2.setInterpolator(new DecelerateInterpolator());

        rotateYanimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                imageView.setImageResource(secondIcon);
                rotateYanimation2.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
    }

    public static void animateIconPlusBackground(Context context, final ImageView imageView,
                                                 final int secondIcon, RelativeLayout relativeLayout,
                                                 int colorToChangeFrom, int colorToChangeTo){
        //define the animators
        ObjectAnimator rotateYanimation = ObjectAnimator
                .ofFloat(imageView, "rotationY", 0.0f, 90f);
        rotateYanimation.setDuration(DEFAULT_ANIMATION_DURATION);
        rotateYanimation.setInterpolator(new AccelerateInterpolator());
        rotateYanimation.start();

        ObjectAnimator rotateYanimation2 = ObjectAnimator
                .ofFloat(imageView, "rotationY", 270f, 360f);
        rotateYanimation2.setDuration(DEFAULT_ANIMATION_DURATION);
        rotateYanimation2.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator changeBackground = ObjectAnimator.ofObject(relativeLayout, "backgroundColor",
                new ArgbEvaluator(),ContextCompat.getColor(context, colorToChangeFrom),
                ContextCompat.getColor(context, colorToChangeTo));
        changeBackground.setDuration(DEFAULT_ANIMATION_DURATION);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(rotateYanimation2).with(changeBackground);

        rotateYanimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {            }

            @Override
            public void onAnimationEnd(Animator animator) {
                imageView.setImageResource(secondIcon);
                animatorSet.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

    }
}
