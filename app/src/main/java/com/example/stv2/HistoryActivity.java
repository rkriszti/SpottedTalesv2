package com.example.stv2;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stv2.adapters.HistoryAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class HistoryActivity extends MenuActivity{
    private boolean ismoderator, admin;
    private String clubId, bookId;
    private Button club_active;
    private DatabaseReference rtdb;
    private ImageView clubpage_background, backButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clubpage_members);

        setupBottomMenu(null);
        setupTopMenu();

        Log.d("history", "Megnyílt az előzmény oldal");
        if(getIntent().getStringExtra("clubId")!=null
                && !getIntent().getStringExtra("clubId").isEmpty()){

            clubId = getIntent().getStringExtra("clubId");
            getIntent().removeExtra("clubId");
        }
        admin = false;
        if(getIntent().getStringExtra("admin")!=null
                && !getIntent().getStringExtra("admin").isEmpty()){

            if(getIntent().getStringExtra("admin").equals("true")){
                admin = true;
            }

            getIntent().removeExtra("admin");
        }


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = FirebaseAuth.getInstance().getUid();
        if (clubId == null) { finish(); return; }

        RecyclerView membersRecycler = findViewById(R.id.members_recycler);
         backButton = findViewById(R.id.club_backbutton);
        membersRecycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        clubpage_background = findViewById(R.id.clubpage_background);

        setBackgroundTheme(clubId, true);
        setBackgroundTheme(clubId, true);


        //moderator e
        if (uid != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            ismoderator = documentSnapshot.getBoolean("admin") != null && documentSnapshot.getBoolean("admin");
                            boolean edit = ismoderator || admin;
                            HistoryAdapter adapter = new HistoryAdapter(clubId, edit);
                            membersRecycler.setAdapter(adapter);
                        }

                    });
        }






        if (backButton != null) {
            backButton.setOnClickListener(b -> finish());
        }
    }

    private void setBackgroundTheme(String theme, boolean clubid){
        String t = theme;
        String c = theme;
        rtdb = FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        //ha le kell kérni
            rtdb.child("club_settings").child(c).child("theme")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String currentSavedTheme = task.getResult().getValue(String.class);

                            if (currentSavedTheme != null) {
                                String themee = currentSavedTheme.trim();
                                backButton.setImageDrawable(null);
                                switch (themee) {
                                    case "Romantikus":
                                        clubpage_background.setImageResource(R.drawable.chat_theme_romance);
                                        backButton.setImageResource(R.drawable.ic_back_purple);

                                        break;
                                    case "Romantasy":
                                        clubpage_background.setImageResource(R.drawable.chat_theme_romantasy);
                                        backButton.setImageResource(R.drawable.ic_back);

                                        break;
                                    default:
                                        clubpage_background.setImageResource(R.drawable.chat_theme_romance);
                                        backButton.setImageResource(R.drawable.ic_back_purple);

                                        break;
                                }
                            } else {
                                //és ha null?
                                clubpage_background.setImageResource(R.drawable.chat_theme_romance);
                                backButton.setImageResource(R.drawable.ic_back_purple);

                            }


                        } else {
                            Log.e("RTDB", "Hiba a lekérés során", task.getException());
                        }
                    });



    }
}
