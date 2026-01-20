package com.example.stv2.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stv2.R;
import com.example.stv2.model.Book;

import java.util.ArrayList;
import java.util.List;

public class SearchBookAdapter extends RecyclerView.Adapter<SearchBookAdapter.VH> {

    private List<Book> books = new ArrayList<>();

    public void setBooks(List<Book> list) {
        books = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Book b = books.get(pos);

        h.title.setText(b.getTitle());

        if (b.getCoverpic() != null) {
            Glide.with(h.itemView.getContext())
                    .load(b.getCoverpic())
                    .into(h.cover);
        } else {
            h.cover.setImageResource(R.drawable.background2);
        }
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        ImageView cover;

        VH(View v) {
            super(v);
            title = v.findViewById(R.id.book_title);
            cover = v.findViewById(R.id.book_cover);
        }
    }
}
