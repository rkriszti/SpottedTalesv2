package com.example.stv2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.stv2.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ProfileActivity extends MenuActivity {

    private int which;
    private int deletehappened = -1;
    private String userid, favbookid, bookid;
    private Boolean ownprofile = false;
    private Boolean favchosen = false;
    private List<String> favs;

    private ImageView profilepic, book1, book2, book3, profile_edit, profile_save, delete_first, delete_second, delete_third;
    private TextView profileusername, book1title, book2title, book3title;
    private EditText username_edittext;

    private User user;

    private boolean isEditing = false;


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


}
