package com.example.stv2.model;

import android.net.Uri;

import java.util.UUID;

public class Book {
    private String id;
    private String title;
    private String author;
    private String email; //ki készítette
    private String coverpic; //nem kötelező elem

    // Üres konstruktor szükséges Firestore-hoz
    public Book() {}

    public Book(String title, String author, String user){
        this.title = title;
        this.author = author;
        this.email = user;
        this.id = UUID.randomUUID().toString();
       /// this.coverpic =  def kép
    }
    public Book(String title, String author, String user, String uri){
        this.title = title;
        this.author = author;
        this.email = user;
        this.coverpic = uri;
    }

    // Getterek Firestore-hoz
    public String getTitle() { return title; }
    public String getId(){ return id; }
    public String getAuthor() { return author; }
    public String getEmail() { return email;}
    public String getCoverpic() { return coverpic;}

    /// setterek módosításhoz
    public void setCoverpic(String u){ this.coverpic = u;}

}
