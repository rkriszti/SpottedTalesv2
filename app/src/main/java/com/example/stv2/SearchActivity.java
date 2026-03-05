package com.example.stv2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stv2.adapters.SearchBookAdapter;
import com.example.stv2.adapters.SearchClubAdapter;
import com.example.stv2.adapters.SearchUserAdapter;
import com.example.stv2.model.Book;
import com.example.stv2.model.Club;
import com.example.stv2.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchActivity extends MenuActivity {

    private EditText etSearch;
    private RecyclerView recyclerSearch;
    private CheckBox sameinterest;
    private String targetUserId;
    private Boolean ismoderator = false;
    private List<String> currentUserFavorites = new ArrayList<>();

    private SearchBookAdapter bookAdapter;
    private SearchClubAdapter clubAdapter;
    private SearchUserAdapter userAdapter;

    private int which;
    private String username, clubidbeforechoosing, email;
    private List<Book> allBooks = new ArrayList<>();
    private List<Club> allClubs = new ArrayList<>();
    private List<User> allUsers = new ArrayList<>();
    private List<String> adminBooks = new ArrayList<>();

    private AppCompatButton buttonBook, buttonClub, buttonUser;
    private boolean optionBook, optionClub, optionUser, chooseForClub, chooseFav;

    private int selectedPosition = -1; // az éppen szerkesztett könyv pozíciója
    private ActivityResultLauncher<String> pickImageLauncher;

    //saját listener
    public interface OnChooseBookListener {
        void onChoose(String bookid, String code);
        //CLUB/PROFILE
    }

    private OnChooseBookListener listener = new OnChooseBookListener() {
        @Override
        public void onChoose(String bookid, String code) {
            if(Objects.equals(code, "CLUB")){
                Intent i = new Intent(SearchActivity.this, ClubPageActivity.class);
                Log.d("ChooseBook", "search megkap bookid:" + bookid );
                i.putExtra("chosenbook", bookid);
                i.putExtra("clubId", clubidbeforechoosing);
                startActivity(i);
            }
            if(Objects.equals(code, "PROFILE")){
                Intent i = new Intent(SearchActivity.this, ProfileActivity.class);
                Log.d("ChooseBook", "profil megkap bookid:" + bookid );

                i.putExtra("whichbook", which);
                i.putExtra("bookid", bookid);
                i.putExtra("userid", targetUserId);
                startActivity(i);
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupBottomMenu(R.id.nav_search);
        setupTopMenu();

        buttonBook = findViewById(R.id.buttonbook);
        buttonClub = findViewById(R.id.buttonclub);
        buttonUser = findViewById(R.id.buttonuser);
        etSearch = findViewById(R.id.etSearch);
        sameinterest = findViewById(R.id.sameinterest);
        recyclerSearch = findViewById(R.id.recyclerSearch);
        recyclerSearch.setLayoutManager(new LinearLayoutManager(this));

        //clugpage-ből hív-----------------------------------------------------
        chooseForClub = false;
        if(getIntent()!=null && getIntent().getStringExtra("choose")!=null &&
        !getIntent().getStringExtra("choose").isEmpty() && getIntent().getStringExtra("choose").equals("true")){
            chooseForClub = true;
        } else {
            chooseForClub = false;
        }
        Log.d("ChooseBook", "search choose:" + chooseForClub );

        clubidbeforechoosing = "";
        if(getIntent()!=null && getIntent().getStringExtra("clubid")!=null &&
                !getIntent().getStringExtra("clubid").isEmpty()){
            clubidbeforechoosing = getIntent().getStringExtra("clubid");
            Log.d("ChooseBook", "search átjött a clubid:" + clubidbeforechoosing );
        }

        String uid = FirebaseAuth.getInstance().getUid();

        if (getIntent() != null) {
            targetUserId = getIntent().getStringExtra("userid");
        }

        //moderator e
        if (uid != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean moderator = documentSnapshot.getBoolean("admin");
                            ismoderator = (moderator != null && moderator);

                            if (moderator != null && moderator) {
                                Log.d("AdminCheck", "A felhasználó admin.");
                            } else {
                                Log.d("AdminCheck", "A felhasználó nem admin.");
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("AdminCheck", "Hiba a lekéréskor", e));
        }


        //profilról hív--------------------------------------------------------------
        chooseFav = false;
        if(getIntent()!=null &&
                getIntent().getIntExtra("favchange", -1)!=-1){
            chooseFav = true;
            which = getIntent().getIntExtra("favchange", -1);
        }


        clubAdapter = new SearchClubAdapter();
        userAdapter = new SearchUserAdapter();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // ActivityResultLauncher regisztrálása
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && selectedPosition != -1 && bookAdapter != null) {
                        bookAdapter.updateBookCover(uri, selectedPosition);
                        selectedPosition = -1;
                    }
                }
        );

        loadAdminBooks(userId);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        username = doc.getString("username");
                        email = doc.getString("email");
                        filter(etSearch.getText().toString());
                    }
                });

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        username = doc.getString("username");
                        email = doc.getString("email");
                        currentUserFavorites = (List<String>) doc.get("favorites");
                        if (currentUserFavorites == null) currentUserFavorites = new ArrayList<>();
                        filter(etSearch.getText().toString());
                    }
                });

        loadClubs();
        loadUsers();

        select(true, false, false);
        if(chooseForClub){
            select(true, false, false);
        }
        if(chooseFav){
            select(true, false, false);
        }

        buttonBook.setOnClickListener(v -> { if (!optionBook) select(true,false,false); });
        buttonClub.setOnClickListener(v -> { if (!optionClub) select(false,true,false); });
        buttonUser.setOnClickListener(v -> { if (!optionUser) select(false,false,true); });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
        });

        sameinterest.setOnClickListener(v -> filter(etSearch.getText().toString()));
    }

    private void select(boolean book, boolean club, boolean user) {
        optionBook = book;
        optionClub = club;
        optionUser = user;

        sameinterest.setVisibility(user ? View.VISIBLE : View.GONE);
        buttonBook.setBackgroundResource(book ? R.drawable.background2 : R.drawable.grey_background);
        buttonClub.setBackgroundResource(club ? R.drawable.background2 : R.drawable.grey_background);
        buttonUser.setBackgroundResource(user ? R.drawable.background2 : R.drawable.grey_background);

        if (book && bookAdapter != null) recyclerSearch.setAdapter(bookAdapter);
        if (club && clubAdapter != null) recyclerSearch.setAdapter(clubAdapter);
        if (user && userAdapter != null) recyclerSearch.setAdapter(userAdapter);
        if (!user) sameinterest.setChecked(false);

        filter(etSearch.getText().toString());
    }

    private void loadBooks() {
        FirebaseFirestore.getInstance()
                .collection("books")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w("SearchActivity", "Firestore hiba", e);
                        return;
                    }
                    if (snapshot != null) {
                        allBooks = snapshot.toObjects(Book.class);
                        if (bookAdapter != null) bookAdapter.setBooks(allBooks, ismoderator);
                    }
                });
    }


    private void loadClubs() {
        FirebaseFirestore.getInstance()
                .collection("club")
                .get()
                .addOnSuccessListener(qs -> {
                    allClubs = qs.toObjects(Club.class);
                    clubAdapter.setClubs(allClubs, email, ismoderator);
                });
    }

    private void loadUsers() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .get()
                .addOnSuccessListener(qs -> {
                    allUsers = qs.toObjects(User.class);
                    userAdapter.setUsers(allUsers);
                });
    }

    private void filter(String text) {
        text = text.toLowerCase();
        if (optionBook && bookAdapter != null) {
            List<Book> filtered = new ArrayList<>();
            for (Book b : allBooks) if (b.getTitle().toLowerCase().contains(text)) filtered.add(b);
            bookAdapter.setBooks(filtered, ismoderator);
        }
        if (optionClub) {
            List<Club> filtered = new ArrayList<>();
            for (Club c : allClubs) if (c.getName().toLowerCase().contains(text)) filtered.add(c);
            clubAdapter.setClubs(filtered, email, ismoderator);
        }
        if (optionUser) {
            List<User> filtered = new ArrayList<>();
            boolean onlySameInterest = sameinterest.isChecked();

            for (User u : allUsers) {
                boolean matchesName = u.getUsername().toLowerCase().contains(text);
                boolean isNotMe = username != null && !u.getUsername().equals(username);

                if (matchesName && isNotMe) {
                    if (onlySameInterest) {
                        if (hasCommonFavorite(u.getFavorites())) {
                            filtered.add(u);
                        }
                    } else {
                        filtered.add(u);
                    }
                }
            }
            userAdapter.setUsers(filtered);
        }
    }

    private void loadAdminBooks(String userid) {
        adminBooks = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance(
                        "https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("connections").child(userid).child("books");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    String bookId = child.getKey();
                    adminBooks.add(bookId);
                }

                // Adapter létrehozása a betöltött adminBooks után
                Log.d("ChooseBook", "search choose átadása adapternek"  );
                bookAdapter = new SearchBookAdapter(adminBooks, pickImageLauncher, chooseForClub, listener, chooseFav, ismoderator);

                // Cover click listener beállítása
                bookAdapter.setOnCoverClickListener(pos -> {
                    selectedPosition = pos;
                    pickImageLauncher.launch("image/*");
                });

                recyclerSearch.setAdapter(bookAdapter);
                loadBooks();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CONNECTION", "RTDB hiba", error.toException());
            }
        });
    }

    private boolean hasCommonFavorite(List<String> otherUserFavorites) {
        if (currentUserFavorites == null || otherUserFavorites == null) return false;
        for (String fav : currentUserFavorites) {
            if (otherUserFavorites.contains(fav)) return true;
        }
        return false;
    }

    // az adapter hívásához a kiválasztott pozíció frissítése
    public void setSelectedPosition(int pos) { selectedPosition = pos; }
}
