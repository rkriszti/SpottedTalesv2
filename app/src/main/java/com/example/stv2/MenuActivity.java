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

        // 1. Toolbar kezelése (Oldalmenü nyitása az ikonnal)
        if (toolbar != null && drawer != null) {
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_logout) { // Ez a Toolbaron lévő ikon ID-ja
                    drawer.openDrawer(androidx.core.view.GravityCompat.END);
                    return true;
                }
                return false;
            });
        }

        // 2. NavigationView kezelése (Oldalmenüben lévő gombok)
        // Itt volt a NullPointerException: a null-check megvédi az appot az összeomlástól
        if (navigationView != null && drawer != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.action_logout_actual) { // A side_menu.xml-ben lévő logout ID
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    // Tisztítjuk a stacket, hogy ne lehessen a "vissza" gombbal visszajönni
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                // Bármelyik gombra nyomunk, a végén becsukjuk a menüt
                drawer.closeDrawer(androidx.core.view.GravityCompat.END);
                return true;
            });
        } else {
            // Logoljuk, ha gond van, így látod a Logcat-ben, de nem száll el az app
            android.util.Log.e("MenuActivity", "Hiba: nav_view vagy drawer_layout nem található a layoutban!");
        }
    }

}
