package com.example.stv2;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.credentials.CredentialManager;
import android.net.Uri;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue; // Ez kell a tagság törléséhez
import android.os.Bundle;
import android.widget.Button;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.stv2.model.User;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
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
    private Boolean ismoderator = false;
    private Boolean favchosen = false;
    private List<String> favs;

    private ShapeableImageView profilepic;
    private Button deleteprofile, profile_moderator, makemoderator;
    private CardView profile_delete_card;
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
        setupTopMenu();

        String currentUid = FirebaseAuth.getInstance().getUid();
        //van e id
        if(getIntent()!=null && getIntent().getStringExtra("userid")!=null &&
                !getIntent().getStringExtra("userid").isEmpty()){
            userid = getIntent().getStringExtra("userid");
        } else {
            userid = currentUid;
            Log.d("profil", "nem talál átadott userid");
        }

        String uid = FirebaseAuth.getInstance().getUid();
        //saját profil oldal
        if (currentUid == null || currentUid.equals(userid)) {
            ownprofile = true;
        }

        //moderator e
        if (uid != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean moderator = documentSnapshot.getBoolean("admin");
                            ismoderator = (moderator != null && moderator);

                            if(ownprofile || ismoderator){
                                profile_edit.setVisibility(View.VISIBLE);
                                Log.d("AdminCheck", "Szerkesztés engedélyezve (Saját vagy Admin)");
                            }
                            if(ismoderator){
                                profile_moderator.setVisibility(View.VISIBLE);
                            }

                            if (moderator != null && moderator) {
                                Log.d("AdminCheck", "A felhasználó admin.");
                            } else {
                                Log.d("AdminCheck", "A felhasználó nem admin.");
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("AdminCheck", "Hiba a lekéréskor", e));
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
        deleteprofile = findViewById(R.id.deleteprofile);
        profile_moderator = findViewById(R.id.profile_moderator);
        makemoderator = findViewById(R.id.makemoderator);

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
        profile_delete_card = findViewById(R.id.profile_delete_card);

        if(ownprofile || ismoderator){
            profile_edit.setVisibility(View.VISIBLE);
        }
        if(ismoderator){
            profile_moderator.setVisibility(View.VISIBLE);
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

        deleteprofile.setOnClickListener(v -> {

            if (ownprofile){
                final EditText passwordInput = new EditText(this);
                passwordInput.setHint("Jelszó");
                passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

                new AlertDialog.Builder(this)
                        .setTitle("Fiók törlése")
                        .setMessage("A törléshez kérlek add meg a jelszavad a megerősítéshez!")
                        .setView(passwordInput)
                        .setPositiveButton("Törlés", (dialog, whichButton) -> {
                            String currentPassword = passwordInput.getText().toString();
                            FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

                            if (fUser != null && !currentPassword.isEmpty()) {
                                // Itt hívjuk meg az osztályt és a statikus metódusát
                                AuthCredential credential = EmailAuthProvider.getCredential(fUser.getEmail(), currentPassword);

                                fUser.reauthenticate(credential).addOnCompleteListener(reAuthTask -> {
                                    if (reAuthTask.isSuccessful()) {
                                        // Ha sikerült, meghívjuk a saját metódusunkat
                                        startFullDeletionProcess(fUser);
                                    } else {
                                        Toast.makeText(this, "Hibás jelszó!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Mégse", null)
                        .show();
            } else if (ismoderator){
                new AlertDialog.Builder(this)
                        .setTitle("Felhasználó moderátori törlése")
                        .setMessage("Biztosan törölni akarod ezt a felhasználót? Az Auth fiókja megmarad, de minden adata és klubtagsága megszűnik.")
                        .setPositiveButton("Igen, törlöm", (dialog, whichButton) -> {
                            startModeratorDeletion();
                        })
                        .setNegativeButton("Mégse", null)
                        .show();
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
                    Glide.with(this).load(user.getProfilepicurl())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.ic_default_avatar)
                            .error(R.drawable.ic_default_avatar)
                            .into(profilepic)
                          ;
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
                            Glide.with(this).load(book.getCoverpic())
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(R.drawable.default_book)
                                    .error(R.drawable.default_book)
                                    .into(book1);
                        });
                    }

                    if (!favs.get(1).isEmpty()) {
                        user.getFavbook(1, book -> {
                            book2title.setText(book.getTitle());
                            Glide.with(this).load(book.getCoverpic())
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(R.drawable.default_book)
                                    .error(R.drawable.default_book)
                                    .into(book2);
                        });
                    }

                    if (!favs.get(2).isEmpty()) {
                        user.getFavbook(2, book -> {
                            book3title.setText(book.getTitle());
                            Glide.with(this).load(book.getCoverpic())
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(R.drawable.default_book)
                                    .error(R.drawable.default_book)
                                    .into(book3);
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
            profile_delete_card.setVisibility(View.VISIBLE);

            profileusername.setVisibility(View.GONE);
            username_edittext.setVisibility(View.VISIBLE);

            delete_first.setVisibility(View.VISIBLE);
            delete_second.setVisibility(View.VISIBLE);
            delete_third.setVisibility(View.VISIBLE);

            if(ismoderator){
                if (user.getAdmin() != null && user.getAdmin()) {
                    makemoderator.setText("Admin jog megvonása");
                } else {
                    makemoderator.setText("Moderátorrá tétel");
                }

                makemoderator.setVisibility(View.VISIBLE);

                makemoderator.setOnClickListener(k -> {
                    if (userid == null || userid.equals(FirebaseAuth.getInstance().getUid())) return;

                    boolean isTargetAdmin = (user.getAdmin() != null && user.getAdmin());
                    String title = isTargetAdmin ? "Jog megvonása" : "Moderátor kinevezése";
                    String message = isTargetAdmin ? "Biztosan elveszed az admin jogot tőle?" : "Biztosan admin jogot adsz neki?";

                    new AlertDialog.Builder(this)
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton("Igen", (dialog, which) -> {
                                boolean newState = !isTargetAdmin; // megfordítjuk az állapotot

                                FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(userid)
                                        .update("admin", newState)
                                        .addOnSuccessListener(aVoid -> {
                                            user.setAdmin(newState); // lokális frissítés
                                            makemoderator.setText(newState ? "Admin jog megvonása" : "Moderátorrá tétel");
                                            profile_moderator.setVisibility(newState ? View.VISIBLE : View.GONE);
                                            Toast.makeText(this, "Sikeres módosítás!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this, "Hiba történt!", Toast.LENGTH_SHORT).show());
                            })
                            .setNegativeButton("Mégse", null)
                            .show();
                });
            }

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
                i.putExtra("userid", userid);
                startActivity(i);
            }
        });

        book2.setOnClickListener(v -> {
            if(isEditing){
                Intent i = new Intent(ProfileActivity.this, SearchActivity.class);
                i.putExtra("favchange", 2);
                i.putExtra("userid", userid);
                startActivity(i);
            }
        });

        book3.setOnClickListener(v -> {
            if(isEditing){
                Intent i = new Intent(ProfileActivity.this, SearchActivity.class);
                i.putExtra("favchange", 3);
                i.putExtra("userid", userid);
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

        if (user != null && user.getAdmin() != null && user.getAdmin()) {
            profile_moderator.setVisibility(View.VISIBLE);
        }

        profile_save.setVisibility(View.GONE);
        profile_edit.setVisibility(View.VISIBLE);
        makemoderator.setVisibility(View.GONE);

        profileusername.setVisibility(View.VISIBLE);
        username_edittext.setVisibility(View.GONE);

        delete_first.setVisibility(View.GONE);
        delete_second.setVisibility(View.GONE);
        delete_third.setVisibility(View.GONE);
        profile_delete_card.setVisibility(View.GONE);

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
    private void startFullDeletionProcess(FirebaseUser fUser) {
        String uid = fUser.getUid();
        String userEmail = fUser.getEmail();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DatabaseReference rtdb = FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        Log.d("DeleteProcess", "Takarítás indul: " + uid);

        FirebaseStorage.getInstance().getReference().child("profiles/" + uid + ".jpg").delete()
                .addOnFailureListener(e -> Log.d("DeleteProcess", "Nincs profilkép a Storage-ban."));

        firestore.collection("club")
                .whereArrayContains("members", userEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot) {
                        String clubId = doc.getId();
                        String adminEmail = doc.getString("admin");

                        if (userEmail != null && userEmail.equals(adminEmail)) {
                            // Admin vagy: Klub + Üzenetek törlése
                            firestore.collection("club").document(clubId).delete();
                            rtdb.child("messages").child(clubId).removeValue();
                            rtdb.child("club_members").child(clubId).removeValue();
                            Log.d("DeleteProcess", "Adminisztrált klub törölve: " + clubId);
                        } else {
                            // Csak tag vagy: Kiléptetés
                            firestore.collection("club").document(clubId)
                                    .update("members", com.google.firebase.firestore.FieldValue.arrayRemove(userEmail));
                            rtdb.child("club_members").child(clubId).child(uid).removeValue();
                            Log.d("DeleteProcess", "Kilépés klubból: " + clubId);
                        }
                    }

                    // 3. User profil és kapcsolatok törlése
                    rtdb.child("connections").child(uid).removeValue();
                    firestore.collection("users").document(uid).delete().addOnSuccessListener(aVoid -> {

                        // 4. Firebase Auth fiók végleges törlése
                        fUser.delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.i("DeleteProcess", "Minden adat törölve, viszlát!");
                                getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();

                                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.e("DeleteProcess", "Hiba a végleges törlésnél", task.getException());
                            }
                        });
                    });
                })
                .addOnFailureListener(e -> Log.e("DeleteProcess", "Firestore keresési hiba", e));
    }

    private void startModeratorDeletion() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DatabaseReference rtdb = FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        Log.d("ModeratorDelete", "Moderátori takarítás indul a következőre: " + userid);

        FirebaseStorage.getInstance().getReference().child("profiles/" + userid + ".jpg").delete()
                .addOnFailureListener(e -> Log.d("Delete", "Nincs törlendő kép."));

        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "Hiba: Felhasználói adatok nem elérhetőek!", Toast.LENGTH_SHORT).show();
            return;
        }
        String targetEmail = user.getEmail();

        firestore.collection("club")
                .whereArrayContains("members", targetEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        String clubId = doc.getId();
                        String adminEmail = doc.getString("admin");

                        if (targetEmail.equals(adminEmail)) {
                            firestore.collection("club").document(clubId).delete();
                            rtdb.child("messages").child(clubId).removeValue();
                            rtdb.child("club_members").child(clubId).removeValue();
                        } else {
                            firestore.collection("club").document(clubId)
                                    .update("members", FieldValue.arrayRemove(targetEmail));
                            rtdb.child("club_members").child(clubId).child(userid).removeValue();
                        }
                    }

                    // 3. Célpont profiljának és kapcsolatainak törlése
                    rtdb.child("connections").child(userid).removeValue();
                    firestore.collection("users").document(userid).delete().addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Felhasználó sikeresen eltávolítva!", Toast.LENGTH_SHORT).show();
                        finish(); // Csak bezárjuk az oldalt, nem léptetünk ki senkit
                    });
                });
    }
}
