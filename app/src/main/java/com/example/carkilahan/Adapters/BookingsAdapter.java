package com.example.carkilahan.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carkilahan.AllBookingsActivity;
import com.example.carkilahan.Classes.Booking;
import com.example.carkilahan.ClientBookingsActivity;
import com.example.carkilahan.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.ViewHolder>{
    private static final String TAG = "AllBookingsActivity";

    //firebase
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private CollectionReference transactionsReference,
                                bookingsReference;

    private DocumentReference carReference, userReference;

    //entities | fields
    private static final String ENTITY_USERS = "Users";
    private static final String KEY_USER_NAME = "name";

    private static final String ENTITY_CARS = "Cars";
    private static final String KEY_CAR_NAME = "car_name";

    private static final String ENTITY_BOOKINGS = "Bookings";
    private static final String KEY_BOOKING_USER_ID = "user_id";
    private static final String KEY_BOOKING_PRICE = "price";
    private static final String KEY_BOOKING_DATE_WHEN = "date_when";
    private static final String KEY_BOOKING_TIME_WHEN = "time_when";
    private static final String KEY_BOOKING_DATE_BOOKED = "date_booked";
    private static final String KEY_BOOKING_TIME_BOOKED = "time_booked";
    private static final String KEY_BOOKING_CAR_ID = "car_id";
    private static final String KEY_BOOKING_TYPE = "type";

    private static final String ENTITY_TRANSACTIONS = "Transactions";
    private static final String KEY_TRANSACTION_BOOKING_ID = "booking_id";
    private static final String KEY_TRANSACTION_USER_ID = "user_id";
    private static final String KEY_TRANSACTION_CAR_ID = "car_id";
    private static final String KEY_TRANSACTION_BOOKING_TIME_WHEN = "time_when";
    private static final String KEY_TRANSACTION_BOOKING_TIME_BOOKED = "time_booked";
    private static final String KEY_TRANSACTION_BOOKING_DATE_WHEN = "date_when";
    private static final String KEY_TRANSACTION_BOOKING_DATE_BOOKED = "date_booked";
    private static final String KEY_TRANSACTION_AMOUNT_TO_PAY = "amount_to_pay";
    private static final String KEY_TRANSACTION_USER_NAME = "user_name";

    //variables
    private ArrayList<Booking> allBookings = new ArrayList<>();
    private Context mContext;
    private String parentActivity;

    //intents
    private static String PARENT_ADMIN_DASHBOARD_ACTIVITY = "AdminDashboardActivity";
    private static String PARENT_CLIENT_DASHBOARD_ACTIVITY = "ClientDashboardActivity";

    public BookingsAdapter(Context mContext, String parentActivity) {
        this.mContext = mContext;
        this.parentActivity = parentActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        carReference = db.collection(ENTITY_CARS).document(allBookings.get(position).getBookingCarID());
        carReference
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        holder.txtViewClientName.setText(allBookings.get(position).getBookingUserName());
                        holder.txtViewCarName.setText(documentSnapshot.getString(KEY_CAR_NAME));
                        holder.txtViewDateWhen.setText(allBookings.get(position).getBookingDateWhen());
                        holder.txtViewDateBooked.setText(allBookings.get(position).getBookingDateBooked());
                        holder.txtViewTimeWhen.setText(allBookings.get(position).getBookingTimeWhen());
                        holder.txtViewTimeBooked.setText(allBookings.get(position).getBookingTimeBooked());
                        holder.txtViewPrice.setText(allBookings.get(position).getBookingPrice());
                        holder.txtViewType.setText(allBookings.get(position).getBookingType());
                    }
                });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder deleteBookingDialog = new AlertDialog.Builder(mContext);
                deleteBookingDialog.setMessage("Are you sure you want to cancel this booking?");
                deleteBookingDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteBooking(position);
                    }
                });

                deleteBookingDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                deleteBookingDialog.create().show();
            }
        });

        holder.btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAmount(position);
            }
        });

        holder.btnMarkPaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builderPaid = new AlertDialog.Builder(mContext);
                builderPaid.setMessage("This booking will be marked as paid, confirm user transaction? ");
                builderPaid.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bookingsReference = db.collection(ENTITY_BOOKINGS);
                        bookingsReference
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        addNewTransaction(position, allBookings.get(position).getBookingPrice());
                                    }
                                });
                    }
                });

                builderPaid.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builderPaid.create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return allBookings.size();
    }

    public void setBookings(ArrayList<Booking> allBookings) {
        this.allBookings = allBookings;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //views
        private CardView parent;
        private TextView txtViewCarName,
                         txtViewDateWhen,
                         txtViewDateBooked,
                         txtViewTimeWhen,
                         txtViewTimeBooked,
                         txtViewPrice,
                         txtViewType;

        private TextView txtViewClientNameTitle, txtViewClientName;
        private Button btnDelete, btnPay, btnMarkPaid;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            txtViewClientNameTitle = itemView.findViewById(R.id.text_view_car_client_name_title);
            txtViewClientName = itemView.findViewById(R.id.text_view_car_client_name);
            txtViewCarName = itemView.findViewById(R.id.text_view_car_name);
            txtViewDateWhen = itemView.findViewById(R.id.text_view_car_date_when);
            txtViewDateBooked = itemView.findViewById(R.id.text_view_car_date_booked);
            txtViewTimeWhen = itemView.findViewById(R.id.text_view_car_time_when);
            txtViewTimeBooked = itemView.findViewById(R.id.text_view_car_time_booked);
            txtViewPrice = itemView.findViewById(R.id.text_view_car_price);
            txtViewType = itemView.findViewById(R.id.text_view_car_type);
            btnDelete = itemView.findViewById(R.id.button_delete_booking);
            btnPay = itemView.findViewById(R.id.button_pay_booking);
            btnMarkPaid = itemView.findViewById(R.id.button_mark_paid);

            db = FirebaseFirestore.getInstance();
            currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (parentActivity.equals(PARENT_ADMIN_DASHBOARD_ACTIVITY)) {
                txtViewClientNameTitle.setVisibility(View.VISIBLE);
                txtViewClientName.setVisibility(View.VISIBLE);
                btnPay.setVisibility(View.GONE);
                btnMarkPaid.setVisibility(View.VISIBLE);
            }

            if (parentActivity.equals(PARENT_CLIENT_DASHBOARD_ACTIVITY)) {
                txtViewClientNameTitle.setVisibility(View.GONE);
                txtViewClientName.setVisibility(View.GONE);
                btnPay.setVisibility(View.VISIBLE);
                btnMarkPaid.setVisibility(View.GONE);
            }
        }
    }

    private void getAmount(int position) {
        bookingsReference = db.collection(ENTITY_BOOKINGS);
        bookingsReference
                .whereEqualTo(KEY_BOOKING_USER_ID, currentUser.getUid())
                .whereEqualTo(KEY_BOOKING_CAR_ID, allBookings.get(position).getBookingCarID())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                            String price = documentSnapshot.getString(KEY_BOOKING_PRICE);
                            String priceHolder [] = price.split(" ");
                            String dateWhen = documentSnapshot.getString(KEY_BOOKING_DATE_WHEN);
                            String timeWhen = documentSnapshot.getString(KEY_BOOKING_TIME_WHEN);
                            String bookingID = documentSnapshot.getId();
                            String carID = documentSnapshot.getString(KEY_BOOKING_CAR_ID);
                            String dateBooked = documentSnapshot.getString(KEY_BOOKING_DATE_BOOKED);
                            String timeBooked = documentSnapshot.getString(KEY_BOOKING_TIME_BOOKED);
                            String type = documentSnapshot.getString(KEY_BOOKING_TYPE);

                            openAmountDialog(price, priceHolder[1].replace(",", ""), dateWhen, timeWhen, bookingID, carID, dateBooked, timeBooked, type);
                            return;
                        }
                    }
                });
    }

    private void openAmountDialog(String price, String priceHolder, String dateWhen, String timeWhen, String bookingID, String carID, String dateBooked, String timeBooked, String type) {
        AlertDialog.Builder builderAmount = new AlertDialog.Builder(mContext);
        builderAmount.setTitle("Payment Details");

        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams lpFirstView = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpFirstView.setMargins(50, 70,50, 20);

        LinearLayout.LayoutParams lpViews = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpViews.setMargins(50, 20, 50, 20);

        final TextView txtViewPayBefore = new TextView(mContext);
        txtViewPayBefore.setText("Please pay before " + dateWhen + ", " + timeWhen);
        txtViewPayBefore.setTextSize(17);
        txtViewPayBefore.setLayoutParams(lpFirstView);

        final TextView txtViewBookingID = new TextView(mContext);
        txtViewBookingID.setText("Booking ID: " + bookingID);
        txtViewBookingID.setTextSize(17);
        txtViewBookingID.setLayoutParams(lpViews);

        final TextView txtViewCarID = new TextView(mContext);
        txtViewCarID.setText("Car ID: " + carID);
        txtViewCarID.setTextSize(17);
        txtViewCarID.setLayoutParams(lpViews);

        final TextView txtViewDateBooked = new TextView(mContext);
        txtViewDateBooked.setText("Date booked: " + dateBooked);
        txtViewDateBooked.setTextSize(17);
        txtViewDateBooked.setLayoutParams(lpViews);

        final TextView txtViewTimeBooked = new TextView(mContext);
        txtViewTimeBooked.setText("Time booked: " + timeBooked);
        txtViewTimeBooked.setTextSize(17);
        txtViewTimeBooked.setLayoutParams(lpViews);

        final TextView txtViewPrice = new TextView(mContext);
        txtViewPrice.setText("Price: " + price);
        txtViewPrice.setTextSize(17);
        txtViewPrice.setLayoutParams(lpViews);

        final TextView txtViewType = new TextView(mContext);
        txtViewType.setText("Type: " + type);
        txtViewType.setTextSize(17);
        txtViewType.setLayoutParams(lpViews);

        layout.addView(txtViewPayBefore);
        layout.addView(txtViewBookingID);
        layout.addView(txtViewCarID);
        layout.addView(txtViewDateBooked);
        layout.addView(txtViewTimeBooked);
        layout.addView(txtViewPrice);
        layout.addView(txtViewType);

        builderAmount.setView(layout);

        builderAmount.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builderAmount.create().show();
    }

    private void addNewTransaction(int position, String price) {
        userReference = db.collection(ENTITY_USERS).document(allBookings.get(position).getBookingUserID());
        userReference
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String, Object> newTransaction = new HashMap<>();
                        newTransaction.put(KEY_TRANSACTION_USER_NAME, documentSnapshot.getString(KEY_USER_NAME));
                        newTransaction.put(KEY_TRANSACTION_BOOKING_ID, allBookings.get(position).getBookingID());
                        newTransaction.put(KEY_TRANSACTION_USER_ID, allBookings.get(position).getBookingUserID());
                        newTransaction.put(KEY_TRANSACTION_CAR_ID, allBookings.get(position).getBookingCarID());
                        newTransaction.put(KEY_TRANSACTION_BOOKING_TIME_WHEN, allBookings.get(position).getBookingTimeWhen());
                        newTransaction.put(KEY_TRANSACTION_BOOKING_TIME_BOOKED, allBookings.get(position).getBookingTimeBooked());
                        newTransaction.put(KEY_TRANSACTION_BOOKING_DATE_WHEN, allBookings.get(position).getBookingDateWhen());
                        newTransaction.put(KEY_TRANSACTION_BOOKING_DATE_BOOKED, allBookings.get(position).getBookingDateBooked());
                        newTransaction.put(KEY_TRANSACTION_AMOUNT_TO_PAY, price);

                        transactionsReference = db.collection(ENTITY_TRANSACTIONS);
                        transactionsReference
                                .add(newTransaction)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        deleteBooking(position);
                                        Toast.makeText(mContext, "Payment successful", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, e.getMessage());
                                    }
                                });
                    }
                });
    }

    private void deleteBooking(int position) {
        bookingsReference = db.collection(ENTITY_BOOKINGS);
        bookingsReference
                .document(allBookings.get(position).getBookingID())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(mContext, "Booking deleted", Toast.LENGTH_SHORT).show();

                        if (parentActivity.equals(PARENT_ADMIN_DASHBOARD_ACTIVITY)) {
                            mContext.startActivity(new Intent(mContext, AllBookingsActivity.class));
                        }

                        if (parentActivity.equals(PARENT_CLIENT_DASHBOARD_ACTIVITY)) {
                            mContext.startActivity(new Intent(mContext, ClientBookingsActivity.class));
                        }
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
