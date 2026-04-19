package com.example.stv2;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stv2.adapters.HistoryAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HistoryActivity extends MenuActivity{
    private boolean ismoderator, admin;
    private String clubId, bookId;
    private Button club_active;

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
        ImageView backButton = findViewById(R.id.club_backbutton);
        membersRecycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));


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
}
