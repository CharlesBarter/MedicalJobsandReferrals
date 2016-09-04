package cbartersolutions.medicalreferralapp.Others;

import android.content.Context;
import android.content.Intent;

import cbartersolutions.medicalreferralapp.Activities.MainActivity;
import cbartersolutions.medicalreferralapp.Adapters.JobsDbAdapter;
import cbartersolutions.medicalreferralapp.Adapters.ReferralsDbAdapter;
import cbartersolutions.medicalreferralapp.ArrayLists.Note;

/**
 * Created by Charles on 22/08/2016.
 */
public class AlteringDatabase {

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
        jobsDbAdapter.changeJobsDeletedStatus(noteId, deleted);
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
                ReferralsDbAdapter referralsDbAdapter = new ReferralsDbAdapter(mContext);
                referralsDbAdapter.open();
                referralsDbAdapter.deleteReferral(noteId);
                referralsDbAdapter.close();
        }
    }


    public void updateReferral (Intent data, int is_deleted){

        ReferralsDbAdapter referralsDbAdapter = new ReferralsDbAdapter(mContext);
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
                is_deleted
        );
        referralsDbAdapter.close();
    }


    public void permanentlyDeleteReferral(Long noteId){
        ReferralsDbAdapter referralsDbAdapter = new ReferralsDbAdapter(mContext);
        referralsDbAdapter.open();
        referralsDbAdapter.deleteReferral(noteId);
        referralsDbAdapter.close();
    }

    public void changeReferralDeletedStatus(long noteId, int deleted){
        ReferralsDbAdapter referralsDbAdapter = new ReferralsDbAdapter(mContext);
        referralsDbAdapter.open();
        referralsDbAdapter.changeDeleteStatus(noteId, deleted);
        referralsDbAdapter.close();
    }

}
