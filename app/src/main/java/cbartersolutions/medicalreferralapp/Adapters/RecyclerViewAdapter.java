package cbartersolutions.medicalreferralapp.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.ArrayList;

import cbartersolutions.medicalreferralapp.Activities.MainActivity;
import cbartersolutions.medicalreferralapp.Fragments.RecyclerViewFragment;
import cbartersolutions.medicalreferralapp.Others.AlteringDatabase;
import cbartersolutions.medicalreferralapp.Others.Note;
import cbartersolutions.medicalreferralapp.R;

/**
 * Created by Charles on 24/08/2016.
 */
public class RecyclerViewAdapter extends RecyclerSwipeAdapter<RecyclerViewAdapter.SimpleViewHolder> {

    private String TAG = "RecyclerViewAdapter";
    private Context mContext;
    private ArrayList<Note> mNotes;
    private Note deleted_note;
    private RecyclerViewFragment fragment_using_adapter;
    private boolean deleted_notes;
    private MainActivity.TypeofNote typeofNote;
    private AlteringDatabase alteringDatabase;
    private SwipeLayout.DragEdge currentDragEdge;
    private int is_change_deleted_status, undo_change_deleted_staus;
    private String what_happened_to_note, snackbar_words;
    private Handler handler = new Handler();
    boolean code_run;

    private static OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder{
        SwipeLayout swipeLayout;
        TextView listPatientName, listPatientNHI, listPatientAge_Sex, listPatientLocation,
                listDetails;
        ImageView listIcon;

        public SimpleViewHolder(final View itemView) {
            super(itemView);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.list_swipe);
            listPatientName = (TextView) itemView.findViewById(R.id.listItemPatientName);
            listPatientNHI = (TextView) itemView.findViewById(R.id.listItemPatientNHI);
            listPatientAge_Sex = (TextView) itemView.findViewById(R.id.list_age_sex);
            listPatientLocation = (TextView) itemView.findViewById(R.id.listItemLocation);
            listDetails = (TextView) itemView.findViewById(R.id.listItemDetails);
            listIcon = (ImageView) itemView.findViewById(R.id.listItemNoteImage);

        }
    }

    public RecyclerViewAdapter (Context context, ArrayList<Note> notes, RecyclerViewFragment fragment){
        mContext = context;
        mNotes = notes;
        fragment_using_adapter = fragment;
        deleted_notes = fragment.getActivity().getIntent()
                .getBooleanExtra(MainActivity.DELETED_NOTES, false);
        typeofNote = (MainActivity.TypeofNote) fragment.getActivity().getIntent()
                .getSerializableExtra(MainActivity.NOTE_TYPE);
        alteringDatabase = new AlteringDatabase(context);
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_swipe_layout,
                parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder viewHolder, int position) {
        final Note note = mNotes.get(viewHolder.getAdapterPosition());

        //setdata into list
        viewHolder.listPatientName.setText(note.getPatientname());
        viewHolder.listPatientNHI.setText(note.getPatientNHI());
        viewHolder.listPatientAge_Sex.setText(note.getPatient_Age_Sex());
        viewHolder.listPatientLocation.setText(note.getPatient_location());
        viewHolder.listDetails.setText(note.getdetails());
        viewHolder.listIcon.setImageResource(note.getAssociatedDrawable());

        //create the 2 icons
        View trash_icon =  viewHolder.itemView.findViewById(R.id.trash);
        View recover_icon = viewHolder.itemView.findViewById(R.id.recover);
        View while_swiping_color = viewHolder.itemView.findViewById(R.id.swipe_background);

        //show items depending on typeOfNot
        if(deleted_notes){//if this view is of deleted notes
            trash_icon.setVisibility(View.GONE); //only show recover add button
            recover_icon.setVisibility(View.VISIBLE);
            while_swiping_color.setBackgroundColor(ContextCompat.getColor(mContext,
                    R.color.recoverSwipeBackground));
            viewHolder.swipeLayout.setBackgroundColor(ContextCompat
                    .getColor(mContext, R.color.recoverSwipeBackground));//set colour of background to recover background
            viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, R.id.swipe_background_recover);
            is_change_deleted_status = 0; //as deleted note change status to not deleted note
            undo_change_deleted_staus = 1; //as deleted note when UNDO pressed return to deleted note
            what_happened_to_note = mContext.getString(R.string.restored_snackbar_string); //set the words of the snackbar
        }else{//if NOT a deleted note
            trash_icon.setVisibility(View.VISIBLE); //only trash icon visible
            recover_icon.setVisibility(View.GONE);
            viewHolder.swipeLayout.setBackgroundColor(ContextCompat
                    .getColor(mContext, R.color.swipeBackground)); //set background to red
            viewHolder.swipeLayout.setRightSwipeEnabled(false);
            is_change_deleted_status = 1;//as NOT a deleted note change it to a deleted note
            undo_change_deleted_staus = 0;//as NOT a deleted note, UNDO back to not deleted
            what_happened_to_note = mContext.getString(R.string.marked_done);
        }

        //set words for snackbar
        switch (typeofNote){
            case JOB:
                snackbar_words = mContext.getString(R.string.jobSingular) + " " + what_happened_to_note;
                break;
            case REFERRAL:
                snackbar_words = mContext.getString(R.string.referralSingular) + " " + what_happened_to_note;
                break;
        }

        //add main delete/recover swipe
        viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Left, R.id.swipe_background);

        viewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        viewHolder.swipeLayout.setWillOpenPercentAfterClose(1f);

        code_run = false;

        //add swipe listener
        viewHolder.swipeLayout.addSwipeListener(new SimpleSwipeListener(){

            @Override
            public void onStartOpen(SwipeLayout layout) {
                super.onStartOpen(layout);
                currentDragEdge = viewHolder.swipeLayout.getDragEdge();//get the drag edge of the opening
            }

            @Override
            public void onOpen(SwipeLayout layout) {
                Log.d(TAG, "onOpen: ");
//                super.onOpen(layout);
                final int position = viewHolder.getAdapterPosition();
                if (position >= 0 && !code_run) { //stops error when position =-1;
                    code_run = true;
                    viewHolder.swipeLayout.getSurfaceView().setVisibility(View.GONE); //hide surface layer to show background
                    //change background to black
                    if(currentDragEdge == SwipeLayout.DragEdge.Right){
                        viewHolder.swipeLayout.setBackgroundColor(ContextCompat.getColor(mContext,
                                R.color.permanentDeleteSwipeBackground));
                    }
                    Runnable runnable = new Runnable() {
                        public void run() {
                        if (currentDragEdge == SwipeLayout.DragEdge.Right) {
                            code_run = false;
                            alteringDatabase.permanentDeleteNote(typeofNote, note.getNoteId(),
                                    position);
                            mNotes.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, mNotes.size());
                            mItemManger.closeAllItems();
                        } else if (currentDragEdge == SwipeLayout.DragEdge.Left) {
                            Log.d("RecyclerViewAdapter", "left swipe onOpen" + code_run);
                            code_run = false;
                            switch (typeofNote) {
                                case JOB:
                                    alteringDatabase.changeJobDeletedStatus(note.getNoteId(),
                                            is_change_deleted_status);
                                    break;
                                case REFERRAL:
                                    alteringDatabase.changeReferralDeletedStatus(note.getNoteId(),
                                            is_change_deleted_status);
                            }
                            deleted_note = mNotes.get(position);
                            mNotes.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, mNotes.size());
                            mItemManger.closeAllItems();
                            //undo snackbar
                            Snackbar snackbar = Snackbar
                                    .make(fragment_using_adapter.getView(), snackbar_words, Snackbar.LENGTH_LONG)
                                    .setAction(mContext.getString(R.string.undo), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            switch (typeofNote){
                                                case JOB:
                                                    alteringDatabase.changeJobDeletedStatus(note.getNoteId(),
                                                            undo_change_deleted_staus);
                                                    break;
                                                case REFERRAL:
                                                    alteringDatabase.changeReferralDeletedStatus(note.getNoteId(),
                                                            undo_change_deleted_staus);
                                                    break;
                                            }
                                            viewHolder.swipeLayout.getSurfaceView().setVisibility(View.VISIBLE);
                                            mNotes.add(position, deleted_note);
                                            notifyItemInserted(position);
                                            notifyItemRangeChanged(position, mNotes.size());
//                                            mItemManger.closeAllItems();
                                        }
                                    });
                            snackbar.setActionTextColor(Color.RED);
                            snackbar.show();
                            }
                        }
                    };
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 300);
                }
            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                super.onHandRelease(layout, xvel, yvel);
                Log.d(TAG, "onHandRelease: ");
            }
        });


        viewHolder.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null){
                    //triggers click upwards to fragment
                    listener.onItemClick(viewHolder.itemView, viewHolder.getLayoutPosition());
                }
            }
        });

        mItemManger.bind(viewHolder.itemView, position);
    }

    @Override
    public int getItemCount(){
        return mNotes.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.list_swipe;
    }
}