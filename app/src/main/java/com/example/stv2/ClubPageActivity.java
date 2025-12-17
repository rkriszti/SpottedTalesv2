package com.example.stv2;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stv2.model.Club;

import java.util.ArrayList;

public class ClubPageActivity extends MenuActivity {

    private TextView clubName, clubAdmin, clubStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clubpage); // ide az XML neve jön

        setupBottomMenu();

        // XML elemek lekötése
        clubName = findViewById(R.id.club_name);
        clubAdmin = findViewById(R.id.club_admin);
        clubStatus = findViewById(R.id.club_public_status);

        // Példa Club objektum (FireStore adat helyett)
        ArrayList<String> customChapters = new ArrayList<>();
        customChapters.add("Beszélgető szoba");
        customChapters.add("Értékelések");

        Club club = new Club("Lila Klub", "admin@email.com", 3, true, customChapters);

        // TextView-ok feltöltése
        clubName.setText(club.getName());
        clubAdmin.setText("Admin: " + club.getAdmin());
        clubStatus.setText(club.getIspublic() ? "Publikus" : "Privát");
    }
}
