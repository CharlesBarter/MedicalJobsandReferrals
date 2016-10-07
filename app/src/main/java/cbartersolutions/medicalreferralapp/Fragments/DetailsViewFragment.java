package cbartersolutions.medicalreferralapp.Fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.github.clans.fab.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import cbartersolutions.medicalreferralapp.Activities.Activity_ListView;
import cbartersolutions.medicalreferralapp.Activities.DetailActivity;
import cbartersolutions.medicalreferralapp.Activities.MainActivity;
import cbartersolutions.medicalreferralapp.Adapters.NotesDbAdapter;
import cbartersolutions.medicalreferralapp.Animations.Animations;
import cbartersolutions.medicalreferralapp.ArrayLists.Note;
import cbartersolutions.medicalreferralapp.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsViewFragment extends Fragment implements View.OnClickListener {

    private static String TAG = "DetailsViewFragment";

    private Bundle view_pager_bundle;
    private View fragmentlayout;
    private FloatingActionButton fab;
    private TextView viewPatientName, viewPatientNHI, viewPatient_Age_and_sex,
            viewPatient_Location, viewDetails,
            viewReferredDetails, viewReferredContact, viewDate_of_Note, viewTime_of_Note;
    private Button button_to_associated_jobs;
    private long noteId = 0, datecreated = 0, date_and_time = 0;
    private int position = 0;
    private boolean newNote;
    private boolean deleted_notes, canCheckImportanceIcon ;
    private MainActivity.TypeofNote typeofNote, noteLaunchedFrom;
    private Note.Category noteCat;
    private ArrayList<Note> arrayListtoSearch;
    private AlertDialog confirmDialogObject;
    private NotesDbAdapter dbAdapter;
    private SharedPreferences sharedPreferences;


    //define the format for dates and times
    String myDateFormat = "E, d MMM yyyy";
    java.text.SimpleDateFormat date_format = new java.text.SimpleDateFormat(myDateFormat, Locale.ENGLISH);

    String myTimeFormat = "HH:mm";
    SimpleDateFormat time_format = new SimpleDateFormat(myTimeFormat, Locale.ENGLISH);

    public DetailsViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //get Shared preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //get data from intent
//        final Intent intent = getActivity().getIntent();
        view_pager_bundle = getArguments();


        //set typeOfNote
        typeofNote = (MainActivity.TypeofNote) view_pager_bundle.getSerializable(MainActivity.NOTE_TYPE);
//        typeofNote = (MainActivity.TypeofNote) intent.getSerializableExtra(MainActivity.NOTE_TYPE);
        noteLaunchedFrom = (MainActivity.TypeofNote)
                getActivity().getIntent().getSerializableExtra(MainActivity.NOTE_TYPE_LAUNCHED_FROM);

        //define adapters
        dbAdapter = new NotesDbAdapter(getActivity());

        //set noteId
        noteId = view_pager_bundle.getLong(MainActivity.NOTE_ID);
//        noteId = intent.getExtras().getLong(MainActivity.NOTE_ID);

        //set position
        position = view_pager_bundle.getInt(MainActivity.LIST_POSITION);
//        position = intent.getExtras().getInt(MainActivity.LIST_POSITION);

        //set the icon
        noteCat = (Note.Category) view_pager_bundle.getSerializable(MainActivity.NOTE_CATEGORY);
//        noteCat = (Note.Category) intent.getSerializableExtra(MainActivity.NOTE_CATEGORY);

        //check if a deleted note
        deleted_notes = view_pager_bundle.getBoolean(MainActivity.DELETED_NOTES);

        //set if new note
//        newNote = intent.getBooleanExtra(MainActivity.NEW_NOTE, false);

        //create the confirm dialog
        buildConfirmDialog();

        //allow options menus
        setHasOptionsMenu(true);

        //load the correct fragment depending on the type of note
        switch (typeofNote) {
            case JOB:
                //inflate the job fragment rather than the referral fragment
                fragmentlayout = inflater.inflate(R.layout.fragment_job_details_view, container, false);
                //make the fab button for the job fragment

               //done button code
                Button done_button = (Button) fragmentlayout.findViewById(R.id.done_button);
                if(deleted_notes){
                    done_button.setText(getString(R.string.restore_job_button_string));
                }
                done_button.setOnClickListener(this);//code to run onClick below
                break;

            case REFERRAL:
                //create the fragment layout
                fragmentlayout = inflater.inflate(R.layout.fragment_referral_details_view, container, false);
                //pass details specific to referral view into the correct fields
                //first by creating the textviews
                viewReferredDetails = (TextView) fragmentlayout.findViewById(R.id.viewReferrerDetails);
                viewReferredContact = (TextView) fragmentlayout.findViewById(R.id.viewReferrerContact);
                //then by adding data to them
                viewReferredDetails.setText(view_pager_bundle.getString(MainActivity.NOTE_REFERRER_NAME));
                //contact code to change it to blue and clickable
                String referrer_contact_details = view_pager_bundle
                        .getString(MainActivity.NOTE_REFERRER_CONTACT);
                SpannableString spannableString = new SpannableString(referrer_contact_details);
                spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), 0);
                viewReferredContact.setText(spannableString);

                //add job from referral button code
                Button create_job_for_patient = (Button) fragmentlayout.findViewById(R.id.create_new_job_for_patient);
                create_job_for_patient.setOnClickListener(this);

                //button for referrer contact details clickable to phone
                viewReferredContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:"+viewReferredContact.getText().toString().trim()));
                        startActivity(callIntent );
                    }
                });

//                setAssociatedNotes();

                break;
        }

        //create the fab
        fab = (FloatingActionButton) fragmentlayout.findViewById(R.id.edit_fab);
        //it a animationsListener
        fab.setOnClickListener(this);

        //deal with dateSetListener and time
        setDateandTime();
        //get dateSetListener created
        datecreated = view_pager_bundle.getLong(MainActivity.NOTE_DATE_CREATED, 10);

        //code which works for all cases
        setCommonTextViews();
        //remove scrollbars
        removeVerticalScrollBar();

        //set size of scrollable window based on size of buttons on all screens;
        button_to_associated_jobs.post(new Runnable() {//put as a post runnable otehrwise .getHeight returns 0 as not yet drawn;
            @Override
            public void run() {
                setScrollViewSize(button_to_associated_jobs.getHeight());
            }
        });
//
        // Inflate the layout for this fragment
        return fragmentlayout;
    }


    public void removeVerticalScrollBar(){
        //remove the vertical scroll from scroll view
        ScrollView scrollView = (ScrollView) fragmentlayout.findViewById(R.id.ScrollViewDetailsView);
        scrollView.setVerticalScrollBarEnabled(false);
    }

    public void setScrollViewSize(int bottom_margin){
        ScrollView scrollView = (ScrollView) fragmentlayout.findViewById(R.id.ScrollViewDetailsView);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                scrollView.getLayoutParams();
        layoutParams.setMargins(0,0,0,bottom_margin);
        scrollView.setLayoutParams(layoutParams);
    }

    public void setAssociatedNotes (){
        //check if there is a Job or referral for the same patient
        int number_of_jobs = 0;
        //create the array list
        dbAdapter.open();
        switch (typeofNote) {
            case REFERRAL:
                arrayListtoSearch = dbAdapter
                        .getNotesNoHeaders(deleted_notes, MainActivity.TypeofNote.JOB);

                break;
            case JOB:
                arrayListtoSearch = dbAdapter
                        .getNotesNoHeaders(deleted_notes, MainActivity.TypeofNote.REFERRAL);
                break;
            default:
                arrayListtoSearch = dbAdapter
                        .getNotesNoHeaders(deleted_notes, MainActivity.TypeofNote.JOB);
        }
        dbAdapter.close();
        //search the array list for the same patient name and NHI
        for(int i = 0; i < arrayListtoSearch.size(); i++){
            String comparing_to_name = arrayListtoSearch.get(i).getPatientname();
            String comparing_to_NHI = arrayListtoSearch.get(i).getPatientNHI();
            String patient_name = view_pager_bundle.getString(MainActivity.NOTE_PATIENT_NAME);
            String patient_NHI = view_pager_bundle.getString(MainActivity.NOTE_PATIENT_NHI);
            if(comparing_to_name.equals(patient_name) &&
                    comparing_to_NHI.equals(patient_NHI)){
                number_of_jobs ++;
            }
        }
        switch (typeofNote) {
            case REFERRAL:
                if (number_of_jobs == 1) {
                    button_to_associated_jobs.setText(String.valueOf(number_of_jobs)
                            + " " + getResources().getString(R.string.jobSingular));
                } else if (number_of_jobs > 1) {
                    button_to_associated_jobs.setText(String.valueOf(number_of_jobs)
                            + " " + getResources().getString(R.string.jobs));
                }
                break;
            case JOB:
                if (number_of_jobs > 0) {
                    button_to_associated_jobs.setText(getResources().getString(R.string.associated_referral));
                }
        }
        if(number_of_jobs > 0){
            button_to_associated_jobs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //create the Intent to launch Detail View if there are notes for the same patient in another area
                    Intent launchDetailedView = new Intent(getContext(), DetailActivity.class);
                    switch (typeofNote){//say to launch other type of note
                        case REFERRAL:
                            launchDetailedView.putExtra
                                    (MainActivity.NOTE_TYPE, MainActivity.TypeofNote.JOB);
                            break;
                        case JOB:
                            launchDetailedView.putExtra
                                    (MainActivity.NOTE_TYPE, MainActivity.TypeofNote.REFERRAL);
                            break;
                    }
                    launchDetailedView.putExtra(MainActivity.NOTE_PATIENT_NAME,
                            view_pager_bundle.getString(MainActivity.NOTE_PATIENT_NAME));//add patient name so view pager only has that patients notes
                    launchDetailedView.putExtra(MainActivity.NOTE_PATIENT_NHI,
                            view_pager_bundle.getString(MainActivity.NOTE_PATIENT_NHI));//add patient NHI to ensure same patient
                    launchDetailedView.putExtra(MainActivity.LIST_POSITION, 0); //put list position in
                    if(typeofNote == MainActivity.TypeofNote.REFERRAL) {
                        launchDetailedView.putExtra(MainActivity.NOTE_TYPE_LAUNCHED_FROM,
                                MainActivity.TypeofNote.REFERRAL_DETAILED_VIEW);
                    }else{
                        launchDetailedView.putExtra(MainActivity.NOTE_TYPE_LAUNCHED_FROM,
                                typeofNote);//say this intent has been launched from something
                    }
                    launchDetailedView.putExtra(MainActivity.DELETED_NOTES, deleted_notes);//add if a deleted note
                    launchDetailedView.putExtra(MainActivity.NOTE_FRAGMENT_TO_LOAD_EXTRA,
                            MainActivity.FragmentToLaunch.VIEW); //tell it to open a view type
                    startActivity(launchDetailedView);
                    //then relaunching the viewPager;
//                    switch (typeofNote){//say to launch
                }
            });
        }else{
            button_to_associated_jobs.setText("");
        }
    }

    public void setCommonTextViews(){
        //set Buttons
        button_to_associated_jobs = (Button) fragmentlayout
                .findViewById(R.id.number_of_other_type_of_note);

        //create the Textview variables
        viewPatientName = (TextView) fragmentlayout.findViewById(R.id.viewPatientName);
        viewPatientNHI = (TextView) fragmentlayout.findViewById(R.id.viewPatientNHI);
        viewPatient_Age_and_sex = (TextView) fragmentlayout.findViewById(R.id.view_age_sex);
        viewPatient_Location = (TextView) fragmentlayout.findViewById(R.id.viewLocation);
        viewDetails = (TextView) fragmentlayout.findViewById(R.id.viewDetails);
        final ImageView viewIcon = (ImageView) fragmentlayout.findViewById(R.id.viewNoteIcon);

        //set the text for the fields

        viewPatientName.setText(view_pager_bundle.getString(MainActivity.NOTE_PATIENT_NAME));
        viewPatientNHI.setText(view_pager_bundle.getString(MainActivity.NOTE_PATIENT_NHI));
        viewPatient_Age_and_sex.setText(view_pager_bundle.getString(MainActivity.NOTE_PATIENT_AGE_AND_SEX));
        viewPatient_Location.setText(view_pager_bundle.getString(MainActivity.NOTE_PATIENT_LOCATION));
        viewDetails.setText(view_pager_bundle.getString(MainActivity.NOTE_DETAILS));
        viewIcon.setBackgroundResource(Note.categoryToDrawable(noteCat));

        switch (typeofNote) {
            case JOB:
                canCheckImportanceIcon = sharedPreferences.getBoolean("JOB_CHECKBOX_VISIBLE", false);
                break;
            case REFERRAL:
                canCheckImportanceIcon = sharedPreferences.getBoolean("REFERRAL_CHECKBOX_VISIBLE", false);
                break;
            default:
                canCheckImportanceIcon = true;
                break;
        }

        if(canCheckImportanceIcon) {
            //if checked add a tick
            if (view_pager_bundle.getLong(MainActivity.CHECKED_STATUS) == 1) {
                viewIcon.setImageResource(R.drawable.ic_tick);
            } else {
                viewIcon.setImageResource(android.R.color.transparent);
            }
            viewIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Animations.setAnimationDuration(200);
                    dbAdapter.open();
                    if (view_pager_bundle.getLong(MainActivity.CHECKED_STATUS) == 1) {//if checked
                        //animate the ICON
                        Animations.animateIconClick(viewIcon, Note.categoryToDrawable(noteCat));
                        //change the database
                        dbAdapter.changeCheckboxStatus(noteId, 0);//change to unchecked
                        view_pager_bundle.putLong(MainActivity.CHECKED_STATUS, 0);
                    } else {//if unchecked
                        //animate the icon
                        Animations.animateIconClick(viewIcon, R.drawable.ic_tick);
                        dbAdapter.changeCheckboxStatus(noteId, 1);//change to checked
                        view_pager_bundle.putLong(MainActivity.CHECKED_STATUS, 1);
                    }
                    dbAdapter.close();
                }
            });
        }else{
            viewIcon.setImageResource(android.R.color.transparent);
        }
    }

    public void setDateandTime(){
        //call the millis reference of the dateSetListener from the bundle
        date_and_time = view_pager_bundle.getLong(MainActivity.NOTE_DATE_AND_TIME);
        //dim the Textviews so we can populate it
        viewDate_of_Note = (TextView) fragmentlayout.findViewById(R.id.view_date);
        viewTime_of_Note = (TextView) fragmentlayout.findViewById(R.id.view_time);
        //get details from the milli using a calendar creation
        Calendar myCalendar = Calendar.getInstance();
        myCalendar.setTimeInMillis(date_and_time);
        viewDate_of_Note.setText(date_format.format(myCalendar.getTime()));
        viewTime_of_Note.setText(time_format.format(myCalendar.getTime()));
    }

    //on click code for fab
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.edit_fab:
                Animations.setAnimationDuration(80);
                Animations.animateIconClick(fab, android.R.drawable.ic_menu_save);
                //have to put all the data into the intent as this has been launch from the viewPager where the
                //data is in a bundle, not in the intent
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                //  intent = putInfoIntoIntent(intent);
                intent.putExtras(view_pager_bundle);
                //change the view type within the intent data
                intent.putExtra(MainActivity.NOTE_FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.EDIT);
                //stop the edit activity from being in the backstack
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                //restart the DetailActivity with the new fragment to load
                startActivityForResult(intent, 1);
                break;
            case R.id.create_new_job_for_patient:
                Intent intent1 = new Intent(getActivity(), DetailActivity.class);
                intent1 = putInfoIntoIntent(intent1);
                intent1.putExtra(MainActivity.NOTE_TYPE, MainActivity.TypeofNote.JOB);
                intent1.putExtra(MainActivity.NOTE_DATE_AND_TIME, "");//so the time is set for the time the button is clicked, not the time the referral has on it
                intent1.putExtra(MainActivity.NOTE_DETAILS, "");//remove detail information
                intent1.putExtra(MainActivity.NOTE_TYPE_LAUNCHED_FROM, typeofNote);
                intent1.putExtra(MainActivity.NOTE_FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.CREATE);
                startActivity(intent1);
                break;
            case R.id.done_button:
                if (noteLaunchedFrom == MainActivity.TypeofNote.REFERRAL_DETAILED_VIEW) {
                    changedNoteDeleteStatus();
                    sharedPreferences.edit()
                            .putLong("NOTE_ID", noteId).apply();//save the correct noteId
                    sharedPreferences.edit()
                            .putBoolean("JOB_DELETED_CHANGED", true).apply();//tell the code a job's deleted status has been changed
                    sharedPreferences.edit()
                            .putBoolean("CHANGE_TO_DATABASE_OCCURRED", true).apply();
                    getActivity().onBackPressed();
                } else {
                    returntoList();
                }
                break;
            }
        }


    //code to return to the listView sending back details to allow for undo button
    public void returntoList() {
        Intent returnIntent = new Intent(getActivity(), Activity_ListView.class);
        returnIntent.putExtra(MainActivity.NOTE_ID, noteId);
        returnIntent.putExtra(MainActivity.DELETED_NOTES, deleted_notes);
        returnIntent.putExtra(MainActivity.NOTE_TYPE, typeofNote);
        if(noteLaunchedFrom != null) {
            returnIntent.putExtra(MainActivity.NOTE_TYPE_LAUNCHED_FROM, noteLaunchedFrom);
            changedNoteDeleteStatus();
        }
        returnIntent.putExtra(MainActivity.JOB_DONE, true);
        startActivity(returnIntent);
    }

    public void changedNoteDeleteStatus(){
        int is_deleted;
        if(deleted_notes){
            is_deleted = 0;
        }else {
            is_deleted = 1;
        }
        dbAdapter.open();
        dbAdapter.changeDeleteStatus(noteId, is_deleted);
        dbAdapter.close();
    }

    public void snackbar(){
        //reset shared preferences
        sharedPreferences.edit().putBoolean("JOB_DELETED_CHANGED", false).apply();
        //get info from shared preferences
        final long noteIdDeleted = sharedPreferences.getLong("NOTE_ID",0);
        //create the crrect values depending on if these are deleted notes
        final int undo_deleted;
        String what_happened_to_note, toastEndText;
        if(!deleted_notes) { //allows the same code to change the note from deleted to none deleted
            undo_deleted = 0; //allow for the undo button to do the opposite
            what_happened_to_note = getString(R.string.marked_done); //set the words of the snackbar
            toastEndText = getString(R.string.restored_snackbar_string);
        }else {
            undo_deleted = 1; //if UNDO action in snackbar clicked changes back to a completed job
            what_happened_to_note = getString(R.string.restored_snackbar_string);
            toastEndText = getString(R.string.marked_done);
        }
        //make full snackbar workds
        String snackbarWords = getString(R.string.jobSingular)
                + " " + what_happened_to_note;
        final String toastText = getString(R.string.jobSingular)
                + " " + toastEndText;
        Snackbar snackbar = Snackbar
                .make(fragmentlayout, snackbarWords, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dbAdapter.open();
                        dbAdapter.changeDeleteStatus(noteIdDeleted, undo_deleted);
                        dbAdapter.close();
                        Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT).show();
                        setAssociatedNotes();
                    }
                })
                .setActionTextColor(Color.RED);
        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar snackbar) {
                super.onShown(snackbar);
                setScrollViewSize(button_to_associated_jobs.getHeight() + snackbar.getView().getHeight());
            }

            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                setScrollViewSize(button_to_associated_jobs.getHeight());
            }
        });
        snackbar.show();
    }


    //confirm dialog to delete a referral
    public void buildConfirmDialog() {
        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(getActivity());
        confirmBuilder.setTitle(R.string.confirm_title);
        if(!deleted_notes) {
            confirmBuilder.setMessage(R.string.confirm_message);
        }else{
            confirmBuilder.setMessage(R.string.permanently_delete_message);
        }

        confirmBuilder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteReferral(noteId);
                Intent goToList = new Intent(getActivity(), Activity_ListView.class);
                goToList.putExtra(MainActivity.NOTE_TYPE, MainActivity.TypeofNote.REFERRAL);
                startActivity(goToList);
            }
        });

        confirmBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //DO NOTHING
            }
        });

        confirmDialogObject = confirmBuilder.create();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        switch(typeofNote){
            case REFERRAL:
                inflater.inflate(R.menu.detail_view_menu, menu);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                    getActivity().onBackPressed();
                break;
            case R.id.delete:
                confirmDialogObject.show();
                break;
        }
        return true;
    }

    public void deleteReferral (Long noteId){
//        referralsDbAdapter.deleteReferral(noteId);
        dbAdapter.open();
        int is_deleted = 1;
        int permanentely_deleted = 2;
        if(deleted_notes){
            dbAdapter.changeDeleteStatus(noteId, permanentely_deleted);
        }else{//if not deleted notes make this a deleted note
            dbAdapter.changeDeleteStatus(noteId, is_deleted);
        }
        dbAdapter.close();
    }

    public Intent putInfoIntoIntent(Intent intent){
        intent.putExtra(MainActivity.NOTE_PATIENT_NAME, viewPatientName.getText());
        intent.putExtra(MainActivity.NOTE_PATIENT_NHI, viewPatientNHI.getText());
        intent.putExtra(MainActivity.NOTE_PATIENT_AGE_AND_SEX, viewPatient_Age_and_sex.getText());
        intent.putExtra(MainActivity.NOTE_PATIENT_LOCATION, viewPatient_Location.getText());
        intent.putExtra(MainActivity.NOTE_DATE_AND_TIME, date_and_time);
        intent.putExtra(MainActivity.NOTE_DETAILS, viewDetails.getText());
        intent.putExtra(MainActivity.NOTE_CATEGORY, noteCat);
        intent.putExtra(MainActivity.NOTE_TYPE, typeofNote);
        intent.putExtra(MainActivity.NOTE_ID, noteId);
        intent.putExtra(MainActivity.LIST_POSITION, position);
        intent.putExtra(MainActivity.DELETED_NOTES, deleted_notes);
        intent.putExtra(MainActivity.NOTE_DATE_CREATED, datecreated);
        intent.putExtra(MainActivity.CHECKED_STATUS, view_pager_bundle.getLong(MainActivity.CHECKED_STATUS));
        switch(typeofNote){
            case REFERRAL:
                intent.putExtra(MainActivity.NOTE_REFERRER_NAME, viewReferredDetails.getText());
                intent.putExtra(MainActivity.NOTE_REFERRER_CONTACT, viewReferredContact.getText());
                break;
            case JOB:
                //do nothing
                break;
        }
        return intent;
    }

    @Override
    public void onResume() {
        super.onResume();
        setAssociatedNotes();
        if (sharedPreferences.getBoolean("JOB_DELETED_CHANGED", false)){//if a job has been deleted to load this view run snackbar code
            snackbar();
        }
    }
}



