package com.example.carkilahan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextClock;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.example.carkilahan.Adapters.BookingsAdapter;
import com.example.carkilahan.Classes.Booking;

public class AllBookingsActivity extends AppCompatActivity {
    private static final String TAG = "AllBookingsActivity";

    //firebase
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private CollectionReference bookingsReference, usersReference;

    //fields
    private static final String ENTITY_USERS = "Users";
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_USER_ROLE = "role";

    private static final String ENTITY_BOOKINGS = "Bookings";
    private static final String KEY_BOOKING_CAR_ID = "car_id";
    private static final String KEY_BOOKING_DATE_WHEN = "date_when";
    private static final String KEY_BOOKING_DATE_BOOKED = "date_booked";
    private static final String KEY_BOOKING_TIME_WHEN = "time_when";
    private static final String KEY_BOOKING_TIME_BOOKED = "time_booked";
    private static final String KEY_BOOKING_PRICE = "price";
    private static final String KEY_BOOKING_TYPE = "type";
    private static final String KEY_BOOKING_USER_ID = "user_id";
    private static final String KEY_BOOKING_USER_NAME = "user_name";

    //views
    private RecyclerView rViewAllBookings;
    private BookingsAdapter bookingAdapter;
    private Spinner spinnerNames;
    private ImageView imgViewEmptyLogo;
    private TextView txtViewBookingsCounter,
                     txtViewNoClientsBookingActivityTitle;

    //variables
    private static String bookingID,
                          bookingCarID,
                          bookingDateWhen,
                          bookingDateBooked,
                          bookingTimeWhen,
                          bookingTimeBooked,
                          bookingPrice,
                          bookingType,
                          bookingUserID,
                          bookingUserName;

    private ArrayList<Booking> allBookings = new ArrayList<>();
    private ArrayList<String> userNames = new ArrayList<>();

    //date
    private TextView txtViewDate;
    private SimpleDateFormat dateFormat;
    private String date;
    private Calendar calendar;
    private TextClock txtClock;

    //intents
    private static String PARENT_ACTIVITY_HOLDER = "AdminDashboardActivity";

    @Override
    protected void onStart() {
        super.onStart();
        allBookings.clear();
        getUserNames();

        Log.d(TAG, "onStart: called");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.admin_bookings_menu, menu);

        Log.d(TAG, "onCreateOptionsMenu: called");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_back: {
                startActivity(new Intent(AllBookingsActivity.this, AdminDashboardActivity.class));
            }
        }

        Log.d(TAG, "onOptionsItemSelected: called");
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_bookings);

        initAll();

        spinnerNames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean isSpinnerInitial = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isSpinnerInitial) {
                    isSpinnerInitial = false;
                } else {
                    allBookings.clear();

                    if (spinnerNames.getSelectedItem().toString().equals("All")) {
                        getAllBookings();
                    } else {
                        bookingsReference = db.collection(ENTITY_BOOKINGS);
                        bookingsReference
                                .whereEqualTo(KEY_BOOKING_USER_NAME, spinnerNames.getSelectedItem().toString())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            bookingID = documentSnapshot.getId();
                                            bookingCarID = documentSnapshot.getString(KEY_BOOKING_CAR_ID);
                                            bookingDateWhen = documentSnapshot.getString(KEY_BOOKING_DATE_WHEN);
                                            bookingDateBooked = documentSnapshot.getString(KEY_BOOKING_DATE_BOOKED);
                                            bookingTimeWhen = documentSnapshot.getString(KEY_BOOKING_TIME_WHEN);
                                            bookingTimeBooked = documentSnapshot.getString(KEY_BOOKING_TIME_BOOKED);
                                            bookingPrice = documentSnapshot.getString(KEY_BOOKING_PRICE);
                                            bookingType = documentSnapshot.getString(KEY_BOOKING_TYPE);
                                            bookingUserID = documentSnapshot.getString(KEY_BOOKING_USER_ID);
                                            bookingUserName = documentSnapshot.getString(KEY_BOOKING_USER_NAME);

                                            allBookings.add(new Booking(bookingID,
                                                                        bookingCarID,
                                                                        bookingDateWhen,
                                                                        bookingDateBooked,
                                                                        bookingTimeWhen,
                                                                        bookingTimeBooked,
                                                                        bookingPrice,
                                                                        bookingType,
                                                                        bookingUserID,
                                                                        bookingUserName));
                                        }

                                        loadBookings(allBookings);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, e.getMessage());
                                    }
                                });
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });

        Log.d(TAG, "onCreate: called");
    }

    private void initAll() {
        txtClock = findViewById(R.id.text_clock_time);
        txtViewDate = findViewById(R.id.text_view_date);
        rViewAllBookings = findViewById(R.id.recycler_view_all_bookings);
        spinnerNames = findViewById(R.id.spinner_names);
        imgViewEmptyLogo = findViewById(R.id.image_view_empty_logo);
        txtViewBookingsCounter = findViewById(R.id.text_view_bookings_counter);
        txtViewNoClientsBookingActivityTitle = findViewById(R.id.text_view_no_clients_booking_activity_title);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();

        dateFormat = new SimpleDateFormat("MMM d, yyyy");
        date = dateFormat.format(calendar.getTime());
        txtViewDate.setText(date);

        Log.d(TAG, "initAll: called");
    }

    private void getUserNames() {
        userNames.add("All");
        usersReference = db.collection(ENTITY_USERS);
        usersReference
                .whereEqualTo(KEY_USER_ROLE, "2")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            userNames.add(documentSnapshot.getString(KEY_USER_NAME));
                        }

                        ArrayAdapter<String> namesAdapter = new ArrayAdapter<String>(AllBookingsActivity.this, R.layout.spinner_item, userNames);
                        namesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerNames.setAdapter(namesAdapter);
                    }
                });

        getAllBookings();

        Log.d(TAG, "getUserNames: called");
    }

    private void getAllBookings() {
        bookingsReference = db.collection(ENTITY_BOOKINGS);
        bookingsReference
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                            bookingID = documentSnapshot.getId();
                            bookingCarID = documentSnapshot.getString(KEY_BOOKING_CAR_ID);
                            bookingDateWhen = documentSnapshot.getString(KEY_BOOKING_DATE_WHEN);
                            bookingDateBooked = documentSnapshot.getString(KEY_BOOKING_DATE_BOOKED);
                            bookingTimeWhen = documentSnapshot.getString(KEY_BOOKING_TIME_WHEN);
                            bookingTimeBooked = documentSnapshot.getString(KEY_BOOKING_TIME_BOOKED);
                            bookingPrice = documentSnapshot.getString(KEY_BOOKING_PRICE);
                            bookingType = documentSnapshot.getString(KEY_BOOKING_TYPE);
                            bookingUserID = documentSnapshot.getString(KEY_BOOKING_USER_ID);
                            bookingUserName = documentSnapshot.getString(KEY_BOOKING_USER_NAME);

                            allBookings.add(new Booking(bookingID,
                                                        bookingCarID,
                                                        bookingDateWhen,
                                                        bookingDateBooked,
                                                        bookingTimeWhen,
                                                        bookingTimeBooked,
                                                        bookingPrice,
                                                        bookingType,
                                                        bookingUserID,
                                                        bookingUserName));
                        }

                        loadBookings(allBookings);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                });

        Log.d(TAG, "getAllBookings: called");
    }

    private void loadBookings(ArrayList<Booking> allBookings) {
        txtViewBookingsCounter.setText(Integer.toString(allBookings.size()));

        if (allBookings.size() == 0) {
            imgViewEmptyLogo.setVisibility(View.VISIBLE);
            txtViewNoClientsBookingActivityTitle.setVisibility(View.VISIBLE);
        } else {
            imgViewEmptyLogo.setVisibility(View.GONE);
            txtViewNoClientsBookingActivityTitle.setVisibility(View.GONE);

            bookingAdapter = new BookingsAdapter(AllBookingsActivity.this, PARENT_ACTIVITY_HOLDER);
            rViewAllBookings.setAdapter(bookingAdapter);
            rViewAllBookings.setLayoutManager(new LinearLayoutManager(AllBookingsActivity.this));
            bookingAdapter.setBookings(allBookings);
        }

        Log.d(TAG, "loadBookings: called");
    }
}