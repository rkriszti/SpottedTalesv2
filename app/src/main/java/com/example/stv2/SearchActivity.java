package com.example.stv2;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Switch;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stv2.adapters.BookAdapter;
import com.example.stv2.adapters.ClubAdapter;
import com.example.stv2.model.Book;
import com.example.stv2.model.Club;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends MenuActivity {
    private RecyclerView recyclerView;
    private EditText etSearch;
    private Switch switchSearch;

    private BookAdapter bookAdapter;
    private ClubAdapter clubAdapter;

    private List<Book> allBooks = new ArrayList<>();
    private List<Club> allClubs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
/*
        setupBottomMenu(R.id.nav_search);

        recyclerView = findViewById(R.id.recyclerSearch);
        etSearch = findViewById(R.id.etSearch);
        switchSearch = findViewById(R.id.switchSearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bookAdapter = new BookAdapter();
        clubAdapter = new ClubAdapter();

        // alapértelmezés: könyvek
        recyclerView.setAdapter(bookAdapter);

        switchSearch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            recyclerView.setAdapter(isChecked ? clubAdapter : bookAdapter);
            filter(etSearch.getText().toString());
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        // ide majd jöhet Firestore-ból a feltöltés
        // loadBooks();
        // loadClubs();*/
    }
/*
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

 */
}
