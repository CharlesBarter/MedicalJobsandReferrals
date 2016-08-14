package cbartersolutions.medicalreferralapp.Fragments;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

//import com.github.clans.fab.FloatingActionButton;

import java.util.ArrayList;

import cbartersolutions.medicalreferralapp.Activities.DetailActivity;
import cbartersolutions.medicalreferralapp.Adapters.JobsDbAdapter;
import cbartersolutions.medicalreferralapp.Activities.MainActivity;
import cbartersolutions.medicalreferralapp.Listeners.MyListener;
import cbartersolutions.medicalreferralapp.Others.Note;
import cbartersolutions.medicalreferralapp.Adapters.NoteAdapter;
import cbartersolutions.medicalreferralapp.R;
import cbartersolutions.medicalreferralapp.Adapters.ReferralsDbAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListViewFragment extends android.support.v4.app.ListFragment implements MyListener {

    private ArrayList<Note> referralslist, jobslist;
    private NoteAdapter noteAdapter;
    private Intent intent;
    MainActivity.TypeofNote typeofNote;

    private Long noteId;

    private Intent launched_intent;
    private Bundle fragment_bundle;

    private Boolean deleted_notes, long_press_deleted_notes;

    @Override
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        //grab the type of note that was clicked on from the main page
        launched_intent = getActivity().getIntent();

        //if typeofNote has not been created before i.e a back button is not calling this activity
        //choose type of note
//        typeofNote = (MainActivity.TypeofNote) fragment_bundle.getSerializable(MainActivity.NOTE_TYPE);

//         if (typeofNote == null){
         typeofNote = (MainActivity.TypeofNote)
                 launched_intent.getSerializableExtra(MainActivity.NOTE_TYPE);
//         }

        deleted_notes = launched_intent.getBooleanExtra(MainActivity.DELETED_NOTES, false);

        //set deleted notes from fragments
        fragment_bundle = getArguments();
        if (fragment_bundle != null){
            deleted_notes = fragment_bundle.getBoolean(MainActivity.DELETED_NOTES);
        }

        //create different lists of notes depending on what was clicked on
        setListAdapter(makeAdapter(typeofNote));

        registerForContextMenu(getListView());

        //if this activity is being launched from a job done button then run the job done code
        boolean jobDone = launched_intent.getBooleanExtra(MainActivity.JOB_DONE, false);
        if (jobDone){
            jobDone(launched_intent);
        }
//        listView = getListView();
//
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//                launchDetailActivity(MainActivity.FragmentToLaunch.VIEW, position);
//            }
//        });
    }

    @Override
    public void onSingleClick(int position){
        launchDetailActivity(MainActivity.FragmentToLaunch.VIEW, position);
    }

    public static ListViewFragment newInstance (Bundle bundle){
        ListViewFragment listViewFragment = new ListViewFragment();
        listViewFragment.setArguments(bundle);
        return listViewFragment;
    }

    @Override
    public void onListItemClick(ListView list_view, View v, int position, long id) {
        super.onListItemClick(list_view, v, position, id);
        //launch job detail activity with the position of the list item clicked sent to the code as position
        launchDetailActivity(MainActivity.FragmentToLaunch.VIEW, position);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.list_long_press_menu, menu);
        MenuItem delete_menu = menu.findItem(R.id.delete);
        long_press_deleted_notes = deleted_notes;
        if (!deleted_notes) {
            delete_menu.setTitle(getResources().getString(R.string.mark_complete));
        } else {
            delete_menu.setTitle(getResources().getString(R.string.list_delete_menu));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        if(getUserVisibleHint()) {
            //gets information of which note is long pressed
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            //get position of note clicked in list
            int rowPosition = info.position;
            Note note = (Note) getListAdapter().getItem(rowPosition);

            //returns id of whatever item is selected
            switch (item.getItemId()) {
                //case edit clicked
                case R.id.list_edit:
                    //launch the edit fragment via details activity
                    launchDetailActivity(MainActivity.FragmentToLaunch.EDIT, rowPosition);
                    return true;
                case R.id.delete:
                    //delete note depending on typeOfNote
                    switch (typeofNote) {
                        case JOB:
                            // get the data from the list item clicked on and make it an intent
                            Intent row_for_delete = putIntentInfo(rowPosition);
                            //load the on Activity result code which works for the done button
//                        jobDone(row_for_delete);
                            if (!deleted_notes) {
                                jobDone(row_for_delete); //change the note to a deleted note
                            } else {
                                permanentlyDeleteJob(note.getNoteId(), rowPosition);//permanently deleted note
                                setListAdapter(makeAdapter(typeofNote));
                            }
                            break;
                        case REFERRAL:
                            ReferralsDbAdapter referralsDbAdapter = new ReferralsDbAdapter(getActivity().getBaseContext());
                            referralsDbAdapter.open();
                            if (!deleted_notes) {
                                int is_deleted = 1;
                                updateReferral(note, is_deleted, referralsDbAdapter);
                            } else {
                                referralsDbAdapter.deleteReferral(note.getNoteId()); //permanently deletes note
                            }
                            //notify the array that a delete has occurred and update it
                            referralslist.remove(rowPosition);
                            noteAdapter.notifyDataSetChanged();
                            //close the database adaptor
                            referralsDbAdapter.close();
                    }
            }
            return super.onContextItemSelected(item);
        }else{
            return false;
        }
    }


    public void launchDetailActivity(MainActivity.FragmentToLaunch ftl, int position){

        Intent intent = putIntentInfo(position);

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

    //get the information for the position clicked into an intent
    public Intent putIntentInfo(int position){
        //grab the note information that launches out noteDetailActivity using the position identifier
        Note note = (Note) getListAdapter().getItem(position);

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
    private String snackbar_words;

    public void jobDone (Intent data){
            final Intent previous_intent = getActivity().getIntent();
            Boolean job_done_deleted_notes = previous_intent.getBooleanExtra(MainActivity.DELETED_NOTES, false);
            //get data from the intent
            final Intent intent = data;
            if(!job_done_deleted_notes) { //allows the same code to change the note from deleted to none deleted
                is_deleted = 1;
                undo_deleted = 0; //allow for the undo button to do the opposite
                snackbar_words = getString(R.string.job_deleted);
            }else {
                is_deleted = 0;
                undo_deleted = 1; //if UNDO action in snackbar clicked changes back to a completed job
                snackbar_words = getString(R.string.restore_job_snackbar_string);
            }

            updateJob(intent, is_deleted);//update the job to be deleted
            setListAdapter(makeAdapter(typeofNote)); //reset the GUI so the note is gone

            //snackbar code
            Snackbar snackbar = Snackbar
                    .make(getListView(), snackbar_words, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            updateJob(intent, undo_deleted); //update the job to undeleted
                            previous_intent.putExtra(MainActivity.JOB_DONE, false);
                            startActivity(previous_intent);
//                            setListAdapter(makeAdapter(typeofNote));
                        }
                    });
            snackbar.setActionTextColor(Color.RED);
            snackbar.show();

    }

    public void updateReferral (Note note, int is_deleted, ReferralsDbAdapter referralsDbAdapter){
        referralsDbAdapter.updateReferral(note.getNoteId(), note.getPatientname(),
                note.getPatientNHI(), note.getPatient_Age_Sex(), note.getPatient_location(),
                note.get_date_and_time(), note.getReferrerName(), note.getReferrerContact(),
                note.getdetails(), note.getCategory(), is_deleted);
    }


    public void updateJob (Intent data, int deleted){

        JobsDbAdapter jobsDbAdapter = new JobsDbAdapter(getActivity().getBaseContext());
        jobsDbAdapter.open();
        jobsDbAdapter.updateJob(
                data.getExtras().getLong(MainActivity.NOTE_ID),
                data.getExtras().getString(MainActivity.NOTE_PATIENT_NAME) + "",
                data.getExtras().getString(MainActivity.NOTE_PATIENT_NHI) + "",
                data.getExtras().getString(MainActivity.NOTE_PATIENT_AGE_AND_SEX) + "",
                data.getExtras().getString(MainActivity.NOTE_PATIENT_LOCATION),
                data.getExtras().getLong(MainActivity.NOTE_DATE_AND_TIME),
                data.getExtras().getString(MainActivity.NOTE_DETAILS) + "",
                (Note.Category) data.getSerializableExtra(MainActivity.NOTE_CATEGORY),
                deleted,
                data.getExtras().getLong(MainActivity.NOTE_DATE_CREATED));
        jobsDbAdapter.close();
    }

    public void permanentlyDeleteJob(Long noteId, int row_Position){
        //delete the note currently shown
        JobsDbAdapter jobsDbAdapter = new JobsDbAdapter(getActivity().getBaseContext());
        jobsDbAdapter.open();
        jobsDbAdapter.deleteJob(noteId);
        jobsDbAdapter.close();
    }

    JobsDbAdapter jobsDbAdapter;

    //make the adapter depending on the type of job
    public ListAdapter makeAdapter (MainActivity.TypeofNote typeofNote) {
        //see if a user wants deleted notes

//        deleted_notes = launched_intent.getExtras().getBoolean(MainActivity.DELETED_NOTES, false);
//        fragment_bundle = getArguments();
//        if (fragment_bundle != null){
//            deleted_notes = fragment_bundle.getBoolean(MainActivity.DELETED_NOTES);
//        }

        switch (typeofNote) {
            case JOB:
                jobsDbAdapter = new JobsDbAdapter(getActivity().getBaseContext());
                jobsDbAdapter.open();
                if (!deleted_notes) {
                    jobslist = jobsDbAdapter.getNonDeletedJobs();
                }else{
                    jobslist = jobsDbAdapter.getDeleteJobs();
                }
                noteAdapter = new NoteAdapter(getActivity(), jobslist, ListViewFragment.this);
                jobsDbAdapter.close();
                break;
            case REFERRAL:
                ReferralsDbAdapter referralsDbAdapter = new ReferralsDbAdapter(getActivity().getBaseContext());
                referralsDbAdapter.open();
                if(!deleted_notes) {
                    referralslist = referralsDbAdapter.getCurrentReferrals();
                }else {
                    referralslist = referralsDbAdapter.getDeletedReferrals();
                }
                noteAdapter = new NoteAdapter(getActivity(), referralslist, ListViewFragment.this);
                referralsDbAdapter.close();
                break;
        }
        return noteAdapter;
    }
}
