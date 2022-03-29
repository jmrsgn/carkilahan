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
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.example.carkilahan.Classes.Utils;
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

public class ClientBookingsActivity extends AppCompatActivity {
    private static final String TAG = "AllBookingsActivity";

    //firebase
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private CollectionReference bookingsReference;

    //fields
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
    private RecyclerView rViewClientBookings;
    private BookingsAdapter bookingAdapter;
    private ImageView imgViewEmptyLogo;
    private TextView txtViewBookingsCounter,
                     txtViewNoBookingsActivityTitle,
                     txtViewBookOne;

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

    private ArrayList<Booking> clientBookings = new ArrayList<>();

    //date
    private TextView txtViewDate;
    private SimpleDateFormat dateFormat;
    private String date;
    private Calendar calendar;
    private TextClock txtClock;

    private static String PARENT_ACTIVITY_HOLDER = "ClientDashboardActivity";

    //intent
    private static String PARENT_ACTIVITY_TITLE = "parent_activity";
    private static String PARENT_CLIENT_DASHBOARD_ACTIVITY = "ClientDashboardActivity";

    @Override
    protected void onStart() {
        super.onStart();
        clientBookings.clear();
        getBookings();

        Log.d(TAG, "onStart: called");
        Utils.divider();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.client_bookings_menu, menu);

        Log.d(TAG, "onCreateOptionsMenu: called");
        Utils.divider();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_back: {
                startActivity(new Intent(ClientBookingsActivity.this, ClientDashboardActivity.class));
            }
        }

        Log.d(TAG, "onOptionsItemSelected: called");
        Utils.divider();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_bookings);

        initAll();

        txtViewBookOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientBookingsActivity.this, AllCarsActivity.class);
                intent.putExtra(PARENT_ACTIVITY_TITLE, PARENT_CLIENT_DASHBOARD_ACTIVITY);
                startActivity(intent);
            }
        });

        Log.d(TAG, "onCreate: called");
        Utils.divider();
    }

    private void initAll() {
        txtClock = findViewById(R.id.text_clock_time);
        txtViewDate = findViewById(R.id.text_view_date);
        rViewClientBookings = findViewById(R.id.recycler_view_client_bookings);
        imgViewEmptyLogo = findViewById(R.id.image_view_empty_logo);
        txtViewBookingsCounter = findViewById(R.id.text_view_bookings_counter);
        txtViewNoBookingsActivityTitle = findViewById(R.id.text_view_no_bookings_activity_title);
        txtViewBookOne = findViewById(R.id.text_view_book_one);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();

        dateFormat = new SimpleDateFormat("MMM d, yyyy");
        date = dateFormat.format(calendar.getTime());
        txtViewDate.setText(date);

        Log.d(TAG, "initAll: called");
        Utils.divider();
    }

    private void getBookings() {
        bookingsReference = db.collection(ENTITY_BOOKINGS);
        bookingsReference
                .whereEqualTo(KEY_BOOKING_USER_ID, currentUser.getUid())
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

                            clientBookings.add(new Booking(bookingID,
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

                        loadBookings(clientBookings);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                });

        Log.d(TAG, "getBookings: called");
        Utils.divider();
    }

    private void loadBookings(ArrayList<Booking> clientBookings) {
        txtViewBookingsCounter.setText(Integer.toString(clientBookings.size()));

        if (clientBookings.size() == 0) {
            imgViewEmptyLogo.setVisibility(View.VISIBLE);
            txtViewNoBookingsActivityTitle.setVisibility(View.VISIBLE);
            txtViewBookOne.setVisibility(View.VISIBLE);
        } else {
            imgViewEmptyLogo.setVisibility(View.GONE);
            txtViewNoBookingsActivityTitle.setVisibility(View.GONE);
            txtViewBookOne.setVisibility(View.GONE);

            bookingAdapter = new BookingsAdapter(ClientBookingsActivity.this, PARENT_ACTIVITY_HOLDER);
            rViewClientBookings.setAdapter(bookingAdapter);
            rViewClientBookings.setLayoutManager(new LinearLayoutManager(ClientBookingsActivity.this));
            bookingAdapter.setBookings(clientBookings);
        }

        Log.d(TAG, "loadBookings: called");
        Utils.divider();
    }
}