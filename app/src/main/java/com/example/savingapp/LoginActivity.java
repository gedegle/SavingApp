package com.example.savingapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.service.autofill.TextValueSanitizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

import com.tabian.saveanddisplaysql.PlanDatabaseHelper;
import com.tabian.saveanddisplaysql.PostsDatabaseHelper;
import com.tabian.saveanddisplaysql.SettingsDatabaseHelper;
import com.tabian.saveanddisplaysql.UserDatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private Button singinBtn;
    private TextView signupBtn;
    private EditText email;
    private EditText password;
    private String url = "http://192.168.1.196:8000/api/";
    UserDatabaseHelper userDb;
    PostsDatabaseHelper postDb;
    PlanDatabaseHelper planDb;
    SettingsDatabaseHelper settingsDb;
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
        setContentView(R.layout.activity_main_login);
        signupBtn = findViewById(R.id.goBackBtn);
        userDb = new UserDatabaseHelper(this);
        postDb = new PostsDatabaseHelper(this);
        planDb = new PlanDatabaseHelper(this);
        settingsDb = new SettingsDatabaseHelper(this);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSignUp();
            }
        });

        signIn();
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
            Toast.makeText(LoginActivity.this, "Press again to exit", Toast.LENGTH_LONG).show();


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

    public void openSignUp() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void openNewPlan() {
        Intent intent = new Intent(this, NewPlanFragment.class);
        startActivity(intent);
    }

    public void signIn() {
        singinBtn = findViewById(R.id.loginBtn);
        email = findViewById(R.id.emailLoginInput);
        password = findViewById(R.id.passLoginInput);

        singinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (email.getText().toString().length() > 0
                        && password.getText().toString().length() > 0) {
                    OkHttpClient client = new OkHttpClient();

                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("email", email.getText().toString())
                            .addFormDataPart("password", password.getText().toString())
                            .build();

                    Request request = new Request.Builder()
                            .url(url + "login-app")
                            .post(requestBody)
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        String jsonData = response.body().string();
                        JSONObject data = new JSONObject(jsonData);

                        Integer user_id = data.getInt("id");
                        String user_name = data.getString("name");
                        String user_email = data.getString("email");

                        /*-----------------------------------------------
                            Get USER plan
                        * -----------------------------------------------*/
                        try {
                            Request requestPosts = new Request.Builder()
                                    .url(url + "plan/user/" + user_id)
                                    .build();

                            Call call = client.newCall(requestPosts);
                            Response responsePosts = call.execute();
                            String jsonPosts = responsePosts.body().string();
                            JSONObject plan = new JSONObject(jsonPosts);
                            Log.d("plan", String.valueOf(plan));

                            Integer plan_id = plan.getInt("id");
                            if (plan_id != null) {
                                String date = plan.getString("created_at");
                                String goal = plan.getString("goal");
                                Integer sum = plan.getInt("sum");
                                Integer income = plan.getInt("income");
                                Integer status = plan.getInt("status");
                                Integer if_saved = plan.getInt("if_saved");

                                boolean postQueried = planDb.addData(plan_id, date, sum, income, status, if_saved, goal);

                                if (!postQueried) {
                                    Log.e("NOT ADDED PLAN", "PLAN WAS NOT ADDED");
                                    return;
                                } else {
                                    Log.d("ADDED PLAN", plan_id + " " + sum);
                                }
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                            Log.e("MyApp", String.valueOf(e));
                        }

                        /*-----------------------------------------------
                            Get USER posts
                        * -----------------------------------------------*/
                        try {
                            Request requestPosts = new Request.Builder()
                                    .url(url + "posts/user/" + user_id)
                                    .build();

                            Call call = client.newCall(requestPosts);
                            Response responsePosts = call.execute();
                            String jsonPosts = responsePosts.body().string();
                            JSONArray posts = new JSONArray(jsonPosts);

                            Log.d("posts", String.valueOf(posts));

                            for(int i=0; i<=posts.length(); i++){
                                Integer post_id = posts.getJSONObject(i).getInt("id");
                                String date = posts.getJSONObject(i).getString("date");
                                Integer sum = posts.getJSONObject(i).getInt("sum");
                                String type = posts.getJSONObject(i).getString("type");

                                boolean postQueried = postDb.addData(post_id, date, sum, type);

                                if (!postQueried) {
                                    return;
                                } else {
                                    Log.d("ADDED POST", post_id + " " + type);
                                }
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                            Log.e("MyApp", String.valueOf(e));
                        }

                        boolean userQueried = userDb.addData(user_name, user_email, user_id, password.getText().toString());
                        if (userQueried) {
                            Log.d("user added to db", "Successfully added to db");
                        } else {
                            Log.e("user not added to db", "NOT successfully added to db");
                        }

                        settingsDb.addData();

                        Log.e("app", data.getString("id"));
                        Cursor planQueried = planDb.getData();
                        if (!planQueried.moveToFirst()){
                            openNewPlan();
                        } else {
                            openHome();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Log.e("MyApp", String.valueOf(e));
                        Toast.makeText(LoginActivity.this, "Login unsuccessful", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "All the fields must be filled", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}