package com.example.stv2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;

public class HomeActivity extends MenuActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);

        setupBottomMenu(R.id.nav_home);
        setupTopMenu();

    } //oncreate vége
} //activity vége
