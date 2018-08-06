package com.example.ccs.maincrabproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "crabs";

    // Contacts Table Columns names
    private static final String CRAB_ID = "id";
    private static final String CLASSIFIER = "classify";
    private static final String CLAS_PROB = "name";
    private static final String FILE_URL = "url";

    // Contacts table name
    private static final String CRAB_DETAIL = "crabdetails";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_CRAB_DETAIL_TABLE = "CREATE TABLE " + CRAB_DETAIL + "("
                + CRAB_ID + " INTEGER PRIMARY KEY,"
                + CLASSIFIER + " TEXT,"
                + FILE_URL + " TEXT,"
                + CLAS_PROB + " REAL " + ")";

        sqLiteDatabase.execSQL(CREATE_CRAB_DETAIL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CRAB_DETAIL);

        // Create tables again
        onCreate(sqLiteDatabase);

    }

    // Adding new Student Information
    void addNewStudent(CrabModel newCrab) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(CLASSIFIER, newCrab.getClassifier());
        values.put(FILE_URL, newCrab.getFileurl());
        values.put(CLAS_PROB, newCrab.getProbval());


        // Inserting Row
        db.insert(CRAB_DETAIL, null, values);
        db.close(); // Closing database connection
    }


}
