package com.tabian.saveanddisplaysql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_NAME = "USER";

    private static final String COL1 = "ID";
    private static final String COL2 = "name";
    private static final String COL3 = "email";
    private static final String COL4 = "password";


    public UserDatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY, " +
                COL2 +" TEXT," + COL3 + " TEXT," + COL4 + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String name, String email, Integer id, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, id);
        contentValues.put(COL2, name);
        contentValues.put(COL3, email);
        contentValues.put(COL4, password);

        Log.d(TAG, "addData: Adding " + name + " and " + email + " to " + TABLE_NAME);

        long result = db.insert(TABLE_NAME, null, contentValues);

        //if date as inserted incorrectly it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }
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

    public Cursor getUserID(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + COL1 + " FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public void updateUser(String newName, int id, String newEmail, String newPass){
        SQLiteDatabase db = this.getWritableDatabase();
        String queryName = "UPDATE " + TABLE_NAME + " SET " + COL2 +
                " = '" + newName + "' WHERE " + COL1 + " = '" + id + "'";

        String queryEmail = "UPDATE " + TABLE_NAME + " SET " + COL3 +
                " = '" + newEmail + "' WHERE " + COL1 + " = '" + id + "'";
        String queryPass = "UPDATE " + TABLE_NAME + " SET " + COL4 +
                " = '" + newPass + "' WHERE " + COL1 + " = '" + id + "'";
        Log.d(TAG, "updateName: query: " + queryName);
        Log.d(TAG, "and updateEmail: query: " + queryEmail);
        Log.d(TAG, "and updatePass: query: " + queryPass);

        db.execSQL(queryName);
        db.execSQL(queryEmail);
        db.execSQL(queryPass);
    }
}
