package com.example.savingapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
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

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

import com.tabian.saveanddisplaysql.PlanDatabaseHelper;
import com.tabian.saveanddisplaysql.PostsDatabaseHelper;
import com.tabian.saveanddisplaysql.SettingsDatabaseHelper;
import com.tabian.saveanddisplaysql.UserDatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private Button signupBtn;
    private TextView singinBtn;
    private EditText name;
    private EditText email;
    private EditText password;
    private EditText password2;
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
        setContentView(R.layout.activity_main_register);
        singinBtn = findViewById(R.id.loginInReg);
        userDb = new UserDatabaseHelper(this);
        postDb = new PostsDatabaseHelper(this);
        planDb = new PlanDatabaseHelper(this);
        settingsDb = new SettingsDatabaseHelper(this);


        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        singinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLogin();
            }
        });

        signUp();
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
            Toast.makeText(MainActivity.this, "Press again to exit", Toast.LENGTH_LONG).show();


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

    public void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void signUp() {
        signupBtn = findViewById(R.id.signup);
        name = findViewById(R.id.nameRegInput);
        email = findViewById(R.id.emailRegInput);
        password = findViewById(R.id.pass1RegInput);
        password2 = findViewById(R.id.pass2RegInput);

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().toString().length() > 0 && email.getText().toString().length() > 0
                        && password.getText().toString().length() > 0 && password2.getText().toString().length() > 0) {
                    if (password.getText().toString().equals(password2.getText().toString())) {
                        OkHttpClient client = new OkHttpClient();

                        RequestBody requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("email", email.getText().toString())
                                .addFormDataPart("password", password.getText().toString())
                                .addFormDataPart("name", name.getText().toString())
                                .build();

                        Request request = new Request.Builder()
                                .url(url + "user-app")
                                .post(requestBody)
                                .build();
                        try {
                            Response response = client.newCall(request).execute();
                            String jsonData = response.body().string();
                            JSONObject data = new JSONObject(jsonData);

                            Log.d("data", String.valueOf(data));
                            Integer user_id = data.getInt("id");
                            String user_name = data.getString("name");
                            String user_email = data.getString("email");

                            boolean userQueried = userDb.addData(user_name, user_email, user_id, password.getText().toString());
                            if (userQueried) {
                                Log.d("user added to db", "Successfully added to db");
                            } else {
                                Log.e("user not added to db", "NOT successfully added to db");
                            }
                            settingsDb.addData();
                            openNewPlan();
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                            Log.e("MyApp", String.valueOf(e));
                            Toast.makeText(MainActivity.this, "Registration unsuccessful. Please try again later.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Passwords must match!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "All the fields must be filled", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}