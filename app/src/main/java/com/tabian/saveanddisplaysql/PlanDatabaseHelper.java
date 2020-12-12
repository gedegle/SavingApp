package com.tabian.saveanddisplaysql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class PlanDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_NAME = "USER_PLAN";

    private static final String COL1 = "ID";
    private static final String COL2 = "date";
    private static final String COL3 = "sum";
    private static final String COL4 = "income";
    private static final String COL5 = "status";
    private static final String COL6 = "if_saved";
    private static final String COL7 = "goal";


    public PlanDatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY, " +
                COL2 +" TEXT," + COL3 + " TEXT," + COL4 + " TEXT," + COL5 + " TEXT," + COL6 + " TEXT," + COL7 +" TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(Integer id, String date, Integer sum, Integer income, Integer status, Integer if_saved, String goal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, id);
        contentValues.put(COL2, date);
        contentValues.put(COL3, sum);
        contentValues.put(COL4, income);
        contentValues.put(COL5, status);
        contentValues.put(COL6, if_saved);
        contentValues.put(COL7, goal);



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

    /**
     * Returns only the ID that matches the name passed in
     * @param id
     * @return
     */
    public Cursor getItemID(Integer id){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + COL1 + " FROM " + TABLE_NAME +
                " WHERE " + COL1 + " = '" + id + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public void updatePlan(Integer id, String date, Integer sum, Integer income, Integer status, Integer if_saved, String goal){
        SQLiteDatabase db = this.getWritableDatabase();
        String queryDate = "UPDATE " + TABLE_NAME + " SET " + COL2 +
                " = '" + date + "'" + "' WHERE " + COL1 + " = '" + id + "'";
        String querySum = "UPDATE " + TABLE_NAME + " SET " + COL3 +
                " = '" + sum + "'" + "' WHERE " + COL1 + " = '" + id + "'";
        String queryIncome = "UPDATE " + TABLE_NAME + " SET " + COL4 +
                " = '" + income + "'" + "' WHERE " + COL1 + " = '" + id + "'";
        String queryStatus = "UPDATE " + TABLE_NAME + " SET " + COL5 +
                " = '" + status + "'" + "' WHERE " + COL1 + " = '" + id + "'";
        String querySaved = "UPDATE " + TABLE_NAME + " SET " + COL6 +
                " = '" + if_saved + "'" + "' WHERE " + COL1 + " = '" + id + "'";
        String queryGoal = "UPDATE " + TABLE_NAME + " SET " + COL7 +
                " = '" + goal + "'" + "' WHERE " + COL1 + " = '" + id + "'";

        db.execSQL(queryDate);
        db.execSQL(querySum);
        db.execSQL(queryIncome);
        db.execSQL(queryStatus);
        db.execSQL(querySaved);
        db.execSQL(queryGoal);
    }

    public void deletePlan(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE "
                + COL1 + " = '" + id + "'";
        Log.d(TAG, "deleteName: query: " + query);
        Log.d(TAG, "deleteName: Deleting " + id + " from database.");
        db.execSQL(query);
    }
}
