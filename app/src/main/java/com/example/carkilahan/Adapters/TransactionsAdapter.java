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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carkilahan.AllTransactionsActivity;
import com.example.carkilahan.Classes.Transaction;
import com.example.carkilahan.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.ViewHolder>{
    private static final String TAG = "AllTransactionsActivity";

    //firebase
    private FirebaseFirestore db;
    private DocumentReference transactionReference;

    //entities | fields
    private static final String ENTITY_TRANSACTIONS = "Transactions";

    //variables
    private ArrayList<Transaction> allTransactions = new ArrayList<>();
    private Context mContext;
    private String parentActivity;

    //intents
    private static String PARENT_ADMIN_DASHBOARD_ACTIVITY = "AdminDashboardActivity";
    private static String PARENT_CLIENT_DASHBOARD_ACTIVITY = "ClientDashboardActivity";

    public TransactionsAdapter(Context mContext, String parentActivity) {
        this.mContext = mContext;
        this.parentActivity = parentActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtViewClientName.setText(allTransactions.get(position).getUserName());
        holder.txtViewDateWhen.setText(allTransactions.get(position).getDateWhen());
        holder.txtViewDateBooked.setText(allTransactions.get(position).getDateBooked());
        holder.txtViewTimeWhen.setText(allTransactions.get(position).getTimeWhen());
        holder.txtViewTimeBooked.setText(allTransactions.get(position).getTimeBooked());
        holder.txtViewAmountToPay.setText(allTransactions.get(position).getAmountToPay());
        holder.txtViewBookingID.setText(allTransactions.get(position).getBookingID());
        holder.txtViewCarID.setText(allTransactions.get(position).getCarID());
        holder.txtViewUserID.setText(allTransactions.get(position).getUserID());

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builderDeleteTransaction = new AlertDialog.Builder(mContext);
                builderDeleteTransaction.setMessage("Are you sure you want to delete this transaction?");
                builderDeleteTransaction.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        transactionReference = db.collection(ENTITY_TRANSACTIONS).document(allTransactions.get(position).getId());
                        transactionReference
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(mContext, "Transaction deleted", Toast.LENGTH_SHORT).show();
                                        mContext.startActivity(new Intent(mContext, AllTransactionsActivity.class));
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

                builderDeleteTransaction.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builderDeleteTransaction.create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return allTransactions.size();
    }

    public void setAllTransactions(ArrayList<Transaction> allTransactions) {
        this.allTransactions = allTransactions;
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
                         txtViewAmountToPay,
                         txtViewBookingID,
                         txtViewCarID,
                         txtViewUserID;

        private TextView txtViewClientNameTitle, txtViewClientName;
        private Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            btnDelete = itemView.findViewById(R.id.button_delete_transaction);
            txtViewCarName = itemView.findViewById(R.id.text_view_car_name);
            txtViewClientNameTitle = itemView.findViewById(R.id.text_view_transaction_client_name_title);
            txtViewClientName = itemView.findViewById(R.id.text_view_transaction_client_name);
            txtViewDateWhen = itemView.findViewById(R.id.text_view_transaction_date_when);
            txtViewDateBooked = itemView.findViewById(R.id.text_view_transaction_date_booked);
            txtViewTimeWhen = itemView.findViewById(R.id.text_view_transaction_time_when);
            txtViewTimeBooked = itemView.findViewById(R.id.text_view_transaction_time_booked);
            txtViewAmountToPay = itemView.findViewById(R.id.text_view_transaction_amount_to_pay);
            txtViewBookingID = itemView.findViewById(R.id.text_view_transaction_booking_id);
            txtViewCarID = itemView.findViewById(R.id.text_view_transaction_car_id);
            txtViewUserID = itemView.findViewById(R.id.text_view_transaction_user_id);

            db = FirebaseFirestore.getInstance();

            if (parentActivity.equals(PARENT_ADMIN_DASHBOARD_ACTIVITY)) {
                txtViewClientNameTitle.setVisibility(View.VISIBLE);
                txtViewClientName.setVisibility(View.VISIBLE);
            }

            if (parentActivity.equals(PARENT_CLIENT_DASHBOARD_ACTIVITY)) {
                txtViewClientNameTitle.setVisibility(View.GONE);
                txtViewClientName.setVisibility(View.GONE);
            }
        }
    }
}
