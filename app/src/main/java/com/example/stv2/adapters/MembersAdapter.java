package com.example.stv2.adapters;

import android.app.Activity;
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
import com.example.stv2.HomeActivity;
import com.example.stv2.R;
import com.example.stv2.model.Club;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {

    private List<String> memberEmails;
    private List<String> pendingUserIds;
    private ClubPageActivity.OnChooseBookListener listener;
    private Club club;
    private String currentemail;

    public MembersAdapter(List<String> memberEmails, List<String> pending,
                          ClubPageActivity.OnChooseBookListener l, Club c, String currentuseremail) {
        this.memberEmails = memberEmails;
        this.pendingUserIds = pending;
        this.listener = l;
        this.club = c;
        this.currentemail = currentuseremail;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Alaphelyzetbe állítás (RecyclerView újrahasznosítás miatt fontos!)
        holder.delete.setVisibility(View.GONE);
        holder.accept.setVisibility(View.GONE);
        holder.refuse.setVisibility(View.GONE);
        holder.username.setText("Betöltés...");

        boolean isAdmin = currentemail.equals(club.getAdmin());

        if (position < pendingUserIds.size()) {
            // --- PENDING USER LOGIKA ---
            String userId = pendingUserIds.get(position);

            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String nickname = doc.getString("username");
                            String email = doc.getString("email");
                            holder.username.setText(nickname + " (Várakozik)");

                            String picUrl = doc.getString("profilepicurl");
                            if (picUrl != null && !picUrl.isEmpty()) {
                                Glide.with(holder.itemView.getContext()).load(picUrl).circleCrop().into(holder.userPic);
                            }

                            if (isAdmin) {
                                holder.accept.setVisibility(View.VISIBLE);
                                holder.refuse.setVisibility(View.VISIBLE);
                            }

                            // ELFOGADÁS (Pipa)
                            holder.accept.setOnClickListener(v -> {
                                // 1. RTDB pending törlés
                                FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/")
                                        .getReference("pending_requests").child(club.getId()).child(userId).removeValue();

                                // 2. Firestore members hozzáadás
                                FirebaseFirestore.getInstance().collection("club").document(club.getId())
                                        .update("members", FieldValue.arrayUnion(email));

                                // 3. RTDB connections hozzáadás
                                FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/")
                                        .getReference("connections").child(userId).child("clubs").child(club.getId()).setValue(true);

                                // Lokális modell frissítése, hogy azonnal látszódjon
                                if (!memberEmails.contains(email)) memberEmails.add(email);
                            });

                            // ELUTASÍTÁS (X)
                            holder.refuse.setOnClickListener(v -> {
                                FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/")
                                        .getReference("pending_requests").child(club.getId()).child(userId).removeValue();
                            });
                        }
                    });

        } else {
            // --- BENT LÉVŐ TAG LOGIKA ---
            int memberPos = position - pendingUserIds.size();
            String memberEmail = memberEmails.get(memberPos);

            FirebaseFirestore.getInstance().collection("users").whereEqualTo("email", memberEmail).get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            com.google.firebase.firestore.DocumentSnapshot doc = query.getDocuments().get(0);
                            String nickname = doc.getString("username");
                            String id = doc.getId();
                            holder.username.setText(nickname);

                            String picUrl = doc.getString("profilepicurl");
                            if (picUrl != null && !picUrl.isEmpty()) {
                                Glide.with(holder.itemView.getContext()).load(picUrl).circleCrop().into(holder.userPic);
                            }

                            holder.userButton.setOnClickListener(v -> listener.onChoose(id));

                            // Törlés gomb láthatósága
                            if (isAdmin && !memberEmail.equals(currentemail)) {
                                holder.delete.setVisibility(View.VISIBLE);
                            } else if (!isAdmin && memberEmail.equals(currentemail)) {
                                holder.delete.setVisibility(View.VISIBLE);
                            }

                            holder.delete.setOnClickListener(v -> {
                                handleMemberDelete(v, memberEmail, id, memberPos);
                            });
                        }
                    });
        }
    }

    private void handleMemberDelete(View v, String email, String userId, int pos) {
        club.deleteMember(email);

        FirebaseFirestore.getInstance().collection("club").document(club.getId())
                .update("members", club.getMembers());

        FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("connections").child(userId).child("clubs").child(club.getId()).removeValue();

        if (email.equals(currentemail)) {
            Intent intent = new Intent(v.getContext(), HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            v.getContext().startActivity(intent);
        } else {
            memberEmails.remove(pos);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return pendingUserIds.size() + memberEmails.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        ImageView userPic, delete, accept, refuse;
        Button userButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.menu_username);
            userPic = itemView.findViewById(R.id.menu_userpic);
            userButton = itemView.findViewById(R.id.user_button);
            delete = itemView.findViewById(R.id.member_delete);
            accept = itemView.findViewById(R.id.member_accept);
            refuse = itemView.findViewById(R.id.member_refuse);
        }
    }
}