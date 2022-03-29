package com.example.carkilahan.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;
import com.example.carkilahan.AllCarsActivity;
import com.example.carkilahan.Classes.Car;
import com.example.carkilahan.Classes.Utils;
import com.example.carkilahan.ClientDashboardActivity;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class CarsAdapter extends RecyclerView.Adapter<CarsAdapter.ViewHolder>{
    private static final String TAG = "AllCarsActivity";

    //firebase
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private DocumentReference userReference;
    private CollectionReference carsReference, bookingsReference;

    //variables
    private String dateFormat1 = "MM/dd/yy";
    private String dateFormat2 = "MMM d, yyyy";
    private ArrayList<Car> allCars = new ArrayList<>();
    private ArrayList<String> carIDs = new ArrayList<>();
    private Context mContext;
    private String parentActivity;
    private String userEmail = "";

    //intents
    private static String PARENT_ADMIN_DASHBOARD_ACTIVITY = "AdminDashboardActivity";
    private static String PARENT_CLIENT_DASHBOARD_ACTIVITY = "ClientDashboardActivity";

    //dates
    private SimpleDateFormat dateFormat;
    private Calendar dateWhen,
                     dateToday;

    //entities | fields
    private static final String ENTITY_USERS = "Users";
    private static final String KEY_USER_NAME = "name";

    private static final String ENTITY_CARS = "Cars";

    private static final String ENTITY_BOOKINGS = "Bookings";
    private static final String KEY_BOOKING_USER_ID = "user_id";
    private static final String KEY_BOOKING_USER_NAME = "user_name";
    private static final String KEY_BOOKING_CAR_ID = "car_id";
    private static final String KEY_BOOKING_DATE_WHEN = "date_when";
    private static final String KEY_BOOKING_DATE_BOOKED = "date_booked";
    private static final String KEY_BOOKING_TIME_WHEN = "time_when";
    private static final String KEY_BOOKING_TIME_BOOKED = "time_booked";
    private static final String KEY_BOOKING_PRICE = "price";
    private static final String KEY_BOOKING_TYPE = "type";

    //intent
    private static String PARENT_ACTIVITY_TITLE = "parent_activity";

    public CarsAdapter(Context mContext, String parentActivity) {
        this.mContext = mContext;
        this.parentActivity = parentActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_car, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.txtViewCarName.setText(allCars.get(position).getCarName());
        holder.txtViewCarPlateNumber.setText(allCars.get(position).getCarPlateNumber());
        holder.txtViewCarColor.setText(allCars.get(position).getCarColor());
        holder.txtViewCarYear.setText(allCars.get(position).getCarYear());
        holder.txtViewCarTransmission.setText(allCars.get(position).getCarTransmission());
        holder.txtViewCarSeater.setText(allCars.get(position).getCarSeater());

        Glide.with(mContext)
                .asBitmap()
                .load(allCars.get(position).getCarImgUrl())
                .into(holder.imgViewCarAvatar);

        holder.rButtonCarPrice1.setText(allCars.get(position).getCarPrices().get(0));
        holder.rButtonCarPrice2.setText(allCars.get(position).getCarPrices().get(1));
        holder.rButtonCarPrice3.setText(allCars.get(position).getCarPrices().get(2));

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder deleteCarBuilder = new AlertDialog.Builder(mContext);
                deleteCarBuilder.setMessage("Delete this car?");
                deleteCarBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCar(allCars.get(position).getCarID());
                    }
                });

                deleteCarBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                deleteCarBuilder.create().show();
            }
        });

        holder.btnRent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.rGroupCarPrices.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(mContext, "Select at least one of the prices", Toast.LENGTH_SHORT).show();
                } else {
                    int rButtonID = holder.rGroupCarPrices.getCheckedRadioButtonId();
                    holder.rButtonCarPriceHolder =  holder.itemView.findViewById(rButtonID);
                    openDateDialog(holder, position);
                }
            }
        });

        if (allCars.get(position).isCarIsExpanded()) {
            TransitionManager.beginDelayedTransition(holder.parent);
            holder.expandedRelativeLayout.setVisibility(View.VISIBLE);
            holder.imgViewDownArrow.setVisibility(View.GONE);
        } else {
            TransitionManager.beginDelayedTransition(holder.parent);
            holder.expandedRelativeLayout.setVisibility(View.GONE);
            holder.imgViewDownArrow.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return allCars.size();
    }

    public void setCars(ArrayList<Car> allCars) {
        this.allCars = allCars;
        notifyDataSetChanged();
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //views
        private CardView parent;
        private ImageView imgViewCarAvatar,
                          imgViewDownArrow,
                          imgViewUpArrow;

        private TextView txtViewCarName,
                         txtViewCarPlateNumber,
                         txtViewCarColor,
                         txtViewCarYear,
                         txtViewCarTransmission,
                         txtViewCarSeater;

        private RelativeLayout expandedRelativeLayout;
        private RadioGroup rGroupCarPrices;
        private RadioButton rButtonCarPrice1,
                            rButtonCarPrice2,
                            rButtonCarPrice3,
                            rButtonCarPriceHolder;

        private Button btnDelete, btnRent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            imgViewCarAvatar = itemView.findViewById(R.id.image_view_car_avatar);
            btnDelete = itemView.findViewById(R.id.button_delete_car);
            btnRent = itemView.findViewById(R.id.button_rent_car);
            imgViewDownArrow = itemView.findViewById(R.id.image_view_down_arrow);
            imgViewUpArrow = itemView.findViewById(R.id.image_view_up_arrow);
            txtViewCarName = itemView.findViewById(R.id.text_view_car_name);
            txtViewCarPlateNumber = itemView.findViewById(R.id.text_view_car_plate_numer);
            txtViewCarColor = itemView.findViewById(R.id.text_view_car_color);
            txtViewCarYear = itemView.findViewById(R.id.text_view_car_year);
            txtViewCarTransmission = itemView.findViewById(R.id.text_view_car_transmission);
            txtViewCarSeater = itemView.findViewById(R.id.text_view_car_seater);
            expandedRelativeLayout = itemView.findViewById(R.id.expanded_relative_layout);
            rGroupCarPrices = itemView.findViewById(R.id.radio_group_car_prices);
            rButtonCarPrice1 = itemView.findViewById(R.id.radio_button_car_price1);
            rButtonCarPrice2 = itemView.findViewById(R.id.radio_button_car_price2);
            rButtonCarPrice3 = itemView.findViewById(R.id.radio_button_car_price3);

            db = FirebaseFirestore.getInstance();
            currentUser = FirebaseAuth.getInstance().getCurrentUser();

            dateWhen = Calendar.getInstance();
            dateToday = Calendar.getInstance();

            if (parentActivity.equals(PARENT_ADMIN_DASHBOARD_ACTIVITY)) {
                btnDelete.setVisibility(View.VISIBLE);
                btnRent.setVisibility(View.GONE);
                rGroupCarPrices.setVisibility(View.GONE);
            }

            if (parentActivity.equals(PARENT_CLIENT_DASHBOARD_ACTIVITY)) {
                btnDelete.setVisibility(View.GONE);
                btnRent.setVisibility(View.VISIBLE);
                rGroupCarPrices.setVisibility(View.VISIBLE);
            }

            imgViewDownArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Car car = allCars.get(getAdapterPosition());
                    car.setCarIsExpanded(!car.isCarIsExpanded());
                    notifyItemChanged(getAdapterPosition());
                }
            });

            imgViewUpArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Car car = allCars.get(getAdapterPosition());
                    car.setCarIsExpanded(!car.isCarIsExpanded());
                    notifyItemChanged(getAdapterPosition());
                    rGroupCarPrices.clearCheck();
                }
            });
        }
    }

    private void openDateDialog(ViewHolder holder, int position) {
        AlertDialog.Builder builderDate = new AlertDialog.Builder(mContext);
        builderDate.setTitle("Date");

        LinearLayout layoutDate = new LinearLayout(mContext);
        layoutDate.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lpDate = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpDate.setMargins(50, 50, 50, 50);

        final EditText editTxtDateWhen = new EditText(mContext);
        editTxtDateWhen.setCursorVisible(false);
        editTxtDateWhen.setClickable(false);
        editTxtDateWhen.setFocusable(false);
        editTxtDateWhen.setHint("Choose date to pickup");
        editTxtDateWhen.setFocusableInTouchMode(false);
        editTxtDateWhen.setLayoutParams(lpDate);

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) { ;
                dateWhen.set(Calendar.YEAR, year);
                dateWhen.set(Calendar.MONTH, monthOfYear);
                dateWhen.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                dateFormat = new SimpleDateFormat(dateFormat2, Locale.US);
                editTxtDateWhen.setText(dateFormat.format(dateWhen.getTime()));
            }
        };

        editTxtDateWhen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(mContext, date,
                                               dateWhen.get(Calendar.YEAR),
                                               dateWhen.get(Calendar.MONTH),
                                               dateWhen.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        layoutDate.addView(editTxtDateWhen);
        builderDate.setView(layoutDate);

        builderDate.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (editTxtDateWhen.getText().toString().equals("")) {
                    Toast.makeText(mContext, "Select a date", Toast.LENGTH_SHORT).show();
                } else {
                    openTimeDialog(holder, position);
                }
            }
        });

        builderDate.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builderDate.create().show();
    }

    private void openTimeDialog(ViewHolder holder, int position) {
        LinearLayout.LayoutParams lpTime = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpTime.setMargins(50, 50, 50, 50);

        dateFormat = new SimpleDateFormat(dateFormat1, Locale.US);
        String dateWhenShort = dateFormat.format(dateWhen.getTime()).toString();
        String dateTodayShort = dateFormat.format(dateToday.getTime()).toString();

        dateFormat = new SimpleDateFormat(dateFormat2, Locale.US);
        String dateWhenLong = dateFormat.format(dateWhen.getTime()).toString();
        String dateTodayLong = dateFormat.format(dateToday.getTime()).toString();

        if (compareDate(dateWhenShort, dateTodayShort)) {
            AlertDialog.Builder builderTime = new AlertDialog.Builder(mContext);
            builderTime.setTitle("Time");

            final EditText editTxtTimeWhen = new EditText(mContext);
            editTxtTimeWhen.setCursorVisible(false);
            editTxtTimeWhen.setClickable(false);
            editTxtTimeWhen.setFocusable(false);
            editTxtTimeWhen.setHint("Choose time to pickup");
            editTxtTimeWhen.setFocusableInTouchMode(false);
            editTxtTimeWhen.setLayoutParams(lpTime);

            LinearLayout layout = new LinearLayout(mContext);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setLayoutParams(lpTime);

            layout.addView(editTxtTimeWhen);
            builderTime.setView(layout);

            TimePickerDialog.OnTimeSetListener timePickerDialog = new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    int mHour = hourOfDay;
                    int mMin = minute;

                    String time = mHour + ":" + mMin;
                    dateFormat = new SimpleDateFormat("HH:mm");
                    try {
                        Date date = dateFormat.parse(time);
                        dateFormat = new SimpleDateFormat("h:mm a");
                        editTxtTimeWhen.setText(dateFormat.format(date));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            };

            editTxtTimeWhen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new TimePickerDialog(mContext ,timePickerDialog, 12, 0, false).show();
                }
            });

            builderTime.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (editTxtTimeWhen.getText().toString().equals("")) {
                        Toast.makeText(mContext, "Select a time", Toast.LENGTH_SHORT).show();
                    } else {
                        String timeWhenString = editTxtTimeWhen.getText().toString();

                        String priceAndTypeHolder [] = holder.rButtonCarPriceHolder.getText().toString().split(" ");
                        String price = priceAndTypeHolder[0] + " " + priceAndTypeHolder[1];
                        String type = "";

                        for (int i = 2; i < priceAndTypeHolder.length; i ++) {
                            type += priceAndTypeHolder[i];

                            if (i != (priceAndTypeHolder.length - 1)) {
                                type += " ";
                            }
                        }

                        addNewBooking(position, dateWhenLong, dateTodayLong, timeWhenString, price, type);
                    }
                }
            });

            builderTime.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builderTime.create().show();
        } else {
            Toast.makeText(mContext, "Invalid date", Toast.LENGTH_SHORT).show();
        }
    }

    private void addNewBooking(int position, String dateWhen, String dateToday, String timeWhenString, String price, String type) {
        bookingsReference = db.collection(ENTITY_BOOKINGS);
        bookingsReference
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            carIDs.add(documentSnapshot.getString(KEY_BOOKING_CAR_ID));
                        }

                        boolean carIDFound = false;

                        for (String carID : carIDs) {
                            if (allCars.get(position).getCarID().equals(carID)) {
                                carIDFound = true;
                                break;
                            }
                        }

                        if (carIDFound) {
                            Toast.makeText(mContext, "Car is already reserved", Toast.LENGTH_SHORT).show();
                        } else {
                            userReference = db.collection(ENTITY_USERS).document(currentUser.getUid());
                            userReference
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            final Date timeBooked = Calendar.getInstance().getTime();
                                            dateFormat = new SimpleDateFormat("h:mm a");

                                            Map<String, Object> newBooking = new HashMap<>();
                                            newBooking.put(KEY_BOOKING_USER_ID, currentUser.getUid());
                                            newBooking.put(KEY_BOOKING_USER_NAME, documentSnapshot.getString(KEY_USER_NAME));
                                            newBooking.put(KEY_BOOKING_CAR_ID, allCars.get(position).getCarID());
                                            newBooking.put(KEY_BOOKING_DATE_WHEN, dateWhen);
                                            newBooking.put(KEY_BOOKING_DATE_BOOKED, dateToday);
                                            newBooking.put(KEY_BOOKING_TIME_WHEN, timeWhenString);
                                            newBooking.put(KEY_BOOKING_TIME_BOOKED, dateFormat.format(timeBooked).toString());
                                            newBooking.put(KEY_BOOKING_PRICE, price);
                                            newBooking.put(KEY_BOOKING_TYPE, type);

                                            bookingsReference = db.collection(ENTITY_BOOKINGS);
                                            bookingsReference
                                                    .add(newBooking)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            Toast.makeText(mContext, "Booking added", Toast.LENGTH_SHORT).show();
                                                            notifyUserEmail(userEmail, dateWhen, timeWhenString, allCars.get(position).getCarID(), price, type, dateToday, dateFormat.format(timeBooked).toString());

                                                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                                            builder.setTitle("Email notification");
                                                            builder.setMessage("Email has been sent, please check for the complete details of your booking transaction");
                                                            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    mContext.startActivity(new Intent(mContext, ClientDashboardActivity.class));
                                                                }
                                                            });

                                                            builder.create().show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(mContext, "Booking not added", Toast.LENGTH_SHORT).show();
                                                            Log.d(TAG, e.getMessage());
                                                        }
                                                    });
                                        }
                                    });
                        }
                    }
                });
    }

    private void notifyUserEmail(String userEmail, String dateWhen, String timeWhenString, String carID, String price, String type, String dateBooked, String timeBooked) {
        bookingsReference = db.collection(ENTITY_BOOKINGS);
        bookingsReference
                .whereEqualTo(KEY_BOOKING_USER_ID, currentUser.getUid())
                .whereEqualTo(KEY_BOOKING_CAR_ID, carID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String emailMessage = "Hey " + documentSnapshot.getString(KEY_BOOKING_USER_NAME) + "!" +
                                                  "\n\nThank you for booking through our mobile application! Below are the details of your booking transaction: " +
                                                  "\n\nPlease proceed with the payment before " + dateWhen + ", " + timeWhenString +
                                                  "\nBooking ID: " + documentSnapshot.getId() +
                                                  "\n\nCar ID: " + carID +
                                                  "\nPrice: " + price +
                                                  "\nType: " + type +
                                                  "\nDate booked: " + dateBooked +
                                                  "\nTime booked: " + timeBooked +
                                                  "\n\nThank you, " +
                                                  "\nCode Brewers";

                            String emailSubject = "Carkilahan";

                            Properties properties = new Properties();
                            properties.put("mail.smtp.auth", "true");
                            properties.put("mail.smtp.starttls.enable", "true");
                            properties.put("mail.smtp.host", "smtp.gmail.com");
                            properties.put("mail.smtp.port", "587");

                            Session session = Session.getInstance(properties, new Authenticator() {
                                @Override
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication(Utils.EMAIL, Utils.PASSWORD);
                                }
                            });

                            try {
                                Message message = new MimeMessage(session);
                                message.setFrom(new InternetAddress(Utils.EMAIL));
                                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));

                                message.setText(emailMessage);
                                message.setSubject(emailSubject);
                                new SendMail().execute(message);
                            } catch (MessagingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    private class SendMail extends AsyncTask<Message, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Message... messages) {
            try {
                Transport.send(messages[0]);
                return "Success";
            } catch (MessagingException e) {
                e.printStackTrace();
                return "Error";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    private boolean compareDate(String date1, String date2) {
        String dates1 [] = date1.split("/");
        String dates2 [] = date2.split("/");

        if (Integer.parseInt(dates1[2]) >= Integer.parseInt(dates2[2])) {
            if (Integer.parseInt(dates1[0]) >= Integer.parseInt(dates2[0])) {
                if (Integer.parseInt(dates1[1]) >= Integer.parseInt(dates2[1])) {
                    return true;
                }
            }
        }

        return false;
    }

    private void deleteCar(String carId) {
        carsReference = db.collection(ENTITY_CARS);
        carsReference
                .document(carId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(mContext, "Car deleted", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(mContext, AllCarsActivity.class);
                        intent.putExtra(PARENT_ACTIVITY_TITLE, PARENT_ADMIN_DASHBOARD_ACTIVITY);
                        mContext.startActivity(intent);
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
