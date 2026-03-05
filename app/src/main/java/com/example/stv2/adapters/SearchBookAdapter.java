package com.example.stv2.adapters;

import android.app.AlertDialog;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
    private boolean ismoderator = false;
    private boolean isfavchoose = false;
    private SearchActivity.OnChooseBookListener listener;

    public SearchBookAdapter(List<String> usersbooks,
                             ActivityResultLauncher<String> pickImageLauncher,
                             boolean ischoosing, SearchActivity.OnChooseBookListener list,
                             boolean isfavchoose, boolean moderator) {
        this.usersbooks = usersbooks;
        this.pickImageLauncher = pickImageLauncher;
        this.ischoosing = ischoosing;
        this.listener = list;
        this.isfavchoose = isfavchoose;
        this.ismoderator = moderator;
    }

    public void setBooks(List<Book> list, boolean moderator) {
        this.ismoderator = moderator;
        this.books = list;
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
        String bookId = b.getId();

        h.title.setText(b.getTitle());
        h.author.setText(b.getAuthor());

        boolean canEdit = (bookId != null && (usersbooks.contains(bookId) || ismoderator));

        if (b.isEditing()) {
            h.title.setVisibility(View.GONE);
            h.author.setVisibility(View.GONE);
            h.titleedit.setVisibility(View.VISIBLE);
            h.authoredit.setVisibility(View.VISIBLE);
            h.book_save.setVisibility(View.VISIBLE);
            h.book_edit.setVisibility(View.GONE);
            h.book_delete.setVisibility(View.VISIBLE);
            h.book_chooseforclub.setVisibility(View.GONE);
            h.book_favchoose.setVisibility(View.GONE);

            h.titleedit.setText(b.getTitle());
            h.authoredit.setText(b.getAuthor());
        } else {
            h.title.setVisibility(View.VISIBLE);
            h.author.setVisibility(View.VISIBLE);
            h.titleedit.setVisibility(View.GONE);
            h.authoredit.setVisibility(View.GONE);
            h.book_save.setVisibility(View.GONE);

            h.book_edit.setVisibility(canEdit ? View.VISIBLE : View.GONE);

            h.book_chooseforclub.setVisibility(ischoosing ? View.VISIBLE : View.GONE);
            h.book_favchoose.setVisibility(isfavchoose ? View.VISIBLE : View.GONE);

            if(ischoosing) {
                h.book_chooseforclub.setOnClickListener(v -> listener.onChoose(bookId, "CLUB"));
            }
            if(isfavchoose) {
                h.book_favchoose.setOnClickListener(v -> listener.onChoose(bookId, "PROFILE"));
            }

            h.book_delete.setVisibility(View.GONE);
        }

        h.book_edit.setOnClickListener(v -> {
            b.setEditing(true);
            notifyItemChanged(pos);
        });

        h.book_save.setOnClickListener(v -> saveEdit(b, h, pos));

        h.book_delete.setOnClickListener(v -> {
            new AlertDialog.Builder(h.itemView.getContext())
                    .setTitle("Könyv törlése")
                    .setMessage("Biztosan törölni szeretnéd ezt a könyvet?")
                    .setPositiveButton("Igen", (dialog, which) -> deleteBook(b, pos))
                    .setNegativeButton("Mégse", null)
                    .show();
        });

        h.cover.setOnClickListener(v -> {
            if (b.isEditing() && coverClickListener != null) {
                coverClickListener.onCoverClick(pos);
            }
        });

        if (b.getCoverpic() != null && !b.getCoverpic().isEmpty()) {
            Glide.with(h.itemView.getContext())
                    .load(b.getCoverpic())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.background2)
                    .into(h.cover);
        } else {
            h.cover.setImageResource(R.drawable.background2);
        }
    }

    private void saveEdit(Book b, VH h, int pos) {
        String newTitle = h.titleedit.getText().toString().trim();
        String newAuthor = h.authoredit.getText().toString().trim();

        if (newTitle.isEmpty() || newAuthor.isEmpty()) {
            Toast.makeText(h.itemView.getContext(), "Nem lehet üres!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("books")
                .document(b.getId())
                .update("title", newTitle, "author", newAuthor)
                .addOnSuccessListener(aVoid -> {
                    b.setTitle(newTitle);
                    b.setAuthor(newAuthor);
                    b.setEditing(false);
                    notifyItemChanged(pos);
                })
                .addOnFailureListener(e -> Log.e("Search", "Hiba mentéskor", e));
    }

    public void updateBookCover(Uri uri, int pos) {
        if (pos < 0 || pos >= books.size()) return;
        Book b = books.get(pos);

        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("books/" + UUID.randomUUID().toString() + ".jpg");

        ref.putFile(uri).addOnSuccessListener(task -> ref.getDownloadUrl().addOnSuccessListener(url -> {
            String newUrl = url.toString();
            FirebaseFirestore.getInstance().collection("books").document(b.getId())
                    .update("coverpic", newUrl)
                    .addOnSuccessListener(aVoid -> {
                        b.setCoverpic(newUrl);
                        notifyItemChanged(pos);
                    });
        }));
    }

    private void deleteBook(Book b, int pos) {
        String bookId = b.getId();


        FirebaseFirestore.getInstance().collection("books").document(bookId).delete()
                .addOnSuccessListener(aVoid -> {
                    DatabaseReference rtdb = FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/").getReference("connections");
                    for (String uid : usersbooks) {
                        rtdb.child(uid).child("books").child(bookId).removeValue();
                    }
                    if (b.getCoverpic() != null && b.getCoverpic().startsWith("http")) {
                        FirebaseStorage.getInstance().getReferenceFromUrl(b.getCoverpic()).delete();
                    }

                    books.remove(pos);
                    notifyItemRemoved(pos);
                    Log.d("Search", "Könyv sikeresen törölve mindenhonnan.");
                });
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
        ImageView cover, book_edit, book_save, book_delete, book_chooseforclub, book_favchoose;

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
            book_chooseforclub = v.findViewById(R.id.book_chooseforclub);
            book_favchoose = v.findViewById(R.id.book_favchoose);
        }
    }
}