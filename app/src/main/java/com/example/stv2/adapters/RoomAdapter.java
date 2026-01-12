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

    public interface OnItemClickListener { void onClick(String title); }

    private List<String> titles;
    private Map<String, List<String>> data;
    private OnItemClickListener listener;
    private Boolean isAdmin, isSettingon;

    public RoomAdapter(List<String> titles, Map<String, List<String>> data, OnItemClickListener listener, Boolean admin, Boolean setting) {
        this.titles = titles;
        this.data = data;
        this.listener = listener;
        this.isAdmin = admin;
        this.isSettingon = setting;
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

        // Alapból elrejtve a chatszobák
        holder.contentLayout.removeAllViews();
        holder.contentLayout.setVisibility(View.GONE);

        Context context = holder.itemView.getContext();

        // Click toggle: mutatja/elrejti a chatszobákat
        holder.container.setOnClickListener(v -> {
            if (holder.contentLayout.getVisibility() == View.GONE) {
                holder.contentLayout.removeAllViews();
                List<String> items = data.get(title);
                if (items != null) {
                    for (String item : items) {
                        TextView tv = new TextView(context);
                        tv.setText(item);
                        tv.setTextColor(Color.BLACK);
                        tv.setPadding(32, 16, 32, 16);
                        tv.setOnClickListener(view -> {
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("roomTitle", item);
                            context.startActivity(intent);
                        });
                        holder.contentLayout.addView(tv);
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
        ImageView delete;
        LinearLayout contentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            titleText = itemView.findViewById(R.id.titleText);
            titleEdit = itemView.findViewById(R.id.titleText_edittext);
            delete = itemView.findViewById(R.id.chapter_delete);
            contentLayout = itemView.findViewById(R.id.contentLayout);
        }
    }
}
