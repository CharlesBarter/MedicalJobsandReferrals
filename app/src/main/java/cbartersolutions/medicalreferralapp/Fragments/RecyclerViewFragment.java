package cbartersolutions.medicalreferralapp.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.swipe.util.Attributes;

import java.util.ArrayList;

import cbartersolutions.medicalreferralapp.Activities.DetailActivity;
import cbartersolutions.medicalreferralapp.Activities.MainActivity;
import cbartersolutions.medicalreferralapp.Adapters.JobsDbAdapter;
import cbartersolutions.medicalreferralapp.Adapters.RecyclerViewAdapter;
import cbartersolutions.medicalreferralapp.Adapters.ReferralsDbAdapter;
import cbartersolutions.medicalreferralapp.Listeners.OnSwipeTouchListener;
import cbartersolutions.medicalreferralapp.Others.AlteringDatabase;
import cbartersolutions.medicalreferralapp.Others.Note;
import cbartersolutions.medicalreferralapp.R;

/**
 * Created by Charles on 18/08/2016.
 */
public class RecyclerViewFragment extends Fragment {
    private static String TAG = "RecyclerViewFragment";

    private ArrayList<Note> referralslist, jobslist;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private View fragmentLayout;
    private AlteringDatabase alteringDatabase;

    private Intent intent;

    MainActivity.TypeofNote typeofNote;
    private boolean deleted_notes;

    JobsDbAdapter jobsDbAdapter = new JobsDbAdapter(getActivity());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        alteringDatabase = new AlteringDatabase(getActivity());

        typeofNote = (MainActivity.TypeofNote) getActivity().getIntent()
                .getExtras().getSerializable(MainActivity.NOTE_TYPE);

        deleted_notes = getActivity().getIntent().getBooleanExtra(MainActivity.DELETED_NOTES, false);

        fragmentLayout = inflater.inflate(R.layout.recycler_view_fragment, container, false);

        //create fab\
        FloatingActionButton fab = (FloatingActionButton) fragmentLayout.findViewById(R.id.fab);
        fab.setOnClickListener(createNewfab);

        setUpRecyclerView();

        //fling code
//        View view = fragmentLayout.findViewById(R.id.recycler_view_fragment_coordinator_layout);
//        view.setOnTouchListener(onTouchListener);

        return fragmentLayout;
    }

    public RecyclerView setUpRecyclerView(){
        //create recyclerView
        recyclerView = (RecyclerView)fragmentLayout.findViewById(R.id.recycler_list_widget);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //create recycler adapter
        mRecyclerViewAdapter = makeAdapter(typeofNote);
        mRecyclerViewAdapter.setMode(Attributes.Mode.Single);
        //on item click code for each row using a interface in the adapter code
        mRecyclerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(View itemView, int position) {
                launchDetailActivity(MainActivity.FragmentToLaunch.VIEW, position);
//                Toast.makeText(getActivity(), "Single Click " + " " + position,
//                        Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(mRecyclerViewAdapter);
        return recyclerView;
    }

    private View.OnTouchListener onTouchListener = new OnSwipeTouchListener(getActivity()){
        @Override
        public void onSwipeRight() {
            if(!deleted_notes) {
                getActivity().finish();
            }else{
                intent.putExtra(MainActivity.DELETED_NOTES, false);
                intent.putExtra(MainActivity.JOB_DONE, false);
                startActivity(intent);
            }
        }
        @Override
        public void onSwipeLeft() {
            if (!deleted_notes) {
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

        switch (typeofNote) {
            case JOB:
                note = jobslist.get(position);
                break;
            case REFERRAL:
                note = referralslist.get(position);
        }
        intent = new Intent(getActivity(), DetailActivity.class);

        //pass along the information of the clicked note to the note detail activity
        intent.putExtra(MainActivity.NOTE_PATIENT_NAME, note.getPatientname());
        intent.putExtra(MainActivity.NOTE_PATIENT_NHI, note.getPatientNHI());
        intent.putExtra(MainActivity.NOTE_PATIENT_AGE_AND_SEX, note.getPatient_Age_Sex());
        intent.putExtra(MainActivity.NOTE_PATIENT_LOCATION, note.getPatient_location());
        intent.putExtra(MainActivity.NOTE_DATE_AND_TIME, note.get_date_and_time());
        intent.putExtra(MainActivity.NOTE_REFERRER_NAME, note.getReferrerName());
        intent.putExtra(MainActivity.NOTE_REFERRER_CONTACT, note.getReferrerContact());
        intent.putExtra(MainActivity.NOTE_DETAILS, note.getdetails());
        intent.putExtra(MainActivity.NOTE_CATEGORY, note.getCategory());
        intent.putExtra(MainActivity.NOTE_DATE_CREATED, note.getDateCreatedMilli());
        intent.putExtra(MainActivity.NOTE_ID, note.getNoteId());
        intent.putExtra(MainActivity.NOTE_TYPE, typeofNote);
        intent.putExtra(MainActivity.DELETED_NOTES, deleted_notes);
        intent.putExtra(MainActivity.LIST_POSITION, position);

        return intent;
    }

    private int is_deleted;
    private int undo_deleted;
    private String snackbar_words, what_happened_to_note;

    public void jobDone (final long noteId){
        //create the Java code which allows editing the database
        alteringDatabase = new AlteringDatabase(getActivity());

        //work out if a deleted note
        Boolean job_done_deleted_notes = getActivity().getIntent().getBooleanExtra(MainActivity.DELETED_NOTES, false);

        if(!job_done_deleted_notes) { //allows the same code to change the note from deleted to none deleted
            is_deleted = 1;
            undo_deleted = 0; //allow for the undo button to do the opposite
            what_happened_to_note = getString(R.string.marked_done); //set the words of the snackbar
        }else {
            is_deleted = 0;
            undo_deleted = 1; //if UNDO action in snackbar clicked changes back to a completed job
            what_happened_to_note = getString(R.string.restored_snackbar_string);
        }


        //snackbar code
        Snackbar snackbar = Snackbar
            .make(getView(), snackbar_words, Snackbar.LENGTH_LONG)
            .setAction(R.string.undo, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (typeofNote){
                        case JOB:
                            alteringDatabase.changeJobDeletedStatus(noteId, undo_deleted); //update the job to undeleted
                            break;
                        case REFERRAL:
                            alteringDatabase.changeReferralDeletedStatus(noteId, undo_deleted);
                            break;
                    }
                    recyclerView.setAdapter(makeAdapter(typeofNote));
                }
            });
        snackbar.setActionTextColor(Color.RED);
        snackbar.show();
    }

    //make the adapter depending on the type of job
    public RecyclerViewAdapter makeAdapter (MainActivity.TypeofNote typeofNote) {

        switch (typeofNote) {
            case JOB:
                jobsDbAdapter = new JobsDbAdapter(getActivity().getBaseContext());
                jobsDbAdapter.open();
                if (!deleted_notes) {
                    jobslist = jobsDbAdapter.getNonDeletedJobs();
                }else{
                    jobslist = jobsDbAdapter.getDeleteJobs();
                }
                jobsDbAdapter.close();
                mRecyclerViewAdapter = new RecyclerViewAdapter(getActivity(), jobslist,
                        RecyclerViewFragment.this);
                break;
            case REFERRAL:
                ReferralsDbAdapter referralsDbAdapter = new ReferralsDbAdapter(getActivity().getBaseContext());
                referralsDbAdapter.open();
                if(!deleted_notes) {
                    referralslist = referralsDbAdapter.getCurrentReferrals();
                }else {
                    referralslist = referralsDbAdapter.getDeletedReferrals();
                }
                referralsDbAdapter.close();
                mRecyclerViewAdapter = new RecyclerViewAdapter(getActivity(), referralslist,
                        RecyclerViewFragment.this);
                break;
        }
        mRecyclerViewAdapter.setMode(Attributes.Mode.Single);
        return mRecyclerViewAdapter;
    }
}
