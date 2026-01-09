package com.example.stv2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stv2.adapters.ClubAdapter;
import com.example.stv2.model.Club;
import com.example.stv2.adapters.ClubAdapter;
import com.example.stv2.model.Club;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.List;

public class ClubsActivity extends MenuActivity{
    //saját klubbok listája

    private RecyclerView recyclerView; //activity_clubs-ban van
    private ClubAdapter clubAdapter;
    private List<Club> clubs = new ArrayList<>(); //lekért klubbok

    public interface OnClubClickListener { //interface, ezt írjuk felül
        void onClubClick(Club club);
    }

    private OnClubClickListener listener = new OnClubClickListener() {
        @Override
        public void onClubClick(Club club) {
            Intent i = new Intent(ClubsActivity.this, ClubPageActivity.class);
            i.putExtra("clubId", club.getId()); //nem firestore id
            Log.d("clubsid", club.getId() );
            startActivity(i);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clubs);
        setupBottomMenu(R.id.nav_clubs);

        //1. példányok létrehozása
        recyclerView = findViewById(R.id.recyclerClubs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        clubAdapter = new ClubAdapter(listener); //létrehozunk egy adapter példányt amit majd használunk


        //2. kapcsolat
        recyclerView.setAdapter(clubAdapter); //és összekapcsoljuk


        //3. adatok lekérése
        loadClubsFromFirebase();



    }


    private void loadClubsFromFirebase() {
        Log.d("Clubs", "loadclubs függvény elindult");

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // connection rész kellett
        DatabaseReference ref = FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("connections") //connection kollekcióból
                .child(userId) //a userhez tartozó
                .child("clubs"); //klubok
        Log.d("Clubs", "loadclubs függvény folytat");

        //connection nem tárol konrét objektumot!!!
        ref.addListenerForSingleValueEvent(new ValueEventListener() { //addListenerForSingleValueEvent egyszeri lekérdezés
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("Clubs", "data changed");
                clubs.clear(); //ne legyen duplikáció

                List<String> clubIds = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {

                    clubIds.add(ds.getKey());
                }

                if (clubIds.isEmpty()) {
                    clubAdapter.setClubs(clubs); // üres lista
                    return;
                }

                // Firestore lekérések számlálása
                final int[] count = {0};
                for (String clubId : clubIds) {
                    FirebaseFirestore.getInstance().collection("club")
                            .document(clubId)
                            .get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    Club c = doc.toObject(Club.class);
                                    clubs.add(c);
                                }
                                count[0]++;
                                if (count[0] == clubIds.size()) {
                                    // csak amikor minden lekérés kész
                                    Log.d("Clubs", "kész");
                                    clubAdapter.setClubs(clubs);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.d("Clubs", "Nincs benn semmi");
                                count[0]++;
                                if (count[0] == clubIds.size()) {
                                    clubAdapter.setClubs(clubs);
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


}
