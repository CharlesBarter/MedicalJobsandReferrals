package cbartersolutions.medicalreferralapp.Activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

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



public static class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
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
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++){
            Preference preference = getPreferenceScreen().getPreference(i);
            if(preference instanceof PreferenceGroup){
                PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                for(int j=0; j<preferenceGroup.getPreferenceCount(); j++){
                    Preference singlePref = preferenceGroup.getPreference(j);
                    updatePreference(singlePref, singlePref.getKey());
                    updateIcon(singlePref.getKey());
                }
            }else{
                updatePreference(preference, preference.getKey());
                updateIcon(preference.getKey());
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreference(findPreference(key), key);
        updateIcon(key);
    }

    public void updateIcon(String key){
        if(key.equals("REFERRAL_DEFAULT_IMPORTANCE_ICON")||key.equals("JOB_DEFAULT_IMPORTANCE_ICON")){
            ListPreference defaultIconPreference = (ListPreference)findPreference(key);
            if(defaultIconPreference != null) {
                String defaultIcon = defaultIconPreference.getValue();
                switch (defaultIcon) {
                    case "High":
                        defaultIconPreference.setIcon(R.drawable.ic_priority_high);
                        break;
                    case "Medium":
                        defaultIconPreference.setIcon(R.drawable.ic_priority_medium);
                        break;
                    case "Low":
                        defaultIconPreference.setIcon(R.drawable.ic_priority_low);
                        break;
                }
            }
        }
        if(key.equals("JOB_ASC_DESC")||key.equals("REFERRAL_ASC_DESC")){
            ListPreference asc_desc = (ListPreference)findPreference(key);
            String choice = asc_desc.getValue();
            switch (choice){
                case "ASC":
                    asc_desc.setIcon(R.drawable.ic_arrow_upward_black_24dp_xxxhdpi);
                    break;
                case "DESC":
                    asc_desc.setIcon(R.drawable.ic_arrow_downward_black_24dp_xxxhdpi);
                    break;
            }
        }
    }

    private void updatePreference(Preference preference, String key) {
        if (preference == null) return;
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setSummary(listPreference.getEntry());
            return;
        }
        if(!(preference instanceof CheckBoxPreference)) {
            SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
            preference.setSummary(sharedPreferences.getString(key, "Default"));
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
