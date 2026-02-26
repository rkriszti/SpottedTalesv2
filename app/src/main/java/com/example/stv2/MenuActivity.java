package com.example.stv2;

import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public abstract class MenuActivity extends AppCompatActivity {

    //ezt a settup függvényt fogjuk csak meghívni
    protected void setupBottomMenu(int selectedItemId) {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(selectedItemId);


        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent = new Intent(MenuActivity.this, HomeActivity.class);
                startActivity(intent);
                // Toast.makeText(this, "🏠 Kezdőlap", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_search) {
                Intent intent = new Intent(MenuActivity.this, SearchActivity.class);
                startActivity(intent);
                // Toast.makeText(this, "🔍 Keresés", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_clubs) {
                Intent intent = new Intent(MenuActivity.this, ClubsActivity.class);
                startActivity(intent);
                // Toast.makeText(this, "👥 Klubok", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(MenuActivity.this, ProfileActivity.class);
                intent.putExtra("userid",  FirebaseAuth.getInstance().getUid());
                startActivity(intent);
                //  Toast.makeText(this, "👤 Profil", Toast.LENGTH_SHORT).show();
                return true;
            }
            Toast.makeText(this, "OpenAct hiba", Toast.LENGTH_SHORT).show();
            return false;
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, NewBookActivity.class))
        );
    }

    protected void setupBottomMenu() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            Intent intent = null;
            if (id == R.id.nav_home) intent = new Intent(this, HomeActivity.class);
            else if (id == R.id.nav_search) intent = new Intent(this, SearchActivity.class);
            else if (id == R.id.nav_clubs) intent = new Intent(this, ClubsActivity.class);
            else if (id == R.id.nav_profile) intent = new Intent(this, ProfileActivity.class);

            if (intent != null) {
                startActivity(intent);
                finish();
            }
            return true;
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, NewBookActivity.class))
        );
    }

    protected void setupTopMenu() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.top_toolbar);
        androidx.drawerlayout.widget.DrawerLayout drawer = findViewById(R.id.drawer_layout);
        com.google.android.material.navigation.NavigationView navigationView = findViewById(R.id.nav_view);

        // Amikor a Toolbaron az ikonra kattintunk
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) { // Vagy amit az ikonnak adtál
                drawer.openDrawer(androidx.core.view.GravityCompat.END);
                return true;
            }
            return false;
        });

        // Az oldalmenüben lévő kattintások kezelése
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.action_logout_actual) { // A menüben lévő Logout ID-ja
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            drawer.closeDrawer(androidx.core.view.GravityCompat.END);
            return true;
        });
    }

}
