package com.example.savingapp;

        import android.content.Intent;
        import android.database.Cursor;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.StrictMode;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.Toast;

        import androidx.appcompat.app.AppCompatActivity;

        import com.tabian.saveanddisplaysql.PlanDatabaseHelper;
        import com.tabian.saveanddisplaysql.PostsDatabaseHelper;
        import com.tabian.saveanddisplaysql.SettingsDatabaseHelper;
        import com.tabian.saveanddisplaysql.UserDatabaseHelper;

        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.IOException;

        import okhttp3.MultipartBody;
        import okhttp3.OkHttpClient;
        import okhttp3.Request;
        import okhttp3.RequestBody;
        import okhttp3.Response;

public class NewExpenseFragment extends AppCompatActivity {
    EditText amountInput;
    EditText typeInput;
    EditText dateInput;
    Button confirm;
    Button discard;
    PostsDatabaseHelper postDb;
    PlanDatabaseHelper planDb;
    UserDatabaseHelper userDb;
    SettingsDatabaseHelper settingsDb;
    boolean isEdit = false;
    Integer ID;
    boolean allowGoingBack = false;
    private String url = "http://192.168.1.196:8000/api/";
    int timesClicked = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.fragment_new_expense);

        postDb = new PostsDatabaseHelper(this);
        planDb = new PlanDatabaseHelper(this);
        userDb = new UserDatabaseHelper(this);
        settingsDb = new SettingsDatabaseHelper(this);

        amountInput = findViewById(R.id.amountInput);
        typeInput = findViewById(R.id.typeInput);
        dateInput = findViewById(R.id.dateInput);
        confirm = findViewById(R.id.confirm);
        discard = findViewById(R.id.discard);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        try {
            String SUM = getIntent().getStringExtra("SUM");
            String TYPE = getIntent().getStringExtra("TYPE");
            String DATE = getIntent().getStringExtra("DATE");
            ID = getIntent().getIntExtra("ID", 0);

            if (SUM.length() > 0 && TYPE.length() > 0 && DATE.length() > 0) {
                isEdit = true;
            }

            amountInput.setText(SUM);
            typeInput.setText(TYPE);
            dateInput.setText(DATE);


        } catch (Exception e) {
            e.printStackTrace();
        }

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewExpense();
            }
        });
        discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHome();
            }
        });
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
            Toast.makeText(NewExpenseFragment.this, "Press again to exit", Toast.LENGTH_LONG).show();


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

    public void openHome() {
        Intent intent = new Intent(this, HomeFragment.class);
        startActivity(intent);
    }

    public void openHistory() {
        Intent intent = new Intent(this, HistoryFragment.class);
        startActivity(intent);
    }

    public void addNewExpense() {
        if (amountInput.getText().length() > 0 && typeInput.getText().length() > 0 && dateInput.getText().length() > 0) {
            Cursor plan = planDb.getData();
            plan.moveToFirst();
            Integer planId = plan.getInt(plan.getColumnIndex("ID"));

            Cursor user = userDb.getData();
            user.moveToFirst();
            Integer userId = user.getInt(user.getColumnIndex("ID"));

            OkHttpClient client = new OkHttpClient();

            Request request;

            if (isEdit) {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("sum", amountInput.getText().toString())
                        .addFormDataPart("date", dateInput.getText().toString())
                        .addFormDataPart("type", typeInput.getText().toString())
                        .addFormDataPart("_method", "put")
                        .build();

                request = new Request.Builder()
                        .url(url + "post-app/" + ID)
                        .post(requestBody)
                        .build();
            } else {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("user_id", String.valueOf(userId))
                        .addFormDataPart("plan_id", String.valueOf(planId))
                        .addFormDataPart("sum", amountInput.getText().toString())
                        .addFormDataPart("date", dateInput.getText().toString())
                        .addFormDataPart("type", typeInput.getText().toString())
                        .build();
                request = new Request.Builder()
                        .url(url + "post-app")
                        .post(requestBody)
                        .build();

            }

            try {
                Response response = client.newCall(request).execute();

                String jsonData = response.body().string();
                JSONObject data = new JSONObject(jsonData);

                Integer id = data.getInt("id");
                Integer sum = data.getInt("sum");
                String date = data.getString("date");
                String type = data.getString("type");

                boolean postQueried;

                if (isEdit) {
                    Log.d("IS EDIT", "EDITING QUERY");
                    postQueried = postDb.updatePost(id, date, sum, type);
                } else {
                    Log.d("IS POST", "POSTING QUERY");
                    postQueried = postDb.addData(id, date, sum, type);
                }

                if (!postQueried) {
                    Log.e("POST", "POST WAS NOT ADDED");
                    return;
                } else {
                    Log.d("ADDED POST", type + " " + sum);
                }

                openHistory();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Log.e("Post adding", String.valueOf(e));
                Toast.makeText(NewExpenseFragment.this, "Failed to add post ", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(NewExpenseFragment.this, "All the fields must be filled", Toast.LENGTH_LONG).show();
        }
    }
}