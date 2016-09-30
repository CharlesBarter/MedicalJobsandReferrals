package cbartersolutions.medicalreferralapp.Activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import cbartersolutions.medicalreferralapp.R;

public class AppPreferencesActivity extends AppCompatActivity {

    private MainActivity.TypeofNote typeofNote;
    private boolean deleted_notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_preferences);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    if(getIntent().getSerializableExtra(MainActivity.NOTE_TYPE) != null) {
        //take info from calling intent
        typeofNote = (MainActivity.TypeofNote) getIntent().getSerializableExtra(MainActivity.NOTE_TYPE);
        deleted_notes = getIntent().getBooleanExtra(MainActivity.DELETED_NOTES, false);
    }

    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    SettingsFragment settingsFragment = new SettingsFragment();
    fragmentTransaction.add(R.id.preferences_frame_layout, settingsFragment, "SETTINGS FRAGMENT");
    fragmentTransaction.commit();
    }

public static class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.TypeofNote typeofNote = (MainActivity.TypeofNote)
                getActivity().getIntent().getSerializableExtra(MainActivity.NOTE_TYPE);
        switch (typeofNote){
            case JOB:
                addPreferencesFromResource(R.xml.job_app_preferences);
                getActivity().setTitle(getResources().getString(R.string.jobSingular) + " "
                        + getResources().getString(R.string.title_activity_app_preferences));
                break;
            case REFERRAL:
                addPreferencesFromResource(R.xml.referral_app_preferences);
                getActivity().setTitle(getResources().getString(R.string.referralSingular) + " "
                        + getResources().getString(R.string.title_activity_app_preferences));
                break;
        }
    }
}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(getIntent().getSerializableExtra(MainActivity.NOTE_TYPE) != null) {
            Intent intent = new Intent(this, Activity_ListView.class);
            intent.putExtra(MainActivity.NOTE_TYPE, typeofNote);
            intent.putExtra(MainActivity.DELETED_NOTES, deleted_notes);
            startActivity(intent);
        }else{
            finish();
        }
    }
}
