package com.example.carkilahan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.carkilahan.Classes.Utils;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    //firebase
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private DocumentReference userReference, bookingReference;
    private CollectionReference bookingsReference;

    //fields
    private static final String ENTITY_USERS = "Users";
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_USER_EMAIL = "email";
    private static final String KEY_USER_CONTACT_NO = "contact_no";
    private static final String KEY_USER_IMAGE_URL = "image_url";

    private static final String ENTITY_BOOKINGS = "Bookings";
    private static final String KEY_BOOKING_USER_ID = "user_id";

    //views
    private EditText editTxtName,
                     editTxtEmail,
                     editTxtContactNo;

    private Button btnUpdate,
                   btnConfirmUpdate,
                   btnDeleteAccount;

    private ImageView imgViewChangeImage;
    private TextView txtViewUID;
    private ImageView imgViewAvatar;

    //static variables
    private static String userID,
                          userEmail,
                          userName,
                          userContactNo,
                          userImageURL;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.client_profile_menu, menu);

        Log.d(TAG, "onCreateOptionsMenu: called");
        Utils.divider();

        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_back: {
                startActivity(new Intent(ProfileActivity.this, ClientDashboardActivity.class));
            }
        }

        Log.d(TAG, "onOptionsItemSelected: called");
        Utils.divider();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initAll();

        imgViewChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageUrlDialog();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableEditTexts();
                btnUpdate.getBackground().setAlpha(128);
                btnConfirmUpdate.getBackground().setAlpha(255);
                btnUpdate.setEnabled(false);
                btnConfirmUpdate.setEnabled(true);
            }
        });

        btnConfirmUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableEditTexts();
                updateCredentials();
                btnConfirmUpdate.getBackground().setAlpha(128);
                btnUpdate.getBackground().setAlpha(255);
                btnConfirmUpdate.setEnabled(false);
                btnUpdate.setEnabled(true);
            }
        });

        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder deleteDialog = new AlertDialog.Builder(ProfileActivity.this);
                deleteDialog.setMessage("Are you sure you want to delete your account? It will also delete all the bookings you made");
                deleteDialog.setTitle("WARNING");
                deleteDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder deleteConfirmDialog = new AlertDialog.Builder(ProfileActivity.this);
                        deleteConfirmDialog.setMessage("This process can not be undone, proceed?");
                        deleteConfirmDialog.setTitle("WARNING");
                        deleteConfirmDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteCurrentUser();
                            }
                        });

                        deleteConfirmDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        deleteConfirmDialog.create().show();
                    }
                });

                deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                deleteDialog.create().show();
            }
        });

        Log.d(TAG, "onCreate: called");
        Utils.divider();
    }

    private void initAll() {
        imgViewAvatar = findViewById(R.id.image_view_avatar);
        editTxtName = findViewById(R.id.edit_text_name);
        editTxtEmail = findViewById(R.id.edit_text_email);
        editTxtContactNo = findViewById(R.id.edit_text_contact_no);
        imgViewChangeImage = findViewById(R.id.image_view_change_image);
        btnUpdate = findViewById(R.id.button_update_profile);
        btnConfirmUpdate = findViewById(R.id.button_confirm_update);
        btnDeleteAccount = findViewById(R.id.button_delete_account);
        txtViewUID = findViewById(R.id.text_view_uid);
        btnConfirmUpdate.getBackground().setAlpha(128);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        userID = currentUser.getUid();
        userEmail = currentUser.getEmail();

        getUserData();

        Log.d(TAG, "initAll: called");
        Utils.divider();
    }

    private void getUserData() {
        userReference = db.collection(ENTITY_USERS).document(userID);
        userReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            userName = documentSnapshot.getString(KEY_USER_NAME);
                            userContactNo = documentSnapshot.getString(KEY_USER_CONTACT_NO);
                            userImageURL = documentSnapshot.getString(KEY_USER_IMAGE_URL);
                            loadUser(userID, userName, userEmail, userContactNo, userImageURL);
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
        Utils.divider();
    }

    private void updateCredentials() {
        userName = editTxtName.getText().toString();
        userContactNo = editTxtContactNo.getText().toString();

        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put(KEY_USER_NAME, userName);
        userUpdate.put(KEY_USER_CONTACT_NO, userContactNo);

        userReference = db.collection(ENTITY_USERS).document(userID);
        userReference
                .set(userUpdate, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Update success", Toast.LENGTH_SHORT).show();
                        disableEditTexts();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                });

        Log.d(TAG, "updateCredentials: called");
        Utils.divider();
    }

    private void loadUser(String user_id, String user_name, String user_email, String user_contactNo, String user_imageURL) {
        txtViewUID.setText(user_id);
        editTxtName.setText(user_name);
        editTxtEmail.setText(user_email);
        editTxtContactNo.setText(user_contactNo);

        if (user_imageURL != "") {
            Glide.with(this)
                    .asBitmap()
                    .load(user_imageURL)
                    .into(imgViewAvatar);
        }

        Log.d(TAG, "loadUser: called");
        Utils.divider();
    }

    private void enableEditTexts() {
        editTxtName.setEnabled(true);
        editTxtContactNo.setEnabled(true);
    }

    private void disableEditTexts() {
        editTxtName.setEnabled(false);
        editTxtContactNo.setEnabled(false);
    }

    private void showImageUrlDialog() {
        AlertDialog.Builder imageURLBuilder = new AlertDialog.Builder(this);
        imageURLBuilder.setTitle("Image URL");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(50, 50, 50, 50);

        final EditText editTxtImageURL = new EditText(this);
        editTxtImageURL.setHint("Enter Image URL");
        editTxtImageURL.setLayoutParams(lp);

        layout.addView(editTxtImageURL);
        imageURLBuilder.setView(layout);

        imageURLBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (editTxtImageURL.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Enter image URL", Toast.LENGTH_SHORT).show();
                } else {
                    loadImage(editTxtImageURL.getText().toString());
                }
            }
        });

        imageURLBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        imageURLBuilder.create().show();

        Log.d(TAG, "showImageUrlDialog: called");
    }

    private void loadImage(String imageURL) {
        Glide.with(this)
                .asBitmap()
                .load(imageURL)
                .into(imgViewAvatar);

        addUserImageURL(imageURL);

        Log.d(TAG, "loadImage: called");
    }

    private void addUserImageURL(String imageURL) {
        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put(KEY_USER_IMAGE_URL, imageURL);

        userReference = db.collection(ENTITY_USERS).document(userID);
        userReference.set(userUpdate, SetOptions.merge());

        Log.d(TAG, "addUserImageURL: called");
    }

    private void deleteCurrentUser() {
        getUserBookings();
    }

    private void getUserBookings() {
        ArrayList<String> bookingsID = new ArrayList<>();

        bookingsReference = db.collection(ENTITY_BOOKINGS);
        bookingsReference
                .whereEqualTo(KEY_BOOKING_USER_ID, userID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            bookingsID.add(documentSnapshot.getId());
                        }

                        deleteBookings(bookingsID);
                    }
                });
    }

    private void deleteBookings(ArrayList<String> bookingsID) {
        for (String bookingID : bookingsID) {
            bookingReference = db.collection(ENTITY_BOOKINGS).document(bookingID);
            bookingReference
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //some text
                        }
                    });
        }

        userReference = db.collection(ENTITY_USERS).document(userID);
        userReference.delete();

        currentUser.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            AuthUI.getInstance().signOut(ProfileActivity.this).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        Toast.makeText(ProfileActivity.this, "User account deleted", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Failed user deletion", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, task.getException().getMessage());
                                    }
                                }
                            });
                        }
                    }
                });
    }
}