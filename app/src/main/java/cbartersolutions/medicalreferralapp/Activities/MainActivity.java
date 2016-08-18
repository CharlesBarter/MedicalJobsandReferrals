package cbartersolutions.medicalreferralapp.Activities;

import android.content.Intent;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import cbartersolutions.medicalreferralapp.Listeners.OnSwipeTouchListener;
import cbartersolutions.medicalreferralapp.R;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";

    public static final String NOTE_ID = "com.cbartersolutions.medicalreferralapp.Identifier";
    public static final String NOTE_PATIENT_NAME = "com.cbartersolutions.medicalreferralapp.Patient_Name";
    public static final String NOTE_PATIENT_NHI = "com.cbartersolutions.medicalreferralapp.Patient_NHI";
    public static final String NOTE_PATIENT_AGE_AND_SEX = "com.cbartersolutions.medicalreferralapp.Patient_Age_and_Sex";
    public static final String NOTE_PATIENT_LOCATION = "com.cbartersolutions.medicalreferralapp.Patient_Location";
    public static final String NOTE_DATE_AND_TIME = "com.cbartersolutions.medicalreferralapp.Date_on_Note";
    public static final String NOTE_REFERRER_NAME = "com.cbartersolutions.medicalreferralapp.Referrer_Name";
    public static final String NOTE_REFERRER_CONTACT = "com.cbartersolutions.medicalreferralapp.Referrer_Contact";
    public static final String NOTE_DETAILS = "com.cbartersolutions.medicalreferralapp.Details";
    public static final String NOTE_CATEGORY = "com.cbartersolutions.medicalreferralapp.Category";
    public static final String NOTE_DATE_CREATED = "com.cbartersolutions.medicalreferralapp.Date";
    public static final String NOTE_FRAGMENT_TO_LOAD_EXTRA = "com.cbartersolutions.medicalreferralapp.Fragment_To_Load";
    public static String NOTE_TYPE = "com.cbartersolutions.medicalreferralapp.Note_Type";
    public static final String LIST_POSITION = "com.cbartersolutions.medicalreferralapp.List_Position";
    public static final String JOB_DONE = "com.cbartersolutions.medicalreferralapp.JOB_DONE";
    public static final String DELETED_NOTES = "com.cbartersolutions.medicalreferralapp.DELTED_NOTES";
    public static final String NEW_NOTE = "com.cbartersolutions.medicalreferralapp.NEW_NOTE";

    MainActivity.TypeofNote typeOfNote;

    //enum for which type of note it is
    public enum TypeofNote {JOB, REFERRAL};

    //define a new enum to say which fragment is to be launched
    public enum FragmentToLaunch{ VIEW, EDIT, CREATE}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        typeOfNote = (MainActivity.TypeofNote) getIntent().getSerializableExtra(MainActivity.NOTE_TYPE);

        View view = findViewById(R.id.main_activity);
        view.setOnTouchListener(new OnSwipeTouchListener(this){
            @Override
            public void onSwipeLeft(){
              if (typeOfNote != null){
                  openList(typeOfNote);
              }
            };
        });


        //main page floating button code

        //create the buttons to work with
        FloatingActionMenu menubutton = (FloatingActionMenu) findViewById(R.id.menubutton);
        FloatingActionButton add_job_button = (FloatingActionButton) findViewById(R.id.add_job_fab_button);
        FloatingActionButton add_referral_button = (FloatingActionButton) findViewById(R.id.add_referral_fab_button);

        //set the menu button to close on pressing outside
        menubutton.setClosedOnTouchOutside(true);

        add_job_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DetailActivity.class);
                intent.putExtra(MainActivity.NOTE_FRAGMENT_TO_LOAD_EXTRA, FragmentToLaunch.CREATE);
                intent.putExtra(MainActivity.NOTE_TYPE, TypeofNote.JOB);
                startActivity(intent);
            }
        });

        add_referral_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DetailActivity.class);
                intent.putExtra(MainActivity.NOTE_FRAGMENT_TO_LOAD_EXTRA, FragmentToLaunch.CREATE);
                intent.putExtra(MainActivity.NOTE_TYPE, TypeofNote.REFERRAL);
                startActivity(intent);
            }
        });

    }

    //on click job button open job list activity
    public void openJobsList (View view){
        typeOfNote = TypeofNote.JOB;
        openList(typeOfNote);
//        Intent openList = new Intent(this, Activity_ListView.class);
//        openList.putExtra(MainActivity.NOTE_TYPE, TypeofNote.JOB);
//        startActivity(openList);
    }

    //on click referral list button open referral list activity
    public void openReferralList (View view){
        typeOfNote = TypeofNote.REFERRAL;
        openList(typeOfNote);
//        Intent openList = new Intent(this, Activity_ListView.class);
//        openList.putExtra(MainActivity.NOTE_TYPE, TypeofNote.REFERRAL);
//        startActivity(openList);
    }

    public void openList(MainActivity.TypeofNote typeOfNote){
        Intent openList = new Intent(this, Activity_ListView.class);
        openList.putExtra(MainActivity.NOTE_TYPE, typeOfNote);
        startActivity(openList);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
