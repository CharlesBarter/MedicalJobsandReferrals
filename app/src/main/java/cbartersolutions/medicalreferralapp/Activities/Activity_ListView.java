package cbartersolutions.medicalreferralapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.support.design.widget.FloatingActionButton;

import cbartersolutions.medicalreferralapp.Fragments.ListViewFragment;
import cbartersolutions.medicalreferralapp.Fragments.RecyclerViewFragment;
import cbartersolutions.medicalreferralapp.Listeners.OnSwipeTouchListener;
import cbartersolutions.medicalreferralapp.R;
//import com.github.clans.fab.FloatingActionButton;

public class Activity_ListView extends AppCompatActivity implements View.OnClickListener{

    private static String TAG = "Activity_ListView";

    MainActivity.TypeofNote typeofNote;
    FloatingActionButton fab;

    private String jobs_menu_title, referrals_menu_title;
    boolean deleted_notes;

    private ViewPager mViewPager;
    private PagerAdapter listViewPagerAdapter;

    private Intent intent;

    private int position = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //get intent variables
        intent = getIntent();
        typeofNote = (MainActivity.TypeofNote) intent.getSerializableExtra(MainActivity.NOTE_TYPE);

        deleted_notes = intent.getBooleanExtra(MainActivity.DELETED_NOTES, false);
        if(deleted_notes){position = 1;}else{position = 0;}

        //code to handle deleted note vs. not deleted on create title
        final String is_deleted_title;
        if (!deleted_notes){//if current notes wanted
            is_deleted_title = "";
            jobs_menu_title = getResources().getString(R.string.view_completed_jobs);
            referrals_menu_title = getResources().getString(R.string.view_completed_referrals);
        }else{//if a delete notes wanted
            is_deleted_title = getString(R.string.completed_list_title) + " ";//sets title to deleted + type of note as below
            jobs_menu_title = getResources().getString(R.string.view_current_jobs);
            referrals_menu_title = getResources().getString(R.string.view_current_referrals);
        } //switch case for titles depending on type of note
        switch(typeofNote) {//creates the title depending on if JOB/REFERRAL or deleted notes
            case JOB:
                setTitle(is_deleted_title + getResources().getString(R.string.title_activity_jobs_list));
                break;
            case REFERRAL:
                setTitle(is_deleted_title + getResources().getString(R.string.title_activity_referral_list));
                break;
        }

//        fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //fragment for recyclerview
        createRecyclerViewFragment();

//        //code to swipe back to main activity with 2 buttons
        View view = findViewById(R.id.list_activity_layout);

        //code for onTouchListener
        view.setOnTouchListener(onTouchListener);

        //for view pager code
//        makeViewPager();

    }

    private void createRecyclerViewFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RecyclerViewFragment recyclerViewFragment = new RecyclerViewFragment();
        fragmentTransaction.add(R.id.list_layout, recyclerViewFragment);
        fragmentTransaction.commit();
    }

    private View.OnTouchListener onTouchListener = new OnSwipeTouchListener(getBaseContext()){
        @Override
        public void onSwipeRight() {
            if(!deleted_notes) {
                finish();
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
//                    intent.putExtra(MainActivity.DELETED_NOTES, false);
            }
        }
    };


    private void makeViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.list_ViewPager);
        listViewPagerAdapter = new list_View_Pager_Adapter(getSupportFragmentManager());
        mViewPager.setAdapter(listViewPagerAdapter);
        mViewPager.setCurrentItem(position);
        mViewPager.addOnPageChangeListener(pageChangeListener);
        setTitle(listViewPagerAdapter.getPageTitle(position));
    }

    private class list_View_Pager_Adapter extends FragmentStatePagerAdapter {
        public list_View_Pager_Adapter(FragmentManager fm){
            super (fm);
        }
        @Override
        public Fragment getItem(int pos){
            Bundle bundle = new Bundle();
            bundle.putSerializable(MainActivity.NOTE_TYPE, typeofNote);
            switch(pos){
                case 0:
                    bundle.putBoolean(MainActivity.DELETED_NOTES, false);
                    bundle.putBoolean(MainActivity.JOB_DONE, false);
//                    return ListViewFragment.newInstance(bundle);
                    return  RecyclerViewFragment.newInstance(bundle);

                case 1:
                    bundle.putBoolean(MainActivity.DELETED_NOTES, true);
                    bundle.putBoolean(MainActivity.JOB_DONE, false);
//                    return ListViewFragment.newInstance(bundle);
                    return RecyclerViewFragment.newInstance(bundle);
                default:
                    bundle.putBoolean(MainActivity.DELETED_NOTES, false);
                    bundle.putBoolean(MainActivity.JOB_DONE, false);
//                    return ListViewFragment.newInstance(bundle);
                    return RecyclerViewFragment.newInstance(bundle);
            }
        }
        @Override
        public int getCount(){
            return 2;
        }

        //ViewPager title String setting code, actual title is set below

        String title_typeOfNote;
        String second_title;

        @Override
        public CharSequence getPageTitle(int pos){

            switch (typeofNote){
                case JOB:
                    title_typeOfNote = getString(R.string.jobs);
                    break;
                case REFERRAL:
                    title_typeOfNote = getString(R.string.referrals);
            }
            switch (pos){
                case 0:
                    second_title = "";
                    break;
                case 1:
                    second_title = getString(R.string.completed_list_title) + " ";
                    break;
            }
            return second_title + title_typeOfNote;
        }
    }

    //on ViewPager change code
    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener(){
        @Override
        public void onPageSelected (int pos){
            setTitle(listViewPagerAdapter.getPageTitle(pos));//changes title based on getPageTitle code
        }
    };

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_menu, menu);
        MenuItem deleted_or_not = menu.findItem(R.id.view_deleted_jobs);
        switch(typeofNote){
            case JOB:
                deleted_or_not.setTitle(jobs_menu_title);
                break;
            case REFERRAL:
                deleted_or_not.setTitle(referrals_menu_title);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case android.R.id.home:
                finish();
            case R.id.action_settings:
                return true;
            case R.id.action_add:
                Intent intent = new Intent(this, DetailActivity.class);
                intent.putExtra(MainActivity.NOTE_FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.CREATE);
                intent.putExtra(MainActivity.NOTE_TYPE, typeofNote);
                startActivity(intent);
                return true;
            case R.id.view_deleted_jobs:
                Boolean deleted_wanted;

                // switches between deleted and current notes using the same menu button
                deleted_wanted = !deleted_notes;
                //restart intent
                Intent deleted_intent = new Intent(this, Activity_ListView.class);
                deleted_intent.putExtra(MainActivity.DELETED_NOTES, deleted_wanted);
                deleted_intent.putExtra(MainActivity.NOTE_TYPE, typeofNote);
                startActivity(deleted_intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(MainActivity.NOTE_FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.CREATE);
        intent.putExtra(MainActivity.NOTE_TYPE, typeofNote);
        startActivity(intent);
    }

    @Override
    public void finish(){
        if(!deleted_notes) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.NOTE_TYPE, typeofNote);
            startActivity(intent);
        }else{
            Intent go_back_to_not_deleted_notes = new Intent(this, Activity_ListView.class);
            go_back_to_not_deleted_notes.putExtra(MainActivity.NOTE_TYPE, typeofNote);
            go_back_to_not_deleted_notes.putExtra(MainActivity.DELETED_NOTES, false);
            startActivity(go_back_to_not_deleted_notes);
        }
    }

}
