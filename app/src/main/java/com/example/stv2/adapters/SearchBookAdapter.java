package com.example.stv2.adapters;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.stv2.ClubPageActivity;
import com.example.stv2.R;
import com.example.stv2.SearchActivity;
import com.example.stv2.model.Book;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SearchBookAdapter extends RecyclerView.Adapter<SearchBookAdapter.VH> {

    private List<Book> books = new ArrayList<>();
    private List<String> usersbooks = new ArrayList<>();
    private ActivityResultLauncher<String> pickImageLauncher;
    private OnCoverClickListener coverClickListener;
    private boolean ischoosing = false;
    SearchActivity.OnChooseBookListener listener;

    public SearchBookAdapter(List<String> usersbooks,
                             ActivityResultLauncher<String> pickImageLauncher, boolean ischoosing, SearchActivity.OnChooseBookListener list) {
        this.usersbooks = usersbooks;
        this.pickImageLauncher = pickImageLauncher;
        this.ischoosing = ischoosing;
        this.listener = list;
    }

    public void setBooks(List<Book> list) {
        books = list;
        notifyDataSetChanged();
    }

    public void setOnCoverClickListener(OnCoverClickListener listener) {
        this.coverClickListener = listener;
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

        // --- SZÖVEGEK ---
        h.title.setText(b.getTitle());
        h.author.setText(b.getAuthor());

        // --- EDIT ÁLLAPOT KEZELÉSE ---
        if (b.isEditing()) {
            h.title.setVisibility(View.GONE);
            h.author.setVisibility(View.GONE);
            h.titleedit.setVisibility(View.VISIBLE);
            h.authoredit.setVisibility(View.VISIBLE);
            h.book_save.setVisibility(View.VISIBLE);
            h.book_edit.setVisibility(View.GONE);
            h.book_delete.setVisibility(View.VISIBLE);

            h.titleedit.setText(b.getTitle());
            h.authoredit.setText(b.getAuthor());
        } else {
            h.title.setVisibility(View.VISIBLE);
            h.author.setVisibility(View.VISIBLE);
            h.titleedit.setVisibility(View.GONE);
            h.authoredit.setVisibility(View.GONE);
            h.book_save.setVisibility(View.GONE);
            h.book_edit.setVisibility(View.GONE);
            if(ischoosing){
                h.book_edit.setVisibility(View.VISIBLE);
                listener.onChoose(b.getId());
            }

            h.book_delete.setVisibility(View.GONE);
        }

        // --- JOGOSULTSÁG ELLENŐRZÉS ---
        boolean canEdit = false;
        String bookId = b.getId();

        if (bookId != null) {
            for (String a : usersbooks) {
                if (a != null && bookId.equals(a)) {
                    canEdit = true;
                    h.book_edit.setOnClickListener(v -> startEdit(h, b, a));
                    break;
                }
            }
        }

        if (!b.isEditing()) {
            h.book_edit.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        }

        // --- KÉP BETÖLTÉS ---
        if (b.getCoverpic() != null) {
            Glide.with(h.itemView.getContext())
                    .load(b.getCoverpic())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(h.cover);
        } else {
            h.cover.setImageResource(R.drawable.background2);
        }

        // --- KÉP CSERE CSAK EDIT MÓDBAN ---
        h.cover.setOnClickListener(v -> {
            if (b.isEditing() && coverClickListener != null) {
                coverClickListener.onCoverClick(h.getAdapterPosition());
            }
        });

        // --- SAVE ---
        h.book_save.setOnClickListener(v -> saveEdit(b, h, bookId));

        // --- DELETE ---
        h.book_delete.setOnClickListener(v -> {
            if (bookId != null) {
                new AlertDialog.Builder(h.itemView.getContext())
                        .setTitle("Könyv törlése")
                        .setMessage("Biztosan törölni szeretnéd ezt a könyvet?")
                        .setPositiveButton("Igen", (dialog, which) -> deleteBook(b, pos))
                        .setNegativeButton("Mégse", null)
                        .show();
            }
        });
    }

    private void startEdit(VH h, Book b, String id) {
        b.setEditing(true);
        h.titleedit.setText(b.getTitle());
        h.authoredit.setText(b.getAuthor());
        notifyItemChanged(h.getAdapterPosition());
    }

    private void saveEdit(Book b, VH h, String id) {
        if (id == null) return;

        String newTitle = h.titleedit.getText().toString();
        String newAuthor = h.authoredit.getText().toString();

        if (newTitle == null || newTitle.isEmpty()) {
            newTitle = h.title.getText().toString();
        }
        if (newAuthor == null || newAuthor.isEmpty()) {
            newAuthor = h.author.getText().toString();
        }

        b.setTitle(newTitle);
        b.setAuthor(newAuthor);

        FirebaseFirestore.getInstance()
                .collection("books")
                .document(id)
                .update("title", newTitle, "author", newAuthor)
                .addOnSuccessListener(aVoid -> {
                    b.setEditing(false);
                    notifyItemChanged(h.getAdapterPosition());
                    Log.d("Search", "Könyv frissítve");
                })
                .addOnFailureListener(e -> Log.e("Search", "Mentési hiba", e));
    }

    // --- KÉP FRISSÍTÉSE ---
    public void updateBookCover(Uri uri, int pos) {
        if (pos < 0 || pos >= books.size()) {
            Log.e("Search", "Hibás pozíció: " + pos);
            return;
        }

        Book b = books.get(pos);
        String bookId = b.getId();
        if (bookId == null || bookId.isEmpty()) {
            Log.e("Search", "Book ID hiányzik, nem lehet menteni");
            return;
        }

        Log.d("Search", "Kép feltöltés kezdődik könyvhöz: " + bookId);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageRef = storage.getReference()
                .child("books/" + UUID.randomUUID() + ".jpg");

        imageRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("Search", "Kép feltöltve Storage-ba");
                    imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        Log.d("Search", "Download URL: " + downloadUri.toString());

                        b.setCoverpic(downloadUri.toString());
                        notifyItemChanged(pos);

                        FirebaseFirestore.getInstance()
                                .collection("books")
                                .document(bookId)
                                .update("coverpic", downloadUri.toString())
                                .addOnSuccessListener(aVoid -> Log.d("Search", "Kép mentve Firestore-ba"))
                                .addOnFailureListener(e -> Log.e("Search", "Firestore mentési hiba", e));
                    }).addOnFailureListener(e -> Log.e("Search", "Download URL lekérés hiba", e));
                })
                .addOnFailureListener(e -> Log.e("Search", "Kép feltöltési hiba", e));
    }

    // --- TÖRLÉS MINDENHOL ---
    private void deleteBook(Book b, int pos) {
        String bookId = b.getId();
        if (bookId == null) return;

        // Firestore törlés
        FirebaseFirestore.getInstance()
                .collection("books")
                .document(bookId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("Search", "Könyv törölve Firestore-ból"))
                .addOnFailureListener(e -> Log.e("Search", "Firestore törlési hiba", e));

        // Realtime Database connections törlés
        DatabaseReference ref = FirebaseDatabase.getInstance(
                        "https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("connections");

        for (String userId : usersbooks) {
            ref.child(userId).child("books").child(bookId).removeValue()
                    .addOnSuccessListener(aVoid -> Log.d("Search", "Könyv connection törölve: " + userId))
                    .addOnFailureListener(e -> Log.e("Search", "Connection törlési hiba: " + userId, e));
        }

        // Storage törlés, ha van borító
        if (b.getCoverpic() != null && !b.getCoverpic().isEmpty()) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(b.getCoverpic());
            imageRef.delete()
                    .addOnSuccessListener(aVoid -> Log.d("Search", "Borítókép törölve Storage-ból"))
                    .addOnFailureListener(e -> Log.e("Search", "Borítókép törlési hiba", e));
        }

        // RecyclerView frissítése
        books.remove(pos);
        notifyItemRemoved(pos);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public interface OnCoverClickListener {
        void onCoverClick(int pos);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, author;
        EditText titleedit, authoredit;
        ImageView cover, book_edit, book_save, book_delete;

        VH(View v) {
            super(v);
            title = v.findViewById(R.id.book_title);
            author = v.findViewById(R.id.book_author);
            cover = v.findViewById(R.id.book_cover);
            titleedit = v.findViewById(R.id.book_title_edittext);
            authoredit = v.findViewById(R.id.book_author_edittext);
            book_edit = v.findViewById(R.id.book_edit);
            book_save = v.findViewById(R.id.book_save);
            book_delete = v.findViewById(R.id.book_delete);
        }
    }
}
