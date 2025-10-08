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
    private String picurl; //= downloadUri.toString(); majd launcherben
    private Uri selectedImageUri; //ActivityResultLauncher-ben használva (egyből visszakapott uri)
    private Book currentBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        Switch kapcsolo = findViewById(R.id.switchForm);
        LinearLayout form_book = findViewById(R.id.form_book);
        LinearLayout form_club = findViewById(R.id.form_club);

        //book elemek--------------------------------------
        ImageView form_book_borito = findViewById(R.id.form_book_borito);
        Button form_book_button = findViewById(R.id.form_book_button);
        //club elemek--------------------------------------
        /// club elemek

        //űrlapok közötti nagiválás switchel
        kapcsolo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                form_book.setVisibility(LinearLayout.GONE);
                form_club.setVisibility(LinearLayout.VISIBLE);
            } else {
                form_book.setVisibility(LinearLayout.VISIBLE);
                form_club.setVisibility(LinearLayout.GONE);
            }
        });

        //launcher
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        form_book_borito.setImageURI(uri); //imagieviewba meg is jelenítjük egyből

                        //bucket-be feltöltés
                        FirebaseStorage storage = FirebaseStorage.getInstance("gs://stv2-84ad0.firebasestorage.app"); //csatlakozás
                        StorageReference imageRef = storage.getReference()
                                .child("books/" + UUID.randomUUID() + ".jpg"); //egyedi fájlnév generálás

                        //mentés
                        imageRef.putFile(uri)
                                .addOnSuccessListener(taskSnapshot ->
                                        imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                                            picurl = downloadUri.toString();
                                            Log.d("FirebaseUpload", "Kép 1: " + picurl);
                                            if (currentBook != null) { //ha van könyv
                                                currentBook.setCoverpic(picurl);
                                            }
                                        })
                                )
                                .addOnFailureListener(e -> Log.e("FirebaseUpload", "Hiba: " + e.getMessage()));
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                }
        );

        //kép választás felugrik
        form_book_borito.setOnClickListener(v -> pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                        .build()
        ));

        //feltöltés gomb
        form_book_button.setOnClickListener(v -> {
            //beérkezett adatok
            EditText cim = findViewById(R.id.form_book_cim);
            EditText szerzo = findViewById(R.id.form_book_szerzo);

            String cimm = cim.getText().toString().trim();
            String szerzoo = szerzo.getText().toString().trim();

            if (cimm.isEmpty()) {
                cim.setError("Adj meg egy címet!");
                cim.requestFocus();
                return;
            }
            if (szerzoo.isEmpty()) {
                szerzo.setError("Adj meg egy szerzőt!");
                szerzo.requestFocus();
                return;
            }

            //be van jelentkezve?
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Be kell jelentkezned a könyv feltöltéséhez!", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = user.getEmail();

            //könyv
            currentBook = new Book(cimm, szerzoo, email);

            // van kép (nem kötelező)
            if (picurl != null) {
                currentBook.setCoverpic(picurl);
            }

            //könyv feltöltése kollekcióba
            FirebaseFirestore.getInstance().collection("books")
                    .add(currentBook)
                    .addOnSuccessListener(docRef ->
                            Toast.makeText(this, "Könyv sikeresen feltöltve!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Hiba a feltöltés során: " + e.getMessage(), Toast.LENGTH_SHORT).show());


        });

        //navigációs menü
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

        //plusz gomb
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

    }//oncreate vége
} //activity vége
