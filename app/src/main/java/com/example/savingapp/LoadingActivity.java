package com.example.savingapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tabian.saveanddisplaysql.PlanDatabaseHelper;
import com.tabian.saveanddisplaysql.UserDatabaseHelper;

public class LoadingActivity extends AppCompatActivity {
    UserDatabaseHelper userDb;
    PlanDatabaseHelper planDb;
    boolean allowGoingBack = false;
    int timesClicked = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_loading);
        userDb = new UserDatabaseHelper(this);
        planDb = new PlanDatabaseHelper(this);

        Cursor userQueried = userDb.getData();
        Cursor planQueried = planDb.getData();
        if (userQueried.moveToFirst() && planQueried.moveToFirst()) {
            openHome();
        } else if (userQueried.moveToFirst() && !planQueried.moveToFirst()){
            openNewPlan();
        } else {
            openSignUp();
        }
    }

    @Override
    public void onBackPressed() {
        timesClicked++;
        Handler handler = new Handler();
        Runnable r = new Runnable() {

            @Override
            public void run() {
                timesClicked = 0;
            }
        };

        if (timesClicked == 1 && !allowGoingBack) {
            //Single click
            handler.postDelayed(r, 250);
            Toast.makeText(LoadingActivity.this, "Press again to exit", Toast.LENGTH_LONG).show();


        } else if (timesClicked == 2) {
            //Double click
            timesClicked = 0;
            finishAffinity();
        }

        if (allowGoingBack) {
            super.onBackPressed();
        } else {
            Log.d("Back", "Back pressed in phone");
        }
    }

    public void openNewPlan() {
        Intent intent = new Intent(this, NewPlanFragment.class);
        startActivity(intent);
    }

    public void openSignUp() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void openHome() {
        Intent intent = new Intent(this, HomeFragment.class);
        startActivity(intent);
    }
}
