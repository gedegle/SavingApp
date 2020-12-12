package com.example.savingapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentManager;

import com.tabian.saveanddisplaysql.PlanDatabaseHelper;
import com.tabian.saveanddisplaysql.PostsDatabaseHelper;
import com.tabian.saveanddisplaysql.SettingsDatabaseHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeFragment extends AppCompatActivity {
    private Button historyBtn;
    private ImageButton addNewBtn;
    private ImageButton openAccBtn;
    private ImageButton archiveBtn;
    private TextView moneyTarget;
    private TextView typeOne;
    private TextView typeTwo;
    private TextView typeThree;
    private TextView sumOne;
    private TextView sumTwo;
    private TextView sumThree;
    TextView moneyRemain;
    TextView totalSaved;
    ProgressBar progressBar;
    boolean allowGoingBack = false;
    int timesClicked = 0;
    PlanDatabaseHelper planDb;
    PostsDatabaseHelper postsDb;
    SettingsDatabaseHelper settingsDb;
    private String url = "http://192.168.1.196:8000/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.fragment_home);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        planDb = new PlanDatabaseHelper(this);
        postsDb = new PostsDatabaseHelper(this);
        settingsDb = new SettingsDatabaseHelper(this);

        typeOne = findViewById(R.id.type1);
        sumOne = findViewById(R.id.sum1);
        typeTwo = findViewById(R.id.type2);
        sumTwo = findViewById(R.id.sum2);
        typeThree = findViewById(R.id.type3);
        sumThree = findViewById(R.id.sum3);
        moneyRemain = findViewById(R.id.moneyRemain);
        totalSaved = findViewById(R.id.totalSaved);
        progressBar = findViewById(R.id.progressBar);

        homeData();

        historyBtn = findViewById(R.id.viewHistory);
        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHistory();
            }
        });

        addNewBtn = findViewById(R.id.addNew);
        addNewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddNew();
            }
        });

        openAccBtn = findViewById(R.id.accBtn);
        openAccBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAcc();
            }
        });

        archiveBtn = findViewById(R.id.archiveBtn);
        archiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                archive();
            }
        });


        try {
            calculateSavings();
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("ERRORAS", String.valueOf(e));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        Cursor settings = settingsDb.getData();
        settings.moveToFirst();

        Integer status = settings.getInt(settings.getColumnIndex("status"));
        Log.e("status", String.valueOf(status));
        if (status == 1) {
            startService(new Intent(this, NotificationService.class));
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
            Toast.makeText(HomeFragment.this, "Press twice to exit app", Toast.LENGTH_SHORT).show();


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
    public void openAcc() {
        Intent intent = new Intent(this, AccountFragment.class);
        startActivity(intent);
    }

    public void openHistory() {
        Intent intent = new Intent(this, HistoryFragment.class);
        startActivity(intent);
    }

    public void openAddNew() {
        Intent intent = new Intent(this, NewExpenseFragment.class);
        startActivity(intent);
    }

    public void openNewPlan() {
        Intent intent = new Intent(this, NewPlanFragment.class);
        startActivity(intent);
    }

    public void archive() {
        Cursor plan = planDb.getData();
        plan.moveToFirst();
        Integer planId = plan.getInt(plan.getColumnIndex("ID"));
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url + "plan/" + planId)
                .delete()
                .build();

        try {
            Response response = client.newCall(request).execute();
            planDb.deletePlan(planId);
            openNewPlan();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Plan add", String.valueOf(e));
            Toast.makeText(HomeFragment.this, "Failed to destroy plan ", Toast.LENGTH_LONG).show();
        }
    }

    public void homeData() {
        /*---------------------------------
         * Get plan money target
         * ---------------------------------*/
        Cursor plan = planDb.getData();
        plan.moveToFirst();
        moneyTarget = findViewById(R.id.moneyTarget);
        String planSum = plan.getString(plan.getColumnIndex("sum"));
        moneyTarget.setText(planSum);

        /*---------------------------------
         * Get plan goal
         * ---------------------------------*/
        TextView goalView = findViewById(R.id.goal);
        goalView.setText(plan.getString(plan.getColumnIndex("goal")));

        /*---------------------------------
         * Get top three spendings
         * ---------------------------------*/
        Cursor topThree = postsDb.getTopTheree();

        if (topThree.getCount() > 0) {
            topThree.moveToFirst();

            typeOne.setText(topThree.getString(topThree.getColumnIndex("type")));
            sumOne.setText(topThree.getString(topThree.getColumnIndex("sum")));

            if (topThree.getCount() > 1) {
                topThree.moveToNext();

                typeTwo.setText(topThree.getString(topThree.getColumnIndex("type")));
                sumTwo.setText(topThree.getString(topThree.getColumnIndex("sum")));
            } else {
                typeTwo.setVisibility(View.GONE);
                sumTwo.setVisibility(View.GONE);
            }

            if (topThree.getCount() > 2) {
                topThree.moveToLast();

                typeThree.setText(topThree.getString(topThree.getColumnIndex("type")));
                sumThree.setText(topThree.getString(topThree.getColumnIndex("sum")));
            } else {
                typeThree.setVisibility(View.GONE);
                sumThree.setVisibility(View.GONE);
            }
        } else {
            typeOne.setText("No data");
            sumOne.setVisibility(View.GONE);
            typeTwo.setVisibility(View.GONE);
            sumTwo.setVisibility(View.GONE);
            typeThree.setVisibility(View.GONE);
            sumThree.setVisibility(View.GONE);
        }
        plan.close();
    }

    public void calculateSavings() throws ParseException {
        Cursor plan = planDb.getData();
        plan.moveToFirst();

        Integer sum = plan.getInt(plan.getColumnIndex("sum"));
        Integer income = plan.getInt(plan.getColumnIndex("income"));
        double tempWhileSum = 0;
        double monthCount = 1;
        double daysToSave = 0;
        double saved;
        Integer inAll = 0;

        String date = plan.getString(plan.getColumnIndex("date"));

        SimpleDateFormat format = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date dateNow = new java.util.Date();
        Date dateCreated = format.parse(date);
        long tempDays = dateCreated.getTime() - dateNow.getTime();

        tempDays = (long) Math.ceil(tempDays / (24 * 60 * 60 * 1000));

        Cursor countQuery = postsDb.getCount();
        countQuery.moveToFirst();
        Integer count = countQuery.getInt(countQuery.getColumnIndex("COUNT(*)"));
        Cursor spentQuery = postsDb.spentInAll();
        spentQuery.moveToFirst();
        if (count > 0) {
            inAll = spentQuery.getInt(spentQuery.getColumnIndex("total"));
        } else {
            inAll = 0;
        }

        Integer avgPerDay = inAll > 0 ? inAll / count : 0;
        Integer avgPerMonth = avgPerDay * 30;
        if (avgPerMonth > income) {
            monthCount = Math.ceil((double) avgPerMonth / income);
        } else {
            monthCount = 1;
        }

        double tempIncome = monthCount * income;
        double tempLeftAMonth = tempIncome - avgPerMonth;

        if (tempLeftAMonth < sum) {
            tempWhileSum = sum;

            while (tempWhileSum >= sum) {
                monthCount++;
                tempWhileSum = sum - tempLeftAMonth;
            }
        }


            saved = (double) Math.round(((income / 30) * tempDays - inAll) * 100) / 100;
            if (saved < 0) {
                saved = 0;
            }

            double leftToSave = sum - saved < 0 ? sum : sum - saved;

            moneyRemain.setText(String.valueOf(leftToSave));
            totalSaved.setText(String.valueOf(saved + "€"));
            long progress = Math.round(saved * 100 / sum);
            progressBar.setProgress((int) progress);

    }
}