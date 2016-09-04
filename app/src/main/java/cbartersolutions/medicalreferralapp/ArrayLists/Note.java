package cbartersolutions.medicalreferralapp.ArrayLists;

import cbartersolutions.medicalreferralapp.R;

/**
 * Created by Charles on 12/07/2016.
 */
public class Note {
    private String patientname, patientNHI, patient_Age_Sex, patient_location,
            referrerName, referrerContact, details, is_header, header_string;
    private long noteId, date_and_time, dateCreatedMilli;
    private Category category;

    public enum Category{ HIGHIMPORTANCE, MODERATEIMPORTANCE, Z_LOWIMPORTANCE}

    public Note(){}

    public Note(String patientname, String patientNHI, String patient_Age_Sex,
                String patient_location, long date_and_time,
                String details, Category category,
                long noteId, long dateCreatedMilli){
        this.patientname = patientname;
        this.patientNHI = patientNHI;
        this.patient_Age_Sex = patient_Age_Sex;
        this.patient_location = patient_location;
        this.date_and_time = date_and_time;
        this.referrerName = "";
        this.referrerContact = "";
        this.details = details;
        this.category = category;
        this.noteId = noteId;
        this.dateCreatedMilli = dateCreatedMilli;
    }

    public Note(String patientname, String patientNHI, String patient_Age_Sex,
                String patient_location, long date_and_time,
                String referrerName, String referrerContact,
                String details, Category category, long noteId, long dateCreatedMilli){
        this(patientname, patientNHI, patient_Age_Sex, patient_location, date_and_time,
                details, category, noteId, dateCreatedMilli);
        this.referrerName = referrerName;
        this.referrerContact = referrerContact;
    }

    public String getPatientname(){
        return patientname;
    }

    public String getPatientNHI (){
        return patientNHI;
    }

    public String getPatient_Age_Sex () {return patient_Age_Sex; }

    public String getPatient_location () {return patient_location; }

    public long get_date_and_time() {return date_and_time; }

    public String getReferrerName(){
        return referrerName;
    }

    public String getReferrerContact(){
        return referrerContact;
    }

    public String getdetails (){
        return details;
    }

    public Category getCategory (){
      return category;
    };

    public long getNoteId () {
        return noteId;
    }

    public long getDateCreatedMilli (){
        return dateCreatedMilli;
    }

    public String toString() {
        return "ID: " + noteId + "Patient Name: " + patientname + "NHI:" + patientNHI
                + "Patient Age/Sex" + patient_Age_Sex
                + "Patient location" + patient_location
                + "date and time" + date_and_time
                + "Referrer Name" + referrerName + "Referrer Contact" + referrerContact
                + "Details: " + details + "IconID: " + category.name() + " Date: ";
    }

    public int getAssociatedDrawable(){
        return categoryToDrawable(category);
    }

    public static int categoryToDrawable(Category noteCategory){
        switch(noteCategory){
            case HIGHIMPORTANCE:
                return R.drawable.ic_priority_high_xhdpi;
            case MODERATEIMPORTANCE:
                return R.drawable.ic_priority_medium_xhdpi;
            case Z_LOWIMPORTANCE:
                return R.drawable.ic_priority_low_xhdpi;
        }
        return R.drawable.ic_priority_high_xhdpi;
    }

    public static int categorytoInteger(Category noteCategory){
        switch(noteCategory){
            case HIGHIMPORTANCE:
                return 0;
            case MODERATEIMPORTANCE:
                return 1;
            case Z_LOWIMPORTANCE:
                return 2;
        }
        return 0;
    }


}
