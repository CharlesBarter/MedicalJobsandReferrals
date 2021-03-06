package cbartersolutions.medicalreferralapp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.support.design.widget.FloatingActionButton;

import java.util.ArrayList;

import cbartersolutions.medicalreferralapp.Adapters.NotesDbAdapter;
import cbartersolutions.medicalreferralapp.ArrayLists.Note;
import cbartersolutions.medicalreferralapp.Fragments.RecyclerViewFragment;
import cbartersolutions.medicalreferralapp.Listeners.OnSwipeTouchListener;
import cbartersolutions.medicalreferralapp.R;

public class Activity_ListView extends AppCompatActivity implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener{

    private static String TAG = "Activity_ListView";

    private MainActivity.TypeofNote typeofNote, noteTypeLaunchedFrom;
    private FloatingActionButton fab;

    private String jobs_menu_title, referrals_menu_title,
            is_deleted_title, fullTitle;
    boolean deleted_notes, canCheckImportanceIcon;

    private ViewPager mViewPager;
    private PagerAdapter listViewPagerAdapter;
    private Intent intent;
    private int position = -1;
    private RecyclerViewFragment recyclerViewFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout_list_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //get intent variables
        intent = getIntent();
        typeofNote = (MainActivity.TypeofNote) intent.getSerializableExtra(MainActivity.NOTE_TYPE);
        noteTypeLaunchedFrom = (MainActivity.TypeofNote)
                intent.getSerializableExtra(MainActivity.NOTE_TYPE_LAUNCHED_FROM);
        if(noteTypeLaunchedFrom != null){
            typeofNote = noteTypeLaunchedFrom;
        }

        deleted_notes = intent.getBooleanExtra(MainActivity.DELETED_NOTES, false);
        if(deleted_notes){position = 1;}else{position = 0;}

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        //Set up drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle bundle = new Bundle();
        bundle.putSerializable(MainActivity.NOTE_TYPE, typeofNote);
        bundle.putBoolean(MainActivity.DELETED_NOTES, deleted_notes);
        if(noteTypeLaunchedFrom != null){
            bundle.putSerializable(MainActivity.NOTE_TYPE_LAUNCHED_FROM, noteTypeLaunchedFrom);
        }

        setTitle(getTitleString(bundle));

        //fragment for recyclerview
        createRecyclerViewFragment(bundle);

        //for view pager code
//        makeViewPager();

        //get shared preference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        switch (typeofNote){
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
    }

    public String getTitleString(Bundle bundle){

        deleted_notes = bundle.getBoolean(MainActivity.DELETED_NOTES);
        typeofNote = (MainActivity.TypeofNote) bundle.getSerializable(MainActivity.NOTE_TYPE);

        //code to handle deleted note vs. not deleted on create title
        if (!deleted_notes){//if current notes wanted
            is_deleted_title = "";
            jobs_menu_title = getResources().getString(R.string.view_completed_jobs);//sets the menu string
            referrals_menu_title = getResources().getString(R.string.view_completed_referrals);
        }else{//if a delete notes wanted
            is_deleted_title = getString(R.string.completed_list_title) + " ";//sets title to deleted + type of note as below
            jobs_menu_title = getResources().getString(R.string.view_current_jobs);
            referrals_menu_title = getResources().getString(R.string.view_current_referrals);
        } //switch case for titles depending on type of note
        switch(typeofNote) {//creates the title depending on if JOB/REFERRAL or deleted notes
            case JOB:
                fullTitle = is_deleted_title + getResources().getString(R.string.title_activity_jobs_list);
                break;
            case REFERRAL:
                fullTitle = is_deleted_title + getResources().getString(R.string.title_activity_referral_list);
                break;
        }
        return fullTitle;
    }

    private void createRecyclerViewFragment(Bundle bundle){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        recyclerViewFragment = new RecyclerViewFragment();
        recyclerViewFragment.setArguments(bundle);
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
                    return  RecyclerViewFragment.newInstance(bundle);
                case 1:
                    bundle.putBoolean(MainActivity.DELETED_NOTES, true);
                    bundle.putBoolean(MainActivity.JOB_DONE, false);
                    return RecyclerViewFragment.newInstance(bundle);
                default:
                    bundle.putBoolean(MainActivity.DELETED_NOTES, false);
                    bundle.putBoolean(MainActivity.JOB_DONE, false);
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
        MenuItem deleted_or_not = menu.findItem(R.id.view_deleted);
        switch(typeofNote){
            case JOB:
                deleted_or_not.setTitle(jobs_menu_title);
                break;
            case REFERRAL:
                deleted_or_not.setTitle(referrals_menu_title);
                break;
        }
        if(!canCheckImportanceIcon){
            MenuItem reset_checkboxes = menu.findItem(R.id.reset_checkboxes);
            reset_checkboxes.setVisible(false);
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
                Intent settings_intent = new Intent(this, AppPreferencesActivity.class);
                settings_intent.putExtra(MainActivity.NOTE_TYPE, typeofNote);
                settings_intent.putExtra(MainActivity.DELETED_NOTES, deleted_notes);
                startActivity(settings_intent);
                return true;
            case R.id.action_add:
                Intent intent = new Intent(this, DetailActivity.class);
                intent.putExtra(MainActivity.NOTE_FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.CREATE);
                intent.putExtra(MainActivity.NOTE_TYPE, typeofNote);
                startActivity(intent);
                return true;
            case R.id.view_deleted:
                Boolean deleted_wanted;
                // switches between deleted and current notes using the same menu button
                deleted_wanted = !deleted_notes;
                //restart intent
                Intent deleted_intent = new Intent(this, Activity_ListView.class);
                deleted_intent.putExtra(MainActivity.DELETED_NOTES, deleted_wanted);
                deleted_intent.putExtra(MainActivity.NOTE_TYPE, typeofNote);
                startActivity(deleted_intent);
                return true;
            case R.id.reset_checkboxes:
                NotesDbAdapter notesDbAdapter = new NotesDbAdapter(this);
                notesDbAdapter.open();
                ArrayList<Note> mNotes = notesDbAdapter.getNotesNoHeaders(deleted_notes, typeofNote);
                for(int i = 0;i < mNotes.size(); i++){
                    Note note = mNotes.get(i);
                    notesDbAdapter.changeCheckboxStatus(note.getNoteId(),0);//set checked status to 0
                }
                notesDbAdapter.close();
                recyclerViewFragment.notifyRecyclerViewChanged();
                return true;
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
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        Intent intent = new Intent(this, Activity_ListView.class); //attempt via intent
        FragmentManager fragmentManager = getSupportFragmentManager(); //for attempt via fragments (which I would prefer overall)
        Bundle bundle = new Bundle(); //bundle to make fragment

        switch (id){
            case R.id.uncompleted_jobs_menu:
                bundle.putSerializable(MainActivity.NOTE_TYPE, MainActivity.TypeofNote.JOB);
                bundle.putBoolean(MainActivity.DELETED_NOTES, false);
                break;
            case R.id.completed_jobs_menu:
                bundle.putSerializable(MainActivity.NOTE_TYPE, MainActivity.TypeofNote.JOB);
                bundle.putBoolean(MainActivity.DELETED_NOTES, true);
                break;
            case R.id.uncompleted_referrals_menu:
                bundle.putSerializable(MainActivity.NOTE_TYPE, MainActivity.TypeofNote.REFERRAL);
                bundle.putBoolean(MainActivity.DELETED_NOTES, false);
                break;
            case R.id.completed_referrals_menu:
                bundle.putSerializable(MainActivity.NOTE_TYPE, MainActivity.TypeofNote.REFERRAL);
                bundle.putBoolean(MainActivity.DELETED_NOTES, true);
                break;
        }
        RecyclerViewFragment fragment = RecyclerViewFragment.newInstance(bundle);
        fragmentManager.beginTransaction()
                .replace(R.id.list_layout, fragment)
                .commit();
        setTitle(getTitleString(bundle));
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
