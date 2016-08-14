package cbartersolutions.medicalreferralapp.Listeners;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Created by Charles on 10/08/2016.
 */
public class OnSwipeTouchListener implements OnTouchListener {

    private final GestureDetector gestureDetector;

    public OnSwipeTouchListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSingleTap(){
    }

    public boolean onTouch(View view, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener{
        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent event) {
            return true; //allows more complex gestures than just onDown, if false only onDown allowed
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event){
            onSingleTap();
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float distanceX = e2.getX() - e1.getX(); //distance moved in X direction
            float distanceY = e2.getY() - e1.getY(); //distance moved in y direction
            if (Math.abs(distanceX) > Math.abs(distanceY) //if move in X direction more than Y direction do...
                    && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD //if moved more than the threhold in a lateral direction
                    && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) { //if moved t more than a certain speed in lateral direction
                if (distanceX > 0) //move to the right, ended futher to the right of the screen than started on
                    onSwipeRight();
                else
                    onSwipeLeft();
                return true;
            }
            return false; //if a fling is detected no other gestures will do anything until hand released
        }

    }


}
