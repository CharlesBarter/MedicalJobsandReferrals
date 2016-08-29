package cbartersolutions.medicalreferralapp.Behaviours;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by Charles on 27/08/2016.
 */
public class ScrollAwareFABBehaviour extends FloatingActionButton.Behavior {

    private String TAG = "ScrollAwareFABBehaviour";

    public ScrollAwareFABBehaviour(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout,
                                       FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target,
                        nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton fab,
                               View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, fab, target, dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed);

        int height = fab.getHeight();

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        int fabbottomargin = params.bottomMargin;

        if (dyConsumed > 0 && fab.getVisibility() == View.VISIBLE) {
            fab.hide();
//            fab.animate().translationY(fab.getHeight()+fabbottomargin)
//                    .setInterpolator(new AccelerateInterpolator()).start();
        } else if (dyConsumed < 0 && fab.getVisibility() != View.VISIBLE) {
            fab.show();
//            fab.animate().translationY(0)
//                    .setInterpolator(new DecelerateInterpolator()).start();
        }
    }

        // ...

}
