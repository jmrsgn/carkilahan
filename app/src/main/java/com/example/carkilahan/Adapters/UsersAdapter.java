package com.example.carkilahan.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carkilahan.Classes.User;
import com.example.carkilahan.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private static final String TAG = "AllUsersActivity";

    //variables
    private ArrayList<User> allUsers = new ArrayList<>();
    private Context mContext;

    //firebase
    private FirebaseFirestore db;
    private FirebaseUser user;

    //references
    private DocumentReference userReference;

    //entities | fields
    private static final String ENTITY_USERS = "Users";

    public UsersAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user, parent, false);
        return new UsersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        userReference = db.collection(ENTITY_USERS).document(allUsers.get(position).getUserID());
        userReference
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        holder.txtViewUserID.setText(allUsers.get(position).getUserID());
                        holder.txtViewUserName.setText(allUsers.get(position).getUserName());
                        holder.txtViewUserContactNo.setText(allUsers.get(position).getUserContactNo());

                        if (allUsers.get(position).getUserImageURL() != "") {
                            Glide.with(mContext)
                                    .asBitmap()
                                    .load(allUsers.get(position).getUserImageURL())
                                    .into(holder.imgViewAvatar);
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return allUsers.size();
    }

    public void setAllUsers(ArrayList<User> allUsers) {
        this.allUsers = allUsers;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //views
        private CardView parent;
        private ImageView imgViewAvatar;
        private TextView txtViewUserID,
                         txtViewUserName,
                         txtViewUserContactNo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            imgViewAvatar = itemView.findViewById(R.id.image_view_avatar);
            txtViewUserID = itemView.findViewById(R.id.text_view_user_id);
            txtViewUserName = itemView.findViewById(R.id.text_view_user_name);
            txtViewUserContactNo = itemView.findViewById(R.id.text_view_user_contact_no);

            db = FirebaseFirestore.getInstance();
            user = FirebaseAuth.getInstance().getCurrentUser();
        }
    }
}
