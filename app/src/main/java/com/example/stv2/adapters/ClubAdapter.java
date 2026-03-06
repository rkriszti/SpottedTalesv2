package com.example.stv2.adapters;

import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
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
import com.example.stv2.ClubsActivity;
import com.example.stv2.HomeActivity;
import com.example.stv2.R;
import com.example.stv2.RegistActivity;
import com.example.stv2.model.Club;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.ArrayList;
import java.util.List;

public class ClubAdapter extends RecyclerView.Adapter<ClubAdapter.ClubViewHolder> {

    private List<Club> aktualis_clubs = new ArrayList<>();

    private ClubsActivity.OnClubClickListener listener;

    public ClubAdapter(ClubsActivity.OnClubClickListener l) {
        listener = l;
    }


    //frissítés
    public void setClubs(List<Club> list) {
        aktualis_clubs = list; //mindig hozzáad egyet az activity
        notifyDataSetChanged(); //frissíti az oldalon
    }

    //ha új elem kell
    @NonNull
    @Override
    public ClubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_club, parent, false); //betölti


        return new ClubViewHolder(v);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(@NonNull ClubViewHolder holder, int position) {
        Club c = aktualis_clubs.get(position);
        holder.name.setText(c.getName());
        holder.members.setText(String.valueOf(c.getMembers().size()));

        // kép alaphelyzetbe állítása (fontos a görgetés miatt!)
        holder.pic.setImageResource(R.drawable.background2);

        String bookId = c.getBookId();

        if (bookId != null && !bookId.isEmpty()) {
            // Realtime DB helyett FIRESTORE lekérés
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("books")
                    .document(bookId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String imageUrl = documentSnapshot.getString("coverpic");

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(holder.itemView.getContext())
                                        .load(imageUrl)
                                        .placeholder(R.drawable.background2)
                                        .error(R.drawable.background2)
                                        .centerCrop()
                                        .into(holder.pic);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", "Hiba a könyv lekérésekor: " + e.getMessage());
                    });
        }

        holder.button.setOnClickListener(v -> listener.onClubClick(c));
    }

    @Override
    public int getItemCount() {
        return aktualis_clubs.size();
    }

    static class ClubViewHolder extends RecyclerView.ViewHolder {
        TextView name, members;
        ImageView pic;
        Button button;

        ClubViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.menu_clubname);
            members = v.findViewById(R.id.menu_clubtags); // tagok TextView
            pic = v.findViewById(R.id.menu_clubpic);      // club kép
            button = v.findViewById(R.id.clubs_button);

        }
    }

}

