package cbartersolutions.medicalreferralapp.Adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

import cbartersolutions.medicalreferralapp.Others.Note;

/**
 * Created by Charles on 17/07/2016.
 */
public class JobsDbAdapter {
    private static final String DATABASE_NAME = "Jobs.db";
    private static final int DATABASE_VERSION = 2;

    public static final String JOBS_TABLE = "Jobs_table";

    //define column titles
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PATIENT_NAME = "patient_name";
    public static final String COLUMN_PATIENT_NHI = "patient_NHI";
    public static final String COLUMN_PATIENT_AGE_AND_SEX = "patient_Age_Sex";
    public static final String COLUMN_PATIENT_LOCATION = "patient_location";
    public static final String COLUMN_DATE_AND_TIME = "date_and_time";
    public static final String COLUMN_DETAILS = "details";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DELETED = "deleted";
    public static final String COLUMN_DATE = "date";

    private String[] allColumns = {COLUMN_ID, COLUMN_PATIENT_NAME, COLUMN_PATIENT_NHI,
            COLUMN_PATIENT_AGE_AND_SEX, COLUMN_PATIENT_LOCATION, COLUMN_DATE_AND_TIME,
            COLUMN_DETAILS, COLUMN_CATEGORY, COLUMN_DELETED, COLUMN_DATE};

    public static final String JOBS_DATABASE_CREATE = "create table " + JOBS_TABLE + " ( "
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_PATIENT_NAME + " text not null, "
            + COLUMN_PATIENT_NHI + " text not null, "
            + COLUMN_PATIENT_AGE_AND_SEX + " text not null, "
            + COLUMN_PATIENT_LOCATION + " text not null, "
            + COLUMN_DATE_AND_TIME + " integer, "
            + COLUMN_DETAILS + " text not null, "
            + COLUMN_CATEGORY + " text not null, "
            + COLUMN_DELETED + " integer not null, "
            + COLUMN_DATE + ");";

    private SQLiteDatabase sqlDB;
    private Context context;

    private JobsDbHelper jobsDbHelper;

    public JobsDbAdapter(Context ctx) {
        context = ctx;
    }

    public JobsDbAdapter open() throws android.database.SQLException {
        jobsDbHelper = new JobsDbHelper(context);
        sqlDB = jobsDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        jobsDbHelper.close();
    }

    public Note createJob(String patient_name, String patient_NHI, String patient_Age_Sex, String patient_location,
                          long date_and_time,
                          String details, Note.Category category, int deleted) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_PATIENT_NAME, patient_name);
            values.put(COLUMN_PATIENT_NHI, patient_NHI);
            values.put(COLUMN_PATIENT_AGE_AND_SEX, patient_Age_Sex);
            values.put(COLUMN_PATIENT_LOCATION, patient_location);
            values.put(COLUMN_DATE_AND_TIME, date_and_time);
            values.put(COLUMN_DETAILS, details);
            values.put(COLUMN_CATEGORY, category.name());
            values.put(COLUMN_DELETED, deleted);
            values.put(COLUMN_DATE, Calendar.getInstance().getTimeInMillis() + "");

            long insertId = sqlDB.insert(JOBS_TABLE, null, values);
            Cursor cursor = sqlDB.query(JOBS_TABLE, allColumns, COLUMN_ID + " = " + insertId, null, null, null, null);

            cursor.moveToFirst();
            Note newNote = cursorToNote(cursor);
            cursor.close();
            return newNote;
        }

    public Note recreateNote (String patient_name, String patient_NHI,String patient_Age_Sex,
                              String patient_location,
                              long date_and_time, String details,
                              Note.Category category, int deleted, long date){
        ContentValues values = new ContentValues();
        values.put(COLUMN_PATIENT_NAME, patient_name);
        values.put(COLUMN_PATIENT_NHI, patient_NHI);
        values.put(COLUMN_PATIENT_AGE_AND_SEX, patient_Age_Sex);
        values.put(COLUMN_PATIENT_LOCATION, patient_location);
        values.put(COLUMN_DATE_AND_TIME, date_and_time);
        values.put(COLUMN_DETAILS, details);
        values.put(COLUMN_CATEGORY, category.name());
        values.put(COLUMN_DELETED, deleted);
        values.put(COLUMN_DATE, date);

        long insertId = sqlDB.insert(JOBS_TABLE, null, values);
        Cursor cursor = sqlDB.query(JOBS_TABLE, allColumns, COLUMN_ID + " = " + insertId, null, null, null, null);

        cursor.moveToFirst();
        Note newNote = cursorToNote(cursor);
        cursor.close();
        return newNote;
    }

    //delete notes
    public long deleteJob(long idToDelete){
        return sqlDB.delete(JOBS_TABLE, COLUMN_ID + " = " + idToDelete, null);
    }

    //edit jobs
    public long updateJob (long idToUpdate, String new_patient_name, String new_patient_NHI,
                           String new_patient_age_and_sex, String patient_location,
                           long new_date_and_time, String new_details,
                           Note.Category new_category, int deleted, Long date){
        ContentValues values = new ContentValues();
        values.put(COLUMN_PATIENT_NAME, new_patient_name);
        values.put(COLUMN_PATIENT_NHI, new_patient_NHI);
        values.put(COLUMN_PATIENT_AGE_AND_SEX, new_patient_age_and_sex);
        values.put(COLUMN_PATIENT_LOCATION, patient_location);
        values.put(COLUMN_DATE_AND_TIME, new_date_and_time);
        values.put(COLUMN_DETAILS, new_details);
        values.put(COLUMN_CATEGORY, new_category.name());
        values.put(COLUMN_DELETED, deleted);
        values.put(COLUMN_DATE, date);

        //update the database with the new information
        return sqlDB.update(JOBS_TABLE, values, COLUMN_ID + " = " + idToUpdate, null);
    }

    public ArrayList<Note> getAllJobs() {
        ArrayList<Note> notes = new ArrayList<>();

        String add = ", ";
        String orderByCategory_DESC = COLUMN_CATEGORY + " DESC";
        String orderByTime_DESC = COLUMN_DATE + " DESC";
        String orderByTime_ASC = COLUMN_DATE + " ASC";

        String orderBy = orderByCategory_DESC;

        //grab all off the information in our database for the notes in it
        Cursor cursor = sqlDB.query(JOBS_TABLE, allColumns, null, null, null, null, orderBy);

        for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {
            Note note = cursorToNote(cursor);
            notes.add(note);
        }

        cursor.close();
        return notes;
    }


    String orderByCategory_DESC = COLUMN_CATEGORY + " DESC";
    String orderByTime_DESC = COLUMN_DATE + " DESC";
    String orderByTime_ASC = COLUMN_DATE + " ASC";

    public ArrayList<Note> getNonDeletedJobs() {
        ArrayList<Note> notes = new ArrayList<>();

        String add = ", ";

        String orderBy = orderByCategory_DESC;

        int is_deleted = 0;

        //grab all off the information in our database for the notes in it
        Cursor cursor = sqlDB.query(JOBS_TABLE, allColumns, COLUMN_DELETED + " = " + is_deleted,
                null, null, null, orderBy);

        for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {
            Note note = cursorToNote(cursor);
            notes.add(note);
        }

        cursor.close();
        return notes;
    }

    public ArrayList<Note> getDeleteJobs(){
        ArrayList<Note> notes = new ArrayList<>();

        String test = "test";
        int deleted = 1;

        String orderBy = orderByCategory_DESC;

        Cursor cursor = sqlDB.query(JOBS_TABLE, allColumns, COLUMN_DELETED + " = " + deleted,
                null,null,null,orderBy);

//        Cursor cursor = sqlDB.rawQuery(query_deleted, null);

        for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {
            Note note = cursorToNote(cursor);
            notes.add(note);
        }

        cursor.close();
        return notes;
    }

    private Note cursorToNote(Cursor cursor) {
        Note newNote = new Note(cursor.getString(1), cursor.getString(2), cursor.getString(3),
                cursor.getString(4),
                cursor.getLong(5), cursor.getString(6),
                Note.Category.valueOf(cursor.getString(7)), cursor.getLong(0), cursor.getLong(9));
        return newNote;
    }

    private static class JobsDbHelper extends SQLiteOpenHelper {

        JobsDbHelper(Context ctx) {
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(JOBS_DATABASE_CREATE);
//            db.execSQL(ReferralsDbAdapter.REFERRALS_DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(JobsDbHelper.class.getName(),
                    "Upgrading database from version" + oldVersion + " to "
                            + newVersion + ". which wil destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + JOBS_TABLE);
            onCreate(db);
        }
    }
}
