package com.example.stv2.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stv2.R;
import com.example.stv2.model.Club;

import java.util.ArrayList;
import java.util.List;

public class ClubAdapter extends RecyclerView.Adapter<ClubAdapter.ClubViewHolder> {

    private List<Club> aktualis_clubs = new ArrayList<>();

    //frissítés
    public void setClubs(List<Club> list) {
        aktualis_clubs = list;
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

    @Override
    public void onBindViewHolder(@NonNull ClubViewHolder holder, int position) {
        Club c = aktualis_clubs.get(position);
        holder.name.setText(c.getName());
        holder.admin.setText("Admin: " + c.getAdmin());
    }

    @Override
    public int getItemCount() {
        return aktualis_clubs.size();
    }

    static class ClubViewHolder extends RecyclerView.ViewHolder {
        TextView name, admin;
        ClubViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.menu_clubname);
           // admin = v.findViewById(R.id.tvClubAdmin);
        }
    }
}

