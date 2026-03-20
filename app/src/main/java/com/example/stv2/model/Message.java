package com.example.stv2.model;

public class Message {
    private String id;
    private String message;
    private String useremail;
    private long timestamp;
    private String roomPath;
    private String imageUrl;

    public Message() {}

    public Message(String id, String message, String useremail, long timestamp, String roomPath) {
        this.id = id;
        this.message = message;
        this.useremail = useremail;
        this.timestamp = timestamp;
        this.timestamp = timestamp;
        this.roomPath = roomPath;
        this.imageUrl = null;
    }


    // Getterek / Setterek
    public String getId() { return id; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setId(String id) { this.id = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getUseremail() { return useremail; }
    public void setUseremail(String useremail) { this.useremail = useremail; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getRoomPath() { return roomPath; }
    public void setRoomPath(String roomPath) { this.roomPath = roomPath; }
}