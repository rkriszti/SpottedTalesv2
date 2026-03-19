package com.example.stv2;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stv2.adapters.ClubChatAdapter;
import com.example.stv2.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatActivity extends MenuActivity {
    private String clubId, roomName, currentUserEmail;
    private RecyclerView recyclerView;
    private ClubChatAdapter adapter;
    private List<Message> messages = new ArrayList<>();
    private EditText messageInput;
    private ImageButton sendButton;
    private boolean oldhappened;
    private DatabaseReference rtdb;
    private String clubTheme = "";
    private ImageView chat_backbutton, chat_background;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setupBottomMenu();
        setupTopMenu();

        db = FirebaseFirestore.getInstance();
        rtdb = FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        clubId = getIntent().getStringExtra("clubId");
        roomName = getIntent().getStringExtra("roomName");
        oldhappened = "true".equals(getIntent().getStringExtra("isOldChat"));
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        recyclerView = findViewById(R.id.chat_recycler);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        chat_backbutton = findViewById(R.id.chat_backbutton);
        chat_background = findViewById(R.id.chat_background_image);

        adapter = new ClubChatAdapter(messages, currentUserEmail, clubTheme);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadMessages();

        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (!text.isEmpty()) sendMessage(text);
        });

        chat_backbutton.setOnClickListener(k -> finish());
    }

    private void loadMessages() {
        String collectionPath = oldhappened ? "oldclub" : "club";

        db.collection(collectionPath).document(clubId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;

                    Map<String, Object> roomsMap = (Map<String, Object>) snapshot.get("chapters");
                    if (roomsMap == null || !roomsMap.containsKey(roomName)) {
                        roomsMap = (Map<String, Object>) snapshot.get("customs");
                    }

                    if (roomsMap != null && roomsMap.containsKey(roomName)) {
                        List<String> messageIds = (List<String>) roomsMap.get(roomName);

                        if (messageIds != null && !messageIds.isEmpty()) {
                            db.collection("messages")
                                    .whereIn("id", messageIds)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        List<Message> fetched = queryDocumentSnapshots.toObjects(Message.class);
                                        fetched.sort((m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));

                                        messages.clear();
                                        messages.addAll(fetched);
                                        adapter.notifyDataSetChanged();

                                        loadClubTheme();

                                        if (!messages.isEmpty()) {
                                            recyclerView.scrollToPosition(messages.size() - 1);
                                        }
                                    });
                        } else {
                            messages.clear();
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void sendMessage(String text) {
        String msgId = db.collection("messages").document().getId();
        long timestamp = System.currentTimeMillis();
        String roomPath = clubId + "_" + roomName;

        Message msg = new Message(msgId, text, currentUserEmail, timestamp, roomPath);

        db.collection("messages").document(msgId).set(msg)
                .addOnSuccessListener(aVoid -> {
                    String collectionPath = oldhappened ? "oldclub" : "club";
                    db.collection(collectionPath).document(clubId)
                            .update(FieldPath.of("chapters", roomName), FieldValue.arrayUnion(msgId))
                            .addOnSuccessListener(v -> messageInput.setText(""))
                            .addOnFailureListener(e -> {
                                // Ha nem chapter, akkor biztos custom szoba
                                db.collection(collectionPath).document(clubId)
                                        .update(FieldPath.of("customs", roomName), FieldValue.arrayUnion(msgId));
                            });
                });
    }

    private void loadClubTheme() {
        rtdb.child("club_settings").child(clubId).child("theme").get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        clubTheme = snapshot.getValue(String.class);

                        // Csak szólunk az adapternek, nem cseréljük le!
                        adapter.updateTheme(clubTheme);

                        // Háttérkép váltása
                        if (clubTheme != null) {
                            String theme = clubTheme.trim();
                            switch (theme) {
                                case "Romantikus":
                                    chat_background.setImageResource(R.drawable.chat_theme_romance);
                                    break;
                                case "Romantasy":
                                    chat_background.setImageResource(R.drawable.chat_theme_romantasy);
                                    break;
                                default:
                                    chat_background.setImageResource(R.drawable.chat_theme_romance);
                                    break;
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("ChatTheme", "Hiba a téma lekérésekor", e));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (clubId != null) {
            loadClubTheme();
        }
    }
}