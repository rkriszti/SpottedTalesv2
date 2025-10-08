package com.example.stv2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stv2.model.Book;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class NewBookActivity extends AppCompatActivity {

    private String downloadUrlString; // Firebase Storage download URL
    private Uri selectedImageUri;
    private Book currentBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        Switch switchForm = findViewById(R.id.switchForm);
        LinearLayout form_book = findViewById(R.id.form_book);
        LinearLayout form_club = findViewById(R.id.form_club);
        ImageView form_book_borito = findViewById(R.id.form_book_borito);
        Button form_book_button = findViewById(R.id.form_book_button);

        // 1️⃣ Kép kiválasztás launcher
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        form_book_borito.setImageURI(uri);

                        // Firebase Storage-be feltöltés
                        FirebaseStorage storage = FirebaseStorage.getInstance("gs://stv2-84ad0.firebasestorage.app");

                        StorageReference imageRef = storage.getReference()
                                .child("books/" + UUID.randomUUID() + ".jpg");

                        imageRef.putFile(uri)
                                .addOnSuccessListener(taskSnapshot ->
                                        imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                                            downloadUrlString = downloadUri.toString();
                                            Log.d("FirebaseUpload", "Kép 1: " + downloadUrlString);
                                            if (currentBook != null) {
                                                currentBook.setCoverpic(downloadUrlString);
                                            }
                                        })
                                )
                                .addOnFailureListener(e -> Log.e("FirebaseUpload", "Hiba: " + e.getMessage()));
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                }
        );

        // 2️⃣ Switch a formok váltására
        switchForm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                form_book.setVisibility(LinearLayout.GONE);
                form_club.setVisibility(LinearLayout.VISIBLE);
            } else {
                form_book.setVisibility(LinearLayout.VISIBLE);
                form_club.setVisibility(LinearLayout.GONE);
            }
        });

        // 3️⃣ Kép választás
        form_book_borito.setOnClickListener(v -> pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                        .build()
        ));

        // 4️⃣ Mentés gomb
        form_book_button.setOnClickListener(v -> {
            EditText cim = findViewById(R.id.form_book_cim);
            EditText szerzo = findViewById(R.id.form_book_szerzo);

            String cimm = cim.getText().toString().trim();
            String szerzoo = szerzo.getText().toString().trim();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Be kell jelentkezned a könyv feltöltéséhez!", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = user.getEmail();
            currentBook = new Book(cimm, szerzoo, email);

            Log.d("FirebaseUpload", "Kép download URL: " + downloadUrlString);
            // Ha van feltöltött kép, állítsuk be
            if (downloadUrlString != null) {
                currentBook.setCoverpic(downloadUrlString);
            }
            Log.d("FirebaseUpload", "Kép download URL: " + downloadUrlString);

            FirebaseFirestore.getInstance().collection("books")
                    .add(currentBook)
                    .addOnSuccessListener(docRef ->
                            Toast.makeText(this, "Könyv sikeresen feltöltve!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Hiba a feltöltés során: " + e.getMessage(), Toast.LENGTH_SHORT).show());


        });

        // 5️⃣ Navigációs menü
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) startActivity(new Intent(this, OpenAppActivity.class));
            else if (id == R.id.nav_search) startActivity(new Intent(this, SearchActivity.class));
            else if (id == R.id.nav_clubs) startActivity(new Intent(this, ClubsActivity.class));
            else if (id == R.id.nav_profile) startActivity(new Intent(this, ProfileActivity.class));
            else Toast.makeText(this, "OpenAct hiba", Toast.LENGTH_SHORT).show();
            return true;
        });

        // 6️⃣ Plusz gomb menü
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenuInflater().inflate(R.menu.menu_new_items, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_club) startActivity(new Intent(this, NewClubAcvitity.class));
                else if (id == R.id.nav_book) startActivity(new Intent(this, NewBookActivity.class));
                return true;
            });
            popup.show();
        });
    }
}
