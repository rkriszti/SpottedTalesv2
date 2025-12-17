package com.example.stv2;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;

public class ClubsActivity extends MenuActivity{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_clubs);

        setupBottomMenu(R.id.nav_clubs);
    }
}
