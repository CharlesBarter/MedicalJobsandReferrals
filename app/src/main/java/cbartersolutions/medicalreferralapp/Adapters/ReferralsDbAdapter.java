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
public class ReferralsDbAdapter {

    private static String TAG = "ReferralsDbAdapter";

    private static final String DATABASE_NAME = "Referrals.db";
    private static final int DATABASE_VERSION = 1;

    public static final String REFERRALS_TABLE = "Referrals_table";

    //define column titles
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PATIENT_NAME = "patient_name";
    public static final String COLUMN_PATIENT_NHI = "patient_NHI";
    public static final String COLUMN_AGE_AND_SEX = "patient_age_and_sex";
    public static final String COLUMN_PATIENT_LOCATION = "patient_location";
    public static final String COLUMN_DATE_AND_TIME = "date_and_time_on_note";
    public static final String COLUMN_REFERRER_DETAILS = "referrer_details";
    public static final String COLUMN_REFERRER_CONTACT = "referrer_contact";
    public static final String COLUMN_DETAILS = "details";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DELETED = "deleted";
    public static final String COLUMN_DATE_CREATED = "date";

    private String[] allColumns = {COLUMN_ID, COLUMN_PATIENT_NAME, COLUMN_PATIENT_NHI,
            COLUMN_AGE_AND_SEX, COLUMN_PATIENT_LOCATION, COLUMN_DATE_AND_TIME,
            COLUMN_REFERRER_DETAILS, COLUMN_REFERRER_CONTACT,
            COLUMN_DETAILS,COLUMN_CATEGORY, COLUMN_DELETED, COLUMN_DATE_CREATED};

    public static final String REFERRALS_DATABASE_CREATE = "create table " + REFERRALS_TABLE + " ( "
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_PATIENT_NAME + " text not null, "
            + COLUMN_PATIENT_NHI + " text not null, "
            + COLUMN_AGE_AND_SEX + " text not null, "
            + COLUMN_PATIENT_LOCATION + " text not null, "
            + COLUMN_DATE_AND_TIME + " integer, "
            + COLUMN_REFERRER_DETAILS + " text not null, "
            + COLUMN_REFERRER_CONTACT + " text not null, "
            + COLUMN_DETAILS + " text not null, "
            + COLUMN_CATEGORY + " text not null, "
            + COLUMN_DELETED + " integer not null, "
            + COLUMN_DATE_CREATED + ");";

    private SQLiteDatabase sqlDB;
    private Context context;

    private ReferralsDbHelper referralsDbHelper;

    public ReferralsDbAdapter(Context ctx) {
        context = ctx;
    }

    public ReferralsDbAdapter open() throws android.database.SQLException {
        referralsDbHelper = new ReferralsDbHelper(context);
        sqlDB = referralsDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        referralsDbHelper.close();
    }


    //add new referral
    public Note createReferral(String patient_name, String patient_NHI, String patient_age_and_sex,
                               String patient_location,
                               long date_on_note,
                               String referrer_details, String referrer_contact,
                               String details, Note.Category category,
                               long deleted){
        ContentValues values = new ContentValues();
        values.put(COLUMN_PATIENT_NAME, patient_name);
        values.put(COLUMN_PATIENT_NHI, patient_NHI);
        values.put(COLUMN_AGE_AND_SEX, patient_age_and_sex);
        values.put(COLUMN_PATIENT_LOCATION, patient_location);
        values.put(COLUMN_DATE_AND_TIME, date_on_note);
        values.put(COLUMN_REFERRER_DETAILS, referrer_details);
        values.put(COLUMN_REFERRER_CONTACT, referrer_contact);
        values.put(COLUMN_DETAILS, details);
        values.put(COLUMN_CATEGORY, category.name());
        values.put(COLUMN_DELETED, deleted);
        values.put(COLUMN_DATE_CREATED, Calendar.getInstance().getTimeInMillis() + "");

        //insert the new values into the databse at a new position
        long insertId = sqlDB.insert(REFERRALS_TABLE, null, values);
        //query the new database entry and find the values
        Cursor cursor = sqlDB.query(REFERRALS_TABLE, allColumns, COLUMN_ID + " = " + insertId, null, null, null, null);
        //send the values to the note code to add to the Note Array by running cursor to Note on them
        cursor.moveToFirst();
        Note newNote = cursorToNote(cursor);
        cursor.close();
        return newNote;
    }

    //delete note
    public long deleteReferral(long idToDelete){
        return sqlDB.delete(REFERRALS_TABLE, COLUMN_ID + " = " + idToDelete, null);
    }

    //edit referrals
    public long updateReferral (long idToUpdate, String new_patient_name, String new_patient_NHI,
                                String new_patient_age_and_sex,
                                String patient_location, long new_date_on_note,
                                String new_referrer_details, String new_referrer_contact,
                                String new_details, Note.Category new_category,
                                int deleted){
        ContentValues values = new ContentValues();
        values.put(COLUMN_PATIENT_NAME, new_patient_name);
        values.put(COLUMN_PATIENT_NHI, new_patient_NHI);
        values.put(COLUMN_AGE_AND_SEX, new_patient_age_and_sex);
        values.put(COLUMN_PATIENT_LOCATION, patient_location);
        values.put(COLUMN_DATE_AND_TIME, new_date_on_note);
        values.put(COLUMN_REFERRER_DETAILS, new_referrer_details);
        values.put(COLUMN_REFERRER_CONTACT, new_referrer_contact);
        values.put(COLUMN_DETAILS, new_details);
        values.put(COLUMN_CATEGORY, new_category.name());
        values.put(COLUMN_DELETED, deleted);
        values.put(COLUMN_DATE_CREATED, Calendar.getInstance().getTimeInMillis() + "");

        //update the database with the new information
        return sqlDB.update(REFERRALS_TABLE, values, COLUMN_ID + " = "+ idToUpdate, null);
    }

    public long changeDeleteStatus(long idToUpdate, int deleted){
        ContentValues values = new ContentValues();
        values.put(COLUMN_DELETED, deleted);
        return  sqlDB.update(REFERRALS_TABLE, values, COLUMN_ID + " = " + idToUpdate, null);
    }

//    String orderByOverall = getOrderBy();


   //cycles through the database and creates notes to add to the Arraylist<Note> which is then used by
    //all the other code to populate the data
    public ArrayList<Note> getCurrentReferrals() {
        int is_deleted = 0;
//        String orderby = getOrderBy();
        //grab all off the information in our database for the notes in it
//        Cursor cursor = sqlDB.query(REFERRALS_TABLE, allColumns, null, null, null, null, null);
        Cursor cursor = sqlDB.query(REFERRALS_TABLE, allColumns, COLUMN_DELETED + " = " + is_deleted,
                null, null, null, getOrderBy());
        ArrayList<Note> notes = createNoteArray(cursor);
        cursor.close();
        return notes;
    }

    public ArrayList<Note> getCurrentReferralsNoHeaders(){
        int is_deleted = 0;
        ArrayList<Note> notes = new ArrayList<>();
        Cursor cursor = sqlDB.query(REFERRALS_TABLE, allColumns, COLUMN_DELETED + " = " + is_deleted,
                null,null,null, getOrderBy());
        for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()){
            notes.add(cursorToNote(cursor));
        }
        cursor.close();
        return notes;
    }

    public ArrayList<Note> getDeletedReferrals() {
        int is_deleted = 1;
        //grab all off the information in our database for the notes in it
        Cursor cursor = sqlDB.query(REFERRALS_TABLE, allColumns, COLUMN_DELETED + " = " + is_deleted,
                null, null, null, getOrderBy());
        ArrayList<Note> notes = createNoteArray(cursor);//creates a new list based on the cursor;
        cursor.close();
        return notes;
    }

    public ArrayList<Note> getDeletedReferralsNoHeaders(){
        int is_deleted = 1;
        ArrayList<Note> notes = new ArrayList<>();
        Cursor cursor = sqlDB.query(REFERRALS_TABLE, allColumns, COLUMN_DELETED + " = " + is_deleted,
                null, null, null, getOrderBy());
        for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()){
            notes.add(cursorToNote(cursor));
        }
        cursor.close();
        return notes;
    }

    //takes the data from the row the cursor variable is on in the database
    //and add them to the note array in the correct place
    private Note cursorToNote(Cursor cursor) {
        Note newNote = new Note(cursor.getString(1), cursor.getString(2), cursor.getString(3),
                cursor.getString(4), cursor.getLong(5), cursor.getString(6), cursor.getString(7),
                cursor.getString(8),
                Note.Category.valueOf(cursor.getString(9)), cursor.getLong(0), cursor.getLong(11));
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

    private static class ReferralsDbHelper extends SQLiteOpenHelper {

        ReferralsDbHelper(Context ctx) {
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(REFERRALS_DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(ReferralsDbHelper.class.getName(),
                    "Upgrading database from version" + oldVersion + " to "
                            + newVersion + ". which wil destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS" + REFERRALS_TABLE);
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
