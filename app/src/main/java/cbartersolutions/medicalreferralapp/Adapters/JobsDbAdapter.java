package cbartersolutions.medicalreferralapp.Adapters;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import cbartersolutions.medicalreferralapp.ArrayLists.Header;
import cbartersolutions.medicalreferralapp.ArrayLists.Note;

/**
 * Created by Charles on 17/07/2016.
 */
public class JobsDbAdapter {

    private static String TAG = "JobsDbAdapter";

    private static final String DATABASE_NAME = "Jobs.db";
    private static final int DATABASE_VERSION = 2;

    public static final String JOBS_TABLE = "Jobs_table";

    //define column titles
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PATIENT_NAME = "patient_name";
    public static final String COLUMN_PATIENT_NHI = "patient_NHI";
    public static final String COLUMN_PATIENT_AGE_AND_SEX = "patient_Age_Sex";
    public static final String COLUMN_PATIENT_LOCATION = "patient_location";
    public static final String COLUMN_DATE_AND_TIME = "date_and_time_on_note";
    public static final String COLUMN_DETAILS = "details";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DELETED = "deleted";
    public static final String COLUMN_DATE_CREATED = "date";

    private String[] allColumns = {COLUMN_ID, COLUMN_PATIENT_NAME, COLUMN_PATIENT_NHI,
            COLUMN_PATIENT_AGE_AND_SEX, COLUMN_PATIENT_LOCATION, COLUMN_DATE_AND_TIME,
            COLUMN_DETAILS, COLUMN_CATEGORY, COLUMN_DELETED, COLUMN_DATE_CREATED};

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
            + COLUMN_DATE_CREATED + ");";

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
            values.put(COLUMN_DATE_CREATED, Calendar.getInstance().getTimeInMillis() + "");

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
        values.put(COLUMN_DATE_CREATED, date);

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
        values.put(COLUMN_DATE_CREATED, date);

        //update the database with the new information
        return sqlDB.update(JOBS_TABLE, values, COLUMN_ID + " = " + idToUpdate, null);
    }

    public long changeJobsDeletedStatus (long idToUpdate, int deleted){
        ContentValues values = new ContentValues();
        values.put(COLUMN_DELETED, deleted);
        return sqlDB.update(JOBS_TABLE, values, COLUMN_ID + " = " + idToUpdate, null);
    }

    public ArrayList<Note> getAllJobs() {
        ArrayList<Note> notes = new ArrayList<>();

        //grab all off the information in our database for the notes in it
        Cursor cursor = sqlDB.query(JOBS_TABLE, allColumns, null, null, null, null, getOrderBy());

        for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {
            Note note = cursorToNote(cursor);
            notes.add(note);
        }

        cursor.close();
        return notes;
    }

    String desc = " DESC";
    String asc = " ASC";
    String add = ",";
    String orderByCategory = COLUMN_CATEGORY;
    String orderByTime_DESC = COLUMN_DATE_CREATED + " DESC";
    String orderByTime_ASC = COLUMN_DATE_CREATED + " ASC";
    String orderByLocation = COLUMN_PATIENT_LOCATION;

    String orderByOverall = orderByLocation + asc + add + orderByCategory + desc;

    public ArrayList<Note> getNonDeletedJobs() {
        int is_deleted = 0;
        Log.d(TAG, "getNonDeletedJobs: " + orderByOverall);
        //grab all off the information in our database for the notes in it
        Cursor cursor = sqlDB.query(JOBS_TABLE, allColumns, COLUMN_DELETED + " = " + is_deleted,
                null, null, null, getOrderBy());
       ArrayList<Note> notes = createNoteArray(cursor);
        cursor.close();
        return notes;
    }

    public ArrayList<Note> getNonDeletedJobsNoHeaders() {
        ArrayList<Note> notes = new ArrayList<>();
        int is_deleted = 0;
        //grab all off the information in our database for the notes in it
        Cursor cursor = sqlDB.query(JOBS_TABLE, allColumns, COLUMN_DELETED + " = " + is_deleted,
                null, null, null, getOrderBy());
        for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {
            notes.add(cursorToNote(cursor));
        }
        cursor.close();
        return notes;
    }

    public ArrayList<Note> getDeletedJobs(){
        int deleted = 1;
        Cursor cursor = sqlDB.query(JOBS_TABLE, allColumns, COLUMN_DELETED + " = " + deleted,
                null,null,null,getOrderBy());
        ArrayList<Note> notes = createNoteArray(cursor);
        cursor.close();
        return notes;
    }

    public ArrayList<Note> getDeletedJobsNoHeaders(){
        ArrayList<Note> notes = new ArrayList<>();
        int deleted = 1;
        Cursor cursor = sqlDB.query(JOBS_TABLE, allColumns, COLUMN_DELETED + " = " + deleted,
                null,null,null,getOrderBy());
        for(cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()){
            notes.add(cursorToNote(cursor));
        }
        cursor.close();
        return notes;
    }

    private Note cursorToNote(Cursor cursor) {
        Note newNote;
        newNote = new Note(cursor.getString(1), cursor.getString(2), cursor.getString(3),
                cursor.getString(4),
                cursor.getLong(5), cursor.getString(6),
                Note.Category.valueOf(cursor.getString(7)), cursor.getLong(0), cursor.getLong(9));
        return newNote;
    }


    public ArrayList<Note> createNoteArray(Cursor cursor){

        ArrayList<Note> notes = new ArrayList<>();

        String blank_header_name = "No Location";

        String header_item_to_check = "header to check against";
        for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {
            String current_header_item = "";
            //change current header item depending on sort option
            switch (first_sort_preference){
                case COLUMN_PATIENT_LOCATION:
                    current_header_item = cursor.getString(4); //in this case headers are locations
                    if(current_header_item.contains(" ")){
                        current_header_item = current_header_item.substring(0, current_header_item.indexOf(" "));
                    }
                    break;
                case COLUMN_CATEGORY:
                    current_header_item = cursor.getString(9);//get the category as a string
                    current_header_item = current_header_item
                            .replace("IMPORTANCE","");//remove IMPORTANCE from the category names
                    current_header_item = current_header_item.substring(0,1) +
                            current_header_item.substring(1,current_header_item.length())
                                    .toLowerCase();//change the categories to lower case
                    break;
                case COLUMN_DATE_AND_TIME:
                    Calendar myCalendar = Calendar.getInstance();
                    myCalendar.setTimeInMillis(cursor.getLong(5));
                    SimpleDateFormat date_format = new SimpleDateFormat("E, d MMM", Locale.ENGLISH);
                    current_header_item = date_format.format(myCalendar.getTime());
                    break;
            }
            Log.d(TAG, "createNoteArray: " + current_header_item);

            if(!header_item_to_check.equals(current_header_item)) {
                if(current_header_item.equals("")){
                    notes.add(new Header(blank_header_name));//for locations
                }else {
                    notes.add(new Header(current_header_item));
                }
                header_item_to_check = current_header_item;
                notes.add(cursorToNote(cursor));
            }else {
                Note note = cursorToNote(cursor);
                notes.add(note);
            }
        }
        return notes;
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

    private String first_sort_preference;

    public String getOrderBy() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        first_sort_preference = sharedPreferences.getString("FIRST_SORT_PREFERENCE", COLUMN_PATIENT_LOCATION);
        String asc_desc = sharedPreferences.getString("ASC_DESC", " ASC");
        String sort_preference = first_sort_preference + " " + asc_desc;
        if(first_sort_preference.equals(COLUMN_DATE_AND_TIME)){
            sort_preference = "strftime('%Y-%m-%d', " + COLUMN_DATE_AND_TIME + " /1000, 'unixepoch' )" +
                    asc_desc + ", " + COLUMN_CATEGORY + " DESC";
        }else if(!first_sort_preference.equals("category")){
            sort_preference = sort_preference + ", " + COLUMN_CATEGORY + " DESC";
        }
        return sort_preference;
    }

}
