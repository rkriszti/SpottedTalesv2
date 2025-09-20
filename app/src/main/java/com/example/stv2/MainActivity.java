package com.example.stv2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        // Firebase Auth inicializálás
        mAuth = FirebaseAuth.getInstance();

        // UI elemek
        emailEditText = findViewById(R.id.loginemail);
        passwordEditText = findViewById(R.id.loginpassword);
        TextView loginButton = findViewById(R.id.loginbutton);
        TextView skipToRegist = findViewById(R.id.registtologin);

        // Edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Login gomb esemény
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Kérlek, töltsd ki az e-mailt és jelszót!", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(email, password);
        });

        // Regisztrációra ugrás
        skipToRegist.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sikeres login
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(MainActivity.this, "Sikeres belépés: " + user.getEmail(), Toast.LENGTH_SHORT).show();

                        // OpenActivity megnyitása
                        Intent intent = new Intent(MainActivity.this, OpenAppActivity.class);
                        startActivity(intent);
                        finish(); // MainActivity bezárása
                    } else {
                        // Sikertelen login
                        Toast.makeText(MainActivity.this, "Belépés sikertelen: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("FirebaseAuth", "Login failed", task.getException());
                    }
                });
    }
}
