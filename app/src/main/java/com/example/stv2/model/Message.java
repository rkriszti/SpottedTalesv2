package com.example.stv2.model;

public class Message {
    private String id;
    private String message;
    private String useremail;
    private long timestamp; // long egyszerűbb a Firebase-nek

    public Message() {} // Üres konstruktor kell a Firebase-nek!

    public Message(String id, String message, String useremail, long timestamp) {
        this.id = id;
        this.message = message;
        this.useremail = useremail;
        this.timestamp = timestamp;
    }


    // Getterek / Setterek
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getUseremail() { return useremail; }
    public void setUseremail(String useremail) { this.useremail = useremail; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}