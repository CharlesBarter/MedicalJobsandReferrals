package cbartersolutions.medicalreferralapp.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cbartersolutions.medicalreferralapp.Adapters.NotesDbAdapter;
import cbartersolutions.medicalreferralapp.Fragments.DetailsEditFragment;
import cbartersolutions.medicalreferralapp.Fragments.DetailsViewFragment;
import cbartersolutions.medicalreferralapp.ArrayLists.Note;
import cbartersolutions.medicalreferralapp.R;

public class DetailActivity extends AppCompatActivity  {

    private static String TAG = "Details Activity";

    private MainActivity.TypeofNote typeofNote, note_type_launched_from;
    private String title, fullTitle, patients_name_to_search, patients_NHI_to_search;
    public static final String NEW_NOTE_EXTRA = "New Note";
    private static int position;
    private static long noteId;
    private FragmentTransaction fragmentTransaction;
    private MainActivity.FragmentToLaunch fragmentToLaunch;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private Bundle bundle;
    private static List<android.support.v4.app.Fragment> fragments;
    private NotesDbAdapter dbAdapter;
    //
    ArrayList<Note> list;
    private Boolean deleted_notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayUseLogoEnabled(false);

        getInfoFromIntent();

        switch (fragmentToLaunch) {
            case EDIT:
                //create the detailed edit fragment
                launchFragment(fragmentToLaunch);
                fullTitle = "Edit" + " " + getTitle(typeofNote);
                setTitle(fullTitle);
//                toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.minifab));
                break;
            case CREATE:
                //create the detailed create fragment
                launchFragment(fragmentToLaunch);
                //set the title to create
                fullTitle = getResources().getString(R.string.create_new) + " " + getTitle(typeofNote);
                setTitle(fullTitle);
                break;
            case VIEW:    //for the VIEW case we will add a viewPager
                //create an array of fragment with all the fragments for every list item so they can be swiped through
                setUpViewFragments();
                break;
        }
    }

    public void getInfoFromIntent(){
        //get the type of Note
        typeofNote = (MainActivity.TypeofNote) getIntent()
                .getSerializableExtra(MainActivity.NOTE_TYPE);
        note_type_launched_from = (MainActivity.TypeofNote) getIntent()
                .getSerializableExtra(MainActivity.NOTE_TYPE_LAUNCHED_FROM);
        if(note_type_launched_from != null){
            patients_name_to_search = getIntent().getStringExtra(MainActivity.NOTE_PATIENT_NAME);
            patients_NHI_to_search = getIntent().getStringExtra(MainActivity.NOTE_PATIENT_NHI);
        }

        //get note ID
        noteId = getIntent().getExtras().getLong(MainActivity.NOTE_ID);

        //get position
        position = getIntent().getExtras().getInt(MainActivity.LIST_POSITION);

        //is this a deleted note?
        deleted_notes = getIntent().getBooleanExtra(MainActivity.DELETED_NOTES, false);

        //dim fragment to launch
        Intent intent = getIntent();

        //pull fragment to launch from the intent
        fragmentToLaunch = (MainActivity.FragmentToLaunch)
                intent.getSerializableExtra(MainActivity.NOTE_FRAGMENT_TO_LOAD_EXTRA);
    }

    public void setUpViewFragments (){
        //create the array list again to be used to make the fragments

        dbAdapter = new NotesDbAdapter(this.getBaseContext());
        dbAdapter.open();
        if(note_type_launched_from != null){
            list = dbAdapter.getSinglePatientsReferrals(patients_name_to_search, patients_NHI_to_search,
                    deleted_notes, typeofNote);
        }else{
            list = dbAdapter.getNotesNoHeaders(deleted_notes, typeofNote);
        }
        dbAdapter.close();

        //set view pager fragments
        fragments = new ArrayList<>();
        for (int i=0; i<list.size(); i++) {
            Bundle bundle = createBundle(i, list);
            fragments.add(createFragments(bundle));
            //ensure we are opening the view pager on the correct note
            long note_id_based_on_for_loop = list.get(i).getNoteId();
            if (note_id_based_on_for_loop == noteId) {
                position = i;//when the note clicked on or saved, which launched this activity, is the same as the note
                //being added to the Viewpager fragments, set the first visible fragement to this position
                // i.e the fragment which has been saved from or clicked on in the list.
            }
        }
        //instantiate a ViewPager and a PagerAdapter in the case of a View type detail
        mPager = (ViewPager) findViewById(R.id.details_viewPager);
        mPagerAdapter = new details_View_Pager_Adapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(position);
        mPager.addOnPageChangeListener(pageChangeListener);
        //set the title when first opening or returning to this view
//        CharSequence title = getTitleFromPosition(position, list);
//                setTitle(getTitle(typeofNote));
    }

    public static DetailsViewFragment createFragments (Bundle bundle){
        DetailsViewFragment fragment = new DetailsViewFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private Bundle createBundle (int i, ArrayList<Note> data){
        bundle = new Bundle();
//        create a list view fragment to run code in it
        Note note = data.get(i);
        bundle.putString(MainActivity.NOTE_PATIENT_NAME, note.getPatientname());
        bundle.putString(MainActivity.NOTE_PATIENT_NHI, note.getPatientNHI());
        bundle.putString(MainActivity.NOTE_PATIENT_AGE_AND_SEX, note.getPatient_Age_Sex());
        bundle.putString(MainActivity.NOTE_PATIENT_LOCATION, note.getPatient_location());
        bundle.putLong(MainActivity.NOTE_DATE_AND_TIME, note.get_date_and_time());
        bundle.putString(MainActivity.NOTE_DETAILS, note.getdetails());
        bundle.putString(MainActivity.NOTE_REFERRER_NAME, note.getReferrerName());
        bundle.putString(MainActivity.NOTE_REFERRER_CONTACT, note.getReferrerContact());
        bundle.putSerializable(MainActivity.NOTE_CATEGORY, note.getCategory());
        bundle.putLong(MainActivity.NOTE_ID, note.getNoteId());
        bundle.putLong(MainActivity.CHECKED_STATUS, note.getCheckedStatus());
        bundle.putSerializable(MainActivity.NOTE_TYPE, typeofNote);
        bundle.putBoolean(MainActivity.DELETED_NOTES, deleted_notes);
        bundle.putInt(MainActivity.LIST_POSITION, i);
        return bundle;
    }

    private CharSequence getTitleFromPosition (int position, ArrayList<Note> data){
        Note note = data.get(position);
        return note.getPatientname();
    }

    private class details_View_Pager_Adapter extends FragmentStatePagerAdapter{
        public details_View_Pager_Adapter(FragmentManager fm){
            super(fm);
        }
        @Override
        public android.support.v4.app.Fragment getItem(int pos){
            return fragments.get(pos);
        }

        public int getCount(){
            return list.size();
        }
    }

    //code to run when the page is changed ia a swipe with viewpager
    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //do nothing
        }

        @Override
        public void onPageSelected(int position) {
//            setTitle(getTitleFromPosition(position, list));
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    public String getTitle (MainActivity.TypeofNote typeofNote){
        //set part of the title based on the note type
        switch (typeofNote){
            case JOB:
                title = getResources().getString(R.string.jobSingular);
                break;
            case REFERRAL:
                title = getResources().getString(R.string.referralSingular);
                break;
        }
        return title;
    }

    public void launchFragment (MainActivity.FragmentToLaunch fragmentToLaunch) {
        //set up the fragment manager and begin the ability to import a fragment
        FragmentManager fragmentManager = getSupportFragmentManager();

        //create a new Edit Fragment
        DetailsEditFragment editFragment = new DetailsEditFragment();

        if (fragmentToLaunch == MainActivity.FragmentToLaunch.CREATE){
            //create a bundle into which information is added then this information is attached to the fragment
            Bundle bundle = new Bundle(); //creates bundle
            bundle.putBoolean(NEW_NOTE_EXTRA, true); //adds the value true, to a variable called NEW_NOTE_EXTRA in a bundle
            editFragment.setArguments(bundle); //adds the bundle information to the fragment
        }

        fragmentTransaction = fragmentManager.beginTransaction();
        //add to R.id.jobdetailview (the id for content_job_details), the fragment above and call it DETAILS_EDIT_FRAGMENT
        fragmentTransaction.add(R.id.content_detail, editFragment, "DETAILS_EDIT_FRAGMENT");
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(data.getBooleanExtra("SAVED", false)){
                setUpViewFragments();
            }
        }
    }
}

