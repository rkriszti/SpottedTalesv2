package com.example.stv2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); //teljes képrenyős nézet
        FirebaseApp.initializeApp(this); //firebase sdk, elég app elején elindítani
        setContentView(R.layout.activity_login);



        auth = FirebaseAuth.getInstance();
        //megadott adatok
        EditText emailEditText = findViewById(R.id.loginemail);
        EditText passwordEditText = findViewById(R.id.loginpassword);
        Button loginButton = findViewById(R.id.loginbutton);
        TextView skipToRegist = findViewById(R.id.logintoregist);

        // teljes képrenyő
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Nincs még fiókom gomb
        skipToRegist.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistActivity.class);
            startActivity(intent);
        });

        //Belépés gomb
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Kérlek, töltsd ki az e-mailt és jelszót!", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            //siker
                            FirebaseUser user = auth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Sikeres belépés: " + user.getEmail(), Toast.LENGTH_SHORT).show();

                            ImageView giraffe = findViewById(R.id.giraffestart);
                            Animation anim = AnimationUtils.loadAnimation(this, R.anim.giraffe_move);
                            giraffe.startAnimation(anim);

                            giraffe.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    finish(); // MainActivity bezárása
                                }
                            }, 3000); // 3 másodperc


                        } else {
                            //hiba
                            Toast.makeText(LoginActivity.this, "Belépés sikertelen: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("FirebaseAuth", "Login failed", task.getException());
                        }
                    });
        });


    } //oncreate vége


} //mainactivity vége
