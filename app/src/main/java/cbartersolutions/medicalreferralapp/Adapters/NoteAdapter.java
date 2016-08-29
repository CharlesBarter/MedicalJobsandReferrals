package cbartersolutions.medicalreferralapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;


import cbartersolutions.medicalreferralapp.Activities.MainActivity;
import cbartersolutions.medicalreferralapp.Fragments.ListViewFragment;
import cbartersolutions.medicalreferralapp.Listeners.OnSwipeTouchListener;
import cbartersolutions.medicalreferralapp.Others.Note;
import cbartersolutions.medicalreferralapp.R;

/**
 * Created by Charles on 13/07/2016.
 */
public class NoteAdapter extends ArrayAdapter<Note>{

    private static String TAG = "NoteAdapter";

    public static class ViewHolder{
        TextView notePatientName;
        TextView notePatientNHI;
        TextView notePatient_Age_Sex;
        TextView noteDetails;
        ImageView noteIcon;
    }

    Context mcon;
    ListViewFragment fragment_called_from;

    public NoteAdapter(Context context, ArrayList<Note> notes, ListViewFragment fragment){
        super(context, 0, notes);
        mcon = context;
        this.fragment_called_from = fragment;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){

        //get the data item for this position
        Note note = getItem(position);

        //create the views
        ViewHolder viewHolder;

        //Check if existing view is being reused, otherwise inflate a new view from custom row layout
        if (convertView == null){

            //if we don't already have a view for a list position create a view by inflating the joblistrow.xml
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.joblistrow, parent, false);
            //create the text views within this code so they can be used
            viewHolder.notePatientName = (TextView) convertView.findViewById(R.id.listItemPatientName);
            viewHolder.notePatientNHI = (TextView) convertView.findViewById(R.id.listItemPatientNHI);
            viewHolder.notePatient_Age_Sex = (TextView) convertView.findViewById(R.id.list_age_sex);
            viewHolder.noteDetails = (TextView) convertView.findViewById(R.id.listItemDetails);
            viewHolder.noteIcon = (ImageView) convertView.findViewById(R.id.listItemNoteImage);

            //ensure we know this code has been run before
            convertView.setTag(viewHolder);

        } else {
            //we already have the textviews created previously for this screen position, we just need to retrieve it based on the tag
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //fill each reference text view with the data associated with note identified by position
        viewHolder.notePatientName.setText(note.getPatientname());
        viewHolder.notePatientNHI.setText(note.getPatientNHI());
        viewHolder.notePatient_Age_Sex.setText(note.getPatient_Age_Sex());
        viewHolder.noteDetails.setText(note.getdetails());
        viewHolder.noteIcon.setImageResource(note.getAssociatedDrawable());

        convertView.setOnTouchListener (new OnSwipeTouchListener(getContext()){
            @Override
            public void onSwipeRight(){
                Intent intent_to_delete = fragment_called_from.putIntentInfo(position);
                fragment_called_from.jobDone(intent_to_delete);
                Log.d("Adapter", "Swipe"+position);
            }
            @Override
            public void onSingleTap(){
//                myListener.onSingleClick(position);
                fragment_called_from.launchDetailActivity(MainActivity.FragmentToLaunch.VIEW, position);
                Log.d("Adapter","Single tap"+position);
            }
            @Override
            public void onLong_Press(){
                Log.d("NoteAdapter", "onLongPress: ");
            }
        });

//        convertView.setOnCreateContextMenuListener(this);

        //return the view so it can be displayed
        return convertView;

    }
}
