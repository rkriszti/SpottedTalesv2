package com.example.stv2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stv2.adapters.ChaptersAdapter;
import com.example.stv2.adapters.RoomAdapter;
import com.example.stv2.model.Book;
import com.example.stv2.model.Club;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClubPageActivity extends MenuActivity {

    private TextView clubName, clubBookTitle;
    private ImageView clubBookCover, clubAdminPic, clubStatusIcon;
    private RecyclerView chaptersRecycler, customsRecycler;
    private LinearLayout chaptersCard, customsCard;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clubpage);
        setupBottomMenu(R.id.nav_clubs);

        String clubId = getIntent().getStringExtra("clubId");
        if (clubId == null) { finish(); return; }

        // --- Views ---
        clubName = findViewById(R.id.club_name);
        clubBookTitle = findViewById(R.id.club_book_title);
        clubBookCover = findViewById(R.id.club_book_cover);
        clubAdminPic = findViewById(R.id.club_admin_pic);
        clubStatusIcon = findViewById(R.id.club_status_icon);
        chaptersRecycler = findViewById(R.id.chapters_recycler);
        customsRecycler = findViewById(R.id.customs_recycler);

        // A CardView belsejét adja meg toggle-hoz
        chaptersCard = findViewById(R.id.chapters_recycler).getParent() instanceof LinearLayout ?
                (LinearLayout) chaptersRecycler.getParent() : null;
        customsCard = findViewById(R.id.customs_recycler).getParent() instanceof LinearLayout ?
                (LinearLayout) customsRecycler.getParent() : null;

        // Alapból rejtve
        chaptersRecycler.setVisibility(View.GONE);
        customsRecycler.setVisibility(View.GONE);

        // Click toggle
        if (chaptersCard != null) {
            chaptersCard.setOnClickListener(v -> {
                if (chaptersRecycler.getVisibility() == View.GONE) {
                    chaptersRecycler.setVisibility(View.VISIBLE);
                } else {
                    chaptersRecycler.setVisibility(View.GONE);
                }
            });
        }

        if (customsCard != null) {
            customsCard.setOnClickListener(v -> {
                if (customsRecycler.getVisibility() == View.GONE) {
                    customsRecycler.setVisibility(View.VISIBLE);
                } else {
                    customsRecycler.setVisibility(View.GONE);
                }
            });
        }

        loadClub(clubId);
    }

    private void loadClub(String clubId) {
        FirebaseFirestore.getInstance()
                .collection("club")
                .whereEqualTo("id", clubId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) { finish(); return; }

                    Club club = querySnapshot.getDocuments().get(0).toObject(Club.class);
                    if (club == null) return;

                    // --- Club neve ---
                    clubName.setText(club.getName());

                    // --- Admin profilkép ---
                    String adminPicUri = null; // TODO: lekérni a Firestore felhasználótól
                    if (adminPicUri != null) {
                        Glide.with(this).load(adminPicUri).circleCrop().into(clubAdminPic);
                    } else {
                        clubAdminPic.setImageResource(R.drawable.ic_default_avatar);
                    }

                    // --- Status icon ---
                    clubStatusIcon.setImageResource(
                            club.getIspublic() ? R.drawable.ic_lock_open : R.drawable.ic_lock
                    );

                    // --- Book cover + title ---
                    Book book = club.getBook();
                    if (book != null && book.getCoverpic() != null) {
                        Glide.with(this).load(book.getCoverpic()).centerCrop().into(clubBookCover);
                        clubBookTitle.setText(book.getTitle());
                    } else {
                        clubBookCover.setImageResource(R.drawable.background2);
                        clubBookTitle.setText("nincs még könyv");
                    }

                    // --- Fejezetek RecyclerView ---
                    setupRecycler(chaptersRecycler, club.getChapters());

                    // --- Egyedi szobák RecyclerView ---
                    setupRecycler(customsRecycler, club.getCustoms());

                }).addOnFailureListener(e -> {
                    Log.e("ClubPage", "Hiba a club betöltésénél", e);
                    finish();
                });
    }

    private void setupRecycler(RecyclerView recyclerView, Map<String, List<String>> data) {
        List<String> titles = new ArrayList<>(data.keySet());
        RecyclerView.Adapter adapter = new RoomAdapter(titles, data, title -> {
            // Click a szobára → ChatActivity
            Intent i = new Intent(ClubPageActivity.this, ChatActivity.class);
            i.putExtra("roomTitle", title);
            startActivity(i);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
