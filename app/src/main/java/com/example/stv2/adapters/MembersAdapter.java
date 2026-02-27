package com.example.stv2.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stv2.ClubPageActivity;
import com.example.stv2.ProfileActivity;
import com.example.stv2.R;
import com.example.stv2.SearchActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {

    private List<String> memberEmails;
    private ClubPageActivity.OnChooseBookListener listener;

    public MembersAdapter(List<String> memberEmails, ClubPageActivity.OnChooseBookListener l) {
        this.memberEmails = memberEmails;
        this.listener = l;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("membersadapter", memberEmails.get(position));
        String memberEmail = memberEmails.get(position);
        holder.username.setText("Betöltés...");

        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("email", memberEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        com.google.firebase.firestore.DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        Log.d("membersadapter", "Dokumentum adatai: " + doc.getData());

                        String nickname = doc.getString("username");
                         String id = doc.getId();
                       // holder.username.setText(nickname != null ? nickname : memberEmail);
                        Log.d("membersadapter", nickname);
                        holder.username.setText(nickname);
                        holder.itemView.invalidate();

                        String picUrl = doc.getString("profilepicurl");
                        if (picUrl != null && !picUrl.isEmpty()) {
                            Glide.with(holder.itemView.getContext())
                                    .load(picUrl)
                                    .circleCrop()
                                    .into(holder.userPic);
                        }

                        holder.userButton.setOnClickListener(v -> {
                            listener.onChoose(id);
                        });

                    } else {
                        holder.username.setText(memberEmail);
                    }
                })
                .addOnFailureListener(e -> holder.username.setText("Hiba!"));


    }

    @Override
    public int getItemCount() {
        return memberEmails.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        ImageView userPic;
        Button userButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.menu_username);
            userPic = itemView.findViewById(R.id.menu_userpic);
            userButton = itemView.findViewById(R.id.user_button);
        }
    }
}