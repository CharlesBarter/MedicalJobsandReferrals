package cbartersolutions.medicalreferralapp.Listeners;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by Charles on 29/08/2016.
 *
 */
public abstract class OnRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
    private String TAG = "Recycler View Scroll Listener";

    public abstract void onScrollDown();
    public abstract void onScrollUp();

    private int mThreshold;

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        boolean ifSignificantYShift = Math.abs(dy) > mThreshold;
        if(ifSignificantYShift){
            if(dy > 0){
                onScrollUp();
                Log.d(TAG, "onScrolled: Scroll Up" + dy);
            }else if(dy < 0){
                onScrollDown();
            }
        }
    }

    public void setScrollThreshold(int mThreshold) {
        this.mThreshold = mThreshold;
    }
}
