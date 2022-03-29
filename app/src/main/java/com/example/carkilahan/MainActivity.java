package com.example.carkilahan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.carkilahan.Classes.Utils;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 123;

    //firebase
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private DocumentReference userReference;

    //fields
    private static final String ENTITY_USERS = "Users";
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_USER_CONTACT_NO = "contact_no";
    private static final String KEY_USER_ROLE = "role";
    private static final String KEY_USER_IMAGE_URL = "image_url";

    //static variables
    private static String userID,
                          userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        buildSignInUI();
        Log.d(TAG, "onCreate: called");
        Utils.divider();
    }

    private void buildSignInUI() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build()
        );

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.Theme_Carkilahan)
                        .build(),
                RC_SIGN_IN);

        Log.d(TAG, "buildSignInUI: called");
        Utils.divider();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                initAll();
            } else if (response == null) {
                Toast.makeText(getApplicationContext(), "Sign in failed", Toast.LENGTH_SHORT).show();
                Log.d(TAG, response.getError().getMessage());
                finish();
            }
        }

        Log.d(TAG, "onActivityResult: called");
        Utils.divider();
    }

    private void initAll() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        userID = currentUser.getUid();
        userName = currentUser.getDisplayName();

        userReference = db.collection(ENTITY_USERS).document(userID);
        userReference
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.exists()) {
                            showRolesDialog();
                        } else {
                            goToDashboard();
                        }
                    }
                });

        Log.d(TAG, "initAll: called");
        Utils.divider();
    }

    private void showRolesDialog() {
        AlertDialog.Builder builderRoles = new AlertDialog.Builder(MainActivity.this);
        builderRoles.setTitle("Choose a role");

        LinearLayout layoutRoles = new LinearLayout(MainActivity.this);
        layoutRoles.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(50, 50, 50, 50);

        final RadioGroup rGroupRoles = new RadioGroup(MainActivity.this);
        rGroupRoles.setLayoutParams(lp);
        final RadioButton[] rButtonRoles = new RadioButton[2];
        rButtonRoles[0] = new RadioButton(MainActivity.this);
        rButtonRoles[1] = new RadioButton(MainActivity.this);
        rButtonRoles[0].setText("Admin");
        rButtonRoles[1].setText("Client");
        rGroupRoles.addView(rButtonRoles[0]);
        rGroupRoles.addView(rButtonRoles[1]);

        layoutRoles.addView(rGroupRoles);
        builderRoles.setView(layoutRoles);

        builderRoles.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userRole = "";
                userRole = (rButtonRoles[0].isChecked() ? "1" : "2");
                saveUser(userID, userName, userRole);
            }
        });

        builderRoles.create().show();

        Log.d(TAG, "showRolesDialog: called");
        Utils.divider();
    }

    private void saveUser(String userID, String userName, String userRole) {
        Map<String, Object> newUser = new HashMap<>();
        newUser.put(KEY_USER_NAME, userName);
        newUser.put(KEY_USER_CONTACT_NO, "");
        newUser.put(KEY_USER_ROLE, userRole);
        newUser.put(KEY_USER_IMAGE_URL, "");

        userReference = db.collection(ENTITY_USERS).document(userID);
        userReference
                .set(newUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        goToDashboard();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Sign in failed", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.getMessage());
                    }
                });

        Log.d(TAG, "saveUser: called");
        Utils.divider();
    }

    private void goToDashboard() {
        userReference = db.collection(ENTITY_USERS).document(userID);
        userReference
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.getString(KEY_USER_ROLE).equals("1")) {
                            startActivity(new Intent(MainActivity.this, AdminDashboardActivity.class));
                        } else {
                            startActivity(new Intent(MainActivity.this, ClientDashboardActivity.class));
                        }

                        Toast.makeText(getApplicationContext(), "Signed in successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

        Log.d(TAG, "goToDashboard: called");
        Utils.divider();
    }
}