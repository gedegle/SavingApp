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
        import android.widget.CheckBox;
        import android.widget.CompoundButton;
        import android.widget.EditText;
        import android.widget.ImageButton;
        import android.widget.Toast;

        import androidx.appcompat.app.AppCompatActivity;

        import com.tabian.saveanddisplaysql.SettingsDatabaseHelper;
        import com.tabian.saveanddisplaysql.UserDatabaseHelper;

        import java.io.IOException;

        import okhttp3.MultipartBody;
        import okhttp3.OkHttpClient;
        import okhttp3.Request;
        import okhttp3.RequestBody;
        import okhttp3.Response;

public class AccountFragment extends AppCompatActivity {
    private ImageButton openHome;
    private ImageButton addNewBtn;
    private Button updateButton;
    UserDatabaseHelper userDb;
    SettingsDatabaseHelper settingsDb;
    EditText emailInput;
    EditText nameInput;
    EditText pass1;
    EditText pass2;
    private String url = "http://192.168.1.196:8000/api/";
    boolean allowGoingBack = false;
    int timesClicked = 0;
    CheckBox isNotificationsActive;
    EditText notifRepeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.fragment_account);
        userDb = new UserDatabaseHelper(this);
        settingsDb = new SettingsDatabaseHelper(this);
        emailInput = findViewById(R.id.emailInput);
        nameInput = findViewById(R.id.nameInput);
        pass1 = findViewById(R.id.pass1);
        pass2 = findViewById(R.id.pass2);
        isNotificationsActive = findViewById(R.id.isNotificationsActive);
        notifRepeat = findViewById(R.id.notifRepeat);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Log.d("pass input", "password1.toString()");

        displayUserData();

        openHome = findViewById(R.id.homeBtn);
        openHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHome();
            }
        });

        addNewBtn = findViewById(R.id.addNew);
        addNewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddNew();
            }
        });

        updateButton = findViewById(R.id.saveChanges);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData();
            }
        });

        isNotificationsActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                isChecked = isNotificationsActive.isChecked();

                if (isChecked == true) {
                    notifRepeat.setEnabled(true);
                } else {
                    notifRepeat.setEnabled(false);
                }
            }
        }
        );
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
            Toast.makeText(AccountFragment.this, "Press again to exit", Toast.LENGTH_LONG).show();


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

    public void openAddNew() {
        Intent intent = new Intent(this, NewExpenseFragment.class);
        startActivity(intent);
    }

    public void openHome() {
        Intent intent = new Intent(this, HomeFragment.class);
        startActivity(intent);
    }

    public void displayUserData() {
        Cursor user = userDb.getData();
        user.moveToFirst();
        String userName = user.getString(user.getColumnIndex("name"));
        String userEmail = user.getString(user.getColumnIndex("email"));

        emailInput.setText(userEmail);
        nameInput.setText(userName);

        Cursor settings = settingsDb.getData();
        settings.moveToFirst();

        Integer status = settings.getInt(settings.getColumnIndex("status"));
        double repeat = settings.getDouble(settings.getColumnIndex("repeat_time"));

        if (status == 1) {
            isNotificationsActive.setChecked(true);
            notifRepeat.setEnabled(true);
            notifRepeat.setText(String.valueOf(repeat));
        } else {
            isNotificationsActive.setChecked(false);
            notifRepeat.setText(String.valueOf(repeat));
            notifRepeat.setEnabled(false);
        }

        user.close();
        settings.close();
    }

    public void updateData() {
        Cursor user = userDb.getData();
        user.moveToFirst();
        Integer userId = user.getInt(user.getColumnIndex("ID"));
        String userPassword = user.getString(user.getColumnIndex("password"));


        Editable email = emailInput.getText();
        Editable name = nameInput.getText();
        Editable password1 = pass1.getText();
        Editable password2 = pass2.getText();
        String newPassword;
        String oldPassword;

        boolean isChecked = isNotificationsActive.isChecked();
        Editable repeatValue = notifRepeat.getText();


        if (email.toString().length() > 0 && name.toString().length() > 0) {
            if (password1.toString().length() == 0 && password2.toString().length() == 0) {
                newPassword = userPassword;
                oldPassword = userPassword;
            } else {
                newPassword = password2.toString();
                oldPassword = password1.toString();
            }

            if (oldPassword.equals(userPassword)) {
                OkHttpClient client = new OkHttpClient();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("email", email.toString())
                        .addFormDataPart("name", name.toString())
                        .addFormDataPart("password", newPassword)
                        .addFormDataPart("id", userId.toString())
                        .build();

                Request request = new Request.Builder()
                        .url(url + "user")
                        .put(requestBody)
                        .build();

                try {
                    client.newCall(request).execute();
                    userDb.updateUser(name.toString(), userId, email.toString(), newPassword);

                    settingsDb.updateSettings(isChecked == true ? 1 : 0, Double.parseDouble((repeatValue.toString())));
                    stopService(new Intent(this, NotificationService.class));
                    Toast.makeText(AccountFragment.this, "Update successfully", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(AccountFragment.this, "Update unsuccessful", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e("app",  password1.toString());
                Toast.makeText(AccountFragment.this, "Incorrect password", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(AccountFragment.this, "All the fields must be filled", Toast.LENGTH_LONG).show();
        }
        user.close();
    }
}