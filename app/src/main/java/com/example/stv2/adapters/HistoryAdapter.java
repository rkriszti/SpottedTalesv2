package com.example.stv2.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stv2.ClubPageActivity;
import com.example.stv2.HomeActivity;
import com.example.stv2.R;
import com.example.stv2.model.Book;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<Book> books = new ArrayList<>();
    private String clubId, bookId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean edit;

    public HistoryAdapter(String clubId, boolean edit) {
        this.clubId = clubId;
        this.edit = edit;
        loadAllHistory();
    }

    private void loadAllHistory() {
        db.collection("club").document(clubId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String bId = doc.getString("bookId");
                bookId = bId; //aktív könyv
                if (bId != null) fetchBook(bId);
            }

            db.collection("oldclub").whereEqualTo("id", clubId).get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot d : querySnapshot) {
                            String bId = d.getString("bookId");
                            if (bId != null) fetchBook(bId);
                        }
                    });
        });
    }

    private void fetchBook(String bId) {
        for (Book existingBook : books) {
            if (existingBook.getId().equals(bId)) return;
        }

        db.collection("books").document(bId).get().addOnSuccessListener(doc -> {
            Book b = doc.toObject(Book.class);
            if (b != null) {
                b.setId(doc.getId());
                books.add(b);
                notifyDataSetChanged();
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book b = books.get(position);
        holder.title.setText(b.getTitle());
        holder.author.setText(b.getAuthor());
        Glide.with(holder.itemView.getContext()).load(b.getCoverpic()).centerCrop().into(holder.cover);

        holder.club_active.setVisibility(View.VISIBLE);

        boolean isActive = bookId != null && bookId.equals(b.getId());

        if(isActive){
            holder.club_active.setText("Aktív");
            holder.club_active.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#4CAF50")
            ));
            holder.book_delete.setVisibility(View.GONE); // Az aktívat ne lehessen törölni
        } else {
            holder.club_active.setText("Régi");
            holder.club_active.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.RED
            ));
            if(edit){
                holder.book_delete.setVisibility(View.VISIBLE);
            }

        }

        holder.book_delete.setOnClickListener(l -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            new android.app.AlertDialog.Builder(l.getContext())
                    .setTitle("Törlés")
                    .setMessage("Törlöd az előzményt?")
                    .setPositiveButton("Igen", (dialog, which) -> {

                        db.collection("oldclub")
                                .whereEqualTo("id", clubId)
                                .whereEqualTo("bookId", b.getId())
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    for (QueryDocumentSnapshot doc : querySnapshot) {
                                        String oldDocId = doc.getId();


                                        db.collection("oldclub").document(oldDocId).delete()
                                                .addOnSuccessListener(aVoid -> {


                                                    FirebaseDatabase.getInstance().getReference("messages")
                                                            .orderByChild("roomPath")
                                                            .startAt(oldDocId + "_")
                                                            .endAt(oldDocId + "_\uf8ff")
                                                            .get().addOnSuccessListener(snapshot -> {
                                                                for (com.google.firebase.database.DataSnapshot ds : snapshot.getChildren()) {
                                                                    ds.getRef().removeValue();
                                                                }


                                                                if (currentPos < books.size()) {
                                                                    books.remove(currentPos);
                                                                    notifyItemRemoved(currentPos);
                                                                    notifyItemRangeChanged(currentPos, books.size());
                                                                }


                                                                if (l.getContext() instanceof android.app.Activity) {
                                                                    Toast.makeText(l.getContext(), "Előzmény törölve!", Toast.LENGTH_SHORT).show();

                                                                    android.app.Activity activity = (android.app.Activity) l.getContext();

                                                                    activity.finish();
                                                                }
                                                            });
                                                });
                                    }
                                });
                    })
                    .setNegativeButton("Mégse", null)
                    .show();
        });

        holder.cover.setOnClickListener( v ->{
            Log.d("HISTORY", "2. kiválasztott könyv: " + b.getTitle());
            Log.d("HISTORY", "2. ehhez nagy club" + clubId);
            Intent intent = new Intent(v.getContext(), ClubPageActivity.class);
            intent.putExtra("clubId", clubId); //nagy club id
            intent.putExtra( "oldbook", b.getId());
           // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return books.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover, book_chooseforclub, book_delete;
        TextView title, author;
        Button club_active;

        ViewHolder(View v) {
            super(v);
            cover = v.findViewById(R.id.book_cover);
            title = v.findViewById(R.id.book_title);
            author = v.findViewById(R.id.book_author);
            club_active = v.findViewById(R.id.club_active);
            book_delete = v.findViewById(R.id.book_delete);
            book_chooseforclub = v.findViewById(R.id.book_chooseforclub);

            v.findViewById(R.id.book_edit).setVisibility(View.GONE);
            v.findViewById(R.id.book_save).setVisibility(View.GONE);
            v.findViewById(R.id.book_delete).setVisibility(View.GONE);

        }
    }
}