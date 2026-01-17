package com.example.stv2.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stv2.ChatActivity;
import com.example.stv2.R;

import java.util.List;
import java.util.Map;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    private List<String> titles;
    private Map<String, List<String>> data;
    private OnItemClickListener listener;
    private Boolean isAdmin, isSettingon, isUniqueChapters;

    public interface OnItemClickListener { void onClick(String title); }

    public RoomAdapter(List<String> titles, Map<String, List<String>> data, OnItemClickListener listener,
                       Boolean admin, Boolean setting, Boolean isUniqueChapters) {
        this.titles = titles;
        this.data = data;
        this.listener = listener;
        this.isAdmin = admin;
        this.isSettingon = setting;
        this.isUniqueChapters = isUniqueChapters;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room_expandable, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String title = titles.get(position);
        holder.titleText.setText(title);

        // Főcím törlés gombja (a szoba neve mellett)
        if (isAdmin && isSettingon && isUniqueChapters) {
            holder.deleteChapter.setVisibility(View.VISIBLE);
        } else {
            holder.deleteChapter.setVisibility(View.GONE);
        }

        holder.contentLayout.removeAllViews();
        holder.contentLayout.setVisibility(View.GONE);
        Context context = holder.itemView.getContext();

        holder.container.setOnClickListener(v -> {
            if (holder.contentLayout.getVisibility() == View.GONE) {
                holder.contentLayout.removeAllViews();
                List<String> items = data.get(title);

                if (items != null) {
                    for (String item : items) {
                        LinearLayout rowLayout = new LinearLayout(context);
                        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                        rowLayout.setPadding(32, 16, 32, 16);

                        TextView tv = new TextView(context);
                        tv.setText(item);
                        tv.setTextColor(Color.BLACK);
                        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                        rowLayout.addView(tv);

                        // DINAMIKUS TÖRLÉS IKON LÉTREHOZÁSA
                        if (isAdmin && isSettingon && isUniqueChapters) {
                            ImageView deleteIcon = new ImageView(context);
                            deleteIcon.setImageResource(R.drawable.ic_delete); // Győződj meg róla, hogy ez a fájl létezik!

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(64, 64);
                            params.setMargins(16, 0, 0, 0);
                            deleteIcon.setLayoutParams(params);

                            deleteIcon.setOnClickListener(d -> {
                                rowLayout.setVisibility(View.GONE);
                                // Itt hívd meg a Firestore törlést, ha szükséges
                            });
                            rowLayout.addView(deleteIcon);
                        }

                        holder.contentLayout.addView(rowLayout);
                    }
                }
                holder.contentLayout.setVisibility(View.VISIBLE);
            } else {
                holder.contentLayout.setVisibility(View.GONE);
            }
        });
    }
    @Override
    public int getItemCount() {
        return titles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        TextView titleText;
        EditText titleEdit;
        ImageView deleteChapter;
        LinearLayout contentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            titleText = itemView.findViewById(R.id.titleText);
            contentLayout = itemView.findViewById(R.id.contentLayout);
            titleEdit = itemView.findViewById(R.id.titleText_edittext);
            deleteChapter = itemView.findViewById(R.id.expand_deletebutton);
        }
    }
}