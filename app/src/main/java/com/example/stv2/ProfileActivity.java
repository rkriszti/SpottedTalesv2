package com.example.stv2;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.stv2.model.User;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends MenuActivity {

    private int which;
    private int deletehappened = -1;
    private String userid, favbookid, bookid;
    private Boolean ownprofile = false;
    private Boolean favchosen = false;
    private List<String> favs;

    private ShapeableImageView profilepic;
    private ImageView  book1, book2, book3, profile_edit, profile_save, delete_first, delete_second, delete_third;
    private TextView profileusername, book1title, book2title, book3title;
    private EditText username_edittext;
    private ToggleButton helpButton;

    private User user;

    private boolean isEditing = false;

    //import csv
    private List<String> importcach = new ArrayList<>();
    private static final String PREFS_NAME = "profile_cache";
    private static final String IMPORTED_BOOKS_KEY = "imported_books";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupBottomMenu(R.id.nav_profile);

        //van e id
        if(getIntent()!=null && getIntent().getStringExtra("userid")!=null &&
                !getIntent().getStringExtra("userid").isEmpty()){
            userid = getIntent().getStringExtra("userid");
        } else {
            finish();
        }
        //saját profil oldal
        if (FirebaseAuth.getInstance().getUid()== null || userid.equals( FirebaseAuth.getInstance().getUid())) {
            ownprofile = true;
        }

        //kedvenc változás
        if(getIntent()!=null &&
                getIntent().getIntExtra("whichbook", -1)!=-1){
            favchosen = true;
            which = getIntent().getIntExtra("whichbook", -1);
            bookid = getIntent().getStringExtra("bookid");
        }

        profilepic = findViewById(R.id.profile_pic);
        profileusername = findViewById(R.id.profile_username);

        book1 = findViewById(R.id.bookFirst);
        book2 = findViewById(R.id.bookSecond);
        book3 = findViewById(R.id.bookThird);
        book1title = findViewById(R.id.bookFirstTitle);
        book2title = findViewById(R.id.bookSecondTitle);
        book3title = findViewById(R.id.bookThirdTitle);
        username_edittext = findViewById(R.id.profile_username_edittext);

        profile_edit = findViewById(R.id.profile_edit);
        profile_save = findViewById(R.id.profile_save);

        delete_first = findViewById(R.id.delete_first);
        delete_second = findViewById(R.id.delete_second);
        delete_third = findViewById(R.id.delete_third);

        if(ownprofile){
            profile_edit.setVisibility(View.VISIBLE);
        }

        Button importbutton = findViewById(R.id.buttonimport);
        Button buttonchoose = findViewById(R.id.buttonchoose);
         helpButton = findViewById(R.id.goodreadshelp);

        //csv fájl kiválasztása
        importbutton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("text/csv");
            startActivityForResult(Intent.createChooser(intent, "CSV kiválasztása"), 1001);
        });

        buttonchoose.setOnClickListener(k ->{
            Intent intent = new Intent(ProfileActivity.this, RecommendActivity.class);
            startActivity(intent);
        });

        helpButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showHelpDialog();
                helpButton.setChecked(false);
            }
        });

        loadUser();
        setListeners();

    }


    private void loadUser() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()){
                        Log.d("Profile", "Nem sikerült lekérni usert");
                        return;
                    }

                    user = doc.toObject(User.class);
                    if (user == null) return;

                   /* while(user.getFavorites().size() < which-1){
                        Log.d("Profile", "Which túl nagy --");
                        which--;
                    }*/

                    //adatok betöltése
                    Glide.with(this).load(user.getProfilepicurl()).into(profilepic);
                    profileusername.setText(user.getUsername());
                    username_edittext.setText(user.getUsername());

                    favs = user.getFavorites();
                    if (favs.isEmpty()) {
                        favs.add("");
                    }
                    while(favs.size() <3){
                        favs.add("");
                    }

                    //kedvencek
                    if (!favs.get(0).isEmpty()) {
                        user.getFavbook(0, book -> {
                            book1title.setText(book.getTitle());
                            Glide.with(this).load(book.getCoverpic()).into(book1);
                        });
                    }

                    if (!favs.get(1).isEmpty()) {
                        user.getFavbook(1, book -> {
                            book2title.setText(book.getTitle());
                            Glide.with(this).load(book.getCoverpic()).into(book2);
                        });
                    }

                    if (!favs.get(2).isEmpty()) {
                        user.getFavbook(2, book -> {
                            book3title.setText(book.getTitle());
                            Glide.with(this).load(book.getCoverpic()).into(book3);
                        });
                    }

                    if(favchosen){
                        updateFavorite(which, bookid);
                    }


                });




    }

    private void setListeners(){
        //edit on
        profile_edit.setOnClickListener( v -> {
            isEditing = true;
            profile_save.setVisibility(View.VISIBLE);
            profile_edit.setVisibility(View.GONE);

            profileusername.setVisibility(View.GONE);
            username_edittext.setVisibility(View.VISIBLE);

            delete_first.setVisibility(View.VISIBLE);
            delete_second.setVisibility(View.VISIBLE);
            delete_third.setVisibility(View.VISIBLE);

        });

        //edit off - save
        profile_save.setOnClickListener(k -> {
            if(isEditing){
                save();
            }
        });

        profilepic.setOnClickListener(v -> {
            if (!isEditing) return;

            pickProfileImage.launch(
                    new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build()
            );
        });


        book1.setOnClickListener(v -> {
            if(isEditing){
                Intent i = new Intent(ProfileActivity.this, SearchActivity.class);
                i.putExtra("favchange", 1);
                startActivity(i);
            }
        });

        book2.setOnClickListener(v -> {
            if(isEditing){
                Intent i = new Intent(ProfileActivity.this, SearchActivity.class);
                i.putExtra("favchange", 2);
                startActivity(i);
            }
        });

        book3.setOnClickListener(v -> {
            if(isEditing){
                Intent i = new Intent(ProfileActivity.this, SearchActivity.class);
                i.putExtra("favchange", 3);
                startActivity(i);
            }
        });

        delete_first.setOnClickListener(ff->{
            if(isEditing){
                user.getFavorites().set(0, "");
                deletehappened = 1;
                onlyFirestoreSave(deletehappened);

            }
        });

        delete_second.setOnClickListener(fff->{
            if(isEditing){
                user.getFavorites().set(1, "");
                deletehappened = 2;
                onlyFirestoreSave(deletehappened);

            }
        });

        delete_third.setOnClickListener(ffff->{
            if(isEditing){
                user.getFavorites().set(2, "");
                deletehappened = 3;
                onlyFirestoreSave(deletehappened);

            }
        });
    }

    private void onlyFirestoreSave(int delete){

        if(delete!=-1){
            switch(delete){
                case 1:
                    book1title.setText("Válassz!");
                    Glide.with(this).load(R.drawable.default_book).into(book1);
                    break;
                case 2:
                    book2title.setText("Válassz!");
                    Glide.with(this).load(R.drawable.default_book).into(book2);
                    break;
                case 3:
                    book3title.setText("Válassz!");
                    Glide.with(this).load(R.drawable.default_book).into(book3);
                    break;
            }
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userid)
                    .update("favorites", user.getFavorites())
                    .addOnSuccessListener(aVoid -> Log.d("Profil", "Profil oldalon a fav change megtörtént"))
                    .addOnFailureListener(e -> Log.e("Profil", "Hiba profil oldali fav change", e));

            deletehappened = -1;
        }

        if(!user.getUsername().equals(username_edittext.getText())){
            profileusername.setText(username_edittext.getText());
            username_edittext.setText(username_edittext.getText());

            user.setUsername(username_edittext.getText().toString());

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userid)
                    .update("username", username_edittext.getText().toString())
                    .addOnSuccessListener(aVoid -> Log.d("Profil", "Profil oldalon a username change megtörtént"))
                    .addOnFailureListener(e -> Log.e("Profil", "Hiba profil oldali username change", e));

        }


    }
    private void save(){
        isEditing = false;
        profile_save.setVisibility(View.GONE);
        profile_edit.setVisibility(View.VISIBLE);

        profileusername.setVisibility(View.VISIBLE);
        username_edittext.setVisibility(View.GONE);

        delete_first.setVisibility(View.GONE);
        delete_second.setVisibility(View.GONE);
        delete_third.setVisibility(View.GONE);

        if(!user.getUsername().equals(username_edittext.getText())){
            profileusername.setText(username_edittext.getText());
            username_edittext.setText(username_edittext.getText());

            user.setUsername(username_edittext.getText().toString());

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userid)
                    .update("username", username_edittext.getText().toString())
                    .addOnSuccessListener(aVoid -> Log.d("Profil", "Profil oldalon a username change megtörtént"))
                    .addOnFailureListener(e -> Log.e("Profil", "Hiba profil oldali username change", e));

        }

    }

    private ActivityResultLauncher<PickVisualMediaRequest> pickProfileImage =
            registerForActivityResult(
                    new ActivityResultContracts.PickVisualMedia(),
                    uri -> {
                        if (uri == null) return;

                        // azonnali preview
                        Glide.with(this).load(uri).into(profilepic);

                        // feltöltés Storage-be
                        StorageReference imageRef = FirebaseStorage.getInstance()
                                .getReference()
                                .child("profiles/" + userid + ".jpg");

                        imageRef.putFile(uri)
                                .addOnSuccessListener(taskSnapshot ->
                                        imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                                            String picurl = downloadUri.toString();
                                            user.setProfilepicurl(picurl);

                                            // Firestore frissítés
                                            FirebaseFirestore.getInstance()
                                                    .collection("users")
                                                    .document(userid)
                                                    .update("profilepicurl", picurl);
                                        })
                                )
                                .addOnFailureListener(e ->
                                        Log.e("FirebaseUpload", "Profilkép feltöltési hiba: " + e.getMessage()));
                    }
            );

    private void updateFavorite(int index, String newBookId) {
        if(user == null) return;


        int idx = index - 1;

        // ha a lista túl rövid, töltsd fel üres stringekkel
        if (favs.isEmpty()) {
            favs.add("");
        }
        while(favs.size() <3){
            favs.add("");
        }

        // most már biztonságos a set
        favs.set(idx, newBookId);

        // 2. Firestore frissítés
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userid)
                .update("favorites", user.getFavorites())
                .addOnSuccessListener(aVoid -> Log.d("Profile", "Kedvenc frissítve"))
                .addOnFailureListener(e -> Log.e("Profile", "Hiba a kedvenc mentésénél", e));

        // 3. UI update
        user.getFavbook(index-1, book -> {
            if(index == 1) {
                book1title.setText(book.getTitle());
                Glide.with(this).load(book.getCoverpic()).into(book1);
            } else if(index == 2) {
                book2title.setText(book.getTitle());
                Glide.with(this).load(book.getCoverpic()).into(book2);
            } else if(index == 3) {
                book3title.setText(book.getTitle());
                Glide.with(this).load(book.getCoverpic()).into(book3);
            }
        });
    }


    private void saveImportedBooks() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        JSONArray array = new JSONArray();
        for(String bookJson : importcach){
            try {
                array.put(new JSONObject(bookJson));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        editor.putString(IMPORTED_BOOKS_KEY, array.toString());
        editor.apply();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1001 && resultCode == RESULT_OK && data != null){
            Uri uri = data.getData();
            if(uri != null){
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    importcach.clear();
                    String line;
                    boolean firstLine = true;

                    while((line = reader.readLine()) != null){
                        if(firstLine){
                            firstLine = false;
                            continue;
                        }

                        //regex,
                        //páros " " (ha könyv nevében lenne , feliseri)
                        String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);


                        if(columns.length > 18){
                            String title = columns[1].replace("\"", "").trim();

                            //oszlop index 0-tól van)
                            String qColumn = columns[16].replace("\"", "").trim();
                            String sColumn = columns[18].replace("\"", "").trim();

                            String combinedLists = qColumn;
                            if (!sColumn.isEmpty()) {
                                combinedLists = combinedLists.isEmpty() ? sColumn : combinedLists + ", " + sColumn;
                            }

                            JSONObject bookObject = new JSONObject();
                            bookObject.put("title", title);

                            JSONArray listsJson = new JSONArray();
                            for(String l : combinedLists.split(",")){
                                String clean = l.trim();
                                if(!clean.isEmpty()) listsJson.put(clean);
                            }
                            bookObject.put("lists", listsJson);

                            importcach.add(bookObject.toString());
                        }
                    }
                    reader.close();
                    saveImportedBooks();
                    Log.d("CSV", "Sikeres import: " + importcach.size() + " könyv.");

                } catch (Exception e){
                    Log.e("CSV", "Hiba az importáláskor", e);
                }
            }
        }
    }

    private void showHelpDialog() {
        // Layout felfújása (inflate)
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_help, null);

        new AlertDialog.Builder(this)
                .setView(dialogView) // A saját XML-ünket használjuk
                .setPositiveButton("Értem", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
