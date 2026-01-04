package com.example.stv2;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stv2.adapters.ClubAdapter;
import com.example.stv2.model.Club;
import com.example.stv2.adapters.ClubAdapter;
import com.example.stv2.model.Club;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ClubsActivity extends MenuActivity{
    //saját klubbok listája

    private RecyclerView recyclerView; //activity_clubs-ban van
    private ClubAdapter clubAdapter;
    private List<Club> clubs = new ArrayList<>(); //lekért klubbok

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clubs);
        setupBottomMenu(R.id.nav_clubs);

        //1. példányok létrehozása
        recyclerView = findViewById(R.id.recyclerClubs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        clubAdapter = new ClubAdapter(); //létrehozunk egy adapter példányt amit majd használunk

        //2. kapcsolat
        recyclerView.setAdapter(clubAdapter); //és összekapcsoljuk

        //3. adatok lekérése
        loadClubsFromFirebase();

    }

    private void loadClubsFromFirebase() {
        /// connection rész kell
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("clubs");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                clubs.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Club c = ds.getValue(Club.class);
                    clubs.add(c);
                }

                clubAdapter.setClubs(clubs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


}
