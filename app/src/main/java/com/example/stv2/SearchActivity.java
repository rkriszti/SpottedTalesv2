package com.example.stv2;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends MenuActivity {

    private EditText etSearch;
    private RecyclerView recyclerSearch;

    private SearchBookAdapter bookAdapter;
    private SearchClubAdapter clubAdapter;
    private SearchUserAdapter userAdapter;

    private String username;

    private AppCompatButton buttonBook, buttonClub, buttonUser;

    private boolean optionBook, optionClub, optionUser;

    private List<Book> allBooks = new ArrayList<>();
    private List<Club> allClubs = new ArrayList<>();
    private List<User> allUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupBottomMenu(R.id.nav_search);

        buttonBook = findViewById(R.id.buttonbook);
        buttonClub = findViewById(R.id.buttonclub);
        buttonUser = findViewById(R.id.buttonuser);

        etSearch = findViewById(R.id.etSearch);
        recyclerSearch = findViewById(R.id.recyclerSearch);
        recyclerSearch.setLayoutManager(new LinearLayoutManager(this));

        bookAdapter = new SearchBookAdapter();
        clubAdapter = new SearchClubAdapter();
        userAdapter = new SearchUserAdapter();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        username = doc.getString("username");
                        filter(etSearch.getText().toString());
                    }
                });

        loadBooks();
        loadClubs();
        loadUsers();

        select(false, false, false); // kezdeti állapot

        buttonBook.setOnClickListener(v -> {
            if (!optionBook) select(true, false, false);
        });

        buttonClub.setOnClickListener(v -> {
            if (!optionClub) select(false, true, false);
        });

        buttonUser.setOnClickListener(v -> {
            if (!optionUser) select(false, false, true);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
        });
    }

    private void select(boolean book, boolean club, boolean user) {
        optionBook = book;
        optionClub = club;
        optionUser = user;

        buttonBook.setBackgroundResource(
                book ? R.drawable.background2 : R.drawable.grey_background);
        buttonClub.setBackgroundResource(
                club ? R.drawable.background2 : R.drawable.grey_background);
        buttonUser.setBackgroundResource(
                user ? R.drawable.background2 : R.drawable.grey_background);

        if (book) recyclerSearch.setAdapter(bookAdapter);
        if (club) recyclerSearch.setAdapter(clubAdapter);
        if (user) recyclerSearch.setAdapter(userAdapter);

        filter(etSearch.getText().toString());
    }

    private void loadBooks() {
        FirebaseFirestore.getInstance()
                .collection("books")
                .get()
                .addOnSuccessListener(qs -> {
                    allBooks = qs.toObjects(Book.class);
                    bookAdapter.setBooks(allBooks);
                });
    }

    private void loadClubs() {
        FirebaseFirestore.getInstance()
                .collection("club")
                .get()
                .addOnSuccessListener(qs -> {
                    allClubs = qs.toObjects(Club.class);
                    clubAdapter.setClubs(allClubs);
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

        if (optionBook) {
            List<Book> filtered = new ArrayList<>();
            for (Book b : allBooks) {
                if (b.getTitle().toLowerCase().contains(text)) {
                    filtered.add(b);
                }
            }
            bookAdapter.setBooks(filtered);
        }

        if (optionClub) {
            List<Club> filtered = new ArrayList<>();
            for (Club c : allClubs) {
                if (c.getName().toLowerCase().contains(text)) {
                    filtered.add(c);
                }
            }
            clubAdapter.setClubs(filtered);
        }

        if (optionUser) {
            List<User> filtered = new ArrayList<>();
            for (User u : allUsers) {
                if (u.getUsername().toLowerCase().contains(text)&& username!=null && !u.getUsername().equals(username)) {
                    filtered.add(u);
                }
            }
            userAdapter.setUsers(filtered);
        }
    }
}
