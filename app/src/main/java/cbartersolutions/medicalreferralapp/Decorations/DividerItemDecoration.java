package cbartersolutions.medicalreferralapp.Decorations;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Charles on 3/09/2016.
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;
    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

    public DividerItemDecoration(Context context){
        final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
        mDivider = styledAttributes.getDrawable(0);
        styledAttributes.recycle();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if(parent.getChildAdapterPosition(view) == 0){
            return;
        }
        outRect.top = mDivider.getIntrinsicHeight();
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i<childCount; i++){
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();//get the child layout parameters
            int top = child.getBottom() + params.bottomMargin //set top of the divider at the bottom of the view below the margin of the view
                    + (int)(child.getTranslationY()+0.5f); //does not appear if view is being removed
            int bottom = top + mDivider.getIntrinsicHeight();//set bottom of the divider as the top plus the height of the divider

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}
