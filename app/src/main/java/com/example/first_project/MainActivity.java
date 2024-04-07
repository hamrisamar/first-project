package com.example.first_project;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton addRecordBtn;
    private RecyclerView recordsRv;

    //DB helper
    private MyDbHelper dbHelper;

    //action bar
    ActionBar actionBar;

    //sort options
    String orderByNewest = Constants.C_ADDED_TIMESTAMP+ " DESC";
    String orderByOdest = Constants.C_ADDED_TIMESTAMP+ " ASC";
    String orderByTitleAsc = Constants.C_NAME+ " ASC";
    String orderByTitleDesc = Constants.C_NAME+ " DESC";

    String currentOrederByStatus = orderByNewest ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // actionBar = getSupportActionBar();
       // actionBar.setTitle("All Employees");

        //init views
        addRecordBtn = findViewById(R.id.addRecordBtn);
        recordsRv = findViewById(R.id.recordsRv);

        //init db helper class
        dbHelper = new MyDbHelper(this);

        //load records
        loadRecords(orderByNewest);

        //click to sta rt add record activity
        addRecordBtn.setOnClickListener((v)-> {
                //First Create an activity "AddRecordActivity"
                //startActivity( new Intent(MainActivity.this, AddUpdateRecordActivity.class));
            Intent intent = new Intent(new Intent(MainActivity.this, AddUpdateRecordActivity.class));
            intent.putExtra("isEditMode",false);  // want to add new data,set...
            startActivity(intent);
            });
    }

    private void loadRecords(String orderBy) {
        currentOrederByStatus = orderBy;
      AdapterRecord adapterRecord = new AdapterRecord(MainActivity.this,
              dbHelper.getAllRecords(orderBy));

      recordsRv.setAdapter(adapterRecord);

      //set num of records
        actionBar.setSubtitle("Total: "+dbHelper.getRecordsCount());
    }
   private void searchRecords(String query){
       AdapterRecord adapterRecord = new AdapterRecord(MainActivity.this,
               dbHelper.searchRecords(query));

       recordsRv.setAdapter(adapterRecord);
   }

   protected  void onResume(){
        super.onResume();
        loadRecords(currentOrederByStatus);//refresh records list
   }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate menu
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //searchView
         MenuItem item = menu.findItem(R.id.action_search);
         SearchView searchView = (SearchView) item.getActionView();
         searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

             @Override
             public boolean onQueryTextSubmit(String query) {
                 //search when search button on keyboard clicked
                 searchRecords(query);
                 return true;
             }

             @Override
             public boolean onQueryTextChange(String newText) {
                 //search as you type
                 searchRecords(newText);
                 return true;
             }
         });

        return super.onCreateOptionsMenu(menu);
    }
    public  boolean onOptionsItemSelected(MenuItem item){
        //handle menu items
        int id = item.getItemId();
        if (id==R.id.action_sort){
            //show sort options
            sortOptionDialog();
        }
        else if (id==R.id.action_delete_all){
            // delete all records
            dbHelper.deleteAllData();
            onResume();

        }
        return super.onOptionsItemSelected(item);
    }

    private void sortOptionDialog() {
        //options to display in dialog
        String[] options = {"Title Ascending", "Title Descending", "Newest", "Oldest"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort By")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle option click
                        if(which == 0){
                            //title ascending
                            loadRecords(orderByTitleAsc);
                        }
                        else if (which == 1 ){
                          //title  Descending
                            loadRecords(orderByTitleDesc);
                        }
                        else if (which == 2 ){
                           //Newest
                            loadRecords(orderByNewest);
                        }
                        else if (which == 3 ){
                           //Oldest
                            loadRecords(orderByOdest);
                        }
                    }
                })
                .create().show();
    }
}
