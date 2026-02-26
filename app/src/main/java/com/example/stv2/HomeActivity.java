package com.example.stv2;

import android.os.Bundle;

public class HomeActivity extends MenuActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);

        setupBottomMenu(R.id.nav_home);
        setupTopMenu();

    } //oncreate vége
} //activity vége
