package com.example.stv2;

import android.content.Intent;
import android.content.res.ColorStateList;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.TooltipCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.stv2.model.Book;
import com.example.stv2.model.Club;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.UUID;

public class NewBookActivity extends MenuActivity {
    private String picurl; //= downloadUri.toString(); majd launcherben
    private Uri selectedImageUri; //ActivityResultLauncher-ben használva (egyből visszakapott uri)
    private Book currentBook;

    private EditText customsInput;
    private Button addButton;
    private ChipGroup chipGroup;
    private ArrayList<String> customsList = new ArrayList<>(); // itt tároljuk az értékeket


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        setupBottomMenu(R.id.nav_search);

        Switch kapcsolo = findViewById(R.id.switchForm);
        LinearLayout form_book = findViewById(R.id.form_book);
        ConstraintLayout form_club = findViewById(R.id.form_club);

        kapcsolo.getThumbDrawable().setTint(Color.parseColor("#EC407A")); // rózsaszín
        kapcsolo.getTrackDrawable().setTint(Color.parseColor("#741c60")); // lila


        //book elemek--------------------------------------
        ImageView form_book_borito = findViewById(R.id.form_book_borito);
        Button form_book_button = findViewById(R.id.form_book_button);
        //club elemek--------------------------------------
        /// club elemek
        Button form_club_button = findViewById(R.id.form_club_button);
        TooltipCompat.setTooltipText(findViewById(R.id.club_tooltip), "Publikus klubba bárki csatlakozhat, privátba csak meghívásból.");
        EditText chapters = findViewById(R.id.club_chapters);
        //fejezetekhez
         customsInput = findViewById(R.id.club_customs);
         addButton = findViewById(R.id.button_customs);
         chipGroup = findViewById(R.id.club_customs_container);
         customsList = new ArrayList<>(); // itt tároljuk az értékeket

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

        //fejezetek
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = customsInput.getText().toString().trim();
                if (!text.isEmpty()) {
                    addCustomChip(text);
                    customsInput.setText("");
                }
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

        //Klub feltöltés gomb
        form_club_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("CATTT", "GOMB megnyomva");
                EditText nev = findViewById(R.id.club_name);
                RadioButton publikus = findViewById(R.id.form_club_public);
               // RadioButton privat = findViewById(R.id.form_club_private); kell?
                Log.d("CATTT", "Klub név: " + nev.toString());


                String nevv = nev.getText().toString().trim();
                Log.d("CATTT", "Klub név: " + nevv);

                Boolean publikuss = publikus.isChecked();

                if (nevv.isEmpty()) {
                    nev.setError("Adj meg egy címet!"); //edittextbe írjuk az errort de a sztringet vizsgáljuk
                    nev.requestFocus();
                    return;
                }
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Toast.makeText(NewBookActivity.this, "Be kell jelentkezned a könyv feltöltéséhez!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d("CATTT", "User belépve");


                String email = user.getEmail();

                Integer chapterint = 0;
                if (chapters != null){
                    String ch = chapters.getText().toString().trim();
                    chapterint = ch.isEmpty() ? 0 : Integer.parseInt(ch);
                }
                Log.d("CATTT", "Chapter ok");

                Club currentClub = new Club(nevv, email, chapterint, publikuss, customsList); //itt már megkapja id-t
                String currentClubid = currentClub.getId();
                Log.d("CATTT", "currentClub: " + currentClub.toString());
                Log.d("CATTT", "customsList mérete: " + customsList.size());


                //klub kollekció létrehozása
                FirebaseFirestore.getInstance().collection("club")
                        .add(currentClub)
                        .addOnSuccessListener(docRef -> {
                            String firestoreClubId = docRef.getId();
                            Log.d("CATTT", "Dokumentum létrehozva ID: " + docRef.getId());
                            Toast.makeText(NewBookActivity.this, "Klub sikeresen feltöltve!", Toast.LENGTH_SHORT).show();

                            // CSAK az "id" mezőt írjuk felül az adatbázisban
                            docRef.update("id", firestoreClubId)
                                    .addOnSuccessListener(aVoid -> {
                                        // Itt már szinkronban van a két ID
                                        currentClub.setId(firestoreClubId);
                                        Log.d("Firebase", "Az ID mező felülírva: " + firestoreClubId);



                                        DatabaseReference clubRef = FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/")
                                                .getReference("connections")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("clubs")
                                                .child(firestoreClubId);

                                        clubRef.setValue(true)
                                                .addOnSuccessListener(Void -> {



                                                    Log.d("CONNECTION", "Klub connection létrehozva");
                                                    // csak itt indítsd az intentet
                                                    Intent intent = new Intent(NewBookActivity.this, HomeActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("CONNECTION", "Hiba a klub connection létrehozásakor", e);
                                                    Toast.makeText(NewBookActivity.this, "Connection hiba: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                });
                                        // Mehetünk tovább a Realtime DB-hez...
                                    });

                        })
                        .addOnFailureListener(e -> {
                            Log.e("CATTT", e.getMessage(), e);
                            Toast.makeText(NewBookActivity.this, "Hiba a feltöltés során: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });





            }
        });

        //Könyv feltöltés gomb
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
            String currentBookid = currentBook.getId();

            // van kép (nem kötelező)
            if (picurl != null) {
                currentBook.setCoverpic(picurl);
            }

            //könyv feltöltése kollekcióba
            FirebaseFirestore.getInstance().collection("books")
                    .add(currentBook)
                    .addOnSuccessListener(docRef -> {
                        String firestoreBookId = docRef.getId();
                        Toast.makeText(this, "Könyv sikeresen feltöltve!", Toast.LENGTH_SHORT).show();

                       /* FirebaseDatabase.getInstance()
                                .getReference("connections")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()) //userid
                                .child("books")
                                .child(firestoreBookId)
                                .setValue(true);*/
                        Log.d("CONNECTION", "Könyv 1");
                        DatabaseReference bookRef = FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/")
                                .getReference("connections")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child("books")
                                .child(firestoreBookId);

                        Log.d("CONNECTION", "Könyv 2");
                        bookRef.setValue(true)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("CONNECTION", "Könyv connection létrehozva");
                                    // ide lehet tenni az intentet
                                    Intent intent = new Intent(this, HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("CONNECTION", "Hiba a könyv connection létrehozásakor", e);
                                    Toast.makeText(this, "Connection hiba: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                        Log.d("CONNECTION", "Könyv 3");

                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Hiba a feltöltés során: " + e.getMessage(), Toast.LENGTH_SHORT).show());


            
            //navigációs menü
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) startActivity(new Intent(this, HomeActivity.class));
            else if (id == R.id.nav_search) startActivity(new Intent(this, SearchActivity.class));
            else if (id == R.id.nav_clubs) startActivity(new Intent(this, ClubsActivity.class));
            else if (id == R.id.nav_profile) startActivity(new Intent(this, ProfileActivity.class));
            else Toast.makeText(this, "OpenAct hiba", Toast.LENGTH_SHORT).show();
            return true;
        });

        //plusz gomb
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(vEW -> {
            PopupMenu popup = new PopupMenu(this, vEW);
            popup.getMenuInflater().inflate(R.menu.menu_new_items, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_club) startActivity(new Intent(this, NewBookActivity.class));
                else if (id == R.id.nav_book) startActivity(new Intent(this, NewBookActivity.class));
                return true;
            });
            popup.show();
        });


        });



    }//oncreate vége
    private void addCustomChip(String label) {
        // ne duplikáljunk
        if (customsList.contains(label)) return;

        customsList.add(label);

        Chip chip = new Chip(this);
        chip.setText(label);
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColorResource(R.color.purple);
        chip.setTextColor(Color.WHITE);
        chip.setCloseIconTint(ColorStateList.valueOf(Color.WHITE));

        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chipGroup.removeView(chip);
                customsList.remove(label);
            }
        });

        chipGroup.addView(chip);
    }

}//activity vége



