package com.example.carkilahan;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.carkilahan.Classes.Utils;
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

public class ClientDashboardActivity extends AppCompatActivity {
    private static final String TAG = "ClientDashboardActivity";

    //firebase
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private DocumentReference userReference;

    //fields
    private static final String ENTITY_USERS = "Users";
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_USER_IMAGE_URL = "image_url";

    //views
    private TextView txtViewEmail;
    private CardView cardViewBrowseCars,
                     cardViewMyBookings;

    private ImageView imgViewAvatar;

    //static variables
    private static String userID,
                          userName,
                          userEmail,
                          userImageURL;

    //for date format
    private TextView txtViewDate;
    private SimpleDateFormat dateFormat;
    private String date;
    private Calendar calendar;
    private TextClock txtClockTime;

    //intents
    private static String PARENT_ACTIVITY_TITLE = "parent_activity";
    private static String PARENT_CLIENT_DASHBOARD_ACTIVITY = "ClientDashboardActivity";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.client_dashboard_menu, menu);

        Log.d(TAG, "onCreateOptionsMenu: called");
        Utils.divider();

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
        Utils.divider();
        return super.onOptionsItemSelected(item);
    }

    private void logOutUser() {
        AuthUI.getInstance()
                .signOut(ClientDashboardActivity.this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Sign out successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ClientDashboardActivity.this, MainActivity.class);
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
        Utils.divider();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_dashboard);

        initAll();

        imgViewAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ClientDashboardActivity.this, ProfileActivity.class));
            }
        });

        cardViewBrowseCars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientDashboardActivity.this, AllCarsActivity.class);
                intent.putExtra(PARENT_ACTIVITY_TITLE, PARENT_CLIENT_DASHBOARD_ACTIVITY);
                startActivity(intent);
            }
        });

        cardViewMyBookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ClientDashboardActivity.this, ClientBookingsActivity.class));
            }
        });

        Log.d(TAG, "onCreate: called");
        Utils.divider();
    }

    private void initAll() {
        imgViewAvatar = findViewById(R.id.image_view_avatar);
        cardViewBrowseCars = findViewById(R.id.card_view_browse_cars);
        cardViewMyBookings = findViewById(R.id.card_view_my_bookings);
        txtViewEmail = findViewById(R.id.text_view_email);
        txtViewDate = findViewById(R.id.text_view_date);
        txtClockTime = findViewById(R.id.text_clock_time);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        calendar = Calendar.getInstance();

        dateFormat = new SimpleDateFormat("MMM d, yyyy");
        date = dateFormat.format(calendar.getTime());
        txtViewDate.setText(date);

        userID = currentUser.getUid();
        userEmail = currentUser.getEmail();

        getUserData();

        Log.d(TAG, "initAll: called");
    }

    private void getUserData() {
        userReference = db.collection(ENTITY_USERS).document(userID);
        userReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            userName = documentSnapshot.getString(KEY_USER_NAME);
                            String userEmailModified = modifyEmail(userEmail);
                            userImageURL = documentSnapshot.getString(KEY_USER_IMAGE_URL);

                            loadData(userEmailModified, userImageURL);
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

        Log.d(TAG, "getUserData: called");
    }

    private String modifyEmail(String userEmail) {
        String userEmailModified = "";
        for (int i = 0; i < userEmail.length(); i ++) {
            if (i == 0) {
                userEmailModified += userEmail.charAt(i);
            }

            userEmailModified += "*";

            if (userEmail.charAt(i) == '@') {
                userEmailModified += userEmail.substring(userEmail.indexOf('@'));
                break;
            }
        }

        Log.d(TAG, "modifyEmail: called");

        return userEmailModified;
    }

    private void loadData(String email, String user_imageURL) {
        txtViewEmail.setText(email);

        if (user_imageURL != "") {
            Glide.with(this)
                    .asBitmap()
                    .load(user_imageURL)
                    .into(imgViewAvatar);
        }

        Log.d(TAG, "loadData: called");
    }
}