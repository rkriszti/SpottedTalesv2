package com.example.stv2.adapters;

import static androidx.core.content.ContextCompat.startActivity;

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
import com.example.stv2.ClubPageActivity;
import com.example.stv2.R;

import java.util.List;
import java.util.Map;

public class ClubRoomAdapter extends RecyclerView.Adapter<ClubRoomAdapter.ViewHolder> {

    private String clubid;
    private List<String> titles;
    private Map<String, List<String>> data;
    private Boolean isAdmin, isSettingon, isUniqueChapters;
    private  ClubPageActivity.OnDeleteCustomClickListener deletelistener;

    public interface OnItemClickListener { void onClick(String title); }

    public ClubRoomAdapter(List<String> titles, Map<String, List<String>> data,
                           Boolean admin, Boolean setting, Boolean isUniqueChapters,
                           ClubPageActivity.OnDeleteCustomClickListener listenerr, String clubid) {
        this.titles = titles;
        this.data = data;
        this.isAdmin = admin;
        this.isSettingon = setting;
        this.isUniqueChapters = isUniqueChapters;
        this.deletelistener = listenerr;
        this.clubid = clubid;
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

        holder.titleText.setOnClickListener( k -> {
            Context context = k.getContext();
            Intent i = new Intent(context, ChatActivity.class);
            i.putExtra("clubId", clubid);
            i.putExtra("roomName", title);
            context.startActivity(i);
        });

        if (isAdmin && isSettingon && isUniqueChapters) {
            holder.deleteChapter.setVisibility(View.VISIBLE);

            holder.deleteChapter.setOnClickListener(v -> {
                data.remove(title);
                titles.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, titles.size());
            });
        } else {
            holder.deleteChapter.setVisibility(View.GONE);
        }

        // Alaphelyzetbe állítás
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

                        if (isAdmin && isSettingon && isUniqueChapters) {
                            ImageView deleteIcon = new ImageView(context);
                            deleteIcon.setImageResource(R.drawable.ic_delete);

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(64, 64);
                            params.setMargins(16, 0, 0, 0);
                            deleteIcon.setLayoutParams(params);


                            deleteIcon.setOnClickListener(d -> {
                                if (deletelistener != null) {
                                    deletelistener.onDeleteClick(title);
                                }

                                //frissítés lokál
                                List<String> currentItems = data.get(title);
                                if (currentItems != null) {
                                    currentItems.remove(item);
                                    if (currentItems.isEmpty()) {
                                        data.remove(title);
                                        titles.remove(title);
                                        notifyDataSetChanged();
                                    } else {
                                        rowLayout.setVisibility(View.GONE);
                                    }
                                }
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