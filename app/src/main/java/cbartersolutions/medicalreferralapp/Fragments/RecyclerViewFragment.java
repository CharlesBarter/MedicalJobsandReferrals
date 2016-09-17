package cbartersolutions.medicalreferralapp.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.daimajia.swipe.util.Attributes;

import java.util.ArrayList;

import cbartersolutions.medicalreferralapp.Activities.Activity_ListView;
import cbartersolutions.medicalreferralapp.Activities.DetailActivity;
import cbartersolutions.medicalreferralapp.Activities.MainActivity;
import cbartersolutions.medicalreferralapp.Adapters.JobsDbAdapter;
import cbartersolutions.medicalreferralapp.Adapters.NotesDbAdapter;
import cbartersolutions.medicalreferralapp.Adapters.RecyclerViewAdapter;
import cbartersolutions.medicalreferralapp.ArrayLists.Header;
import cbartersolutions.medicalreferralapp.Decorations.DividerItemDecoration;
import cbartersolutions.medicalreferralapp.Listeners.OnSwipeTouchListener;
import cbartersolutions.medicalreferralapp.Others.AlteringDatabase;
import cbartersolutions.medicalreferralapp.ArrayLists.Note;
import cbartersolutions.medicalreferralapp.R;

/**
 * Created by Charles on 18/08/2016.
 * RecyclerViewFragment for viewing each list
 */
public class RecyclerViewFragment extends Fragment {
    private static String TAG = "RecyclerViewFragment";
    private static final String BUNDLE_RECYCLER_LAYOUT = "cbartersolutions.medicalreferralapp.recycler.layout";

    private ArrayList<Note> referralslist, jobslist, list;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private View fragmentLayout;
    private AlteringDatabase alteringDatabase;
    private Intent intent;
    private MainActivity.TypeofNote typeofNote;
    private boolean deleted_notes, job_done;
    private Header deleted_header;
    private static Bundle state;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        alteringDatabase = new AlteringDatabase(getActivity());

////        Code for using a viewpager
        Bundle fragment_bundle = getArguments();
        if(fragment_bundle != null){
            deleted_notes = fragment_bundle
                    .getBoolean(MainActivity.DELETED_NOTES, false);
            typeofNote = (MainActivity.TypeofNote) fragment_bundle
                    .getSerializable(MainActivity.NOTE_TYPE);
        }else{
            deleted_notes = getActivity().getIntent().getBooleanExtra(MainActivity.DELETED_NOTES, false);
            typeofNote = (MainActivity.TypeofNote) getActivity().getIntent()
                    .getExtras().getSerializable(MainActivity.NOTE_TYPE);
        }

        fragmentLayout = inflater.inflate(R.layout.recycler_view_fragment, container, false);

        //create fab\
//        fab = (FloatingActionButton) fragmentLayout.findViewById(R.id.fab);
//        fab.setOnClickListener(createNewfab);

        setUpRecyclerView();

        //fling code
        RecyclerView view = (RecyclerView) fragmentLayout.findViewById(R.id.recycler_list_widget);
//        view.setOnTouchListener(onTouchListener);

        return fragmentLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //if the job done button has been clicked then remove the view from the recyclerview
        job_done = getActivity().getIntent().getBooleanExtra(MainActivity.JOB_DONE, false);
        if(job_done){
            jobDone(getActivity().getIntent().getExtras().getLong(MainActivity.NOTE_ID));
        }

        if(state != null){
            Parcelable savedRecyclerViewState = state.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerViewState);
        }
    }

    public static RecyclerViewFragment newInstance(Bundle bundle){
        RecyclerViewFragment recyclerViewFragment = new RecyclerViewFragment();
        recyclerViewFragment.setArguments(bundle);
        return recyclerViewFragment;
    }

    public RecyclerView setUpRecyclerView(){
        //create recyclerView
        recyclerView = (RecyclerView)fragmentLayout.findViewById(R.id.recycler_list_widget);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //set decorations;
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));

        //create recycler adapter
        mRecyclerViewAdapter = makeAdapter(typeofNote);
        mRecyclerViewAdapter.setMode(Attributes.Mode.Single);
        //on item click code for each row using a interface in the adapter code
        mRecyclerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(View itemView, int position) {
                launchDetailActivity(MainActivity.FragmentToLaunch.VIEW, position);
            }

            @Override
            public void onLongClick(View itemView, int position) {
                Toast.makeText(getActivity(), "LongCLick", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(mRecyclerViewAdapter);
        return recyclerView;
    }

    private View.OnTouchListener onTouchListener = new OnSwipeTouchListener(getActivity()){
//        Intent intent = new Intent(getActivity(), Activity_ListView.class);
        @Override
        public void onSwipeRight() {
            if(!deleted_notes) {
                getActivity().finish();
            }else{
                intent = new Intent(getActivity(), Activity_ListView.class);
                intent.putExtra(MainActivity.NOTE_TYPE, typeofNote);
                intent.putExtra(MainActivity.DELETED_NOTES, false);
                intent.putExtra(MainActivity.JOB_DONE, false);
                startActivity(intent);
            }
        }
        @Override
        public void onSwipeLeft() {
            if (!deleted_notes) {
                intent = new Intent(getActivity(), Activity_ListView.class);
                intent.putExtra(MainActivity.NOTE_TYPE, typeofNote);
                intent.putExtra(MainActivity.DELETED_NOTES, true);
                intent.putExtra(MainActivity.JOB_DONE, false);
                startActivity(intent);
            } else {
                //DO NOTHING
            }
        }
    };


    public void launchDetailActivity(MainActivity.FragmentToLaunch ftl, int position){
        Intent intent = putInfoIntoIntent(position);
        switch(ftl){
            case VIEW:
                intent.putExtra(MainActivity.NOTE_FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.VIEW);
                break;
            case EDIT:
                intent.putExtra(MainActivity.NOTE_FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.EDIT);
                break;
        }
        startActivityForResult(intent, 0);
    }

    public View.OnClickListener createNewfab = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra(MainActivity.NOTE_FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.CREATE);
            intent.putExtra(MainActivity.NOTE_TYPE, typeofNote);
            startActivity(intent);
        }
    };

    private Note note;

    public Intent putInfoIntoIntent(int position){
//        switch (typeofNote) {
//            case JOB:
//                note = jobslist.get(position);
//                break;
//            case REFERRAL:
//                note = referralslist.get(position);
//        }
        note = list.get(position);
        intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(MainActivity.NOTE_ID, note.getNoteId());
        intent.putExtra(MainActivity.NOTE_TYPE, typeofNote);
        intent.putExtra(MainActivity.DELETED_NOTES, deleted_notes);
        intent.putExtra(MainActivity.LIST_POSITION, position);
        return intent;
    }

    private int is_deleted, undo_deleted, position;
    private String snackbar_words, what_happened_to_note;

    public void jobDone (final long noteId){
        //change JOB done back to false so code not run again
        getActivity().getIntent().putExtra(MainActivity.JOB_DONE, false);
        //create the Java code which allows editing the database
        alteringDatabase = new AlteringDatabase(getActivity());
        //work out if a deleted note
        Boolean job_done_deleted_notes = getActivity().getIntent().getBooleanExtra(MainActivity.DELETED_NOTES, false);
        //find note postion in header containing jobslist
        for (int i=0; i < list.size(); i++){
            long noteIdtocheck = list.get(i).getNoteId();
            if(noteIdtocheck == noteId) {
                position = i;
                break;
            }
        }
        final Note deleted_note = list.get(position);//create a copy of the note to be deleted
        //work out if the above and below note is a header and remove the above header if this is true
        final int ITEM_TYPE_HEADER = 0;
        if(mRecyclerViewAdapter.getItemViewType(position-1) == ITEM_TYPE_HEADER){
            if (position + 1 >= list.size() ||
                    mRecyclerViewAdapter.getItemViewType(position + 1) == ITEM_TYPE_HEADER){
                deleted_header = (Header) list.get(position - 1);
            }
        }

        if(!job_done_deleted_notes) { //allows the same code to change the note from deleted to none deleted
            is_deleted = 1;
            undo_deleted = 0; //allow for the undo button to do the opposite
            what_happened_to_note = getString(R.string.marked_done); //set the words of the snackbar
        }else {
            is_deleted = 0;
            undo_deleted = 1; //if UNDO action in snackbar clicked changes back to a completed job
            what_happened_to_note = getString(R.string.restored_snackbar_string);
        }
        snackbar_words = getString(R.string.jobSingular) + " " + what_happened_to_note;
        //alter note
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                alteringDatabase.changeJobDeletedStatus(noteId, is_deleted); //update the job to undeleted
                //remove from the visable list
                list.remove(position);
                mRecyclerViewAdapter.notifyItemRemoved(position);
                mRecyclerViewAdapter.notifyItemRangeChanged(position, list.size());
                if(deleted_header != null){
                    list.remove(position - 1);
                    mRecyclerViewAdapter.notifyItemRemoved(position - 1);
                    mRecyclerViewAdapter.notifyItemRangeChanged(position - 1, list.size());
                }
                mRecyclerViewAdapter.mItemManger.closeAllItems();
            }
        };
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 200);

        //snackbar code
        Snackbar snackbar = Snackbar
            .make(fragmentLayout, snackbar_words, Snackbar.LENGTH_LONG)
            .setAction(R.string.undo, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alteringDatabase.changeJobDeletedStatus(noteId, undo_deleted); //update the job to undeleted
                    if(deleted_header != null){
                        list.add(position - 1, deleted_note);
                        mRecyclerViewAdapter.notifyItemInserted(position - 1);
                        mRecyclerViewAdapter.notifyItemRangeChanged(position - 1, list.size());
                        list.add(position - 1, deleted_header);
                        mRecyclerViewAdapter.notifyItemInserted(position - 1);
                        mRecyclerViewAdapter.notifyItemRangeChanged(position - 1,
                        list.size());
                    }else{
                        list.add(position, deleted_note);
                        mRecyclerViewAdapter.notifyItemInserted(position);
                        mRecyclerViewAdapter.notifyItemRangeChanged(position, list.size());
                    }
                }
            });
        snackbar.setActionTextColor(Color.RED);
        snackbar.show();
    }

    //make the adapter depending on the type of job
    public RecyclerViewAdapter makeAdapter (MainActivity.TypeofNote typeofNote) {
        NotesDbAdapter dbAdapter = new NotesDbAdapter(getActivity().getBaseContext());
        dbAdapter.open();
        list = dbAdapter.getNotes(deleted_notes, typeofNote);
        dbAdapter.close();
        mRecyclerViewAdapter = new RecyclerViewAdapter(getActivity(), list, RecyclerViewFragment.this);
        return mRecyclerViewAdapter;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null){
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, recyclerView.getLayoutManager().onSaveInstanceState());
        state = new Bundle();
        state.putParcelable(BUNDLE_RECYCLER_LAYOUT, recyclerView.getLayoutManager().onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }
}
