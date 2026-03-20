package com.example.stv2.adapters;

import android.content.Context;
import android.content.Intent;
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

    private String clubid, oldclubid;
    private List<String> titles;
    private Map<String, List<String>> data;
    private Boolean isAdmin, isSettingon, isUniqueChapters, oldhappened;
    private ClubPageActivity.OnDeleteCustomClickListener deletelistener;

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
        oldclubid = "";
        this.oldhappened = false;
    }

    public ClubRoomAdapter(List<String> titles, Map<String, List<String>> data,
                           Boolean admin, Boolean setting, Boolean isUniqueChapters,
                           ClubPageActivity.OnDeleteCustomClickListener listenerr, String clubid, String oldclubid) {
        this.titles = titles;
        this.data = data;
        this.isAdmin = admin;
        this.isSettingon = setting;
        this.isUniqueChapters = isUniqueChapters;
        this.deletelistener = listenerr;
        this.clubid = clubid;
        this.oldclubid = oldclubid;
        this.oldhappened = true;
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

        View.OnClickListener openChat = k -> {
            Context context = k.getContext();
            Intent i = new Intent(context, ChatActivity.class);

            if (oldhappened && oldclubid != null && !oldclubid.isEmpty()) {
                i.putExtra("clubId", oldclubid);
                i.putExtra("isOldChat", "true");
                i.putExtra("roomName", title);
            } else {
                i.putExtra("clubId", clubid);
                i.putExtra("isOldChat", "false");
                i.putExtra("roomName", title);
            }
            context.startActivity(i);
        };

        holder.titleText.setOnClickListener(openChat);
        holder.container.setOnClickListener(openChat);

        if (isAdmin && isSettingon && isUniqueChapters) {
            holder.deleteChapter.setVisibility(View.VISIBLE);
            holder.deleteChapter.setOnClickListener(v -> {
                if (deletelistener != null) {
                    deletelistener.onDeleteClick(title, oldhappened);
                }
                data.remove(title);
                titles.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, titles.size());
            });
        } else {
            holder.deleteChapter.setVisibility(View.GONE);
        }

        holder.contentLayout.removeAllViews();
        holder.contentLayout.setVisibility(View.GONE);
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