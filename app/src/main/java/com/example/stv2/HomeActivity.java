package com.example.stv2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class HomeActivity extends MenuActivity {
    private FirebaseFirestore firestore;
    private DatabaseReference rtDb;
    private TextView bookTitleTv, textView;
    private ImageView dayCoverIv, outline;
    private String today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Log.d("HOME", "oncreate elindul");
        // Realtime Database

        firestore = FirebaseFirestore.getInstance();
        String dbUrl = "https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/";
        rtDb = FirebaseDatabase.getInstance(dbUrl).getReference("DailyBook");
        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        String currentUid = FirebaseAuth.getInstance().getUid();

        isModerator(currentUid);

        bookTitleTv = findViewById(R.id.bookOfTheDayTitle);
        dayCoverIv = findViewById(R.id.daycover);
        outline = findViewById(R.id.imageView2);
        textView  = findViewById(R.id.textView);
       // dayCoverIv.setVisibility(View.GONE);

        checkDailyBook();

        setupBottomMenu(R.id.nav_home);
        setupTopMenu();
    }

    private void checkDailyBook() {
        Log.d("HOME", "check elindul");
         today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        rtDb.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists() && today.equals(snapshot.child("date").getValue(String.class))) {
              //van régi
                Log.d("HOME", "van mára elindul");
                String bookId = snapshot.child("bookId").getValue(String.class);
                loadBookDetails(bookId);
            } else {
            //új nap
                Log.d("HOME", "új kell elindul");
                drawNewBook(today);
            }
        });
    }

    private void drawNewBook(String date) {
        Log.d("HOME", "új sorsolunk elindul");
        firestore.collection("books").limit(50).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                List<DocumentSnapshot> books = queryDocumentSnapshots.getDocuments();
                int randomIndex = new Random().nextInt(books.size());
                DocumentSnapshot selectedBook = books.get(randomIndex);

                String newBookId = selectedBook.getId();

                Map<String, Object> update = new HashMap<>();
                update.put("date", date);
                update.put("bookId", newBookId);

                rtDb.setValue(update).addOnSuccessListener(unused -> loadBookDetails(newBookId));
            }
        });
    }

    private void loadBookDetails(String bookId) {
        Log.d("HOME", "betöltés elindul");
        firestore.collection("books").document(bookId).get().addOnSuccessListener(doc -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (doc.exists()) {
                bookTitleTv.setText(doc.getString("title"));

                String imageUrl = doc.getString("coverpic");
                if ((imageUrl != null && !imageUrl.isEmpty() && !imageUrl.contains("default_book"))){
                    dayCoverIv.setVisibility(View.VISIBLE);
                    outline.setBackground(null);
                    outline.setBackgroundResource(R.drawable.book_outline_cover2);

                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.default_book)
                            .error(R.drawable.default_book)
                            .centerCrop()
                            .into(dayCoverIv);
                } else {
                    dayCoverIv.setVisibility(View.GONE);
                    Glide.with(this).clear(dayCoverIv);
                    outline.setBackground(null);
                    outline.setBackgroundResource(R.drawable.book_outline2);
                }
            }
        });
    }

    private void isModerator(String uid){
        Log.d("HOME", "is moderator");
        if (uid != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean moderator = documentSnapshot.getBoolean("admin");

                            if(Boolean.TRUE.equals(moderator)){
                                Log.d("HOME", "igen");

                                textView.setOnClickListener(k ->{
                                    Log.d("HOME", "rányomott");
                                    drawNewBook(today);
                                });
                            }

                        }
                    })
                    .addOnFailureListener(e -> Log.e("AdminCheck", "Hiba a lekéréskor", e));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentUid = FirebaseAuth.getInstance().getUid();
        isModerator(currentUid);
        checkDailyBook();
        Log.d("HOME", "onResume: Adatok frissítése...");
    }
}