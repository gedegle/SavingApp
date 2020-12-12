package com.example.savingapp;

        import android.app.Dialog;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.database.Cursor;
        import android.graphics.Color;
        import android.graphics.Typeface;
        import android.graphics.drawable.Drawable;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.StrictMode;
        import android.util.Log;
        import android.view.Gravity;
        import android.view.MotionEvent;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageButton;
        import android.widget.LinearLayout;

        import androidx.appcompat.app.AlertDialog;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.cardview.widget.CardView;
        import androidx.core.content.ContextCompat;

        import android.widget.LinearLayout.LayoutParams;
        import android.widget.ScrollView;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.tabian.saveanddisplaysql.PostsDatabaseHelper;
        import com.tabian.saveanddisplaysql.SettingsDatabaseHelper;

        import java.io.IOException;
        import java.lang.reflect.Field;

        import okhttp3.HttpUrl;
        import okhttp3.MediaType;
        import okhttp3.MultipartBody;
        import okhttp3.OkHttpClient;
        import okhttp3.Request;
        import okhttp3.RequestBody;
        import okhttp3.Response;

public class HistoryFragment extends AppCompatActivity {
    Context context;
    PostsDatabaseHelper postsDb;
    SettingsDatabaseHelper settingsDb;
    private TextView moneyInAll;
    private ImageButton addNewBtn;
    private ImageButton openAccBtn;
    private ImageButton openHome;
    private String url = "http://192.168.1.196:8000/api/";
    Cursor inAll;
    boolean allowGoingBack = false;
    ImageButton backButton;
    int timesClicked = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.fragment_history);
        context = getApplicationContext();
        postsDb = new PostsDatabaseHelper(this);
        settingsDb = new SettingsDatabaseHelper(this);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        /*---------------------------------
         * Spent in all
         * ---------------------------------*/
        inAll = postsDb.spentInAll();
        inAll.moveToFirst();

        moneyInAll = findViewById(R.id.totalSpentNr);
        if (inAll.getString(inAll.getColumnIndex("total")) != null) {
            moneyInAll.setText(inAll.getString(inAll.getColumnIndex("total")) + "€");
        } else {
            moneyInAll.setText(0 + "€");
        }


        addNewBtn = findViewById(R.id.addNew);
        addNewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    openAddNew(null);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                    Toast.makeText(HistoryFragment.this, "Unexpected error. Try again later.", Toast.LENGTH_LONG).show();
                }
            }
        });

        openHome = findViewById(R.id.homeBtn);
        openHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHome();
            }
        });

        openAccBtn = findViewById(R.id.accBtn);
        openAccBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAcc();
            }
        });

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowGoingBack = true;
                onBackPressed();
            }
        });

        loopHistoryItems();
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
            Toast.makeText(HistoryFragment.this, "Press again to exit", Toast.LENGTH_LONG).show();


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

    public void openHome() {
        Intent intent = new Intent(this, HomeFragment.class);
        startActivity(intent);
    }

    public void openAddNew(EditableObject data) throws NoSuchFieldException {
        Intent intent = new Intent(this, NewExpenseFragment.class);
        if (data != null) {
            String sum = data.sum;
            String type = data.type;
            String date = data.date;
            Integer id = data.id;
            intent.putExtra("SUM", sum);
            intent.putExtra("TYPE", type);
            intent.putExtra("DATE", date);
            intent.putExtra("ID", id);
        }
        startActivity(intent);
    }

    void loopHistoryItems() {
        final LinearLayout mainLayout = findViewById(R.id.historyLayout);
        final Cursor historyItems = postsDb.getData();

        if (historyItems.moveToFirst()){
            historyItems.moveToFirst();
            do{
                final Integer itemId = historyItems.getInt(historyItems.getColumnIndex("ID"));

                // Create CardView
                final CardView cardView = new CardView(context);
                LayoutParams layoutParams = new LayoutParams(
                        LayoutParams.MATCH_PARENT, // CardView width
                        LayoutParams.MATCH_PARENT // CardView height
                );

                cardView.setLayoutParams(layoutParams);
                cardView.setRadius(0);


                // Create LinearLayout
                LinearLayout cardLayout = new LinearLayout(this);
                cardLayout.setOrientation(LinearLayout.HORIZONTAL);
                cardLayout.setLayoutParams(layoutParams);




                // Create TextView for TYPE
                TextView textViewType = new TextView(context);
                LayoutParams layoutParamsText = new LayoutParams(
                        LayoutParams.MATCH_PARENT, // TextView width
                        LayoutParams.WRAP_CONTENT, // TextView height
                        1
                );
                textViewType.setLayoutParams(layoutParamsText);
                textViewType.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                textViewType.setPadding(0, 10, 0, 10);

                final String type = historyItems.getString(historyItems.getColumnIndex("type"));
                textViewType.setText(type);

                textViewType.setTextColor(Color.parseColor("#540A78"));
                textViewType.setTextSize(16);




                // Create TextView for PRICE
                TextView textViewPrice = new TextView(context);
                LayoutParams layoutParamsPrice = new LayoutParams(
                        LayoutParams.MATCH_PARENT, // TextView width
                        LayoutParams.WRAP_CONTENT, // TextView height
                        (float) 1.04
                );
                textViewPrice.setLayoutParams(layoutParamsPrice);
                textViewPrice.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
                textViewPrice.setPadding(0, 10, 0, 10);

                final String price = historyItems.getString(historyItems.getColumnIndex("sum"));
                textViewPrice.setText(price);

                textViewPrice.setTextColor(Color.parseColor("#F94D6F"));
                textViewPrice.setTextSize(16);
                textViewPrice.setGravity(Gravity.CENTER);




                // Create TextView for DATE
                TextView textViewDate = new TextView(context);
                textViewDate.setLayoutParams(layoutParamsText);
                textViewDate.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
                textViewDate.setPadding(0, 10, 0, 10);

               final String date = historyItems.getString(historyItems.getColumnIndex("date"));
                textViewDate.setText(date);

                textViewDate.setTextColor(Color.parseColor("#540A78"));
                textViewDate.setTextSize(16);
                textViewDate.setGravity(Gravity.RIGHT);



                cardLayout.addView(textViewType);
                cardLayout.addView(textViewPrice);
                cardLayout.addView(textViewDate);
                cardView.addView(cardLayout);
                mainLayout.addView(cardView);

                cardView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final Dialog dialog = new Dialog(HistoryFragment.this);
                        // Setting Dialog Title
                        dialog.setTitle("Action Dialog");
                        /* -------------------------------
                            MAIN layout
                         ----------------------------------*/

                        LinearLayout modalLayout = new LinearLayout(context);

                        LayoutParams layoutParams = new LayoutParams(
                                LayoutParams.WRAP_CONTENT, // CardView width
                                LayoutParams.WRAP_CONTENT // CardView height
                        );

                        modalLayout.setOrientation(LinearLayout.VERTICAL);
                        modalLayout.setLayoutParams(layoutParams);

                        dialog.setContentView(modalLayout);

                        /* ---------------------------------
                            TEXT
                         -----------------------------------*/

                        TextView text = new TextView(context);
                        text.setText("Choose action for item \"" + type + "\"");

                        text.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
                        text.setPadding(75, 55, 75, 45);
                        text.setTextColor(Color.parseColor("#540A78"));
                        text.setTextSize(16);


                        /* -------------------------------
                            BUTTON layout
                         ----------------------------------*/

                        LinearLayout buttonLayout = new LinearLayout(context);

                        LayoutParams buttonLayoutParams = new LayoutParams(
                                LayoutParams.MATCH_PARENT, // CardView width
                                LayoutParams.MATCH_PARENT // CardView height
                        );

                        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
                        buttonLayout.setLayoutParams(buttonLayoutParams);

                        /*-----------------------------------
                            DELETE button
                         --------------------------------------*/

                        Button deleteButton  = new Button(context);

                        LayoutParams buttonParams = new LayoutParams(
                                LayoutParams.WRAP_CONTENT, // CardView width
                                LayoutParams.WRAP_CONTENT, // CardView height,
                                1
                        );

                        buttonParams.setMargins(75,0,20,55);
                        deleteButton.setLayoutParams(buttonParams);
                        deleteButton.setBackground(ContextCompat.getDrawable(context, R.drawable.border_round_discard));
                        deleteButton.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
                        deleteButton.setText("Delete");
                        deleteButton.setTextColor(Color.parseColor("#FFA453EA"));
                        deleteButton.setPadding(115,0,115,0);

                        deleteButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    deleteItem(itemId);
                                    dialog.dismiss();
                                    mainLayout.removeView(cardView);
                                    moneyInAll.setText((inAll.getInt(inAll.getColumnIndex("total")) - Integer.parseInt(price)) + "€");
                                    Toast.makeText(HistoryFragment.this, "Deleted successfully", Toast.LENGTH_LONG).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(HistoryFragment.this, "Deletion unsuccessful", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                         /*-----------------------------------
                            EDIT button
                         --------------------------------------*/

                        Button editButton  = new Button(context);

                        LayoutParams btnParams = new LayoutParams(
                                LayoutParams.WRAP_CONTENT, // CardView width
                                LayoutParams.WRAP_CONTENT, // CardView height,
                                1
                        );

                        btnParams.setMargins(0,0,75,55);
                        editButton.setLayoutParams(btnParams);
                        editButton.setBackground(ContextCompat.getDrawable(context, R.drawable.border_round_confirm));
                        editButton.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
                        editButton.setText("Edit");
                        editButton.setTextColor(Color.parseColor("#F43646"));
                        editButton.setPadding(140,0,140,0);

                        // Add listener
                        editButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                EditableObject data = new EditableObject(price, type, date, itemId);
                                try {
                                    dialog.dismiss();
                                    openAddNew(data);
                                } catch (NoSuchFieldException e) {
                                    e.printStackTrace();
                                    Toast.makeText(HistoryFragment.this, "Unexpected error. Try again later.", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                        modalLayout.addView(text);
                        buttonLayout.addView(deleteButton);
                        buttonLayout.addView(editButton);
                        modalLayout.addView(buttonLayout);

                        dialog.setContentView(modalLayout);

                        // Showing Alert Message
                        dialog.show();
                        return true;
                    }
                });

            }while(historyItems.moveToNext());
        } else {
            // Create TextView for TYPE
            TextView textView = new TextView(context);
            LayoutParams layoutParamsText = new LayoutParams(
                    LayoutParams.MATCH_PARENT, // TextView width
                    LayoutParams.WRAP_CONTENT // TextView height
            );

            textView.setLayoutParams(layoutParamsText);
            textView.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            textView.setPadding(0, 10, 0, 10);

            textView.setText("No data");

            textView.setTextColor(Color.parseColor("#540A78"));
            textView.setTextSize(16);
            textView.setGravity(Gravity.CENTER);

            mainLayout.addView(textView);
        }
        historyItems.close();
    }

    public void openHistory() {
        Intent intent = new Intent(this, HistoryFragment.class);
        startActivity(intent);
    }

    public void deleteItem(Integer id) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .delete()
                .url(url + "post/" + id)
                .build();
        client.newCall(request).execute();
        postsDb.deletePost(id);
    }

    public class EditableObject {
        public String sum = "";
        public String type = "";
        public String date = "";
        public Integer id = 0;
        //constructor
        public EditableObject(String sumVal, String typeVal, String dateVal, Integer idVal) {
            sum = sumVal;
            type = typeVal;
            date = dateVal;
            id = idVal;
        }
    }
}