package com.example.stv2;

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

public class ProfileActivity extends MenuActivity {

    private String userid;
    private Boolean ownprofile = false;

    private ImageView profilepic, book1, book2, book3, profile_edit, profile_save;
    private TextView profileusername, book1title, book2title, book3title;
    private EditText username_edittext;

    private User user;

    private boolean isEditing = false;

    private enum PickTarget {
        PROFILE,
        BOOK1,
        BOOK2,
        BOOK3
    }

    private PickTarget currentPickTarget;


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

        profilepic = findViewById(R.id.profile_pic);
        profileusername = findViewById(R.id.profile_username);

        book1 = findViewById(R.id.bookFirst);
        book2 = findViewById(R.id.bookSecond);
        book3 = findViewById(R.id.bookThird);
        book1title = findViewById(R.id.bookThirdTitle);
        book2title = findViewById(R.id.bookSecondTitle);
        book3title = findViewById(R.id.bookThirdTitle);
        username_edittext = findViewById(R.id.profile_username_edittext);

        profile_edit = findViewById(R.id.profile_edit);
        profile_save = findViewById(R.id.profile_save);

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

                    //adatok betöltése
                    Glide.with(this).load(user.getProfilepicurl()).into(profilepic);
                    profileusername.setText(user.getUsername());
                    username_edittext.setText(user.getUsername());

                    //kedvencek
                    user.getFavbook(0, book -> {
                        book1title.setText(book.getTitle());
                        Glide.with(this).load(book.getCoverpic()).into(book1);
                    });
                    user.getFavbook(1, book -> {
                        book2title.setText(book.getTitle());
                        Glide.with(this).load(book.getCoverpic()).into(book2);
                    });

                    user.getFavbook(2, book -> {
                        book3title.setText(book.getTitle());
                        Glide.with(this).load(book.getCoverpic()).into(book3);
                    });


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

        });

        //edit off - save
        profile_save.setOnClickListener(k -> {
            if(isEditing){
                isEditing = false;
                profile_save.setVisibility(View.GONE);
                profile_edit.setVisibility(View.VISIBLE);

                profileusername.setVisibility(View.VISIBLE);
                username_edittext.setVisibility(View.GONE);

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
        });

        profilepic.setOnClickListener(p -> {
            if (!isEditing) return;
            currentPickTarget = PickTarget.PROFILE;
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });


        book1.setOnClickListener(v -> {
            if (!isEditing) return;
            currentPickTarget = PickTarget.BOOK1;
            launchPicker();
        });

        book2.setOnClickListener(v -> {
            if (!isEditing) return;
            currentPickTarget = PickTarget.BOOK2;
            launchPicker();
        });

        book3.setOnClickListener(v -> {
            if (!isEditing) return;
            currentPickTarget = PickTarget.BOOK3;
            launchPicker();
        });
    }

    private void launchPicker() {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void uploadProfilePic(Uri uri) {
        Glide.with(this).load(uri).into(profilepic); // azonnali preview

       FirebaseStorage storage = FirebaseStorage.getInstance();
         StorageReference ref = storage.getReference()
                .child("profiles/" + userid + ".jpg");

        ref.putFile(uri)
                .addOnSuccessListener(t ->
                        ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            user.setProfilepicurl(downloadUri.toString());

                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(userid)
                                    .update("profilepicurl", downloadUri.toString());
                        })
                );
    }

    private void uploadBookCover(Uri uri, int index) {
        ImageView targetView = index == 0 ? book1 : index == 1 ? book2 : book3;
        Glide.with(this).load(uri).into(targetView);

        String bookId = user.getFavorites().get(index);

        FirebaseStorage.getInstance()
                .getReference()
                .child("books/" + bookId + ".jpg")
                .putFile(uri)
                .addOnSuccessListener(t ->
                        FirebaseStorage.getInstance()
                                .getReference("books/" + bookId + ".jpg")
                                .getDownloadUrl()
                                .addOnSuccessListener(downloadUri ->
                                        FirebaseFirestore.getInstance()
                                                .collection("books")
                                                .document(bookId)
                                                .update("coverpic", downloadUri.toString())
                                )
                );
    }


    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(
                    new ActivityResultContracts.PickVisualMedia(),
                    uri -> {
                        if (uri == null) return;

                        switch (currentPickTarget) {
                            case PROFILE:
                                uploadProfilePic(uri);
                                break;
                            case BOOK1:
                                uploadBookCover(uri, 0);
                                break;
                            case BOOK2:
                                uploadBookCover(uri, 1);
                                break;
                            case BOOK3:
                                uploadBookCover(uri, 2);
                                break;
                        }
                    }
            );



}
