package com.example.stv2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stv2.adapters.RoomAdapter;
import com.example.stv2.model.Book;
import com.example.stv2.model.Club;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClubPageActivity extends MenuActivity {
    //globálisan kell
    private Club club;
    private String userEmail, bookid;

    //xml részek
    private TextView clubName, clubBookTitle, statusText, clubBookAuthor;
    private EditText clubNameEdit, chaptersEdit,addcustomEdit ;
    private ImageView clubBookCover, clubAdminPic, clubStatusIcon, Settingbutton, club_book_edit;
    private ToggleButton statusChange;

    //elhelyezés
    private RecyclerView chaptersRecycler, customsRecycler;
    private LinearLayout chaptersHeader, customsHeader;

    private Boolean settingIsOn = false, choosingHappened = false;
    private Boolean isAdmin;

    //saját listener
    public interface OnDeleteCustomClickListener {
        void onDeleteClick(String custom);
    }

    private OnDeleteCustomClickListener deleteListener = new OnDeleteCustomClickListener() {
        @Override
        public void onDeleteClick(String customKey) {
            if (club != null && club.getCustoms() != null) {

                club.deleteCustom(customKey);

                FirebaseFirestore.getInstance()
                        .collection("club")
                        .document(club.getId())
                        .update("customs", club.getCustoms())
                        .addOnSuccessListener(aVoid -> {
                            Log.d("ClubPage", "Sikeres törlés: " + customKey);

                            setupRecycleruniq(customsRecycler, club.getCustoms());
                        })
                        .addOnFailureListener(e -> Log.e("ClubPage", "Hiba a törlésnél", e));
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clubpage);
        setupBottomMenu(R.id.nav_clubs);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = user != null ? user.getEmail() : null;
        bookid = "";

        String clubId = getIntent().getStringExtra("clubId");
        if (clubId == null) { finish(); return; }

        if(!getIntent().getStringExtra("chosenbook").isEmpty() && getIntent().getStringExtra("chosenbook")!=null){
            //könyv választás történt
            bookid = getIntent().getStringExtra("chosenbook");
            choosingHappened = true;
        }

        //button
        Settingbutton = findViewById(R.id.clubsettingon);

        //edittext
        clubNameEdit = findViewById(R.id.club_name_edittext);
        /// könyvcím ---> search oldal kéne
        /// könyvboritó
        /// adnimpic ---> profiloldal kéne
        statusText = findViewById(R.id.club_status_text);
        statusChange = findViewById(R.id.club_status_change);
        chaptersEdit = findViewById(R.id.chapters_edittext);
        addcustomEdit = findViewById(R.id.addcustom_edittext);

        //elemek
        clubName = findViewById(R.id.club_name); //cím
        clubBookTitle = findViewById(R.id.club_book_title);
        clubBookCover = findViewById(R.id.club_book_cover);
        clubBookAuthor = findViewById(R.id.club_book_author);
        clubAdminPic = findViewById(R.id.club_admin_pic);
        clubStatusIcon = findViewById(R.id.club_status_icon);
        club_book_edit = findViewById(R.id.club_book_edit);

        //lecsukáskor ő jeleníti meg a fejezetek
        chaptersRecycler = findViewById(R.id.chapters_recycler);
        customsRecycler = findViewById(R.id.customs_recycler);

        //erre nyomva csukódik le a recycler
        chaptersHeader = findViewById(R.id.chapters_title_parent);
        customsHeader = findViewById(R.id.customs_title_parent);

        //csak ha rányomunk, alapvetően rejtett
        chaptersRecycler.setVisibility(View.GONE);
        customsRecycler.setVisibility(View.GONE);

        //recycler megjelenítés
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

                    club = querySnapshot.getDocuments().get(0).toObject(Club.class);
                    if (club == null) return;

                    clubName.setText(club.getName());

                    //státusz beállítás
                    if (club.getIspublic()){
                        statusChange.setBackgroundResource(R.drawable.ic_lock_open);
                    } else {
                        statusChange.setBackgroundResource(R.drawable.ic_lock);
                    }



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
                    if (!choosingHappened && book != null && book.getCoverpic() != null) {
                        // nem történt változás és van könyve
                        Glide.with(this).load(book.getCoverpic()).centerCrop().into(clubBookCover);
                        clubBookTitle.setText(book.getTitle());
                        clubBookAuthor.setText(book.getAuthor());
                    } else if (choosingHappened && bookid != null && !bookid.isEmpty()) {
                        // könyvválasztás történt → lekérjük a könyvet és frissítjük a UI-t
                        FirebaseFirestore.getInstance()
                                .collection("books")
                                .document(bookid)
                                .get()
                                .addOnSuccessListener(doc -> {
                                    Book b = doc.toObject(Book.class);
                                    if (b != null) {
                                        club.setBook(b);
                                        // UI frissítés
                                        Glide.with(this).load(b.getCoverpic()).centerCrop().into(clubBookCover);
                                        clubBookTitle.setText(b.getTitle());
                                        clubBookAuthor.setText(b.getAuthor());

                                        // Firestore-ban csak a bookId frissítése
                                        club.setBookId(bookid);
                                        FirebaseFirestore.getInstance()
                                                .collection("club")
                                                .document(club.getId())
                                                .update("bookId", bookid)
                                                .addOnSuccessListener(aVoid -> Log.d("ClubPage", "Club könyv frissítve Firestore-ban"))
                                                .addOnFailureListener(e -> Log.e("ClubPage", "Hiba a club könyv frissítésénél", e));
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("ClubPage", "Hiba a könyv lekérésénél", e));
                    } else {
                        // nincs választott könyv, nincs változás
                        clubBookCover.setImageResource(R.drawable.background2);
                        clubBookTitle.setText("nincs még könyv");
                        clubBookAuthor.setText("");
                    }



                    String adminEmail = club.getAdmin();
                    isAdmin = userEmail != null && userEmail.equals(adminEmail);

                    // RecyclerView-ok
                    setupRecycler(chaptersRecycler, club.getChapters()); //ide már kell admin
                    setupRecycleruniq(customsRecycler, club.getCustoms());


                    //ADMIN----------------------------------------------------------------------

                    if(isAdmin){
                        Settingbutton.setVisibility(View.VISIBLE);

                        Settingbutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!settingIsOn){
                                    //be kell kapcsolni

                                    //státusz változás
                                    statusChange.setOnClickListener(a -> {
                                        if(club.getIspublic()){
                                            //public -> privát

                                            //adatbázisban
                                            club.setIspublic(false);
                                            //kinézetben
                                            statusChange.setBackgroundResource(R.drawable.ic_lock);
                                            clubStatusIcon.setImageResource(R.drawable.ic_lock);

                                        } else {
                                            //priv -> public
                                            //adatbázisban
                                            club.setIspublic(true);
                                            //kinézetben
                                            statusChange.setBackgroundResource(R.drawable.ic_lock_open);
                                            clubStatusIcon.setImageResource(R.drawable.ic_lock_open);
                                        }

                                        FirebaseFirestore dba = FirebaseFirestore.getInstance();
                                        dba.collection("club").document(club.getId())
                                                .update("ispublic", club.getIspublic())
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("ClubPage", "Státusz sikeresen frissítve!");

                                                })
                                                .addOnFailureListener(e -> Log.e("ClubPage", "Mentési hiba", e));
                                    });

                                    //fejezetek módosítása
                                    addcustomEdit.setVisibility(View.VISIBLE);
                                    chaptersEdit.setVisibility(View.VISIBLE);

                                    //név edittext megjelent
                                    clubName.setVisibility(View.GONE);
                                    clubNameEdit.setVisibility(View.VISIBLE);

                                    //érték beállítása
                                    clubNameEdit.setText(clubName.getText().toString());

                                    //publikusság
                                    clubStatusIcon.setVisibility(View.GONE);
                                    statusChange.setVisibility(View.VISIBLE);
                                    statusText.setVisibility(View.VISIBLE);

                                    //book picking for club
                                    club_book_edit.setVisibility(View.VISIBLE);
                                    club_book_edit.setOnClickListener(k -> {
                                        Intent i = new Intent(ClubPageActivity.this, SearchActivity.class);
                                        i.putExtra("choose", "true");
                                        startActivity(i);
                                    });

                                    //mentés gomb lesz
                                    Settingbutton.setImageResource(R.drawable.ic_save);
                                    settingIsOn = true;

                                    //egyből frissítés
                                    setupRecycler(chaptersRecycler, club.getChapters());
                                    setupRecycleruniq(customsRecycler, club.getCustoms());

                                } else {
                                    //MENTENEK

                                    //klub cím változás mentése
                                    if(!clubName.getText().toString().equals(clubNameEdit.getText().toString())){
                                        FirebaseFirestore database = FirebaseFirestore.getInstance();

                                        database.collection("club").document(club.getId())
                                                .update("name", clubNameEdit.getText().toString())
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("ClubPage", "Név sikeresen frissítve!");
                                                    clubName.setText(clubNameEdit.getText().toString());
                                                    club.setName(clubNameEdit.getText().toString());
                                                })
                                                .addOnFailureListener(e -> Log.e("ClubPage", "Mentési hiba", e));
                                    }

                                    //hány fejezet legyen
                                    if(getEditTextNumber(chaptersEdit) > 0 &&
                                            getEditTextNumber(chaptersEdit)!= club.getChaptersSize()){
                                    club.setChapters(getEditTextNumber(chaptersEdit));

                                        //hány fejezet
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        db.collection("club").document(club.getId())
                                                .update("chapters", club.getChapters())
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("ClubPage", "Fejezetek száma sikeresen frissítve Firestore-ban!");
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("ClubPage", "Hiba a fejezetek mentésénél", e);
                                                });

                                    }

                                    //custom fejezet hozzáadás
                                    if(!addcustomEdit.getText().toString().isEmpty()){
                                        club.setCustom(addcustomEdit.getText().toString());


                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        db.collection("club").document(club.getId())
                                                .update("customs", club.getCustoms())
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("ClubPage", "Custom száma sikeresen frissítve Firestore-ban!");
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("ClubPage", "Hiba a Custom mentésénél", e);
                                                });
                                    }


                                    //customtörlés
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    db.collection("club").document(club.getId())
                                            .update("customs", club.getCustoms()) // A módosított Map mentése
                                            .addOnSuccessListener(aVoid -> Log.d("ClubPage", "Egyedi szobák sikeresen frissítve!"))
                                            .addOnFailureListener(e -> Log.e("ClubPage", "Hiba a mentésnél", e));


                                    //név edittext megjelent
                                    clubName.setVisibility(View.VISIBLE);
                                    clubNameEdit.setVisibility(View.GONE);
                                    chaptersEdit.setVisibility(View.GONE);
                                    addcustomEdit.setVisibility(View.GONE);

                                    //újra setting gomb lesz
                                    Settingbutton.setImageResource(R.drawable.ic_setting);
                                    settingIsOn = false;

                                    //publikusság
                                    clubStatusIcon.setVisibility(View.VISIBLE);
                                    statusChange.setVisibility(View.GONE);
                                    statusText.setVisibility(View.GONE);

                                    club_book_edit.setVisibility(View.GONE);
                                    //frissítés
                                    setupRecycler(chaptersRecycler, club.getChapters());
                                    setupRecycleruniq(customsRecycler, club.getCustoms());
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
            i.putExtra("roomTitle", title); ///TODOO
            startActivity(i);
        }, isAdmin, settingIsOn, false, deleteListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupRecycleruniq(RecyclerView recyclerView, Map<String, List<String>> data) {
        List<String> titles = new ArrayList<>(data.keySet());

        RecyclerView.Adapter adapter = new RoomAdapter(titles, data, title -> {
            Intent i = new Intent(ClubPageActivity.this, ChatActivity.class);
            i.putExtra("roomTitle", title); ///TODOO
            startActivity(i);
        }, isAdmin, settingIsOn, true, deleteListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    public int getEditTextNumber(EditText editText) {
        if (editText == null) return 0;

        String text = editText.getText().toString().trim();

        if (text.isEmpty()) return 0;

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }


}
