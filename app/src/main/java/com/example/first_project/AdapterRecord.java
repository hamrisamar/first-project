package com.example.first_project;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/*custom Adapter class for recycleView
*Here we will inflate row_record.xml
* *get and set data to views */
public class AdapterRecord extends RecyclerView.Adapter<AdapterRecord.HolderRecord>  {

    //variables
    private final Context context;
    private final ArrayList<ModelRecord> recordsList;

    MyDbHelper dbHelper;

    public AdapterRecord(Context context,ArrayList<ModelRecord> recordsList){
        this.context = context;
        this.recordsList = recordsList ;
        dbHelper = new MyDbHelper(context);
    }

    @NonNull
    @Override
    public HolderRecord onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view= LayoutInflater.from(context).inflate(R.layout.row_record, parent,  false);
        return new HolderRecord(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderRecord holder, @SuppressLint("RecyclerView") int position) {
        // Get data, set data, handle view clicks in this method

        // Get data
         ModelRecord model = recordsList.get(position);
        String id = model.getId();
        String name = model.getName();
        String image = model.getImage();
        String bio = model.getBio();
        String phone = model.getPhone();
        String email = model.getEmail();
        String dob = model.getDob();
        String addedTime = model.getAddedTime();
        String updatedTime = model.getUpdatedTime();

        // Set data
        holder.nameTv.setText(name);
        holder.phoneTv.setText(phone);
        holder.emailTv.setText(email);
        holder.dobTv.setText(dob);
        //if user doesn't attach image then imageUri .....
        if (image.equals("null")){
            //no image in record , set default
            holder.profileIv.setImageResource(R.drawable.ic_person_black);
        }
        else{
            //have image in record
            holder.profileIv.setImageURI(Uri.parse(image));
        }
        holder.profileIv.setImageURI(Uri.parse(image));

        //handle item clicks (go to detail record activity)
        holder.itemView.setOnClickListener(v -> {
            //pass record id to next activity to show details of that record
            Intent intent = new Intent(context, RecordDetailActivity.class);
            intent.putExtra("RECORD_RD", id);
            context.startActivity(intent);
        });
        //handle more button click listener (show options like edit ,delete et)
        holder.moreBtn.setOnClickListener(v -> {
            //show options menu
            showMoreDialog(
                    String.valueOf(position),
                    id,
                    name,
                    phone,
                    email,
                    dob,
                    bio,
                    image,
                    addedTime,
                    updatedTime
                    );
        });
        Log.d("ImagePath","onBindViewHolder:"+image);
    }

    private void showMoreDialog(String ignoredPosition, final String id, final String name, final String phone, final String email,
                                final String dob, final String image, final String bio , final String addedTime, final String  updatedTime) {
        //option to display in dialog
        String[] options = {"Edit", "Delete"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //add items to dialog
        builder.setItems(options, (dialog, which) -> {
            //handle item clicks
            if (which==0){
                //Edit is clicked

                //start AddUpdateRecordActivity to update existing record
                Intent intent =new Intent(context, AddUpdateRecordActivity.class);
                intent.putExtra("ID", id);
                intent.putExtra("NAME", name);
                intent.putExtra("PHONE", phone);
                intent.putExtra("EMAIL", email);
                intent.putExtra("DOB", dob);
                intent.putExtra("BIO", bio);
                intent.putExtra("IMAGE",image);
                intent.putExtra("ADDED_TIME",addedTime);
                intent.putExtra("UPDATED_TIME",updatedTime);
                intent.putExtra("isEditMode",true);
                context.startActivity(intent);


            } else if (which==1) {
                //delete is clicked
                dbHelper.deleteData(id);
                //refresh record by calling activity's onResume method
                ((MainActivity)context).onResume();
            }
        });
        //show dialog
        builder.create().show();
    }

    @Override
    public int getItemCount() {
        return recordsList.size(); //return size of list/number or records
    }


    static class HolderRecord extends RecyclerView.ViewHolder{
        //views
        ImageView profileIv;
        TextView nameTv , phoneTv, emailTv, dobTv;
        ImageButton moreBtn;

        public HolderRecord(@NonNull View itemView) {
            super(itemView);
            //init views
            profileIv = itemView.findViewById(R.id.profileIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            phoneTv =  itemView.findViewById(R.id.phoneTv);
            emailTv =  itemView.findViewById(R.id.emailTv);
            dobTv =  itemView.findViewById(R.id.dobTv);
            moreBtn =  itemView.findViewById(R.id.moreBtn);
        }
    }



}
