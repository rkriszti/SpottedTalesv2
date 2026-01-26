package com.example.stv2.model;

public class Book {

    private String id;          // Firestore document ID
    private String title;
    private String author;
    private String email;       // ki hozta létre
    private String coverpic;    // downloadUrl
    private boolean editing;  // CSAK UI állapot, NEM Firestore adat

    // Kötelező Firestore-hoz
    public Book() {
        this.editing = false;
    }

    // Új könyv létrehozásakor (ID-t Firestore adja!)
    public Book(String title, String author, String email) {
        this.title = title;
        this.author = author;
        this.email = email;
        this.editing = false;
    }

    // Új könyv képpel
    public Book(String title, String author, String email, String coverpic) {
        this.title = title;
        this.author = author;
        this.email = email;
        this.coverpic = coverpic;
        this.editing = false;
    }

    // -------- GETTEREK --------

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getEmail() {
        return email;
    }

    public String getCoverpic() {
        return coverpic;
    }

    public boolean isEditing() {
        return editing;
    }

    // -------- SETTEREK --------

    // Firestore betöltés után KÖTELEZŐEN be kell állítani
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCoverpic(String coverpic) {
        this.coverpic = coverpic;
    }

    // UI állapot (RecyclerView miatt!)
    public void setEditing(boolean editing) {
        this.editing = editing;
    }
}
