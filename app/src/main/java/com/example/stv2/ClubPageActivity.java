package com.example.stv2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stv2.adapters.ClubRoomAdapter;
import com.example.stv2.adapters.MembersAdapter;
import com.example.stv2.adapters.ClubChatAdapter;
import com.example.stv2.model.Book;
import com.example.stv2.model.Club;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClubPageActivity extends MenuActivity {
    //globálisan kell
    private Club club;
    private String userEmail, bookid;
    private Button members;

    //xml részek
    private TextView clubName, clubBookTitle, statusText, clubBookAuthor;
    private EditText clubNameEdit, chaptersEdit,addcustomEdit ;
    private ImageView clubBookCover, clubAdminPic, clubStatusIcon, Settingbutton, club_book_edit;
    private ToggleButton statusChange;

    private List<String> pendingUserIds = new ArrayList<>();
 //   private com.google.firebase.database.ValueEventListener pendingListener;

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

    //saját listener
    public interface OnChooseBookListener {
        void onChoose(String email);
        //CLUB/PROFILE
    }

    private ClubPageActivity.OnChooseBookListener listener = new ClubPageActivity.OnChooseBookListener() {
        @Override
        public void onChoose(String userid) {
                Intent i = new Intent(ClubPageActivity.this, ProfileActivity.class);
                Log.d("choosemember", "profil megkap userid:" + userid );

                i.putExtra("userid", userid);
                startActivity(i);
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clubpage);

        setupBottomMenu(R.id.nav_clubs);
        setupTopMenu();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = user != null ? user.getEmail() : null;
        bookid = "";

        String clubId = getIntent().getStringExtra("clubId");
        //ha clubid-t nem kap leáll!
        if (clubId == null) { finish(); return; }


        Log.d("ChooseBook", "clubpageben ellenőrzés" );
        if(getIntent().getStringExtra("chosenbook")!=null
                && !getIntent().getStringExtra("chosenbook").isEmpty()){
            //könyv választás történt
            Log.d("ChooseBook", "clubpagenek szóltak hogy kiválasztás történt" );
            bookid = getIntent().getStringExtra("chosenbook");
            choosingHappened = true;
        }

        //button
        Settingbutton = findViewById(R.id.clubsettingon);
        members = findViewById(R.id.club_members);

        //edittext
        clubNameEdit = findViewById(R.id.club_name_edittext);
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

        members.setOnClickListener(v -> {
            // 1. Azonnal váltsunk layoutot
            setContentView(R.layout.activity_clubpage_members);

            // 2. Kérjük meg a rendszert, hogy miután végzett a rajzolással, futtassa ezt:
            getWindow().getDecorView().post(() -> {
                RecyclerView membersRecycler = findViewById(R.id.members_recycler);
                ImageView backButton = findViewById(R.id.club_backbutton);

                if (membersRecycler != null) {
                    membersRecycler.setLayoutManager(new LinearLayoutManager(this));

                    if (club != null && club.getMembers() != null) {
                        // Itt használd az új, 4 paraméteres konstruktorodat!
                        MembersAdapter adapter = new MembersAdapter(
                                club.getMembers(),
                                pendingUserIds,
                                listener,
                                club,
                                userEmail
                        );
                        membersRecycler.setAdapter(adapter);
                        loadPendingRequests(club.getId(), adapter);
                    }
                }

                if (backButton != null) {
                    backButton.setOnClickListener(b -> recreate());
                }

                // Menük visszaállítása
                setupBottomMenu(R.id.nav_clubs);
                setupTopMenu();
            });
        });


        loadClub(clubId);
        //is choosing happened -> update book -> loadclub végén (aszinkron)
    }

    private void loadClub(String clubId) {
        //lekérjük a club adatait
        FirebaseFirestore.getInstance()
                .collection("club")
                .document(clubId)
                .get()
                .addOnSuccessListener(docc -> {
                    if (!docc.exists()) {
                        return;
                    }

                    club = docc.toObject(Club.class);
                    if (club == null) return;

                    clubName.setText(club.getName());
                    club.setId(docc.getId());

                    if (!club.getIspublic() && !club.isMember(userEmail)) {
                        Toast.makeText(this, "Nincs jogosultságod!", Toast.LENGTH_SHORT).show();
                        finish();
                    }


                    //státusz beállítás
                    if (club.getIspublic()){
                        statusChange.setBackgroundResource(R.drawable.ic_lock_open);
                    } else {
                        statusChange.setBackgroundResource(R.drawable.ic_lock);
                    }



                    String adminEmail = club.getAdmin();

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .whereEqualTo("email", adminEmail)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(query -> {

                                if (!query.isEmpty()) {

                                    DocumentSnapshot doc = query.getDocuments().get(0);
                                    String picUrl = doc.getString("profilepicurl");

                                    if (picUrl != null && !picUrl.isEmpty()) {
                                        Glide.with(this)
                                                .load(picUrl)
                                                .circleCrop()
                                                .placeholder(R.drawable.ic_default_avatar)
                                                .error(R.drawable.ic_default_avatar)
                                                .into(clubAdminPic);
                                    } else {
                                        clubAdminPic.setImageResource(R.drawable.ic_default_avatar);
                                    }

                                } else {
                                    clubAdminPic.setImageResource(R.drawable.ic_default_avatar);
                                }
                            })
                            .addOnFailureListener(e ->
                                    clubAdminPic.setImageResource(R.drawable.ic_default_avatar)
                            );


                    // Status icon
                    clubStatusIcon.setImageResource(club.getIspublic() ? R.drawable.ic_lock_open : R.drawable.ic_lock);

                    //ha van könyv beállítva
                    String currentBookId = (bookid != null && !bookid.isEmpty()) ? bookid : club.getBookId();
                    if(currentBookId != null && !currentBookId.isEmpty()) {
                        FirebaseFirestore.getInstance()
                                .collection("books")
                                .document(currentBookId)
                                .get()
                                .addOnSuccessListener(doc -> {
                                    Book b = doc.toObject(Book.class);
                                    if(b != null) {
                                        club.setBook(b);
                                        clubBookCover.setImageResource(R.drawable.background2);
                                        if(b.getCoverpic() != null)
                                            Glide.with(this).load(b.getCoverpic()).centerCrop().into(clubBookCover);
                                        clubBookTitle.setText(b.getTitle());
                                        clubBookAuthor.setText(b.getAuthor());
                                    }
                                });
                    } else {
                        clubBookCover.setImageResource(R.drawable.background2);
                        clubBookTitle.setText("nincs még könyv");
                        clubBookAuthor.setText("");
                    }


                    isAdmin = userEmail != null && userEmail.equals(adminEmail);

                    // RecyclerView-ok
                    setupRecycler(chaptersRecycler, club.getChapters()); //ide már kell admin
                    setupRecycleruniq(customsRecycler, club.getCustoms());


                    //ADMIN----------------------------------------------------------------------

                    if(isAdmin){
                        admin();
                    }

                    if(choosingHappened){
                        updateBook(bookid);
                    }

                }).addOnFailureListener(e -> {
                    Log.e("ClubPage", "Hiba a club betöltésénél", e);
                    finish();
                });
    }

    private void admin(){
        //feltétel hogy már admin, check ->loadclub
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
                        Log.d("ChooseBook", "rányomtak a bookchoose gombra, irány a search" );
                        Intent i = new Intent(ClubPageActivity.this, SearchActivity.class);
                        i.putExtra("choose", "true");
                        i.putExtra("clubid", club.getId());
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

    private void setupRecycler(RecyclerView recyclerView, Map<String, List<String>> data) {
        List<String> titles = new ArrayList<>(data.keySet());
        RecyclerView.Adapter adapter = new ClubRoomAdapter(titles, data, isAdmin, settingIsOn, false, deleteListener, club.getId());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupRecycleruniq(RecyclerView recyclerView, Map<String, List<String>> data) {
        List<String> titles = new ArrayList<>(data.keySet());

        RecyclerView.Adapter adapter = new ClubRoomAdapter(titles, data, isAdmin, settingIsOn, true, deleteListener, club.getId());
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

    @Override
    protected void onResume() {
        super.onResume();

        // Ellenőrizzük, hogy van-e új bookid
        String newBookId = getIntent().getStringExtra("chosenbook");
        if (newBookId != null && !newBookId.equals(bookid) && !newBookId.isEmpty()) {
            bookid = newBookId;
            updateBook(bookid);
        }
    }

    private void updateBook(String bookId) {

        if (club == null || club.getId() == null) {
            Log.e("ClubPage", "Még nem töltött be a klub, nem tudok menteni!");
            return;
        }
        Log.d("ClubPage", "update started" );
        FirebaseFirestore.getInstance()
                .collection("books")
                .document(bookId)
                .get()
                .addOnSuccessListener(doc -> {

                    Log.d("ClubPage", "könyv lekérése" );
                    Log.d("ClubPage", "doc.exists(): " + doc.exists());
                    Log.d("ClubPage", "doc data: " + doc.getData());
                    Book b = doc.toObject(Book.class);
                    Log.d("ClubPage", "Book: " + b);

                    if(doc.toObject(Book.class)==null || !doc.exists()){
                        Log.d("ClubPage", "ilyen doc nincs" );
                    }
                    if (doc.exists() && b != null && club != null) {
                        Log.d("ClubPage", "nem null semmi" );
                        club.setBook(b);
                        club.setBookId(bookId);

                        // UI frissítés
                        clubBookCover.setImageResource(R.drawable.background2);
                        if(b.getCoverpic()!=null)
                            Glide.with(this).load(b.getCoverpic()).centerCrop().into(clubBookCover);
                        clubBookTitle.setText(b.getTitle());
                        clubBookAuthor.setText(b.getAuthor());
                        Log.d("ClubPage", "ui frissít" );

                        // Firestore frissítés
                        FirebaseFirestore.getInstance()
                                .collection("club")
                                .document(club.getId())
                                .update("bookId", bookId)
                                .addOnSuccessListener(aVoid -> Log.d("ClubPage", "Club könyv frissítve Firestore-ban"))
                                .addOnFailureListener(e -> Log.e("ClubPage", "Hiba a club könyv frissítésénél", e));
                    }
                });
    }

    private void loadPendingRequests(String clubId, MembersAdapter adapter) {
        com.google.firebase.database.FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("pending_requests")
                .child(clubId)
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        pendingUserIds.clear();
                        for (com.google.firebase.database.DataSnapshot ds : snapshot.getChildren()) {
                            pendingUserIds.add(ds.getKey()); // Csak a User ID-kat gyűjtjük ki
                        }
                        // Ha az adapter már létezik, frissítjük a nézetet
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                        Log.e("ClubPage", "Hiba a pending lekérésénél", error.toException());
                    }
                });
    }
}