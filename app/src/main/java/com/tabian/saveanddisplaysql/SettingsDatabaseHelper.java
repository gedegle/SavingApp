package com.tabian.saveanddisplaysql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SettingsDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_NAME = "SETTINGS";

    private static final String COL1 = "ID";
    private static final String COL2 = "status";
    private static final String COL3 = "repeat_time";


    public SettingsDatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY, " +
                COL2 +" TEXT," + COL3 + " TEXT)";
        db.execSQL(createTable);

    }

    public boolean addData() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, 0);
        contentValues.put(COL2, "1");
        contentValues.put(COL3, "12");

        long result = db.insert(TABLE_NAME, null, contentValues);
        //if date as inserted incorrectly it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    /**
     * Returns all the data from database
     * @return
     */
    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public void updateSettings(Integer status, double repeat_time){
        SQLiteDatabase db = this.getWritableDatabase();
        String queryStatus = "UPDATE " + TABLE_NAME + " SET " + COL2 +
                " = '" + status + "' WHERE " + COL1 + " = '" + 0 + "'";

        String queryRepeat = "UPDATE " + TABLE_NAME + " SET " + COL3 +
                " = '" + repeat_time + "' WHERE " + COL1 + " = '" + 0 + "'";

        Log.d(TAG, "updateName: query: " + queryStatus);
        Log.d(TAG, "and updateEmail: query: " + queryRepeat);

        db.execSQL(queryStatus);
        db.execSQL(queryRepeat);
    }
}
