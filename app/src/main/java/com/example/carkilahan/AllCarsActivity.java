package com.example.carkilahan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carkilahan.Adapters.CarsAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.example.carkilahan.Classes.Car;

public class AllCarsActivity extends AppCompatActivity {
    private static final String TAG = "AllCarsActivity";

    //firebase
    private FirebaseFirestore db;
    private CollectionReference carsReference;
    private DocumentReference carReference;
    private FirebaseUser currentUser;

    //fields
    private static final String ENTITY_CARS = "Cars";
    private static final String KEY_CAR_NAME = "car_name";
    private static final String KEY_CAR_PLATE_NUMBER = "plate_number";
    private static final String KEY_CAR_COLOR = "color";
    private static final String KEY_CAR_YEAR = "year";
    private static final String KEY_CAR_TRANSMISSION = "transmission";
    private static final String KEY_CAR_SEATER = "seater";
    private static final String KEY_CAR_PRICES = "prices";
    private static final String KEY_CAR_IMAGE_URL = "image_url";

    //views
    private RecyclerView rViewCars;
    private CarsAdapter carAdapter;
    private ImageView imgViewEmptyLogo;
    private TextView txtViewCarsCounter,
                     txtViewNoCarsTitle;

    //variables
    private static String carID,
                          carName,
                          carPlateNumber,
                          carColor,
                          carYear,
                          carTransmission,
                          carSeater,
                          carImgUrl;

    private ArrayList<Car> allCars = new ArrayList<>();
    private List<String> carPrices;

    //date
    private TextView txtViewDate;
    private SimpleDateFormat dateFormat;
    private String date;
    private Calendar calendar;
    private TextClock txtClock;

    //intent
    private static String PARENT_ACTIVITY_TITLE = "parent_activity";
    private static String PARENT_ACTIVITY_HOLDER = "";
    private static String PARENT_ADMIN_DASHBOARD_ACTIVITY = "AdminDashboardActivity";
    private static String PARENT_CLIENT_DASHBOARD_ACTIVITY = "ClientDashboardActivity";

    @Override
    protected void onStart() {
        super.onStart();
        allCars.clear();
        getCars();

        Log.d(TAG, "onStart: called");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Intent intent = getIntent();

        if (intent.getStringExtra(PARENT_ACTIVITY_TITLE).equals(PARENT_ADMIN_DASHBOARD_ACTIVITY)) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.admin_cars_menu, menu);
        }

        if (intent.getStringExtra(PARENT_ACTIVITY_TITLE).equals(PARENT_CLIENT_DASHBOARD_ACTIVITY)) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.client_cars_menu, menu);
        }

        Log.d(TAG, "onCreateOptionsMenu: called");
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_cars);

        initAll();

        Intent intent = getIntent();
        if (intent.getStringExtra(PARENT_ACTIVITY_TITLE).equals(PARENT_ADMIN_DASHBOARD_ACTIVITY)) {
            PARENT_ACTIVITY_HOLDER = PARENT_ADMIN_DASHBOARD_ACTIVITY;
        }

        if (intent.getStringExtra(PARENT_ACTIVITY_TITLE).equals(PARENT_CLIENT_DASHBOARD_ACTIVITY)){
            PARENT_ACTIVITY_HOLDER = PARENT_CLIENT_DASHBOARD_ACTIVITY;
        }

        Log.d(TAG, "onCreate: called");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_back: {
                if (PARENT_ACTIVITY_HOLDER.equals(PARENT_ADMIN_DASHBOARD_ACTIVITY)) {
                    startActivity(new Intent(AllCarsActivity.this, AdminDashboardActivity.class));
                }

                if (PARENT_ACTIVITY_HOLDER.equals(PARENT_CLIENT_DASHBOARD_ACTIVITY)) {
                    startActivity(new Intent(AllCarsActivity.this, ClientDashboardActivity.class));
                }

                break;
            }

            case R.id.item_add_car: {
                showAddCarDialog();
                break;
            }
        }

        Log.d(TAG, "onOptionsItemSelected: called");
        return super.onOptionsItemSelected(item);
    }

    private void showAddCarDialog() {
        AlertDialog.Builder builderAddCar = new AlertDialog.Builder(this);
        builderAddCar.setTitle("New car details");

        LinearLayout layoutCarDetails = new LinearLayout(this);
        layoutCarDetails.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams lpCarDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpCarDetails.setMargins(50, 20, 50, 20);

        final EditText editTxtCarName = new EditText(this);
        editTxtCarName.setHint("Car name");
        editTxtCarName.setLayoutParams(lpCarDetails);

        final EditText editTxtCarColor = new EditText(this);
        editTxtCarColor.setHint("Car color");
        editTxtCarColor.setLayoutParams(lpCarDetails);

        final EditText editTxtCarImageUrl = new EditText(this);
        editTxtCarImageUrl.setHint("Car image URL");
        editTxtCarImageUrl.setLayoutParams(lpCarDetails);

        final EditText editTxtCarPlateNumber = new EditText(this);
        editTxtCarPlateNumber.setLayoutParams(lpCarDetails);
        editTxtCarPlateNumber.setEnabled(false);
        editTxtCarPlateNumber.setText(generateCarPlateNumber());

        //prices
        LinearLayout.LayoutParams lpPrices = new LinearLayout.LayoutParams(500, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpPrices.setMargins(50, 20, 50, 20);

        LinearLayout layoutPrice1 = new LinearLayout(this);
        layoutPrice1.setOrientation(LinearLayout.HORIZONTAL);

        final EditText editTxtCarPrice1 = new EditText(this);
        editTxtCarPrice1.setLayoutParams(lpPrices);
        editTxtCarPrice1.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTxtCarPrice1.setText("PHP ");
        Selection.setSelection(editTxtCarPrice1.getText(), editTxtCarPrice1.getText().length());
        editTxtCarPrice1.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().startsWith("PHP ")){
                    editTxtCarPrice1.setText("PHP ");
                    Selection.setSelection(editTxtCarPrice1.getText(), editTxtCarPrice1.getText().length());
                }
            }
        });

        final TextView txtViewCarPrice1 = new TextView(this);
        txtViewCarPrice1.setText("Self Drive 24 hours");
        txtViewCarPrice1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        layoutPrice1.addView(editTxtCarPrice1);
        layoutPrice1.addView(txtViewCarPrice1);

        //----------------------------------------

        LinearLayout layoutPrice2 = new LinearLayout(this);
        layoutPrice1.setOrientation(LinearLayout.HORIZONTAL);

        final EditText editTxtCarPrice2 = new EditText(this);
        editTxtCarPrice2.setLayoutParams(lpPrices);
        editTxtCarPrice2.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTxtCarPrice2.setText("PHP ");
        Selection.setSelection(editTxtCarPrice2.getText(), editTxtCarPrice2.getText().length());
        editTxtCarPrice2.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().startsWith("PHP ")){
                    editTxtCarPrice2.setText("PHP ");
                    Selection.setSelection(editTxtCarPrice2.getText(), editTxtCarPrice2.getText().length());
                }
            }
        });

        final TextView txtViewCarPrice2 = new TextView(this);
        txtViewCarPrice2.setText("Chauffeur Drive 5 hours");
        txtViewCarPrice2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        layoutPrice2.addView(editTxtCarPrice2);
        layoutPrice2.addView(txtViewCarPrice2);

        //----------------------------------------

        LinearLayout layoutPrice3 = new LinearLayout(this);
        layoutPrice1.setOrientation(LinearLayout.HORIZONTAL);

        final EditText editTxtCarPrice3 = new EditText(this);
        editTxtCarPrice3.setLayoutParams(lpPrices);
        editTxtCarPrice3.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTxtCarPrice3.setText("PHP ");
        Selection.setSelection(editTxtCarPrice3.getText(), editTxtCarPrice3.getText().length());
        editTxtCarPrice3.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().startsWith("PHP ")){
                    editTxtCarPrice3.setText("PHP ");
                    Selection.setSelection(editTxtCarPrice3.getText(), editTxtCarPrice3.getText().length());
                }
            }
        });

        final TextView txtViewCarPrice3 = new TextView(this);
        txtViewCarPrice3.setText("Chauffeur Drive 24 hours");
        txtViewCarPrice3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        layoutPrice3.addView(editTxtCarPrice3);
        layoutPrice3.addView(txtViewCarPrice3);

        LinearLayout layoutSeaterYear = new LinearLayout(this);
        layoutSeaterYear.setLayoutParams(lpCarDetails);
        layoutSeaterYear.setOrientation(LinearLayout.HORIZONTAL);

        final EditText editTxtCarSeater = new EditText(this);
        editTxtCarSeater.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTxtCarSeater.setLayoutParams(new LinearLayout.LayoutParams(500, LinearLayout.LayoutParams.WRAP_CONTENT));
        editTxtCarSeater.setHint("Car seater");
        editTxtCarSeater.setInputType(InputType.TYPE_CLASS_NUMBER);

        final EditText editTxtCarYear = new EditText(this);
        editTxtCarYear.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTxtCarYear.setLayoutParams(new LinearLayout.LayoutParams(500, LinearLayout.LayoutParams.WRAP_CONTENT));
        editTxtCarYear.setHint("Car year");
        editTxtCarYear.setInputType(InputType.TYPE_CLASS_NUMBER);

        final Spinner spinnerCarTransmission = new Spinner(this);
        spinnerCarTransmission.setLayoutParams(lpCarDetails);
        ArrayAdapter<CharSequence> transmissionAdapter = ArrayAdapter.createFromResource(this, R.array.car_transmission, android.R.layout.simple_spinner_item);
        transmissionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCarTransmission.setAdapter(transmissionAdapter);
        spinnerCarTransmission.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        layoutCarDetails.setLayoutParams(lpCarDetails);
        layoutCarDetails.addView(editTxtCarName);
        layoutCarDetails.addView(editTxtCarColor);
        layoutCarDetails.addView(editTxtCarImageUrl);
        layoutCarDetails.addView(editTxtCarPlateNumber);

        //

        layoutCarDetails.addView(layoutPrice1);
        layoutCarDetails.addView(layoutPrice2);
        layoutCarDetails.addView(layoutPrice3);

        //

        layoutSeaterYear.addView(editTxtCarSeater);
        layoutSeaterYear.addView(editTxtCarYear);

        layoutCarDetails.addView(layoutSeaterYear);
        layoutCarDetails.addView(spinnerCarTransmission);

        builderAddCar.setView(layoutCarDetails);

        builderAddCar.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addNewCar(editTxtCarPrice1,
                          editTxtCarPrice2,
                          editTxtCarPrice3,
                          txtViewCarPrice1,
                          txtViewCarPrice2,
                          txtViewCarPrice3,
                          editTxtCarName,
                          editTxtCarColor,
                          editTxtCarImageUrl,
                          editTxtCarPlateNumber,
                          editTxtCarSeater,
                          spinnerCarTransmission,
                          editTxtCarYear);
            }
        });

        builderAddCar.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builderAddCar.create().show();

        Log.d(TAG, "showAddCarDialog: called");
    }

    private void addNewCar(EditText editTxtCarPrice1,
                           EditText editTxtCarPrice2,
                           EditText editTxtCarPrice3,
                           TextView txtViewCarPrice1,
                           TextView txtViewCarPrice2,
                           TextView txtViewCarPrice3,
                           EditText editTxtCarName,
                           EditText editTxtCarColor,
                           EditText editTxtCarImageUrl,
                           EditText editTxtCarPlateNumber,
                           EditText editTxtCarSeater,
                           Spinner spinnerCarTransmission,
                           EditText editTxtCarYear) {

        String [] carPrices = new String[3];

        carPrices[0] = editTxtCarPrice1.getText().toString() + " " + txtViewCarPrice1.getText().toString();
        carPrices[1] = editTxtCarPrice2.getText().toString() + " " + txtViewCarPrice2.getText().toString();
        carPrices[2] = editTxtCarPrice3.getText().toString() + " " + txtViewCarPrice3.getText().toString();

        List<String> carPricesList = Arrays.asList(carPrices);

        Map<String, Object> newCar = new HashMap<>();
        newCar.put(KEY_CAR_NAME, editTxtCarName.getText().toString());
        newCar.put(KEY_CAR_COLOR, editTxtCarColor.getText().toString());
        newCar.put(KEY_CAR_IMAGE_URL, editTxtCarImageUrl.getText().toString());
        newCar.put(KEY_CAR_PLATE_NUMBER, editTxtCarPlateNumber.getText().toString());
        newCar.put(KEY_CAR_PRICES, carPricesList);
        newCar.put(KEY_CAR_SEATER, editTxtCarSeater.getText().toString());
        newCar.put(KEY_CAR_TRANSMISSION, spinnerCarTransmission.getSelectedItem().toString());
        newCar.put(KEY_CAR_YEAR, editTxtCarYear.getText().toString());

        carsReference = db.collection(ENTITY_CARS);
        carsReference
                .add(newCar)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getApplicationContext(), "Car added", Toast.LENGTH_SHORT).show();
                        startActivity(getIntent());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Car not added", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.getMessage());
                    }
                });

        Log.d(TAG, "addNewCar: called");
    }

    private void initAll() {
        txtClock = findViewById(R.id.text_clock_time);
        txtViewDate = findViewById(R.id.text_view_date);
        rViewCars = findViewById(R.id.recycler_view_all_cars);
        imgViewEmptyLogo = findViewById(R.id.image_view_empty_logo);
        txtViewCarsCounter = findViewById(R.id.text_view_cars_counter);
        txtViewNoCarsTitle = findViewById(R.id.text_view_no_cars_title);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();

        dateFormat = new SimpleDateFormat("MMM d, yyyy");
        date = dateFormat.format(calendar.getTime());
        txtViewDate.setText(date);

        Log.d(TAG, "initAll: called");
    }

    private void getCars() {
        carsReference = db.collection(ENTITY_CARS);
        carsReference
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                            carID = documentSnapshot.getId();
                            carName = documentSnapshot.getString(KEY_CAR_NAME);
                            carPlateNumber = documentSnapshot.getString(KEY_CAR_PLATE_NUMBER);
                            carColor = documentSnapshot.getString(KEY_CAR_COLOR);
                            carYear = documentSnapshot.getString(KEY_CAR_YEAR);
                            carTransmission = documentSnapshot.getString(KEY_CAR_TRANSMISSION);
                            carSeater = documentSnapshot.getString(KEY_CAR_SEATER);
                            carPrices = (List<String>) documentSnapshot.get(KEY_CAR_PRICES);
                            carImgUrl = documentSnapshot.getString(KEY_CAR_IMAGE_URL);

                            allCars.add(new Car(carID,
                                                carName,
                                                carPlateNumber,
                                                carColor,
                                                carYear,
                                                carTransmission,
                                                carSeater,
                                                carPrices,
                                                carImgUrl));
                        }

                        loadCars(allCars);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                });

        Log.d(TAG, "getCars: called");
    }

    private void loadCars(ArrayList<Car> allCars) {
        txtViewCarsCounter.setText(Integer.toString(allCars.size()));

        if (allCars.size() == 0) {
            imgViewEmptyLogo.setVisibility(View.VISIBLE);
            txtViewNoCarsTitle.setVisibility(View.VISIBLE);
        } else {
            imgViewEmptyLogo.setVisibility(View.GONE);
            txtViewNoCarsTitle.setVisibility(View.GONE);

            carAdapter = new CarsAdapter(AllCarsActivity.this, PARENT_ACTIVITY_HOLDER);
            rViewCars.setAdapter(carAdapter);
            rViewCars.setLayoutManager(new LinearLayoutManager(AllCarsActivity.this));
            carAdapter.setCars(allCars);
            carAdapter.setUserEmail(currentUser.getEmail());
        }

        Log.d(TAG, "loadCars: called");
    }

    private String generateCarPlateNumber() {
        Random random = new Random();

        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "1234567890";
        String plateNumber = "";

        int number;

        for (int i = 0; i < 6; i ++) {
            number = random.nextInt(6);

            if (plateNumber.length() == 2) {
                plateNumber += " ";
            }

            if (plateNumber.length() >= 2) {
                plateNumber += numbers.charAt(number);
            } else {
                plateNumber += letters.charAt(number);
            }
        }

        Log.d(TAG, "generateCarPlateNumber: called");

        return plateNumber;
    }
}