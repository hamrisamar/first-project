package com.example.first_project;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Objects;

public class AddUpdateRecordActivity extends AppCompatActivity {
    //views
    CircularImageView profileIv;
    private EditText nameEt,phoneEt,emailEt,dobEt,bioEt;

    //permission constants
    private  static  final int CAMERA_REQUEST_CODE =100;
    private  static  final int STORAGE_REQUEST_CODE =101;

    //image pick constants
    private  static  final int IMAGE_PICK_CAMERA_CODE =102;
    private  static  final int IMAGE_PICK_GALLERY_CODE =103;

    //arrays of permissions
    private String[] cameraPermissions; //camera and storage
    private String[] storagePermissions; //only storage

    //variables (will contain data to save)
    private Uri imageUri ;
    private String  id;
    private String name;
    private String phone;
    private String email;
    private String dob;
    private String bio;
    private String addedTime;
    private boolean isEditMode = false;

    //db helper
    private MyDbHelper dpHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_update_record);

        //int
        //actionbar
        ActionBar actionBar = getSupportActionBar();
        //title

        assert actionBar != null;
        actionBar.setTitle("Add Record");
        //back button
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init views
        profileIv = findViewById(R.id.profileIv);
        nameEt  = findViewById(R.id.nameEt);
        phoneEt = findViewById(R.id.phoneEt);
        emailEt = findViewById(R.id.emailEt);
        dobEt = findViewById(R.id.dobEt);
        bioEt = findViewById(R.id.bioEt);
        FloatingActionButton saveBtn = findViewById(R.id.saveBtn);

        //get data from intent
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEditMode",false);

        if(isEditMode){
            //updating data
            actionBar.setTitle("Update Data");
            id=intent.getStringExtra("ID");
            name=intent.getStringExtra("NAME");
            phone=intent.getStringExtra("PHONE");
            email=intent.getStringExtra("EMAIL");
            dob=intent.getStringExtra("DOB");
            bio=intent.getStringExtra("bio");
            imageUri= Uri.parse(intent.getStringExtra("IMAGE"));
            addedTime=intent.getStringExtra("ADDED_TIME");

            //set data to views
            nameEt.setText(name);
            phoneEt.setText(phone);
            emailEt.setText(email);
            dobEt.setText(dob);
            bioEt.setText(bio);

            //if no image was selected while adding data, imageUri value be "null"
              if(imageUri.toString().equals("null")){
                  //no image, set default
                  profileIv.setImageResource(R.drawable.ic_person_black);
              }
              else{
                  //have image , set
                  profileIv.setImageURI(imageUri);
              }
        }else{
            //add data
            actionBar.setTitle("Add Record");
        }


        //init dp helper
        dpHelper = new MyDbHelper(this);

        //init permission arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //click image view to show image pick dialog
        profileIv.setOnClickListener(v -> {
            //show image pick dialog
            imagePickDialog();
        });

        //click save button to save record
        saveBtn.setOnClickListener(v -> inputData());
    }

    private void inputData() {
        //get data
        name = nameEt.getText().toString().trim();
        phone = phoneEt.getText().toString().trim();
        email = emailEt.getText().toString().trim();
        dob = dobEt.getText().toString().trim();
        bio = bioEt.getText().toString().trim();
        String timestamp = String.valueOf(System.currentTimeMillis());
        if(isEditMode){
            //update data
            dpHelper.updateRecord(
                    id,
                    name,
                    String.valueOf(imageUri),
                    bio,
                    phone,
                    email,
                    dob,
                    addedTime, //added time will be same
                    timestamp   //updated time will be changed
            );
            Toast.makeText(this,"Updated...",Toast.LENGTH_SHORT).show();
        }
        else{
            //new data
            //save  to db
            long id = dpHelper.insertRecord(
                    name,
                    String.valueOf(imageUri),
                    bio,
                    phone,
                    email,
                    dob,
                    timestamp,
                    timestamp
            );
            Toast.makeText(this,"Record Added against ID:"+id,Toast.LENGTH_SHORT).show();
        }
    }

    private void imagePickDialog() {
        // Options to display in dialog
        String[] options = {"Camera", "Gallery"};
        // Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Title
        builder.setTitle("Pick Image From");
        // Set items / options
        builder.setItems(options, (dialog, which) -> {
            // Handle clicks
            if (which == 0) {
                // Camera clicked
                if (!checkCameraPermissions()) {
                    requestCameraPermission();
                } else {
                    // Permission already granted
                    pickFromCamera();
                }
            } else if (which == 1) {
                // Gallery clicked
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                } else {
                    // Permission already granted
                    pickFromGallery();
                }
            }
        });
        // Create / show dialog
        builder.create().show();
    }

    private void pickFromGallery() {
        //intent to pick image from gallery, the image will be returned in onActivityResult method
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*"); //we want only images
        //noinspection deprecation
        startActivityForResult(galleryIntent , IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        //intent to pick image from camera, the image will be returned in onActivityResult method
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Image title");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image description ");
        //put image uri
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        //intent to open camera for image
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        //noinspection deprecation
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){
        //check if storage permission is enabled or not
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    private void  requestStoragePermission(){
        //request the storage permission
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermissions(){
        //check if camera permissions is enabled or not
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return  result && result1;
    }

    private void  requestCameraPermission(){
        //request the camera Permission
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }

    private void copyFileOrDirectory(String srcDir, String desDir){
        //create finder in specified directory
        try{
            File src = new File(srcDir);
            File des = new File(desDir, src.getName());
            if(src.isDirectory()){
                String[] files = src.list();
                assert files != null;
                for(String file : files){
                    String src1 = new File(src, file).getPath();
                    String dst1 = des.getPath();

                    copyFileOrDirectory(src1, dst1);
                }
            }
            else{
                copyFile(src, des);
            }
        }
        catch (Exception e){
            Toast.makeText(this, e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private void copyFile(File srcDir, File desDir) throws IOException {
        if(!Objects.requireNonNull(desDir.getParentFile()).exists()){
            //noinspection ResultOfMethodCallIgnored
            desDir.mkdirs();//create if not exists
        }
        if(!desDir.exists()){
            //noinspection ResultOfMethodCallIgnored
            desDir.createNewFile();
        }

        //noinspection resource
        try (FileChannel source = new FileInputStream(srcDir).getChannel(); FileChannel destination = new FileOutputStream(desDir).getChannel()) {
            destination.transferFrom(source, 0, source.size());

            imageUri = Uri.parse(desDir.getPath());//uri of save image
            Log.d("ImagePath", "copyFile:" + imageUri);
        } catch (Exception e) {
            //if there is any error saving image
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        //close resources
    }

    @Override
    public boolean onSupportNavigateUp() {
        //noinspection deprecation
        onBackPressed();//go back by clicking back button of actionbar
        return super.onSupportNavigateUp();
    }
    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        //result of permission allowed/denied
        switch(requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    //if allowed returns true otherwise false
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && storageAccepted){
                        //both permission allowed
                        pickFromCamera();
                    }
                    else{
                        Toast.makeText(this,"Camera & Storage permissions are required ",Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0) {
                    //if allowed returns true otherwise false
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        //storage permission allowed
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Storage permission is required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //image picked from camera of gallery will be received here
        if(resultCode == RESULT_OK){
            //image is picked
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                //picked from gallery

                //crop image
                assert data != null;
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(this);

            }
            else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                //picked from camera

                //crop image
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(this);

            }
            else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                //cropped image received
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                assert result != null;
                Uri resultUri = result.getUri();
                imageUri =resultUri ;
                //set image
                profileIv.setImageURI(resultUri);

                copyFileOrDirectory(imageUri.getPath(), String.valueOf(getDir("SQLiteRecordImages", MODE_PRIVATE)));
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}