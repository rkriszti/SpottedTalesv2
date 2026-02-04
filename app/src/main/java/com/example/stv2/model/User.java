package com.example.stv2.model;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class User {
    String username;
    String email;
    String profilepicurl;
    Boolean admin;
    List<String> favorites; //könyvid??

    public User() {
        this.favorites = new ArrayList<>();
        this.admin = false;
        this.profilepicurl = "";
    }

   public User(String u, String e){
    username = u;
    email = e;
    favorites = new ArrayList<>();
    //res/drawable alatti erőforrásokat nem fájlútvonallal, hanem resource ID-val kell hivatkozni.
    profilepicurl = "";
    admin = false;
    }


    // ---------------- ÚJ: Mentés Firestore-ba ----------------
    public void saveToFirestore(String uid, Runnable onSuccess, Runnable onFailure) {
        FirebaseFirestore store = FirebaseFirestore.getInstance();

        store.collection("users").document(uid)
                .set(this, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("User", "Sikeres mentés Firestore-ba");
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("User", "Firestore mentési hiba", e);
                    if (onFailure != null) onFailure.run();
                });
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfilepicurl() { return profilepicurl; }
    public void setProfilepicurl(String profilepicurl) { this.profilepicurl = profilepicurl; }

    public Boolean getAdmin() { return admin; }
    public void setAdmin(Boolean admin) { this.admin = admin; }

    public List<String> getFavorites() { return favorites; }

    public void getFavbook(int number,  Consumer<Book> callback){
        int size = favorites.size();
        if (favorites == null || favorites.size() == 0) return;


        if(number<0){ number = 0;}if(number >2){number = 2;}

        FirebaseFirestore.getInstance()
                .collection("books")
                .document(favorites.get(number))
                .get()
                .addOnSuccessListener(doc -> {
                    Book book = doc.toObject(Book.class);
                    if (book != null) callback.accept(book);
                })
                .addOnFailureListener(e ->
                        Log.e("BOOK", "Hiba a könyvek lekérésekor", e)
                );


    }
    public void setFavorites(List<String> favorites) { this.favorites = favorites; }
}
