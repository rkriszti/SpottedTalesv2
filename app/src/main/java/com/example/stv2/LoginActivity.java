package com.example.stv2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.core.view.WindowInsetsAnimationCompat;
import android.view.View;
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
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private static boolean isPersistenceSet = false;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!isPersistenceSet) {
            try {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
                isPersistenceSet = true;
            } catch (Exception e) {
            }
        }


        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_login);



        auth = FirebaseAuth.getInstance();
        //megadott adatok
        EditText emailEditText = findViewById(R.id.loginemail);
        EditText passwordEditText = findViewById(R.id.loginpassword);
        Button loginButton = findViewById(R.id.loginbutton);
        TextView skipToRegist = findViewById(R.id.logintoregist);

        View mainView = findViewById(R.id.main);

// rendszer sávok paddingja és billentyűzet figyelés egyben
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // alulra nem kell padding
            return insets;
        });

// ez mozgatja fel-le a képernyőt amikor kinyílik a billentyűzet
        ViewCompat.setWindowInsetsAnimationCallback(mainView, new WindowInsetsAnimationCompat.Callback(WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP) {
            @Override
            public WindowInsetsCompat onProgress(WindowInsetsCompat insets, java.util.List<WindowInsetsAnimationCompat> runningAnimations) {
                int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                mainView.setTranslationY(-imeHeight* 0.5f); // felrántja az egészet
                return insets;
            }
        });



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
                        if (task.isCanceled()) {
                            Log.e("Download", "Task was cancelled");
                            return;
                        }
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
