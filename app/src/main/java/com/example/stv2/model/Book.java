package com.example.stv2.model;

public class Book {
    private String title;
    private String author;
    private int year;

    // Üres konstruktor szükséges Firestore-hoz
    public Book() {}

    public Book(String title, String author, int year) {
        this.title = title;
        this.author = author;
        this.year = year;
    }

    // Getterek Firestore-hoz
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear() { return year; }
}
