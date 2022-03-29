package com.example.carkilahan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AdminDashboardActivity extends AppCompatActivity {
    private static final String TAG = "AdminDashboardActivity";

    //firebase
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private DocumentReference userReference;

    //fields
    private static final String ENTITY_USERS = "Users";
    private static final String KEY_USER_NAME = "name";

    //views
    private Button btnAllUsers,
                   btnAllCars,
                   btnAllBookings,
                   btnAllTransactions;

    private TextClock txtClock;
    private TextView txtViewAdminName;

    //date
    private TextView txtViewDate;
    private SimpleDateFormat dateFormat;
    private String date;
    private Calendar calendar;

    //static variables
    private static String userID,
                          userEmail,
                          userName;

    //intent
    private static String PARENT_ACTIVITY_TITLE = "parent_activity";
    private static String PARENT_ADMIN_DASHBOARD_ACTIVITY = "AdminDashboardActivity";
    private static String PARENT_CLIENT_DASHBOARD_ACTIVITY = "ClientDashboardActivity";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.admin_dashboard_menu, menu);

        Log.d(TAG, "onCreateOptionsMenu: called");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_logout: {
                AlertDialog.Builder builderLogout = new AlertDialog.Builder(this);
                builderLogout.setMessage("Are you sure you want to log out?");
                builderLogout.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logOutUser();
                    }
                });

                builderLogout.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builderLogout.create().show();
            }
        }

        Log.d(TAG, "onOptionsItemSelected: called");
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initAll();

        btnAllUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDashboardActivity.this, AllUsersActivity.class));
            }
        });

        btnAllCars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, AllCarsActivity.class);
                intent.putExtra(PARENT_ACTIVITY_TITLE, PARENT_ADMIN_DASHBOARD_ACTIVITY);
                startActivity(intent);
            }
        });

        btnAllTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDashboardActivity.this, AllTransactionsActivity.class));
            }
        });

        btnAllBookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDashboardActivity.this, AllBookingsActivity.class));
            }
        });
    }

    private void initAll() {
        txtClock = findViewById(R.id.text_clock_time);
        txtViewDate = findViewById(R.id.text_view_date);
        btnAllUsers = findViewById(R.id.button_all_users);
        btnAllCars = findViewById(R.id.button_all_cars);
        btnAllBookings = findViewById(R.id.button_all_bookings);
        btnAllTransactions = findViewById(R.id.button_all_transactions);
        txtViewAdminName = findViewById(R.id.text_view_admin_name);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        calendar = Calendar.getInstance();

        dateFormat = new SimpleDateFormat("MMM d, yyyy");
        date = dateFormat.format(calendar.getTime());

        txtViewDate.setText(date);

        loadUserData();

        Log.d(TAG, "initAll: called");
    }

    private void loadUserData() {
        userID = currentUser.getUid();
        userEmail = currentUser.getEmail();

        userReference = db.collection(ENTITY_USERS).document(userID);
        userReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            userName = documentSnapshot.getString(KEY_USER_NAME);
                            loadData(userName);
                        } else {
                            Toast.makeText(getApplicationContext(), "Document doesn't exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.getMessage());
                    }
                });

        Log.d(TAG, "loadUserData: called");
    }

    private void loadData(String userName) {
        txtViewAdminName.setText(userName);

        Log.d(TAG, "loadData: called");
    }

    private void logOutUser() {
        AuthUI.getInstance().signOut(AdminDashboardActivity.this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Sign out successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent( AdminDashboardActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Sign out failed", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, task.getException().getMessage());
                }
            }
        });

        Log.d(TAG, "logOutUser: called");
    }
}