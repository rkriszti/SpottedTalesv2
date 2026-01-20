package com.example.stv2.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stv2.ClubPageActivity;
import com.example.stv2.R;
import com.example.stv2.model.Club;

import java.util.ArrayList;
import java.util.List;

public class SearchClubAdapter extends RecyclerView.Adapter<SearchClubAdapter.VH> {

    private List<Club> clubs = new ArrayList<>();

    public void setClubs(List<Club> list) {
        clubs = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_club, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Club c = clubs.get(pos);

        h.name.setText(c.getName());
        h.members.setText(String.valueOf(c.getMembers().size()));
        h.pic.setImageResource(R.drawable.background2);

        h.button.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), ClubPageActivity.class);
            i.putExtra("clubId", c.getId());
            v.getContext().startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return clubs.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, members;
        ImageView pic;
        Button button;

        VH(View v) {
            super(v);
            name = v.findViewById(R.id.menu_clubname);
            members = v.findViewById(R.id.menu_clubtags);
            pic = v.findViewById(R.id.menu_clubpic);
            button = v.findViewById(R.id.clubs_button);
        }
    }
}
