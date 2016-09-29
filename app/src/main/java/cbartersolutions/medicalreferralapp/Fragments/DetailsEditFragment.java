package cbartersolutions.medicalreferralapp.Fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TimePicker;


import com.github.clans.fab.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cbartersolutions.medicalreferralapp.Activities.Activity_ListView;
import cbartersolutions.medicalreferralapp.Activities.DetailActivity;
import cbartersolutions.medicalreferralapp.Activities.MainActivity;
import cbartersolutions.medicalreferralapp.Adapters.NotesDbAdapter;
import cbartersolutions.medicalreferralapp.ArrayLists.Note;
import cbartersolutions.medicalreferralapp.R;

/**
 * A simple {@link Fragment} subclass.
 */

public class DetailsEditFragment extends Fragment implements View.OnClickListener {

    private static String TAG = "DetailsEditFragment";

    private Intent intent;

    private View fragmentlayout;
    private FloatingActionButton fab;

    private EditText editPatientName,editPatientNHI, editReferredDetails, editReferredContact,
    editPatientAge_Sex, editDetails, edit_date, edit_time, editLocation;
    private String referrerDetails, referrerContact;
    private ImageButton editIconButton;

    private Note.Category savedIconButtonCategory;
    private AlertDialog iconCategoryDialog;
    private MainActivity.TypeofNote typeofNote;

    private long noteId = 0;
    private long date_created = -1;
    private int ChoiceofIcon, position;

    private static final String mCat = "mCat";

    private boolean newNote = false; //creates the boolean to know if this is editing an old note or creating a new note.

    Calendar myCalendar = Calendar.getInstance();//set calendar

    public DetailsEditFragment() {
        // Required empty public constructor
    }

    //define the format for dates and times
    String myDateFormat = "E, d MMM yyyy";
    java.text.SimpleDateFormat date_format = new java.text.SimpleDateFormat(myDateFormat, Locale.UK);

    String myTimeFormat = "HH:mm";
    SimpleDateFormat time_format = new SimpleDateFormat(myTimeFormat, Locale.UK);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        //set options menu
        setHasOptionsMenu(true);

        //pull the bundle form the fragment
        Bundle bundle = this.getArguments();
        //there is something in the bundle, as there only will be if we put something in their to say this is a new note then
        //set a boolean to say this is a new note
        if (bundle != null) {
            newNote = bundle.getBoolean(DetailActivity.NEW_NOTE_EXTRA, false);
        }

        //retrieve the data saved if things were edited before orientation change
        if (savedInstanceState != null) {
            savedIconButtonCategory = (Note.Category) savedInstanceState.get(mCat);
        }

        //get the type of note from the intent
        intent = getActivity().getIntent();
        //set the type of Note
        typeofNote = (MainActivity.TypeofNote)
                intent.getSerializableExtra(MainActivity.NOTE_TYPE);

        //set layout to inflate depending on type of note
        switch (typeofNote) {
            case JOB:
                //grab layout
                fragmentlayout = inflater.inflate(R.layout.fragment_job_details_edit, container, false);
                break;
            case REFERRAL:
                //grab layout
                fragmentlayout = inflater.inflate(R.layout.fragment_referral_details_edit, container, false);

                //create the referral detail edit text boxes so they can be edited
                editReferredDetails = (EditText) fragmentlayout.findViewById(R.id.editReferrerDetails);
                editReferredContact = (EditText) fragmentlayout.findViewById(R.id.editReferrerContact);
                //set the details of the referral edit text
                editReferredContact.setText(intent.getExtras().getString(MainActivity.NOTE_REFERRER_CONTACT, ""));
                editReferredDetails.setText(intent.getExtras().getString(MainActivity.NOTE_REFERRER_NAME, ""));
                break;

        }

        //create the floating action button
        fab = (FloatingActionButton) fragmentlayout.findViewById(R.id.save_fab);

        //set common EditText Fields
        setEditTextViewsDetails();

        //set up the dateSetListener and time fields
        setUpEditDateandTime();

        setDateField(); //set the value of edit_date field
        setTimeField(); //set the value of edit_time field

        removeVerticalScrollBar();//remove the vertical scrollbar when scrolling

        //get position from intent
        position = intent.getExtras().getInt(MainActivity.LIST_POSITION);


        if (savedIconButtonCategory != null) { //if a rotated note same view field of edit
            editIconButton.setBackgroundResource(Note.categoryToDrawable(savedIconButtonCategory));
        } else if (!newNote) { //if a note that is being edited from view position
            //set the icon
            Note.Category noteCat = (Note.Category) intent.getSerializableExtra(MainActivity.NOTE_CATEGORY);
            savedIconButtonCategory = noteCat;
            editIconButton.setBackgroundResource(Note.categoryToDrawable(noteCat));
            //collect an integer to define which category has been chosen
            //allows the dialog to correctly have selected the current choice
            ChoiceofIcon = Note.categorytoInteger(savedIconButtonCategory);
        } else { //if new note
            editIconButton.setBackgroundResource(R.drawable.ic_priority_medium);
            ChoiceofIcon = 1; //if a new note set choice of icon for the dialog box below to 2 as LOW IMPORTANCE is default;
        }
        if(intent.getExtras().getLong(MainActivity.CHECKED_STATUS) == 1){
            editIconButton.setImageResource(R.drawable.ic_tick);
        }else{editIconButton.setImageResource(android.R.color.transparent);}

        //set focus to first unfilled important section
        setFocus();

        //create fab and make it a listener
        fab.setOnClickListener(this);

        buildIconCategoryDialog();

        editIconButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    iconCategoryDialog.show();
                }
            });

        // Inflate the layout for this fragment
        return fragmentlayout;
    }

    public void setEditTextViewsDetails(){
        //irrespective of type of note do this code
        //grab widget references from layout
        editPatientName = (EditText) fragmentlayout.findViewById(R.id.editPatientName);
        editPatientNHI = (EditText) fragmentlayout.findViewById(R.id.editPatientNHI);
        editPatientAge_Sex = (EditText) fragmentlayout.findViewById(R.id.edit_age_sex);
        editLocation = (EditText) fragmentlayout.findViewById(R.id.editLocation);
        editDetails = (EditText) fragmentlayout.findViewById(R.id.editDetails);
        editIconButton = (ImageButton) fragmentlayout.findViewById(R.id.editNoteIcon);

        //get the data back from the intent which launched the activity
        //populate widgets with note data
        editPatientName.setText(intent.getExtras().getString(MainActivity.NOTE_PATIENT_NAME, ""));
        editPatientNHI.setText(intent.getExtras().getString(MainActivity.NOTE_PATIENT_NHI, ""));
        editPatientAge_Sex.setText(intent.getExtras().getString(MainActivity.NOTE_PATIENT_AGE_AND_SEX, ""));
        editLocation.setText(intent.getExtras().getString(MainActivity.NOTE_PATIENT_LOCATION));
        editDetails.setText(intent.getExtras().getString(MainActivity.NOTE_DETAILS, ""));
        noteId = intent.getExtras().getLong(MainActivity.NOTE_ID, 0);
        date_created = intent.getExtras().getLong(MainActivity.NOTE_DATE_CREATED, 0);
    }

    public void removeVerticalScrollBar(){
        //remove the vertical scroll from scroll view
        ScrollView scrollView = (ScrollView) fragmentlayout.findViewById(R.id.ScrollViewDetailsView);
        scrollView.setVerticalScrollBarEnabled(false);
    }

    public void setUpEditDateandTime() {
        //create the dateSetListener and time buttons
        edit_date = (EditText) fragmentlayout.findViewById(R.id.edit_date);
        edit_date.setOnClickListener(this);
        edit_time = (EditText) fragmentlayout.findViewById(R.id.edit_time);
        edit_time.setOnClickListener(this);
    }

    public void setDateField(){
        long date_on_note = intent.getExtras().getLong(MainActivity.NOTE_DATE_AND_TIME);
        if (date_on_note == 0){
            edit_date.setText(date_format.format( new Date()));
        }else{
           myCalendar.setTimeInMillis(date_on_note);
           updateDate();
        }
    }

    public void updateDate(){
        edit_date.setText(date_format.format(myCalendar.getTime()));
    }

    public void setTimeField(){
        long time_on_note = intent.getExtras().getLong(MainActivity.NOTE_DATE_AND_TIME);
        if (time_on_note == 0){
            edit_time.setText(time_format.format( new Date()));
        }else{
            myCalendar.setTimeInMillis(time_on_note);
            updateTime();
        }
    }

    public void updateTime(){
        edit_time.setText(time_format.format(myCalendar.getTime()));
    }

    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() { //"dateSetListener" becomes the listener for the DatePickerDialog
        @Override
        //set the dateSetListener chosen into the calendar myCalendar
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDate();
        }
    };

    TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            myCalendar.set(Calendar.HOUR, hour);
            myCalendar.set(Calendar.MINUTE, minute);
            updateTime();
        }
    };


    public void onClick (View view){
        switch (view.getId()){
            case R.id.save_fab:

                //change the calendar dateSetListener/time to an Integer to allow for storing
                long convertCalendar = myCalendar.getTimeInMillis();

                //state this note is not a deleted note using 0 1 as booleans
                int deleted = 0;

                //code for setting referrerDetails
                switch (typeofNote) {
                    case JOB:
                        referrerDetails = "";
                        referrerContact = "";
                        break;
                    case REFERRAL:
                        referrerDetails = editReferredDetails.getText().toString();
                        referrerContact = editReferredContact.getText().toString();
                        break;
                }

                //code for Location capitalisation
                //set Text code to capitalise single letters
                String location = editLocation.getText().toString();
                for (int i=1; i<location.length(); i++){
                    if(Character.isDigit(location.charAt(i-1))
                            && !Character.isDigit(location.charAt(i))
                            ) {
                            editLocation.setText(location.substring(0, i) + location.substring(i, i + 1).toUpperCase()
                                    +location.substring(i+1,location.length()));
                    }
                }


                //create the database
                NotesDbAdapter dbAdapter = new NotesDbAdapter(getActivity().getBaseContext());
                dbAdapter.open();

                if(newNote) {
//                    //if new note then create a new database item depending on typeOfNote
                    dbAdapter.createReferral(editPatientName.getText() + "",
                            editPatientNHI.getText() + "", editPatientAge_Sex.getText() + "",
                            editLocation.getText() + "", convertCalendar,
                            referrerDetails,referrerContact,
                            editDetails.getText() + "",
                            (savedIconButtonCategory == null) ? Note.Category.MODERATEIMPORTANCE : savedIconButtonCategory,
                            typeofNote, deleted);
                }else{
                    //otherwise its an old note so update the database
                    dbAdapter.updateReferral(noteId, editPatientName.getText() + "", editPatientNHI.getText() + "",
                            editPatientAge_Sex.getText() + "", editLocation.getText() + "",
                            convertCalendar,
                            referrerDetails, referrerContact,
                            editDetails.getText() + "", savedIconButtonCategory,
                            typeofNote, deleted);
                }

                //close the database's
                dbAdapter.close();

                finishEditing();
                break;

            case R.id.edit_date:
                //create a new DatePickerDialog with the listener as dateSetListener (code below) plus sets
                //the dateSetListener to the dateSetListener in myCalendar, if this is the first time this is opened it will be todays dateSetListener
                new DatePickerDialog(getContext(), dateSetListener,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                break;

            case R.id.edit_time:
                new TimePickerDialog(getContext(), timeSetListener,
                        myCalendar.get(Calendar.HOUR_OF_DAY),
                        myCalendar.get(Calendar.MINUTE), true).show();
                break;
            }

    }


    public void setFocus(){
        //set focus to first empty section
        boolean emptyfound = false;
        if (editPatientName.getText().length() == 0) {
            editPatientName.requestFocus();
            emptyfound = true;
        }
        if (!emptyfound && editPatientNHI.getText().length() == 0) {
            editPatientNHI.requestFocus();
            emptyfound = true;
        }
        if (!emptyfound && editPatientAge_Sex.getText().length() == 0) {
            editPatientAge_Sex.requestFocus();
            emptyfound = true;
        }
        if (!emptyfound && editDetails.getText().length() == 0) {
            editDetails.requestFocus();
            emptyfound = true;
        }
        if (!emptyfound) { // if there is no empty field focus on details section
            editDetails.requestFocus();
        }
    }

    @Override
    public void onSaveInstanceState (Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(mCat, savedIconButtonCategory);
    }


    private void buildIconCategoryDialog (){
        AlertDialog.Builder categoryBuilder = new AlertDialog.Builder(
                new ContextThemeWrapper(getActivity(),android.R.style.Theme_Holo_Light));
        final String [] categories = new String[]{"High Importance", "Medium Importance", "Low Importance"};
        categoryBuilder.setTitle("Choose Importance");
        categoryBuilder.setItems(categories, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int item) {

                //dismissed dialog window
                iconCategoryDialog.cancel();

                //what to do with different choices
                switch (item){
                    case 0:
                        savedIconButtonCategory = Note.Category.HIGHIMPORTANCE;
                        editIconButton.setBackgroundResource(R.drawable.ic_priority_high);
                        break;
                    case 1:
                        savedIconButtonCategory = Note.Category.MODERATEIMPORTANCE;
                        editIconButton.setBackgroundResource(R.drawable.ic_priority_medium);
                        break;
                    case 2:
                        savedIconButtonCategory = Note.Category.Z_LOWIMPORTANCE;
                        editIconButton.setBackgroundResource(R.drawable.ic_priority_low);
                        break;
                }
            }
        });

        iconCategoryDialog = categoryBuilder.create();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //override the home button to be a finish button
                getActivity().finish();
                break;
        }
        return true;
    }


    //put information into an intent
    public void finishEditing () {
        //reload back into the list view based on the typeOfNote
        Intent intent;
        //create an intent depending on if this is a new note or not
        if (newNote) {
            intent = new Intent(getActivity(), Activity_ListView.class);// if this is a new note, go back to the list view
        }else{
            intent = new Intent(getActivity(), DetailActivity.class); //if this is a note being edited, go back to the view note
        }
        intent.putExtra(MainActivity.NOTE_FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.VIEW);//make it a view fragment
        intent.putExtra(MainActivity.NOTE_TYPE, typeofNote);
        intent.putExtra(MainActivity.NOTE_PATIENT_NAME, editPatientName.getText().toString());
        intent.putExtra(MainActivity.NOTE_PATIENT_NHI, editPatientNHI.getText().toString());
        intent.putExtra(MainActivity.NOTE_PATIENT_AGE_AND_SEX, editPatientAge_Sex.getText().toString());
        intent.putExtra(MainActivity.NOTE_PATIENT_LOCATION, editLocation.getText().toString());
        intent.putExtra(MainActivity.NOTE_DETAILS, editDetails.getText().toString());
        intent.putExtra(MainActivity.NOTE_CATEGORY, (savedIconButtonCategory == null) ? Note.Category.Z_LOWIMPORTANCE : savedIconButtonCategory);
        intent.putExtra(MainActivity.NOTE_ID, noteId);
        intent.putExtra(MainActivity.LIST_POSITION, position);
        intent.putExtra(MainActivity.NOTE_DATE_CREATED, date_created);
//        intent.putExtra(MainActivity.NEW_NOTE, newNote);

        //add the extra detail for the referral type of note
        switch (typeofNote) {
            case REFERRAL:
                //add the referral specific stuff
                intent.putExtra(MainActivity.NOTE_REFERRER_NAME, editReferredDetails.getText().toString());
                intent.putExtra(MainActivity.NOTE_REFERRER_CONTACT, editReferredContact.getText().toString());
                break;
            case JOB:
                // do nothing additional
                break;
        }
        startActivity(intent);
    }
}
