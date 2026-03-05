package com.example.stv2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stv2.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;

public class RegistActivity extends AppCompatActivity {

    private static final String TAG = "STV2_DEBUG"; // Egységes tag a kereséshez
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore store = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Kezdődik");

        try {
            setContentView(R.layout.activity_regist);
            Log.d(TAG, "onCreate: Layout betöltve");
        } catch (Exception e) {
            Log.e(TAG, "onCreate: HIBA a setContentView során!", e);
        }

        TextView skip = findViewById(R.id.registtologin);
        // Helyes változat:
        skip.setOnClickListener(v -> {
            Log.d(TAG, "Váltás LoginActivity-re");
            Intent intent = new Intent(RegistActivity.this, LoginActivity.class); // Itt hiányzott az 'Intent' típus
            startActivity(intent);
        });

        Button registbutton = findViewById(R.id.registbutton);
        registbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "--- Regisztráció gomb megnyomva ---");

                // 1. Animáció teszt
                try {
                    ImageView giraffe = findViewById(R.id.giraffestart);
                    if (giraffe != null) {
                        Animation anim = AnimationUtils.loadAnimation(RegistActivity.this, R.anim.giraffe_move);
                        giraffe.startAnimation(anim);
                        Log.d(TAG, "Animáció elindítva");
                    } else {
                        Log.w(TAG, "A giraffe ImageView nem található (null)!");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Hiba az animáció során!", e);
                }

                // 2. Adatok begyűjtése
                EditText usernameE = findViewById(R.id.registusername);
                EditText passwordE = findViewById(R.id.registpassword);
                EditText passwordagainE = findViewById(R.id.registpasswordagain);
                EditText emailE = findViewById(R.id.registemail);

                String username = usernameE.getText().toString().trim();
                String password = passwordE.getText().toString().trim();
                String passwordagain = passwordagainE.getText().toString().trim();
                String email = emailE.getText().toString().trim();

                Log.d(TAG, "Begyűjtött adatok: Email: " + email + ", User: " + username);

                // 3. Ellenőrzések
                if (email.isEmpty() || password.isEmpty() || username.isEmpty() || passwordagain.isEmpty()) {
                    Log.w(TAG, "Hiba: Üres mezők");
                    Toast.makeText(RegistActivity.this, "Tölts ki minden mezőt!", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!password.equals(passwordagain)) {
                    Log.w(TAG, "Hiba: A jelszavak nem egyeznek");
                    Toast.makeText(RegistActivity.this, "A jelszavak nem egyeznek!", Toast.LENGTH_LONG).show();
                    return;
                }

                if (password.length() < 6) {
                    Log.w(TAG, "Hiba: Rövid jelszó");
                    Toast.makeText(RegistActivity.this, "A jelszó legalább 6 karakter legyen!", Toast.LENGTH_LONG).show();
                    return;
                }

                // 4. Firebase Auth indítása
                Log.d(TAG, "Firebase createUser indítása...");
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegistActivity.this, task -> {
                            if (task.isSuccessful()) {
                                Log.i(TAG, "FirebaseAuth regisztráció SIKERES");

                                FirebaseUser fuser = auth.getCurrentUser();
                                if (fuser == null) {
                                    Log.e(TAG, "Fuser null a sikeres regisztráció után!");
                                    return;
                                }

                                String uid = fuser.getUid();
                                Log.d(TAG, "User UID: " + uid);

                                User user = new User(username, email);
                                for (int i = 0; i < 3; i++) {
                                    user.getFavorites().add("");
                                }

                                Log.d(TAG, "Firestore mentés indítása...");
                                user.saveToFirestore(uid,
                                        () -> { // onSuccess
                                            Log.i(TAG, "Firestore mentés SIKERES");
                                            Toast.makeText(RegistActivity.this, "Sikeres regisztráció!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(RegistActivity.this, HomeActivity.class));
                                            finish();
                                        },
                                        () -> { // onFailure
                                            Log.e(TAG, "Firestore mentés SIKERTELEN");
                                            Toast.makeText(RegistActivity.this, "Hiba a Firestore mentéskor", Toast.LENGTH_SHORT).show();
                                        }
                                );

                            } else {
                                Log.e(TAG, "FirebaseAuth HIBA!", task.getException());
                                Toast.makeText(RegistActivity.this, "Hiba: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}