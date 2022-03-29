package com.example.carkilahan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.example.carkilahan.Classes.Utils;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.example.carkilahan.Adapters.UsersAdapter;
import com.example.carkilahan.Classes.User;

public class AllUsersActivity extends AppCompatActivity {
    private static final String TAG = "AllUsersActivity";

    //firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private CollectionReference usersReference;
    private DocumentReference userReference;

    //fields
    private static final String ENTITY_USERS = "Users";
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_USER_CONTACT_NO = "contact_no";
    private static final String KEY_USER_ROLE = "role";
    private static final String KEY_USER_IMAGE_URL = "image_url";

    //views
    private RecyclerView rViewUsers;
    private UsersAdapter userAdapter;
    private ImageView imgViewEmptyLogo;
    private TextView txtViewUsersCounter,
                     txtViewNoUsersTitle;

    //variables
    private static String userID,
                          userName,
                          userContactNo,
                          userRole,
                          userImageURL;

    private ArrayList<User> allUsers = new ArrayList<>();

    //date
    private TextView txtViewDate;
    private SimpleDateFormat dateFormat;
    private String date;
    private Calendar calendar;
    private TextClock txtClock;

    private String parentActivity;

    @Override
    protected void onStart() {
        super.onStart();
        allUsers.clear();
        getUsers();

        Log.d(TAG, "onStart: called");
        Utils.divider();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.admin_users_menu, menu);

        Log.d(TAG, "onCreateOptionsMenu: called");
        Utils.divider();

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        initAll();

        Log.d(TAG, "onCreate: called");
        Utils.divider();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_back: {
                startActivity(new Intent(AllUsersActivity.this, AdminDashboardActivity.class));
                break;
            }
        }

        Log.d(TAG, "onOptionsItemSelected: called");
        Utils.divider();
        return super.onOptionsItemSelected(item);
    }

    private void initAll() {
        txtClock = findViewById(R.id.text_clock_time);
        txtViewDate = findViewById(R.id.text_view_date);
        rViewUsers = findViewById(R.id.recycler_view_all_users);
        imgViewEmptyLogo = findViewById(R.id.image_view_empty_logo);
        txtViewNoUsersTitle = findViewById(R.id.text_view_no_users_title);
        txtViewUsersCounter = findViewById(R.id.text_view_users_counter);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        dateFormat = new SimpleDateFormat("MMM d, yyyy");
        date = dateFormat.format(calendar.getTime());
        txtViewDate.setText(date);

        Log.d(TAG, "initAll: called");
        Utils.divider();
    }

    private void getUsers() {
        usersReference = db.collection(ENTITY_USERS);
        usersReference
                .whereEqualTo(KEY_USER_ROLE, "2")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                            userID = documentSnapshot.getId();
                            userName = documentSnapshot.getString(KEY_USER_NAME);
                            userContactNo = documentSnapshot.getString(KEY_USER_CONTACT_NO);
                            userRole = documentSnapshot.getString(KEY_USER_ROLE);
                            userImageURL = documentSnapshot.getString(KEY_USER_IMAGE_URL);

                            allUsers.add(new User(userID,
                                                  userName,
                                                  userContactNo,
                                                  userRole,
                                                  userImageURL));
                        }

                        loadUsers(allUsers);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                });

        Log.d(TAG, "getUsers: called");
        Utils.divider();
    }

    private void loadUsers(ArrayList<User> allUsers) {
        txtViewUsersCounter.setText(Integer.toString(allUsers.size()));

        if (allUsers.size() == 0) {
            imgViewEmptyLogo.setVisibility(View.VISIBLE);
            txtViewNoUsersTitle.setVisibility(View.VISIBLE);
        } else {
            imgViewEmptyLogo.setVisibility(View.GONE);
            txtViewNoUsersTitle.setVisibility(View.GONE);

            userAdapter = new UsersAdapter(AllUsersActivity.this);
            rViewUsers.setAdapter(userAdapter);
            rViewUsers.setLayoutManager(new LinearLayoutManager(AllUsersActivity.this));
            userAdapter.setAllUsers(allUsers);
        }

        Log.d(TAG, "loadUsers: called");
        Utils.divider();
    }

    private void showNewUserDialog() {
        AlertDialog.Builder builderNewUser = new AlertDialog.Builder(this);
        builderNewUser.setTitle("New user | Client");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams lpFirstView = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpFirstView.setMargins(50, 70,50, 20);

        LinearLayout.LayoutParams lpViews = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpViews.setMargins(50, 20, 50, 20);

        final EditText editTxtName = new EditText(this);
        editTxtName.setHint("Name");
        editTxtName.setLayoutParams(lpFirstView);
        editTxtName.setInputType(InputType.TYPE_CLASS_TEXT);

        final EditText editTxtEmail = new EditText(this);
        editTxtEmail.setHint("Email");
        editTxtEmail.setLayoutParams(lpViews);
        editTxtEmail.setInputType(InputType.TYPE_CLASS_TEXT);

        final EditText editTxtPassword = new EditText(this);
        editTxtPassword.setHint("Password");
        editTxtPassword.setLayoutParams(lpViews);
        editTxtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        final EditText editTxtContactNo = new EditText(this);
        editTxtContactNo.setHint("Contact no.");
        editTxtContactNo.setLayoutParams(lpViews);
        editTxtContactNo.setInputType(InputType.TYPE_CLASS_NUMBER);

        layout.addView(editTxtName);
        layout.addView(editTxtEmail);
        layout.addView(editTxtPassword);
        layout.addView(editTxtContactNo);

        builderNewUser.setView(layout);

        builderNewUser.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addNewUser(editTxtName.getText().toString(),
                           editTxtEmail.getText().toString(),
                           editTxtPassword.getText().toString(),
                           editTxtContactNo.getText().toString());
            }
        });

        builderNewUser.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builderNewUser.create().show();

        Log.d(TAG, "showNewUserDialog: called");
        Utils.divider();
    }

    private void addNewUser(String userName, String userEmail, String userPassword, String userContactNo) {
        auth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser newUserAdded = FirebaseAuth.getInstance().getCurrentUser();

                            Map<String, Object> newUser = new HashMap<>();
                            newUser.put(KEY_USER_NAME, userName);
                            newUser.put(KEY_USER_CONTACT_NO, userContactNo);
                            newUser.put(KEY_USER_IMAGE_URL, "");
                            newUser.put(KEY_USER_ROLE, "2");

                            userReference = db.collection(ENTITY_USERS).document(newUserAdded.getUid());
                            userReference
                                    .set(newUser)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getApplicationContext(), "User added", Toast.LENGTH_SHORT).show();
                                            AuthUI.getInstance().signOut(AllUsersActivity.this).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        startActivity(getIntent());
                                                    } else {
                                                        Toast.makeText(getApplicationContext(), "Sign out failed", Toast.LENGTH_SHORT).show();
                                                        Log.d(TAG, task.getException().getMessage());
                                                    }
                                                }
                                            });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(), "User not added", Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, e.getMessage());
                                        }
                                    });
                        } else {
                            Log.w(TAG, "Invalid credentials", task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}