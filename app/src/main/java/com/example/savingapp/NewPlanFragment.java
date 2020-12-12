package com.example.savingapp;

        import android.content.Intent;
        import android.database.Cursor;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.StrictMode;
        import android.text.Editable;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.Toast;

        import androidx.appcompat.app.AppCompatActivity;

        import com.tabian.saveanddisplaysql.PlanDatabaseHelper;
        import com.tabian.saveanddisplaysql.UserDatabaseHelper;

        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.IOException;

        import okhttp3.MultipartBody;
        import okhttp3.OkHttpClient;
        import okhttp3.Request;
        import okhttp3.RequestBody;
        import okhttp3.Response;

public class NewPlanFragment extends AppCompatActivity {
    EditText goalInput;
    EditText goalName;
    EditText incomeInput;
    Button confirmBtn;
    PlanDatabaseHelper planDb;
    UserDatabaseHelper userDb;
    private String url = "http://192.168.1.196:8000/api/";
    boolean allowGoingBack = false;
    int timesClicked = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.fragment_new_plan);

        planDb = new PlanDatabaseHelper(this);
        userDb = new UserDatabaseHelper(this);

        goalInput = findViewById(R.id.goalInput);
        goalName = findViewById(R.id.goalName);
        incomeInput = findViewById(R.id.incomeInput);
        confirmBtn = findViewById(R.id.confirm);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewPlan();
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
            Toast.makeText(NewPlanFragment.this, "Press again to exit", Toast.LENGTH_LONG).show();

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

    public void addNewPlan() {
        if (goalInput.getText().length() > 0 && goalName.getText().length() > 0 && incomeInput.getText().length() > 0) {
            Cursor user = userDb.getData();
            user.moveToFirst();
            Integer userId = user.getInt(user.getColumnIndex("ID"));

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("user_id", String.valueOf(userId))
                    .addFormDataPart("sum", goalInput.getText().toString())
                    .addFormDataPart("income", incomeInput.getText().toString())
                    .addFormDataPart("goal", goalName.getText().toString())
                    .addFormDataPart("if_saved", String.valueOf(0))
                    .addFormDataPart("status", String.valueOf(1))
                    .build();

            Request request = new Request.Builder()
                    .url(url + "plan-app")
                    .post(requestBody)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String jsonData = response.body().string();
                JSONObject data = new JSONObject(jsonData);

                Integer id = data.getInt("id");
                Integer sum = data.getInt("sum");
                String goal = data.getString("goal");
                Integer income = data.getInt("income");
                String date = data.getString("created_at");
                Log.e("PLAN", String.valueOf(id));

                boolean planQueried = planDb.addData(id, date, sum, income, 1, 0, goal);

                if (!planQueried) {
                    Log.e("PLAN", "PLAN WAS NOT ADDED");
                    return;
                } else {
                    Log.d("ADDED PLAN", goal + " " + sum);
                }

                openHome();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Log.e("Plan add", String.valueOf(e));
                Toast.makeText(NewPlanFragment.this, "Failed to add plan ", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(NewPlanFragment.this, "All the fields must be filled", Toast.LENGTH_LONG).show();
        }
    }
}