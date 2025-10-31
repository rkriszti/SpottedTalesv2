package com.example.stv2.model;

import java.sql.Timestamp;
import java.util.UUID;

public class Message {
    private String id;
    private String message;
    private String useremail;
    private Timestamp time;

    public Message(){};
    /// mivan ha belikeolnak üzenetet?? vagy valamire válasz???

    public Message(String message, Timestamp time, String user) {

        /// message nem lehet üres, user sem
        if (message == null || message.isEmpty())
            throw new IllegalArgumentException("Message nem lehet üres");
        if (user == null)
            throw new IllegalArgumentException("User nem lehet null");
        this.id = UUID.randomUUID().toString();
        this.time = time;
        this.useremail = user;
    }

    //alap setter, getter

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public Timestamp getTime() {
        return time;
    }
    public void setTime(Timestamp time) {
        this.time = time;
    }
    public String getUser() {
        return useremail;
    }
    public void setUser(String user) {
        this.useremail = user;
    }
}
