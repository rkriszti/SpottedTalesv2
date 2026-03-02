package com.example.stv2;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stv2.adapters.ClubChatAdapter;
import com.example.stv2.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends MenuActivity {
    private String clubId, roomName, currentUserEmail;
    private RecyclerView recyclerView;
    private ClubChatAdapter adapter;
    private List<Message> messages = new ArrayList<>();
    private EditText messageInput;
    private ImageButton sendButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setupTopMenu();
        setupBottomMenu();

        db = FirebaseFirestore.getInstance();
        clubId = getIntent().getStringExtra("clubId");
        roomName = getIntent().getStringExtra("roomName");
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        recyclerView = findViewById(R.id.chat_recycler);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);

        adapter = new ClubChatAdapter(messages, currentUserEmail);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadMessages();

        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (!text.isEmpty()) sendMessage(text);
        });
    }

    private void loadMessages() {
        // Kollekció útvonala: clubs -> {clubId} -> rooms -> {roomName} -> messages
        db.collection("clubs").document(clubId)
                .collection("rooms").document(roomName)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        messages.clear();
                        messages.addAll(value.toObjects(Message.class));
                        adapter.notifyDataSetChanged();
                        if (!messages.isEmpty()) recyclerView.scrollToPosition(messages.size() - 1);
                    }
                });
    }

    private void sendMessage(String text) {
        String msgId = db.collection("clubs").document(clubId).collection("rooms").document(roomName).collection("messages").document().getId();
        Message msg = new Message(msgId, text, currentUserEmail, System.currentTimeMillis());

        db.collection("clubs").document(clubId)
                .collection("rooms").document(roomName)
                .collection("messages").document(msgId)
                .set(msg)
                .addOnSuccessListener(aVoid -> messageInput.setText(""));
    }
}