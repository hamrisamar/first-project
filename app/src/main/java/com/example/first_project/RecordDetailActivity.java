package com.example.first_project;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;

import java.util.Calendar;
import java.util.Locale;

public class RecordDetailActivity extends AppCompatActivity {
     //views
    private CircularImageView profileIv;
   private TextView bioTv, nameTv, phoneTv, emailTv, dobTv, addedTimeTv, updatedTimeTv;

   //actionbar
    private ActionBar actionBar;

    //db helper
    private MyDbHelper dbHelper;
    private String recordID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_detail);

        //setting up actionbar with title and back button
        actionBar = getSupportActionBar();
        actionBar.setTitle("Employee Details");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //get record id from adapter through intent
        Intent intent = getIntent();
        recordID = intent.getStringExtra("RECORD_ID");

        //init db helper class
        dbHelper = new MyDbHelper(this);


        //init views
        profileIv = findViewById(R.id.profileIv);
        bioTv = findViewById(R.id.bioTv);
        nameTv = findViewById(R.id.nameTv);
        phoneTv = findViewById(R.id.phoneTv);
        emailTv = findViewById(R.id.emailTv);
        dobTv = findViewById(R.id.dobTv);
        addedTimeTv = findViewById(R.id.addedTimeTv);
        updatedTimeTv = findViewById(R.id.updatedTimeTv);

        showRecordDetails();
    }

    private void showRecordDetails() {
        //get record details

        //query to select record based on record id
        String selectQuery ="SELECT * FROM "+ Constants.TABLE_NAME + " WHERE " + Constants.C_ID +" =\"" + recordID+" \"";

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,null);

        //keep check in whole db for that record
        if(cursor.moveToFirst()){
            do{
                //get data
                @SuppressLint("Range") String id = ""+ cursor.getInt(cursor.getColumnIndex(Constants.C_ID));
                @SuppressLint("Range") String name = ""+ cursor.getString(cursor.getColumnIndex(Constants.C_NAME));
                @SuppressLint("Range") String image = ""+ cursor.getString(cursor.getColumnIndex(Constants.C_IMAGE));
                @SuppressLint("Range") String bio = ""+ cursor.getString(cursor.getColumnIndex(Constants.C_BIO));
                @SuppressLint("Range") String phone = ""+ cursor.getString(cursor.getColumnIndex(Constants.C_PHONE));
                @SuppressLint("Range") String email = ""+ cursor.getString(cursor.getColumnIndex(Constants.C_EMAIL));
                @SuppressLint("Range") String dob = ""+ cursor.getString(cursor.getColumnIndex(Constants.C_DOB));
                @SuppressLint("Range") String timestampAdded = ""+ cursor.getString(cursor.getColumnIndex(Constants.C_ADDED_TIMESTAMP));
                @SuppressLint("Range") String timestampUpdated = cursor.getString(cursor.getColumnIndex(Constants.C_UPDATED_TIMESTAMP));

                //convert timestamp to dd/mm/yyyy
                Calendar calendar1 =Calendar.getInstance(Locale.getDefault());
                calendar1.setTimeInMillis(Long.parseLong(timestampAdded));
                String timeAdded = String.valueOf(DateFormat.format("dd/MM/yyy hh:mm:aa", calendar1));

                Calendar calendar2 =Calendar.getInstance(Locale.getDefault());
                calendar1.setTimeInMillis(Long.parseLong(timestampAdded));
                String timeUpdated = String.valueOf(DateFormat.format("dd/MM/yyy hh:mm:aa", calendar2));

                //set data
                nameTv.setText(name);
                bioTv.setText(bio);
                phoneTv.setText(phone);
                emailTv.setText(email);
                dobTv.setText(dob);
                addedTimeTv.setText(timeAdded);
                updatedTimeTv.setText(timeUpdated);
                //profileIv.setImageURI(Uri.parse(image));
                if (image.equals("null")){
                    //no image in record , set default
                    profileIv.setImageResource(R.drawable.ic_person_black);
                }
                else{
                    //have image in record
                    profileIv.setImageURI(Uri.parse(image));
                }


            }while(cursor.moveToNext());
        }
        //close db connection
        db.close();
    }

    public boolean onSupportNavigateUp(){
        //noinspection deprecation
        onBackPressed(); //goto previous activity
        return super.onSupportNavigateUp();
    }
}