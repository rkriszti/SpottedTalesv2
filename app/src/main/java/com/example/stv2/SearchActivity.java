package com.example.stv2;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Switch;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stv2.adapters.SearchBookAdapter;
import com.example.stv2.adapters.SearchClubAdapter;
import com.example.stv2.model.Book;
import com.example.stv2.model.Club;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends MenuActivity {

    private EditText etSearch;
    private Switch switchSearch;
    private RecyclerView recyclerSearch;

    private SearchBookAdapter bookAdapter;
    private SearchClubAdapter clubAdapter;

    private List<Book> allBooks = new ArrayList<>();
    private List<Club> allClubs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupBottomMenu(R.id.nav_search);

        etSearch = findViewById(R.id.etSearch);
        switchSearch = findViewById(R.id.switchSearch);
        recyclerSearch = findViewById(R.id.recyclerSearch);

        recyclerSearch.setLayoutManager(new LinearLayoutManager(this));

        bookAdapter = new SearchBookAdapter();
        clubAdapter = new SearchClubAdapter();

        recyclerSearch.setAdapter(bookAdapter);

        loadBooks();
        loadClubs();

        switchSearch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                recyclerSearch.setAdapter(clubAdapter);
            } else {
                recyclerSearch.setAdapter(bookAdapter);
            }
            filter(etSearch.getText().toString());
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

    private void filter(String text) {
        text = text.toLowerCase();

        if (switchSearch.isChecked()) {
            List<Club> filtered = new ArrayList<>();
            for (Club c : allClubs) {
                if (c.getName().toLowerCase().contains(text)) {
                    filtered.add(c);
                }
            }
            clubAdapter.setClubs(filtered);
        } else {
            List<Book> filtered = new ArrayList<>();
            for (Book b : allBooks) {
                if (b.getTitle().toLowerCase().contains(text)) {
                    filtered.add(b);
                }
            }
            bookAdapter.setBooks(filtered);
        }
    }
}
