package com.example.stv2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stv2.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Map;

public class VoteAdapter extends RecyclerView.Adapter<VoteAdapter.VH> {
    private List<String> bookTitles;
    private Map<String, Long> voteCounts;
    private OnVoteClickListener listener;
    private java.util.Set<String> userVotes;

    public interface OnVoteClickListener {
        void onVote(String title);
    }

    public VoteAdapter(List<String> bookTitles, Map<String, Long> voteCounts, java.util.Set<String> userVotes, OnVoteClickListener listener) {
        this.bookTitles = bookTitles;
        this.voteCounts = voteCounts;
        this.userVotes = userVotes;
        this.listener = listener;
    }

    // UpdateData frissítése
    public void updateData(List<String> titles, Map<String, Long> counts, java.util.Set<String> userVotes) {
        this.bookTitles = titles;
        this.voteCounts = counts;
        this.userVotes = userVotes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vote, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String title = bookTitles.get(position);
        long count = voteCounts.containsKey(title) ? voteCounts.get(title) : 0;

        holder.title.setText(title);
        holder.count.setText(String.valueOf(count));

        if (userVotes != null && userVotes.contains(title)) {
            holder.voteBtn.setText("Visszavonás");
            holder.voteBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY));
        } else {
            holder.voteBtn.setText("Szavazok");
            holder.voteBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#6E1A5D")));
        }
        holder.voteBtn.setOnClickListener(v -> listener.onVote(title));
    }

    @Override
    public int getItemCount() { return bookTitles.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, count;
        Button voteBtn;
        VH(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.bookTitle);
            count = itemView.findViewById(R.id.voteCount);
            voteBtn = itemView.findViewById(R.id.voteButton);
        }
    }
}