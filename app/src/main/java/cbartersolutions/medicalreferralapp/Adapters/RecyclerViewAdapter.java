package cbartersolutions.medicalreferralapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.ArrayList;

import cbartersolutions.medicalreferralapp.Activities.DetailActivity;
import cbartersolutions.medicalreferralapp.Activities.MainActivity;
import cbartersolutions.medicalreferralapp.ArrayLists.Header;
import cbartersolutions.medicalreferralapp.Fragments.RecyclerViewFragment;
import cbartersolutions.medicalreferralapp.Others.AlteringDatabase;
import cbartersolutions.medicalreferralapp.ArrayLists.Note;
import cbartersolutions.medicalreferralapp.R;

/**
 * Created by Charles on 24/08/2016.
 */
public class RecyclerViewAdapter extends RecyclerSwipeAdapter<RecyclerViewAdapter.SimpleViewHolder> {

    private String TAG = "RecyclerViewAdapter";
    private Context mContext;
    private ArrayList<Note> mNotes, arrayListtoSearch;
    private Note deleted_note;
    private Header deleted_header;
    private RecyclerViewFragment fragment_using_adapter;
    private boolean deleted_notes, handReleased;
    private MainActivity.TypeofNote typeofNote;
    private AlteringDatabase alteringDatabase;
    private SwipeLayout.DragEdge currentDragEdge;
    private int is_change_deleted_status, undo_change_deleted_status, number_of_jobs, job_position;
    private long jobId;
    private String what_happened_to_note, snackbar_words;
    private Handler handler = new Handler();
    private Handler setVisibilityHandler = new Handler();
    private boolean code_run, mOpen;
    private Intent launchDetailedView;

    private static final int ITEM_TYPE_HEADER = 0;
    private static final int ITEM_TYPE_NOTE = 1;


    private static OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(View itemView, int position);
        void onLongClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder{
        SwipeLayout swipeLayout;
        TextView listPatientName, listPatientNHI, listPatientAge_Sex, listPatientLocation,
                listDetails, listNumberofJobs;
        ImageView listIcon;
        TextView mHeader;

        public SimpleViewHolder(final View itemView, int itemType) {
            super(itemView);
            switch (itemType) {
                case ITEM_TYPE_NOTE:
                    swipeLayout = (SwipeLayout) itemView.findViewById(R.id.list_swipe);
                    listPatientName = (TextView) itemView.findViewById(R.id.listItemPatientName);
                    listPatientNHI = (TextView) itemView.findViewById(R.id.listItemPatientNHI);
                    listPatientAge_Sex = (TextView) itemView.findViewById(R.id.ListAgeSex);
                    listPatientLocation = (TextView) itemView.findViewById(R.id.listItemLocation);
                    listDetails = (TextView) itemView.findViewById(R.id.listItemDetails);
                    listIcon = (ImageView) itemView.findViewById(R.id.listItemNoteImage);
                    listNumberofJobs = (TextView) itemView.findViewById(R.id.NumberofJobsListView);
                    break;
                case ITEM_TYPE_HEADER:
                    mHeader = (TextView) itemView.findViewById(R.id.recycler_view_header_text_view);
                    break;
            }
        }
    }

    public RecyclerViewAdapter (Context context, ArrayList<Note> notes, RecyclerViewFragment fragment){
        mContext = context;
        mNotes = notes;
        fragment_using_adapter = fragment;
        deleted_notes = fragment.getArguments()
                .getBoolean(MainActivity.DELETED_NOTES, false);
        typeofNote = (MainActivity.TypeofNote) fragment.getArguments()
                .getSerializable(MainActivity.NOTE_TYPE);
        alteringDatabase = new AlteringDatabase(context);
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == ITEM_TYPE_HEADER){
            View header_View = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_view_headers, parent, false);
            return new SimpleViewHolder(header_View, viewType);
        }
        else if (viewType == ITEM_TYPE_NOTE){
            View note_View = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_swipe_layout, parent, false);
            return new SimpleViewHolder(note_View, viewType);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder viewHolder, int position) {

        final int itemType = getItemViewType(position);

        switch (itemType) {
            case ITEM_TYPE_NOTE:
                final Note note = mNotes.get(viewHolder.getAdapterPosition());
                //setdata into list
                viewHolder.listPatientName.setText(note.getPatientname());
                viewHolder.listPatientNHI.setText(note.getPatientNHI());
                viewHolder.listPatientAge_Sex.setText(note.getPatient_Age_Sex());
                viewHolder.listPatientLocation.setText(note.getPatient_location());
                viewHolder.listDetails.setText(note.getdetails());
                viewHolder.listIcon.setImageResource(note.getAssociatedDrawable());

                //create the 2 icons
                View trash_icon = viewHolder.itemView.findViewById(R.id.trash);
                View recover_icon = viewHolder.itemView.findViewById(R.id.recover);
                View while_swiping_color = viewHolder.itemView.findViewById(R.id.swipe_background);

                //create the Intent to launch Detail View
                launchDetailedView = new Intent(mContext, DetailActivity.class);
                //check if there is a Job or referral for the same patient
                //first create the array list to search
                number_of_jobs = 0;
                switch (typeofNote) {
                    case REFERRAL:
                        JobsDbAdapter jobsDbAdapter = new JobsDbAdapter(mContext);
                        jobsDbAdapter.open();
                        arrayListtoSearch = jobsDbAdapter.getJobsNoHeaders(deleted_notes);
                        jobsDbAdapter.close();
                        launchDetailedView.putExtra(MainActivity.NOTE_TYPE, MainActivity.TypeofNote.JOB);//say to launch the other type of note
                        break;
                    case JOB:
                        ReferralsDbAdapter referralsDbAdapter = new ReferralsDbAdapter(mContext);
                        referralsDbAdapter.open();
                        arrayListtoSearch = referralsDbAdapter.getReferralsNoHeaders(deleted_notes);
                        referralsDbAdapter.close();
                        launchDetailedView.putExtra(MainActivity.NOTE_TYPE, MainActivity.TypeofNote.REFERRAL);
                        break;
                }
                //search the array list for the same patient name and NHI
                for(int i = 0; i < arrayListtoSearch.size(); i++){
                    String comparing_to_name = arrayListtoSearch.get(i).getPatientname();
                    String comparing_to_NHI = arrayListtoSearch.get(i).getPatientNHI();
                    String patient_name = note.getPatientname();
                    String patient_NHI = note.getPatientNHI();
                    if(comparing_to_name.equals(patient_name) &&
                            comparing_to_NHI.equals(patient_NHI)){
                        number_of_jobs = number_of_jobs + 1;
                        jobId = arrayListtoSearch.get(i).getNoteId();
                        job_position = i;
                    }
                }
                if(number_of_jobs > 0){//if there are the same thing in both lists
                    viewHolder.listNumberofJobs.setVisibility(View.VISIBLE);//make the there is something in the other list bit visible
                    switch (typeofNote) {
                        case REFERRAL:
                            viewHolder.listNumberofJobs.setText(String.valueOf(number_of_jobs));//show number of jobs for each referral
                            break;
                        case JOB:
                            viewHolder.listNumberofJobs.setText("\u2713");//show a tick if there is an assocaited referral
                            break;
                    }
                    viewHolder.listNumberofJobs.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            launchDetailedView.putExtra(MainActivity.NOTE_PATIENT_NAME, note.getPatientname());
                            launchDetailedView.putExtra(MainActivity.NOTE_PATIENT_NHI, note.getPatientNHI());
                            launchDetailedView.putExtra(MainActivity.NOTE_TYPE_LAUNCHED_FROM,
                                    typeofNote);//say this intent has been launched from something
                            launchDetailedView.putExtra(MainActivity.DELETED_NOTES, deleted_notes);//add if a deleted note
                            launchDetailedView.putExtra(MainActivity.NOTE_FRAGMENT_TO_LOAD_EXTRA,
                                    MainActivity.FragmentToLaunch.VIEW); //tell it to open a view type
                            launchDetailedView.putExtra(MainActivity.LIST_POSITION, job_position);
                            //add patient details
                            mContext.startActivity(launchDetailedView);
                            return true;
                        }
                    });
                }

                //show items depending on typeOfNot
                if (deleted_notes) {//if this view is of deleted notes
                    trash_icon.setVisibility(View.GONE); //only show recover add button
                    recover_icon.setVisibility(View.VISIBLE);
                    while_swiping_color.setBackgroundColor(ContextCompat.getColor(mContext,
                            R.color.recoverSwipeBackground));
                    viewHolder.swipeLayout.setBackgroundColor(ContextCompat
                            .getColor(mContext, R.color.recoverSwipeBackground));//set colour of background to recover background
                    viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, R.id.swipe_background_recover);
                    is_change_deleted_status = 0; //as deleted note change status to not deleted note
                    undo_change_deleted_status = 1; //as deleted note when UNDO pressed return to deleted note
                    what_happened_to_note = mContext.getString(R.string.restored_snackbar_string); //set the words of the snackbar
                } else {//if NOT a deleted note
                    trash_icon.setVisibility(View.VISIBLE); //only trash icon visible
                    recover_icon.setVisibility(View.GONE);
                    viewHolder.swipeLayout.setBackgroundColor(ContextCompat
                            .getColor(mContext, R.color.swipeBackground)); //set background to red
                    viewHolder.swipeLayout.setRightSwipeEnabled(false);
                    is_change_deleted_status = 1;//as NOT a deleted note change it to a deleted note
                    undo_change_deleted_status = 0;//as NOT a deleted note, UNDO back to not deleted
                    what_happened_to_note = mContext.getString(R.string.marked_done);
                }

                //set words for snackbar
                switch (typeofNote) {
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
                viewHolder.swipeLayout.setWillOpenPercentAfterOpen(1f);

                viewHolder.swipeLayout.addSwipeDenier(new SwipeLayout.SwipeDenier() {
                    @Override
                    public boolean shouldDenySwipe(MotionEvent ev) {
                        return false;
                    }
                });
                viewHolder.swipeLayout.setMinVelocity(20000);

                code_run = false;

                //add swipe listener
                viewHolder.swipeLayout.addSwipeListener(new SimpleSwipeListener() {

                    @Override
                    public void onStartOpen(SwipeLayout layout) {
                        super.onStartOpen(layout);
                        currentDragEdge = viewHolder.swipeLayout.getDragEdge();//get the drag edge of the opening
                    }

                    @Override
                    public void onStartClose(SwipeLayout layout) {
                        super.onStartClose(layout);
                        mOpen = false;
                    }

                    int position;

                    @Override
                    public void onOpen(SwipeLayout layout) {
                        Log.d(TAG, "onOpen: ");
                        super.onOpen(layout);
                        mOpen = true;
                    }

                    public void snackbar() {
                        //undo snackbar
                        Snackbar snackbar = Snackbar
                            .make(fragment_using_adapter.getView(), snackbar_words, Snackbar.LENGTH_LONG)
                            .setAction(mContext.getString(R.string.undo), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                switch (typeofNote) {
                                    case JOB:
                                        alteringDatabase.changeJobDeletedStatus(note.getNoteId(),
                                                undo_change_deleted_status);
                                        break;
                                    case REFERRAL:
                                        alteringDatabase.changeReferralDeletedStatus(note.getNoteId(),
                                                undo_change_deleted_status);
                                        break;
                                }
                                viewHolder.swipeLayout.getSurfaceView().setVisibility(View.VISIBLE);
                                if(deleted_header != null){//if header deleted
                                    mNotes.add(position-1, deleted_note);
                                    notifyItemInserted(position-1);
                                    notifyItemRangeChanged(position-1, mNotes.size());
                                    //reinsert header
                                    mNotes.add(position-1, deleted_header);
                                    notifyItemInserted(position-1);
                                    notifyItemRangeChanged(position-1, mNotes.size());
                                    deleted_header = null;
                                }else{
                                    mNotes.add(position, deleted_note);
                                    notifyItemInserted(position);
                                    notifyItemRangeChanged(position, mNotes.size());
                                }
                                mItemManger.closeAllItems();
                                }
                            });
                        snackbar.setActionTextColor(Color.RED);
                        snackbar.show();
                    }

                    Runnable runnable = new Runnable() {
                        public void run() {
                        if (currentDragEdge == SwipeLayout.DragEdge.Right) {
                            code_run = false;
                            int permanently_deleted = 2;
                            switch (typeofNote) {
                                case JOB:
                                    alteringDatabase.changeJobDeletedStatus(note.getNoteId(), permanently_deleted);
                                    break;
                                case REFERRAL:
                                    alteringDatabase.changeReferralDeletedStatus(note.getNoteId(), permanently_deleted);
                                    break;
                            }
                            mNotes.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, mNotes.size());
                            //remove header
                            if(getItemViewType(position-1) == ITEM_TYPE_HEADER){
                                if(position >= mNotes.size() || getItemViewType(position) == ITEM_TYPE_HEADER) {
                                    deleted_header = (Header) mNotes.get(position - 1);
                                    mNotes.remove(position - 1);
                                    notifyItemRemoved(position - 1);
                                    notifyItemRangeChanged(position - 1, mNotes.size());
                                }
                            }
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
                            if(getItemViewType(position-1) == ITEM_TYPE_HEADER){
                                if(position >= mNotes.size() || getItemViewType(position) == ITEM_TYPE_HEADER){
                                    deleted_header = (Header) mNotes.get(position-1);
                                    mNotes.remove(position-1);
                                    notifyItemRemoved(position-1);
                                    notifyItemRangeChanged(position-1, mNotes.size());
                                }
                            }
                            mItemManger.closeAllItems();
                        }
                        }
                    };


                    Runnable setVisibilityRunnable = new Runnable() {
                        @Override
                        public void run() {
                            viewHolder.swipeLayout.getSurfaceView().setVisibility(View.VISIBLE);
                            Log.d(TAG, "run: Visible code");
                        }
                    };

                    @Override
                    public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                        super.onHandRelease(layout, xvel, yvel);
                        Log.d(TAG, "onHandRelease: ");
                        handReleased = true;
                        position = viewHolder.getAdapterPosition();
                        if (position >= 0 && !code_run && mOpen) { //stops error when position =-1;
                            code_run = true;
                            viewHolder.swipeLayout.getSurfaceView().setVisibility(View.INVISIBLE); //hide surface layer to show background
                            //change background to black
                            if (currentDragEdge == SwipeLayout.DragEdge.Right) {
                                viewHolder.swipeLayout.setBackgroundColor(ContextCompat.getColor(mContext,
                                        R.color.permanentDeleteSwipeBackground));
                            }
                            handler.removeCallbacks(runnable);
                            handler.postDelayed(runnable, 300);
                            if (currentDragEdge == SwipeLayout.DragEdge.Left) {
                                snackbar();//sun snackbar code
                            }

//                    handler.removeCallbacks(runnable);
//                    handler.postDelayed(runnable, 300);

                            setVisibilityHandler.postDelayed(setVisibilityRunnable, 560);
                        }
                    }
                });


                viewHolder.swipeLayout.getSurfaceView().setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        listener.onLongClick(viewHolder.itemView, viewHolder.getLayoutPosition());
                        return true;
                    }
                });

                viewHolder.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (listener != null) {
                            //triggers click upwards to fragment
                            listener.onItemClick(viewHolder.itemView, viewHolder.getLayoutPosition());
                        }
                    }
                });
                mItemManger.bind(viewHolder.itemView, position);
                break;
            case ITEM_TYPE_HEADER:
                final Header header = (Header) mNotes.get(viewHolder.getAdapterPosition());
//               viewHolder.textHeader.setText(header.getHeader());
                viewHolder.mHeader.setText(header.getHeader());
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(mNotes.get(position) instanceof Header){
            return ITEM_TYPE_HEADER;
        }else{
            return ITEM_TYPE_NOTE;
        }
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