package com.example.stv2.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stv2.ChatActivity;
import com.example.stv2.R;

import java.util.List;
import java.util.Map;

public class ChaptersAdapter extends RecyclerView.Adapter<ChaptersAdapter.ViewHolder> {
    private List<String> chapterTitles;
    private Map<String, List<String>> chatsMap; // a chatszobák listája
    private Context context;

    public ChaptersAdapter(Context context, List<String> chapterTitles, Map<String, List<String>> chatsMap) {
        this.context = context;
        this.chapterTitles = chapterTitles;
        this.chatsMap = chatsMap;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        LinearLayout contentLayout;
        LinearLayout container;
        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.chapter_title);
            contentLayout = itemView.findViewById(R.id.chapter_content);
            container = itemView.findViewById(R.id.chapter_container);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String title = chapterTitles.get(position);
        holder.title.setText(title);

        // először rejtve
        holder.contentLayout.setVisibility(View.GONE);

        // click listener a lenyitáshoz
        holder.container.setOnClickListener(v -> {
            if (holder.contentLayout.getVisibility() == View.GONE) {
                holder.contentLayout.setVisibility(View.VISIBLE);
            } else {
                holder.contentLayout.setVisibility(View.GONE);
            }
        });

        // feltöltjük a chatszobákat, ha vannak
        holder.contentLayout.removeAllViews();
        List<String> chats = chatsMap.get(title);
        if (chats != null) {
            for (String chatName : chats) {
                TextView tv = new TextView(context);
                tv.setText(chatName);
                tv.setPadding(16, 8, 16, 8);
                tv.setTextColor(Color.BLACK);
                tv.setOnClickListener(view -> {
                    // ide jön a chat activity indítás
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("chatName", chatName);
                    context.startActivity(intent);
                });
                holder.contentLayout.addView(tv);
            }
        }
    }

    @Override
    public int getItemCount() {
        return chapterTitles.size();
    }
}
