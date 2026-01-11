package com.example.stv2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stv2.adapters.RoomAdapter;
import com.example.stv2.model.Book;
import com.example.stv2.model.Club;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClubPageActivity extends MenuActivity {

    private TextView clubName, clubBookTitle;
    private EditText clubNameEdit;

    private ImageView clubBookCover, clubAdminPic, clubStatusIcon, Settingbutton;
    private ImageView changeClubName, changeBook, changeChapter, changeUniqueChapter;
    private RecyclerView chaptersRecycler, customsRecycler;
    private LinearLayout chaptersHeader, customsHeader;
    private String userEmail;
    Boolean settingIsOn = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clubpage);
        setupBottomMenu(R.id.nav_clubs);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = user != null ? user.getEmail() : null;

        String clubId = getIntent().getStringExtra("clubId");
        if (clubId == null) { finish(); return; }


         Settingbutton = findViewById(R.id.clubsettingon);

        clubNameEdit = findViewById(R.id.club_name_edittext);
        // --- Views ---
        clubName = findViewById(R.id.club_name);
        clubBookTitle = findViewById(R.id.club_book_title);
        clubBookCover = findViewById(R.id.club_book_cover);
        clubAdminPic = findViewById(R.id.club_admin_pic);
        clubStatusIcon = findViewById(R.id.club_status_icon);

        chaptersRecycler = findViewById(R.id.chapters_recycler);
        customsRecycler = findViewById(R.id.customs_recycler);

        changeClubName = findViewById(R.id.club_name_edit);
        changeBook = findViewById(R.id.book_club_edit);
        changeChapter = findViewById(R.id.chapters_edit);
        changeUniqueChapter = findViewById(R.id.unique_chapters_edit);

        chaptersHeader = findViewById(R.id.chapters_title_parent);
        customsHeader = findViewById(R.id.customs_title_parent);

        // Toggle a fejezetekhez
        chaptersRecycler.setVisibility(View.GONE);
        customsRecycler.setVisibility(View.GONE);

        chaptersHeader.setOnClickListener(v -> {
            if (chaptersRecycler.getVisibility() == View.GONE) {
                chaptersRecycler.setVisibility(View.VISIBLE);
            } else {
                chaptersRecycler.setVisibility(View.GONE);
            }
        });

        customsHeader.setOnClickListener(v -> {
            if (customsRecycler.getVisibility() == View.GONE) {
                customsRecycler.setVisibility(View.VISIBLE);
            } else {
                customsRecycler.setVisibility(View.GONE);
            }
        });

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

                    clubName.setText(club.getName());

                    // Admin profilkép
                    String adminPicUri = null; // TODO: lekérni Firestore-ból
                    if (adminPicUri != null) {
                        Glide.with(this).load(adminPicUri).circleCrop().into(clubAdminPic);
                    } else {
                        clubAdminPic.setImageResource(R.drawable.ic_default_avatar);
                    }

                    // Status icon
                    clubStatusIcon.setImageResource(club.getIspublic() ? R.drawable.ic_lock_open : R.drawable.ic_lock);

                    // Book cover + title
                    Book book = club.getBook();
                    if (book != null && book.getCoverpic() != null) {
                        Glide.with(this).load(book.getCoverpic()).centerCrop().into(clubBookCover);
                        clubBookTitle.setText(book.getTitle());
                    } else {
                        clubBookCover.setImageResource(R.drawable.background2);
                        clubBookTitle.setText("nincs még könyv");
                    }

                    // RecyclerView-ok
                    setupRecycler(chaptersRecycler, club.getChapters());
                    setupRecycler(customsRecycler, club.getCustoms());


                    //ADMIN----------------------------------------------------------------------
                    String adminEmail = club.getAdmin();
                    boolean isAdmin = userEmail != null && userEmail.equals(adminEmail);

                    if(isAdmin){
                        Settingbutton.setVisibility(View.VISIBLE);

                        Settingbutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!settingIsOn){
                                    //be kell kapcsolni
                                    //kell?
                                    changeClubName.setVisibility(View.VISIBLE);
                                    changeBook.setVisibility(View.VISIBLE);
                                    changeChapter.setVisibility(View.VISIBLE);
                                    changeUniqueChapter.setVisibility(View.VISIBLE);

                                    //név edittext megjelent
                                    clubName.setVisibility(View.GONE);
                                    clubNameEdit.setVisibility(View.VISIBLE);
                                    //érték beállítása
                                    clubNameEdit.setText(clubName.getText().toString());

                                    //mentés gomb lesz
                                    Settingbutton.setImageResource(R.drawable.ic_save);
                                    settingIsOn = true;
                                } else {
                                    //MENTENEK
                                    if(!clubName.getText().toString().equals(clubNameEdit.getText().toString())){
                                        FirebaseFirestore database = FirebaseFirestore.getInstance();
                                        DocumentReference doksi = database.collection("club").document();
                                        //ITT KÉNE ELMENTENI AZ ÚJ NEVET

                                        // FONTOS: Itt a firebaseId-t használjuk a pontos dokumentum eléréséhez!
                                        database.collection("club").document(club.getId())
                                                .update("name", clubNameEdit.getText().toString())
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("ClubPage", "Név sikeresen frissítve!");
                                                    clubName.setText(clubNameEdit.getText().toString());
                                                    club.setName(clubNameEdit.getText().toString()); // A helyi objektumot is frissítjük
                                                })
                                                .addOnFailureListener(e -> Log.e("ClubPage", "Mentési hiba", e));
                                    }

                                    //név edittext megjelent
                                    clubName.setVisibility(View.VISIBLE);
                                    clubNameEdit.setVisibility(View.GONE);


                                    //kell?
                                    changeClubName.setVisibility(View.GONE);
                                    changeBook.setVisibility(View.GONE);
                                    changeChapter.setVisibility(View.GONE);
                                    changeUniqueChapter.setVisibility(View.GONE);

                                    //újra setting gomb lesz
                                    Settingbutton.setImageResource(R.drawable.ic_setting);
                                    settingIsOn = false;
                                }
                            }
                        });
                    }




                }).addOnFailureListener(e -> {
                    Log.e("ClubPage", "Hiba a club betöltésénél", e);
                    finish();
                });
    }

    private void setupRecycler(RecyclerView recyclerView, Map<String, List<String>> data) {
        List<String> titles = new ArrayList<>(data.keySet());
        RecyclerView.Adapter adapter = new RoomAdapter(titles, data, title -> {
            Intent i = new Intent(ClubPageActivity.this, ChatActivity.class);
            i.putExtra("roomTitle", title);
            startActivity(i);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
