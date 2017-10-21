package com.prabhav.boss.future_g;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    private static String USER_ID;
    private final String CURRENT = "Current", DONE = "Done", FUTURE = "Future";
    DatabaseReference databaseReference1, databaseReference2;
    //firebase auth object
    private FirebaseAuth firebaseAuth;
    private ListView mlistViewCurrent, mlistViewDone, mlistViewFuture;
    private ArrayList<GetSetListView> currentArrayList, doneArrayList, futureArrayList;
    private ListViewAdapter currentListViewAdapter, doneListViewAdapter, futureListViewAdapter;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_current:
                    initializeListView(mlistViewCurrent, mlistViewDone, mlistViewFuture);
                    return true;
                case R.id.navigation_done:
                    initializeListView(mlistViewDone, mlistViewFuture, mlistViewCurrent);
                    return true;
                case R.id.navigation_future:
                    initializeListView(mlistViewFuture, mlistViewCurrent, mlistViewDone);
                    return true;
            }
            return false;
        }
    };

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.navigation_add:
                addNewItem();
                return true;
            case R.id.profile:
                usersProfile();
                return true;
            case R.id.rateUs:
                rateOurApp();
                return true;
            case R.id.help:
                helpUser();
                return true;
            case R.id.share:
                shareApp();
                return true;
            case R.id.logout:
                firebaseAuth.signOut();
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareApp() {
//        String shareBody = "https://play.google.com/store/apps/details?id=com.prabhav.play";
//        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
//        sharingIntent.setType("text/plain");
//        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "APP NAME (Open it in Google Play Store to Download the Application)");
//        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
//        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    private void rateOurApp() {
//        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
//        Copy App URL from Google Play Store.
//        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.prabhav.play"));
//        startActivity(intent);
    }

    private void helpUser() {
        String mailto = "mailto:prabhav.garg.boss@gmail.com";

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse(mailto));

        try {
            startActivity(emailIntent);
        } catch (ActivityNotFoundException e) {
            //TODO: Handle case where no email app is available
        }
    }

    private void usersProfile() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email="";
        if (user != null) {
            // User is signed in
            email = user.getEmail();
        }
        alertDialog.setTitle("User Email Id:");
        alertDialog.setMessage(email);
        alertDialog.setIcon(R.drawable.user);
        alertDialog.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String email="";
        if (user != null) {
            // User is signed in
            email = user.getEmail();
        }
        if (email != null) {
            email = email.replace(".","_");
            email = email.replace("@","__");
        }
        USER_ID = email;

        if(firebaseAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        initUI();
    }

    private void initUI() {
        mlistViewCurrent = (ListView) findViewById(R.id.listview_current);
        mlistViewDone = (ListView) findViewById(R.id.listview_done);
        mlistViewFuture = (ListView) findViewById(R.id.listview_future);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        initializeListView(mlistViewCurrent, mlistViewDone, mlistViewFuture);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setMessage("Loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        currentArrayList = new ArrayList<>();
        doneArrayList = new ArrayList<>();
        futureArrayList = new ArrayList<>();
        currentListViewAdapter = new ListViewAdapter(this, currentArrayList);
        doneListViewAdapter = new ListViewAdapter(this, doneArrayList);
        futureListViewAdapter = new ListViewAdapter(this, futureArrayList);
    }

    private void longPressOfItems(final int position) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Take Action...");
        alertDialog.setMessage("Select your action...??");
        alertDialog.setIcon(R.drawable.ic_next_week_black_24dp);

        alertDialog.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // mlistViewCurrent.
                databaseReference1 = FirebaseDatabase.getInstance().getReference(MainActivity.USER_ID).child(CURRENT);
                databaseReference1.child(currentArrayList.get(position).getId()).removeValue();
                currentArrayList.remove(position);
                Toast.makeText(getApplication(), "Successfully Deleted!!", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.setNegativeButton("DONE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplication(), "DONE", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.setNeutralButton("FUTURE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplication(), "FUTURE", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();
    }

    private String addNewItem() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.add_details, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        final EditText dialogueTitle = (EditText) promptsView.findViewById(R.id.dialogue_title);
        final EditText dialogueDescription = (EditText) promptsView.findViewById(R.id.dialogue_description);
        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                pushingDataToFirebase(getDate(), "Pending", dialogueTitle.getText().toString().trim(), dialogueDescription.getText().toString().trim(),R.drawable.ic_info_black_24dp);
            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
        return dialogueTitle.getText().toString().trim();
    }

    private void showDetails(int position) {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.show_details, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        final TextView titleHeading = (TextView) promptsView.findViewById(R.id.dialogue_title_heading);
        final TextView dateDetails = (TextView) promptsView.findViewById(R.id.date_details);
        final TextView statusDetails = (TextView) promptsView.findViewById(R.id.status_details);
        final TextView detailDescriptions = (TextView) promptsView.findViewById(R.id.dialogue_description_details);
        if(currentArrayList.get(position).getTitle().trim().equals("")){titleHeading.setText("Title");}
        else titleHeading.setText(currentArrayList.get(position).getTitle());
        dateDetails.setText(currentArrayList.get(position).getDate());
        if(currentArrayList.get(position).getStatus().trim().equals("")){
            statusDetails.setText("Pending");
        }else statusDetails.setText(currentArrayList.get(position).getStatus());
        if(currentArrayList.get(position).getDescription().trim().equals("")){
            detailDescriptions.setText("Work is pending...");}
        else detailDescriptions.setText(currentArrayList.get(position).getDescription());
        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseReference1 = FirebaseDatabase.getInstance().getReference(MainActivity.USER_ID).child(CURRENT);
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentArrayList.clear();
                progressDialog.cancel();
                //iterating through all the nodes
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //getting artist
                    GetSetListView getSetListView = postSnapshot.getValue(GetSetListView.class);
                    //adding artist to the list
                    currentArrayList.add(getSetListView);
                }
                //creating adapter
                currentListViewAdapter = new ListViewAdapter(MainActivity.this, currentArrayList);
                //attaching adapter to the listview
                mlistViewCurrent.setAdapter(currentListViewAdapter);
                mlistViewCurrent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        showDetails(position);
                    }
                });
                mlistViewCurrent.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        longPressOfItems(position);
                        return true;
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializeListView(ListView visibleList, ListView invisibleList1, ListView invisibleList2) {
        visibleList.setVisibility(View.VISIBLE);
        invisibleList1.setVisibility(View.INVISIBLE);
        invisibleList2.setVisibility(View.INVISIBLE);
    }

    public String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void pushingDataToFirebase(String dateString, String statusString, String titleString, String descriptionString, int image) {
        databaseReference2 = FirebaseDatabase.getInstance().getReference();
        String id = databaseReference2.push().getKey();
        GetSetListView getSetListView = new GetSetListView(titleString, dateString, descriptionString, statusString, id, image);
        databaseReference2.child(MainActivity.USER_ID).child(CURRENT).child(id).setValue(getSetListView);
        Toast.makeText(this, "Event Saved Successfully", Toast.LENGTH_LONG).show();
    }
}