package cbartersolutions.medicalreferralapp.Fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.github.clans.fab.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import cbartersolutions.medicalreferralapp.Activities.Activity_ListView;
import cbartersolutions.medicalreferralapp.Activities.DetailActivity;
import cbartersolutions.medicalreferralapp.Activities.MainActivity;
import cbartersolutions.medicalreferralapp.Adapters.ReferralsDbAdapter;
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
    private Note.Category noteCat;

    private long noteId = 0, datecreated = 0, date_and_time = 0;
    private int position = 0;

    private MainActivity.TypeofNote typeofNote;

    private AlertDialog confirmDialogObject;

    private boolean newNote;

    private boolean deleted_notes;

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

        //get data from intent
//        final Intent intent = getActivity().getIntent();
        view_pager_bundle = getArguments();

        //set typeOfNote
        typeofNote = (MainActivity.TypeofNote) view_pager_bundle.getSerializable(MainActivity.NOTE_TYPE);
//        typeofNote = (MainActivity.TypeofNote) intent.getSerializableExtra(MainActivity.NOTE_TYPE);

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
                viewReferredContact.setText(view_pager_bundle.getString(MainActivity.NOTE_REFERRER_CONTACT));
                viewReferredDetails.setText(view_pager_bundle.getString(MainActivity.NOTE_REFERRER_NAME));

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
                break;
        }

        //create the fab
        fab = (FloatingActionButton) fragmentlayout.findViewById(R.id.edit_fab);

        //deal with date and time
        setDateandTime();

        //code which works for all cases
        setCommonTextViews();

//        viewPatientName.setText(intent.getStringExtra(MainActivity.NOTE_PATIENT_NAME));
//        viewPatientNHI.setText(intent.getStringExtra(MainActivity.NOTE_PATIENT_NHI));
//        viewPatient_Age_and_sex.setText(intent.getStringExtra(MainActivity.NOTE_PATIENT_AGE_AND_SEX));
//        viewDetails.setText(intent.getStringExtra(MainActivity.NOTE_DETAILS));
//        viewIcon.setImageResource(Note.categoryToDrawable(noteCat));

        //get date created
        datecreated = view_pager_bundle.getLong(MainActivity.NOTE_DATE_CREATED, 10);

//        datecreated = intent.getLongExtra(MainActivity.NOTE_DATE_CREATED, 10);


        //create fab and make it a listener
        fab.setOnClickListener(this);

//
        // Inflate the layout for this fragment
        return fragmentlayout;

    }

    public void setCommonTextViews(){
        //create the Textview variables
        viewPatientName = (TextView) fragmentlayout.findViewById(R.id.viewPatientName);
        viewPatientNHI = (TextView) fragmentlayout.findViewById(R.id.viewPatientNHI);
        viewPatient_Age_and_sex = (TextView) fragmentlayout.findViewById(R.id.view_age_sex);
        viewPatient_Location = (TextView) fragmentlayout.findViewById(R.id.viewLocation);
        viewDetails = (TextView) fragmentlayout.findViewById(R.id.viewDetails);
        ImageView viewIcon = (ImageView) fragmentlayout.findViewById(R.id.viewNoteIcon);

        //set the text for the fields

        viewPatientName.setText(view_pager_bundle.getString(MainActivity.NOTE_PATIENT_NAME));
        viewPatientNHI.setText(view_pager_bundle.getString(MainActivity.NOTE_PATIENT_NHI));
        viewPatient_Age_and_sex.setText(view_pager_bundle.getString(MainActivity.NOTE_PATIENT_AGE_AND_SEX));
        viewPatient_Location.setText(view_pager_bundle.getString(MainActivity.NOTE_PATIENT_LOCATION));
        viewDetails.setText(view_pager_bundle.getString(MainActivity.NOTE_DETAILS));
        viewIcon.setImageResource(Note.categoryToDrawable(noteCat));
    }

    public void setDateandTime(){
        //call the millis reference of the date from the bundle
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
                //have to put all the data into the intent as this has been launch from the viewPager where the
//                data is in a bundle, not in the intent
                Intent intent = new Intent (getActivity(), DetailActivity.class);
                intent = putInfoIntoIntent(intent);
                //change the view type within the intent data
                intent.putExtra(MainActivity.NOTE_FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.EDIT);
                //restart the DetailActivity with the new fragment to load
                startActivity(intent);
                break;
            case R.id.create_new_job_for_patient:
                Intent intent1 = new Intent(getActivity(), DetailActivity.class);
                intent1 = putInfoIntoIntent(intent1);
                intent1.putExtra(MainActivity.NOTE_TYPE, MainActivity.TypeofNote.JOB);
                intent1.putExtra(MainActivity.NOTE_DETAILS, "");
                intent1.putExtra(MainActivity.NOTE_FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.CREATE);
                startActivity(intent1);
                break;
            case R.id.done_button:
                  returntoList(noteId);

                    break;
                }
        }


    //code to return to the listView sending back details to allow for undo button
    public void returntoList(Long noteId) {
        Intent returnIntent = new Intent(getActivity(), Activity_ListView.class);
        returnIntent = putInfoIntoIntent(returnIntent);
        returnIntent.putExtra(MainActivity.JOB_DONE, true);
        startActivity(returnIntent);

//        getActivity().setResult(Activity.RESULT_OK, returnIntent);
//        getActivity().finish();

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
        ReferralsDbAdapter referralsDbAdapter = new ReferralsDbAdapter(getActivity().getBaseContext());
        referralsDbAdapter.open();
//        referralsDbAdapter.deleteReferral(noteId);
        int is_deleted = 1;
        if(!deleted_notes) {
            referralsDbAdapter.updateReferral(noteId, viewPatientName.getText() + "",
                    viewPatientNHI.getText() + "", viewPatient_Age_and_sex.getText() + "",
                    viewPatient_Location.getText() + "", date_and_time, viewReferredDetails.getText() + "",
                    viewReferredContact.getText() + "", viewDetails.getText() + "",
                    noteCat, is_deleted);
        }else{
            referralsDbAdapter.deleteReferral(noteId);
        }
        referralsDbAdapter.close();
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

    // code to force back pressed when in view mode to send to list view
//    public void onBackPressed(){
//        Intent intent  = new Intent(getActivity(), Activity_ListView.class);
//        intent.putExtra(MainActivity.NOTE_TYPE, typeofNote);
//        startActivity(intent);
//    }

}



