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

import com.example.carkilahan.Adapters.TransactionsAdapter;
import com.example.carkilahan.Classes.Transaction;

public class AllTransactionsActivity extends AppCompatActivity {
    private static final String TAG = "AllTransactionsActivity";

    //firebase
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private CollectionReference transactionsReference;

    //fields
    private static final String ENTITY_TRANSACTIONS = "Transactions";
    private static final String KEY_TRANSACTION_AMOUNT_GIVEN = "amount_given";
    private static final String KEY_TRANSACTION_AMOUNT_TO_PAY = "amount_to_pay";
    private static final String KEY_TRANSACTION_BOOKING_ID = "booking_id";
    private static final String KEY_TRANSACTION_CAR_ID = "car_id";
    private static final String KEY_TRANSACTION_DATE_BOOKED = "date_booked";
    private static final String KEY_TRANSACTION_DATE_WHEN = "date_when";
    private static final String KEY_TRANSACTION_TIME_BOOKED = "time_booked";
    private static final String KEY_TRANSACTION_TIME_WHEN = "time_when";
    private static final String KEY_TRANSACTION_USER_ID = "user_id";
    private static final String KEY_TRANSACTION_USER_NAME = "user_name";

    //views
    private RecyclerView rViewAllTransactions;
    private TransactionsAdapter transactionAdapter;
    private ImageView imgViewEmptyLogo;
    private TextView txtViewTransactionsCounter,
                     txtViewNoTransactionsActivityTitle;

    //variables
    private static String transactionID,
                          transactionAmountGiven,
                          transactionAmountToPay,
                          transactionBookingID,
                          transactionCarID,
                          transactionDateBooked,
                          transactionDateWhen,
                          transactionTimeBooked,
                          transactionTimeWhen,
                          transactionUserID,
                          transactionUserName;

    private ArrayList<Transaction> allTransactions = new ArrayList<>();

    //date
    private TextView txtViewDate;
    private SimpleDateFormat dateFormat;
    private String date;
    private Calendar calendar;
    private TextClock txtClock;

    private static String PARENT_ACTIVITY_HOLDER = "AdminDashboardActivity";

    @Override
    protected void onStart() {
        super.onStart();
        allTransactions.clear();
        getAllTransactions();

        Log.d(TAG, "onStart: called");
        Utils.divider();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.admin_transactions_menu, menu);

        Log.d(TAG, "onCreateOptionsMenu: called");
        Utils.divider();

        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_back: {
                startActivity(new Intent(AllTransactionsActivity.this, AdminDashboardActivity.class));
            }
        }

        Log.d(TAG, "onOptionsItemSelected: called");
        Utils.divider();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_transactions);

        initAll();

        Log.d(TAG, "onCreate: called");
        Utils.divider();
    }

    private void initAll() {
        txtClock = findViewById(R.id.text_clock_time);
        txtViewDate = findViewById(R.id.text_view_date);
        rViewAllTransactions = findViewById(R.id.recycler_view_all_transactions);
        imgViewEmptyLogo = findViewById(R.id.image_view_empty_logo);
        txtViewTransactionsCounter = findViewById(R.id.text_view_transactions_counter);
        txtViewNoTransactionsActivityTitle = findViewById(R.id.text_view_no_transactions_activity_title);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();

        dateFormat = new SimpleDateFormat("MMM d, yyyy");
        date = dateFormat.format(calendar.getTime());
        txtViewDate.setText(date);

        Log.d(TAG, "initAll: called");
        Utils.divider();
    }

    private void getAllTransactions() {
        transactionsReference = db.collection(ENTITY_TRANSACTIONS);
        transactionsReference
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                            transactionID = documentSnapshot.getId();
                            transactionAmountGiven = documentSnapshot.getString(KEY_TRANSACTION_AMOUNT_GIVEN);
                            transactionAmountToPay = documentSnapshot.getString(KEY_TRANSACTION_AMOUNT_TO_PAY);
                            transactionBookingID = documentSnapshot.getString(KEY_TRANSACTION_BOOKING_ID);
                            transactionCarID = documentSnapshot.getString(KEY_TRANSACTION_CAR_ID);
                            transactionDateBooked = documentSnapshot.getString(KEY_TRANSACTION_DATE_BOOKED);
                            transactionDateWhen = documentSnapshot.getString(KEY_TRANSACTION_DATE_WHEN);
                            transactionTimeBooked = documentSnapshot.getString(KEY_TRANSACTION_TIME_BOOKED);
                            transactionTimeWhen = documentSnapshot.getString(KEY_TRANSACTION_TIME_WHEN);
                            transactionUserID = documentSnapshot.getString(KEY_TRANSACTION_USER_ID);
                            transactionUserName = documentSnapshot.getString(KEY_TRANSACTION_USER_NAME);

                            allTransactions.add(new Transaction(transactionID,
                                                                transactionAmountGiven,
                                                                transactionAmountToPay,
                                                                transactionBookingID,
                                                                transactionCarID,
                                                                transactionDateBooked,
                                                                transactionDateWhen,
                                                                transactionTimeBooked,
                                                                transactionTimeWhen,
                                                                transactionUserID,
                                                                transactionUserName));
                        }

                        loadTransactions(allTransactions);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                });

        Log.d(TAG, "getAllTransactions: called");
        Utils.divider();
    }

    private void loadTransactions(ArrayList<Transaction> allTransactions) {
        txtViewTransactionsCounter.setText(Integer.toString(allTransactions.size()));

        if (allTransactions.size() == 0) {
            imgViewEmptyLogo.setVisibility(View.VISIBLE);
            txtViewNoTransactionsActivityTitle.setVisibility(View.VISIBLE);
        } else {
            imgViewEmptyLogo.setVisibility(View.GONE);
            txtViewNoTransactionsActivityTitle.setVisibility(View.GONE);

            transactionAdapter = new TransactionsAdapter(AllTransactionsActivity.this, PARENT_ACTIVITY_HOLDER);
            rViewAllTransactions.setAdapter(transactionAdapter);
            rViewAllTransactions.setLayoutManager(new LinearLayoutManager(AllTransactionsActivity.this));
            transactionAdapter.setAllTransactions(allTransactions);
        }

        Log.d(TAG, "loadTransactions: called");
        Utils.divider();
    }
}