package com.example.stv2.adapters;

import android.app.Activity;
import android.content.Context;
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
import com.example.stv2.ProfileActivity;
import com.example.stv2.R;
import com.example.stv2.SearchActivity;
import com.example.stv2.model.Club;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {

    private List<String> memberEmails;
    private ClubPageActivity.OnChooseBookListener listener;
    private Club club;
    private String currentemail;

    public MembersAdapter(List<String> memberEmails, ClubPageActivity.OnChooseBookListener l, Club c, String currentuseremail) {
        this.memberEmails = memberEmails;
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

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        Log.d("membersadapter", "1. " + memberEmails.get(position));
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
                        Log.d("membersadapter", "username: "+nickname);
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

                        // Ha admin, akkor mindenkit törölhet, csak magát ne
                        if (currentemail.equals(club.getAdmin()) && !memberEmail.equals(currentemail)) {
                            holder.delete.setVisibility(View.VISIBLE);
                        }
// Ha nem admin, csak magát törölheti
                        else if (!currentemail.equals(club.getAdmin()) && memberEmail.equals(currentemail)) {
                            holder.delete.setVisibility(View.VISIBLE);
                        }
                        else {
                            holder.delete.setVisibility(View.GONE);
                        }


                        holder.delete.setOnClickListener(v -> {
                            int adapterPos = holder.getAdapterPosition();
                            if (adapterPos == RecyclerView.NO_POSITION) return;

                            String removedEmail = memberEmails.get(adapterPos);

                            // 1. Saját törlés speciális kezelése
                            if (removedEmail.equals(currentemail)) {
                                // Frissítjük az adatbázisokat
                                club.deleteMember(removedEmail);

                                FirebaseFirestore.getInstance()
                                        .collection("club")
                                        .document(club.getId())
                                        .update("members", club.getMembers());

                                if (id != null) {
                                    FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/")
                                            .getReference("connections").child(id).child("club").child(club.getId()).removeValue();
                                }

                                // AZONNALI navigáció, nincs notifyItemRemoved (ez okozta a crasht)
                                Intent intent = new Intent(v.getContext(), HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                v.getContext().startActivity(intent);

                            } else {
                                // 2. Más tag törlése (maradunk az oldalon)
                                club.deleteMember(removedEmail);
                                memberEmails.remove(adapterPos);
                                notifyItemRemoved(adapterPos);
                                notifyItemRangeChanged(adapterPos, memberEmails.size());

                                FirebaseFirestore.getInstance()
                                        .collection("club")
                                        .document(club.getId())
                                        .update("members", club.getMembers());

                                if (id != null) {
                                    FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/")
                                            .getReference("connections").child(id).child("club").child(club.getId()).removeValue();
                                }
                            }
                        });

                    } else {
                        Log.d("membersadapter", "Nincs username? nem találja");
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
        ImageView userPic, delete;
        Button userButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.menu_username);
            userPic = itemView.findViewById(R.id.menu_userpic);
            userButton = itemView.findViewById(R.id.user_button);
            delete = itemView.findViewById(R.id.member_delete);
        }
    }
}