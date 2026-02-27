package com.example.stv2.adapters;

import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
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

        String imageUrl = null;
        if (c.getBook() != null) {
            imageUrl = c.getBook().getCoverpic();
        }

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.background2)
                .error(R.drawable.background2)
                .centerCrop()
                .into(holder.pic);

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

