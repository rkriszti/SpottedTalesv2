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
    private ImageView chat_backbutton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setupBottomMenu();
        setupTopMenu();

        db = FirebaseFirestore.getInstance();
        clubId = getIntent().getStringExtra("clubId");
        roomName = getIntent().getStringExtra("roomName");
        oldhappened = getIntent().getStringExtra("isOldChat").equals("true");
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        recyclerView = findViewById(R.id.chat_recycler);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        chat_backbutton = findViewById(R.id.chat_backbutton);

        adapter = new ClubChatAdapter(messages, currentUserEmail);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


        loadMessages();


        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (!text.isEmpty()) sendMessage(text);
        });

        chat_backbutton.setOnClickListener( k -> {
            finish();
        });
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
                                        if (!messages.isEmpty()) recyclerView.scrollToPosition(messages.size() - 1);
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

                    db.collection(collectionPath).document(clubId).get().addOnSuccessListener(doc -> {
                        if (!doc.exists()) return;

                        String targetMap = "chapters";
                        Map<String, Object> chapters = (Map<String, Object>) doc.get("chapters");

                        if (chapters == null || !chapters.containsKey(roomName)) {
                            targetMap = "customs";
                        }

                        db.collection(collectionPath).document(clubId)
                                .update(FieldPath.of(targetMap, roomName), FieldValue.arrayUnion(msgId))
                                .addOnSuccessListener(v -> messageInput.setText(""))
                                .addOnFailureListener(e -> Log.e("Chat", "Hiba az ID mentésénél", e));
                    });
                });
    }


}