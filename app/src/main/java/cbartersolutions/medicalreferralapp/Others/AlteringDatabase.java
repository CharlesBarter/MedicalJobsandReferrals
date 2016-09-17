package cbartersolutions.medicalreferralapp.Others;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import cbartersolutions.medicalreferralapp.Activities.MainActivity;
import cbartersolutions.medicalreferralapp.Adapters.JobsDbAdapter;
import cbartersolutions.medicalreferralapp.Adapters.NotesDbAdapter;
import cbartersolutions.medicalreferralapp.ArrayLists.Note;

/**
 * Created by Charles on 22/08/2016.
 * Altering Database
 */
public class AlteringDatabase {

    private String TAG = "AlteringDatabase";
    Context mContext;

    public AlteringDatabase (Context context){
        this.mContext = context;
    }

    public void updateJob (Intent data, int deleted){
        JobsDbAdapter jobsDbAdapter = new JobsDbAdapter(mContext);
        jobsDbAdapter.open();
        jobsDbAdapter.updateJob(
                data.getExtras().getLong(MainActivity.NOTE_ID),
                data.getExtras().getString(MainActivity.NOTE_PATIENT_NAME) + "",
                data.getExtras().getString(MainActivity.NOTE_PATIENT_NHI) + "",
                data.getExtras().getString(MainActivity.NOTE_PATIENT_AGE_AND_SEX) + "",
                data.getExtras().getString(MainActivity.NOTE_PATIENT_LOCATION),
                data.getExtras().getLong(MainActivity.NOTE_DATE_AND_TIME),
                data.getExtras().getString(MainActivity.NOTE_DETAILS) + "",
                (Note.Category) data.getSerializableExtra(MainActivity.NOTE_CATEGORY),
                deleted,
                data.getExtras().getLong(MainActivity.NOTE_DATE_CREATED));
        jobsDbAdapter.close();
    }

    public void permanentlyDeleteJob(long noteId, int rowPosition){
        //delete the note currently shown
        JobsDbAdapter jobsDbAdapter = new JobsDbAdapter(mContext);
        jobsDbAdapter.open();
        jobsDbAdapter.deleteJob(noteId);
        jobsDbAdapter.close();
    }

    public void changeJobDeletedStatus(long noteId, int deleted){
        JobsDbAdapter jobsDbAdapter = new JobsDbAdapter(mContext);
        jobsDbAdapter.open();
        jobsDbAdapter.changeDeleteStatus(noteId, deleted);
        jobsDbAdapter.close();
    }

    public void permanentDeleteNote(MainActivity.TypeofNote typeofNote, Long noteId, int position){        switch (typeofNote){
            case JOB:
                JobsDbAdapter jobsDbAdapter = new JobsDbAdapter(mContext);
                jobsDbAdapter.open();
                jobsDbAdapter.deleteJob(noteId);
                jobsDbAdapter.close();
                break;
            case REFERRAL:
                NotesDbAdapter referralsDbAdapter = new NotesDbAdapter(mContext);
                referralsDbAdapter.open();
                referralsDbAdapter.deleteReferral(noteId);
                referralsDbAdapter.close();
        }
    }


    public void updateReferral (Intent data, int is_deleted){
        NotesDbAdapter referralsDbAdapter = new NotesDbAdapter(mContext);
        referralsDbAdapter.open();
        referralsDbAdapter.updateReferral(
                data.getExtras().getLong(MainActivity.NOTE_ID),
                data.getExtras().getString(MainActivity.NOTE_PATIENT_NAME) + "",
                data.getExtras().getString(MainActivity.NOTE_PATIENT_NHI) + "",
                data.getExtras().getString(MainActivity.NOTE_PATIENT_AGE_AND_SEX) + "",
                data.getExtras().getString(MainActivity.NOTE_PATIENT_LOCATION),
                data.getExtras().getLong(MainActivity.NOTE_DATE_AND_TIME),
                data.getExtras().getString(MainActivity.NOTE_REFERRER_NAME),
                data.getExtras().getString(MainActivity.NOTE_REFERRER_CONTACT),
                data.getExtras().getString(MainActivity.NOTE_DETAILS) + "",
                (Note.Category) data.getSerializableExtra(MainActivity.NOTE_CATEGORY),
                (MainActivity.TypeofNote) data.getSerializableExtra(MainActivity.NOTE_TYPE),
                is_deleted
        );
        referralsDbAdapter.close();
    }

    public void permanentlyDeleteReferral(Long noteId){
        NotesDbAdapter referralsDbAdapter = new NotesDbAdapter(mContext);
        referralsDbAdapter.open();
        referralsDbAdapter.deleteReferral(noteId);
        referralsDbAdapter.close();
    }

    int count = 0;

    public void changeReferralDeletedStatus(long noteId, int deleted){
        NotesDbAdapter referralsDbAdapter = new NotesDbAdapter(mContext);
        referralsDbAdapter.open();
        referralsDbAdapter.changeDeleteStatus(noteId, deleted);
        count = count + 1;
        Log.d(TAG, "changeReferralDeletedStatus: " + count);
        referralsDbAdapter.close();
    }

}
