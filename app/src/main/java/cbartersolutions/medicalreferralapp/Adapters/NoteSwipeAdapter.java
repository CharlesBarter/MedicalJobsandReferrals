package cbartersolutions.medicalreferralapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import java.util.ArrayList;

import cbartersolutions.medicalreferralapp.Activities.MainActivity;
import cbartersolutions.medicalreferralapp.Fragments.ListViewFragment;
import cbartersolutions.medicalreferralapp.Others.Note;
import cbartersolutions.medicalreferralapp.R;

/**
 * Created by Charles on 14/08/2016.
 */
public class NoteSwipeAdapter extends BaseSwipeAdapter {

    private Context mContext;
    private final ListViewFragment fragment_called_from;
    private ArrayList<Note> notes;
    private boolean deleted_notes;

    public NoteSwipeAdapter(Context mContext, ArrayList<Note> notes, ListViewFragment fragment){
        this.mContext = mContext;
        this.fragment_called_from = fragment;
        this.notes = notes;

        deleted_notes = fragment_called_from.getActivity()
                .getIntent().getExtras().getBoolean(MainActivity.DELETED_NOTES);

    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.list_swipe;
    }


    @Override
    public View generateView(final int position, ViewGroup parent) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_swipe_layout, parent, false);
        SwipeLayout swipeLayout = (SwipeLayout)v.findViewById(getSwipeLayoutResourceId(position));
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, v.findViewById(R.id.swipe_background));

        View trash_icon = swipeLayout.findViewById(R.id.trash); //create the trash icon
        View add_icon = swipeLayout.findViewById(R.id.recover);
        if(deleted_notes) {//if NOT a deleted notes then only trash icon visible
            trash_icon.setVisibility(View.GONE);
            add_icon.setVisibility(View.VISIBLE);
        }else{//if it is a deleted note only show add icon
            trash_icon.setVisibility(View.VISIBLE);
            add_icon.setVisibility(View.GONE);
        }
        swipeLayout.addRevealListener(R.id.swipe_background, new SwipeLayout.OnRevealListener() {
            @Override
            public void onReveal(View child, SwipeLayout.DragEdge edge, float fraction, int distance) {
                    int c = (Integer) evaluate(fraction, Color.parseColor("#f9f9f9"), Color.parseColor("#d50000"));
                    child.setBackgroundColor(c);
//                if (distance >= 1000){
//                    Intent intent_to_delete = fragment_called_from.putIntentInfo(position);
//                    fragment_called_from.jobDone(intent_to_delete);
//                }
            }
        });
        if(deleted_notes) {
            swipeLayout.addDrag(SwipeLayout.DragEdge.Right, v.findViewById(R.id.swipe_background2));
            swipeLayout.addRevealListener(R.id.swipe_background2, new SwipeLayout.OnRevealListener() {
                @Override
                public void onReveal(View child, SwipeLayout.DragEdge edge, float fraction, int distance) {
                    if(distance >= 500) {
                        int c = (Integer) evaluate(fraction, Color.parseColor("#f9f9f9"), Color.parseColor("#000000"));
                        child.setBackgroundColor(c);
                    }
                    if (distance >= 1000) {
                        fragment_called_from.permanentDeleteNote(notes.get(position).getNoteId(), position);
                    }
                }
            });
        }
        swipeLayout.addSwipeListener(new SimpleSwipeListener(){
            @Override
            public void onOpen(SwipeLayout layout) {
                Log.d("Test", "onOpen");
                Intent intent_to_delete = fragment_called_from.putIntentInfo(position);
                fragment_called_from.jobDone(intent_to_delete);
            }
        });
        return v;
    }

    @Override
    public void fillValues(int position, View convertView) {
        //get the data item for this position
        Note note = notes.get(position);


        //create the text views within this code so they can be used
        TextView notePatientName = (TextView) convertView.findViewById(R.id.listItemPatientName);
        TextView notePatientNHI = (TextView) convertView.findViewById(R.id.listItemPatientNHI);
        TextView notePatient_Age_Sex = (TextView) convertView.findViewById(R.id.list_age_sex);
        TextView noteDetails = (TextView) convertView.findViewById(R.id.listItemDetails);
        ImageView noteIcon = (ImageView) convertView.findViewById(R.id.listItemNoteImage);


        //fill each reference text view with the data associated with note identified by position
        notePatientName.setText(note.getPatientname());
        notePatientNHI.setText(note.getPatientNHI());
        notePatient_Age_Sex.setText(note.getPatient_Age_Sex());
        noteDetails.setText(note.getdetails());
        noteIcon.setImageResource(note.getAssociatedDrawable());
    }

    @Override
    public int getCount() {
        return notes.size();
    }

    @Override
    public Object getItem(int position) {
        return notes.get(position);
    }

    @Override
    public long getItemId(int i) {
        Note active_note = notes.get(i);
        return active_note.getNoteId();
    }

    /*
 Color transition method.
  */
    public Object evaluate(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return ((startA + (int) (fraction * (endA - startA))) << 24) |
               ((startR + (int) (fraction * (endR - startR))) << 16) |
               ((startG + (int) (fraction * (endG - startG))) << 8) |
               ((startB + (int) (fraction * (endB - startB))));
    }

}
