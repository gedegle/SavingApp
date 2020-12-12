package com.tabian.saveanddisplaysql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class PostsDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_NAME = "POSTS";

    private static final String COL1 = "ID";
    private static final String COL2 = "date";
    private static final String COL3 = "sum";
    private static final String COL4 = "type";


    public PostsDatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY, " +
                COL2 +" TEXT," + COL3 + " TEXT, " + COL4 + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(Integer id, String date, Integer sum, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, id);
        contentValues.put(COL2, date);
        contentValues.put(COL3, sum);
        contentValues.put(COL4, type);

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
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY date desc ";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public Cursor getTopTheree() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT type, sum FROM " + TABLE_NAME +
                " ORDER BY sum desc LIMIT 3";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public Cursor spentInAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT SUM(sum) total FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public Cursor getCount(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    /**
     * Returns only the ID that matches the name passed in
     * @param id
     * @return
     */
    public Cursor getItemID(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + COL1 + " FROM " + TABLE_NAME +
                " WHERE " + COL1 + " = '" + id + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public boolean updatePost(Integer id, String date, Integer sum, String type){
        SQLiteDatabase db = this.getWritableDatabase();
        String queryDate = "UPDATE " + TABLE_NAME + " SET " + COL2 +
                " = '" + date + "'"+ " WHERE " + COL1 + " = '" + id + "'";
        String querySum = "UPDATE " + TABLE_NAME + " SET " + COL3 +
                " = '" + sum + "'"+ " WHERE " + COL1 + " = '" + id + "'";
        String queryType = "UPDATE " + TABLE_NAME + " SET " + COL4 +
                " = '" + type + "'"+ " WHERE " + COL1 + " = '" + id + "'";
        try {
            Log.d(TAG, "updateName: query: " + queryDate);
            Log.d(TAG, "and updateEmail: query: " + querySum);
            Log.d(TAG, "and updatePass: query: " + queryType);
            db.execSQL(queryDate);
            db.execSQL(querySum);
            db.execSQL(queryType);

            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public void deletePost(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE "
                + COL1 + " = '" + id + "'";
        Log.d(TAG, "deleteName: query: " + query);
        Log.d(TAG, "deleteName: Deleting " + id + " from database.");
        db.execSQL(query);
    }
}
