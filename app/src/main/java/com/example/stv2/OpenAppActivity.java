package com.example.stv2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class OpenAppActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);

        //menu bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent = new Intent(OpenAppActivity.this, OpenAppActivity.class);
                startActivity(intent);
               // Toast.makeText(this, "🏠 Kezdőlap", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_search) {
                Intent intent = new Intent(OpenAppActivity.this, SearchActivity.class);
                startActivity(intent);
               // Toast.makeText(this, "🔍 Keresés", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_clubs) {
                Intent intent = new Intent(OpenAppActivity.this, ClubsActivity.class);
                startActivity(intent);
               // Toast.makeText(this, "👥 Klubok", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(OpenAppActivity.this, ProfileActivity.class);
                startActivity(intent);
              //  Toast.makeText(this, "👤 Profil", Toast.LENGTH_SHORT).show();
                return true;
            }
            Toast.makeText(this, "OpenAct hiba", Toast.LENGTH_SHORT).show();
            return false;
        });

        //plusz gomb
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
                  Intent intent = new Intent(OpenAppActivity.this, NewBookActivity.class);
                    startActivity(intent);
        });


    } //oncreate vége
} //activity vége
