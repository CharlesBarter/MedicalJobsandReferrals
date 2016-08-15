package cbartersolutions.medicalreferralapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import cbartersolutions.medicalreferralapp.Adapters.JobsDbAdapter;
import cbartersolutions.medicalreferralapp.Adapters.ReferralsDbAdapter;
import cbartersolutions.medicalreferralapp.Fragments.DetailsEditFragment;
import cbartersolutions.medicalreferralapp.Fragments.DetailsViewFragment;
import cbartersolutions.medicalreferralapp.Others.Note;
import cbartersolutions.medicalreferralapp.R;

public class DetailActivity extends AppCompatActivity  {

    private MainActivity.TypeofNote typeofNote;
    private String title;
    private String fullTitle;
    public static final String NEW_NOTE_EXTRA = "New Note";
    private static int position;
    private static long noteId;

    private FragmentTransaction fragmentTransaction;

    MainActivity.FragmentToLaunch fragmentToLaunch;

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private Bundle bundle;
    private static List<android.support.v4.app.Fragment> fragments;
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

                //get the type of Note
        typeofNote = (MainActivity.TypeofNote) getIntent().getSerializableExtra(MainActivity.NOTE_TYPE);

        //get not ID
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

        switch (fragmentToLaunch) {
            case EDIT:
                //create the detailed edit fragment
                createAndAddFragment(fragmentToLaunch);
                fullTitle = "Edit" + " " + getTitle(typeofNote);
                setTitle(fullTitle);
//                toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.minifab));
                break;
            case CREATE:
                //create the detailed create fragment
                createAndAddFragment(fragmentToLaunch);
                break;
            case VIEW:    //for the VIEW case we will add a viewPager
                //create an array of fragment with all the fragments for every list item so they can be swiped through
                setUpViewFragments();
                break;
        }
    }

    public void setUpViewFragments (){
        //create the array list again to be used to make the fragments
        switch (typeofNote) {
            case JOB:
                JobsDbAdapter jobsDbAdapter = new JobsDbAdapter(this.getBaseContext());
                jobsDbAdapter.open();
                if(!deleted_notes) {
                    list = jobsDbAdapter.getNonDeletedJobs();
                }else{
                    list = jobsDbAdapter.getDeleteJobs();
                }
                jobsDbAdapter.close();
                break;
            case REFERRAL:
                ReferralsDbAdapter referralsDbAdapter = new ReferralsDbAdapter(this.getBaseContext());
                referralsDbAdapter.open();
                if(!deleted_notes) {
                    list = referralsDbAdapter.getCurrentReferrals();
                }else{
                    list = referralsDbAdapter.getDeletedReferrals();
                }
                referralsDbAdapter.close();
                break;
        }

        fragments = new ArrayList<>();
        for (int i=0; i<list.size(); i++) {
            Bundle bundle = createBundle(i, list);
            fragments.add(createFragments(bundle));
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
        bundle.putSerializable(MainActivity.NOTE_TYPE, typeofNote);
        bundle.putBoolean(MainActivity.DELETED_NOTES, deleted_notes);
        bundle.putInt(MainActivity.LIST_POSITION, i);
        long note_id_based_on_for_loop = note.getNoteId();
        if (note_id_based_on_for_loop == noteId) {
            position = i;//when the note clicked on or saved, which launched this activity is the same as the note
            //being added to the Viewpager fragments using the for loop, set the position which
            //defines the fragment to open first to this fragment i.e the fragment
            //which has been saved from or clicked on in the list.
        }
        return bundle;
    }


    private CharSequence getTitleFromPosition (int position, ArrayList<Note> data){
        Note note = data.get(position);
        String Patient_Name = note.getPatientname();
        return Patient_Name;
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

//        public CharSequence getPageTitle(ArrayList<Note> data, int position){
//            Note note = data.get(position);
//            return note.getPatientname();
//        }

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

    //code to create a fragment for the EDIT and CREATE Fragments
    private void createAndAddFragment (MainActivity.FragmentToLaunch fragmentToLaunch) {

        //create a variable called intent with the data passed into the intent of this class
        Intent intent = getIntent();

        //set title depending on typeofNote
        title = getTitle(typeofNote);

//        //get the type of fragment to launch, this is a serializable extra as it is an enum, not a string
//        fragmentToLaunch = (MainActivity.FragmentToLaunch)
//                intent.getSerializableExtra(MainActivity.NOTE_FRAGMENT_TO_LOAD_EXTRA);
        //run code to get the fragment transaction based on the fragment to launch
        fragmentTransaction = getFragmenttoLaunch(fragmentToLaunch);
        fragmentTransaction.commit();

    }

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

    public FragmentTransaction getFragmenttoLaunch (MainActivity.FragmentToLaunch fragmentToLaunch) {
        //set up the fragment manager and begin the ability to import a fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        switch (fragmentToLaunch) {
            case EDIT:
                //create the EDIT fragment in this activity
                //create a new fragment of DetailsEditFragment
                DetailsEditFragment detailsEditFragment = new DetailsEditFragment();
                //set the full title of the fragment
                fullTitle = getResources().getString(R.string.edit) + " " + title;
//                setTitle(fullTitle);
                //add to R.id.jobdetailview (the id for content_job_details), the fragment above and call it DETAILS_EDIT_FRAGMENT
                fragmentTransaction.add(R.id.content_detail, detailsEditFragment, "DETAILS_EDIT_FRAGMENT");
                break;
//            case VIEW:
//                //create the VIEW fragment in this activity
//                DetailsViewFragment detailsViewFragment = new DetailsViewFragment();
//                //set the full title of the fragment
//                setTitle(title);
//                //add the fragment to the code
//                fragmentTransaction.add(R.id.content_detail, detailsViewFragment, "DETAILS_VIEW_FRAGMENT");
//                break;
            case CREATE:
                DetailsEditFragment jobCreateFragment = new DetailsEditFragment();
                fullTitle = getResources().getString(R.string.create_new) + " " + title;
                setTitle(fullTitle);

                //create a bundle into which information is added then this information is attached to the fragment
                Bundle bundle = new Bundle(); //creates bundle
                bundle.putBoolean(NEW_NOTE_EXTRA, true); //adds the value true, to a variable called NEW_NOTE_EXTRA in a bundle
                jobCreateFragment.setArguments(bundle); //adds the bundle information to the fragment
                fragmentTransaction.add(R.id.content_detail, jobCreateFragment, "CREATE_JOB_FRAGMENT");
                break;
        }

        return fragmentTransaction;
    }

    @Override
    public void onBackPressed() {
        switch (fragmentToLaunch) {
            case VIEW:
                Intent intent = new Intent(this, Activity_ListView.class);
                intent.putExtra(MainActivity.NOTE_TYPE, typeofNote);
                intent.putExtra(MainActivity.DELETED_NOTES, deleted_notes);
                startActivity(intent);
                break;
            case EDIT:
                finish();
                break;
            case CREATE:
                finish();
                break;
        }
    }

}

