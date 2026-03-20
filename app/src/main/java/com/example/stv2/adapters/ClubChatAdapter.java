package com.example.stv2.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.stv2.R;
import com.example.stv2.model.Message;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class ClubChatAdapter extends RecyclerView.Adapter<ClubChatAdapter.VH> {
    private List<Message> list;
    private String myEmail;
    private String currentTheme;
    private Boolean ismoderator;

    public ClubChatAdapter(List<Message> list, String email, String clubTheme, Boolean ismoderator) {
        this.list = list;
        this.myEmail = email;
        this.currentTheme = clubTheme;
        this.ismoderator = ismoderator;
    }

    public void updateTheme(String newTheme) {
        this.currentTheme = newTheme;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Message m = list.get(position);
        boolean isown = m.getUseremail() != null && m.getUseremail().equals(myEmail);

        holder.msg.setText(m.getMessage());
        holder.msg.setVisibility(View.VISIBLE);
        holder.messageImage.setVisibility(View.GONE);
        holder.user.setText("...");

        holder.itemView.setOnLongClickListener(v -> {
            if (isown || ismoderator) {
                android.content.Context context = v.getContext();
                new android.app.AlertDialog.Builder(context)
                        .setTitle("Üzenet törlése")
                        .setMessage("Biztosan törölni szeretnéd ezt az üzenetet?")
                        .setPositiveButton("Törlés", (dialog, which) -> {
                            deleteMessage(m, holder.getBindingAdapterPosition(), context);
                        })
                        .setNegativeButton("Mégse", null)
                        .show();
            }
            return true;
        });

        if (m.getImageUrl() != null && !m.getImageUrl().isEmpty()) {
            holder.messageImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(m.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fitCenter()
                    .placeholder(R.drawable.default_book)
                    .into(holder.messageImage);

            if ("[Kép]".equals(m.getMessage())) {
                holder.msg.setVisibility(View.GONE);
            }
        }

        String themeColor = "#3c0c3e";
        if (currentTheme != null) {
            String theme = currentTheme.trim();
            if (theme.equals("Romantasy")) themeColor = "#14366b";
            else if (theme.equals("Romantikus")) themeColor = "#69216e";
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("email", m.getUseremail())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        if (isown) {
                            holder.user.setText("Én");
                        } else {
                            String username = queryDocumentSnapshots.getDocuments().get(0).getString("username");
                            holder.user.setText(username != null ? username : m.getUseremail());
                        }

                        String url = queryDocumentSnapshots.getDocuments().get(0).getString("profilepicurl");
                        Glide.with(holder.itemView.getContext())
                                .load(url)
                                .placeholder(R.drawable.default_profile)
                                .circleCrop()
                                .into(holder.profilepic);
                    } else {
                        holder.user.setText(isown ? "Én" : m.getUseremail());
                        holder.profilepic.setImageResource(R.drawable.default_profile);
                    }
                });

        holder.user.setTextColor(Color.parseColor("#d9a9db"));
        holder.msg.getBackground().setTint(Color.parseColor(themeColor));
        holder.msg.setTextColor(Color.WHITE);
    }

    private void deleteMessage(Message m, int position, android.content.Context context) {
        if (position == RecyclerView.NO_POSITION) return;

        if (m.getImageUrl() != null && !m.getImageUrl().isEmpty()) {
            FirebaseStorage.getInstance().getReferenceFromUrl(m.getImageUrl()).delete()
                    .addOnFailureListener(e -> Log.e("Delete", "Storage hiba", e));
        }

        FirebaseFirestore.getInstance().collection("messages").document(m.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    if (position < list.size()) {
                        list.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, list.size());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Sikertelen törlés", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView msg, user;
        ImageView profilepic, messageImage;

        VH(View v) {
            super(v);
            msg = v.findViewById(R.id.text_message);
            user = v.findViewById(R.id.text_user);
            profilepic = v.findViewById(R.id.chat_profilpic);
            messageImage = v.findViewById(R.id.chat_message_image);
        }
    }
}