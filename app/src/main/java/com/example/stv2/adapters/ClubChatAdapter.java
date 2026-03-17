package com.example.stv2.adapters;

import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stv2.R;
import com.example.stv2.model.Message;
import java.util.List;

public class ClubChatAdapter extends RecyclerView.Adapter<ClubChatAdapter.VH> {
    private List<Message> list;
    private String myEmail;

    public ClubChatAdapter(List<Message> list, String email) {
        this.list = list;
        this.myEmail = email;
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
        holder.msg.setText(m.getMessage());


        if (m.getUseremail() != null && m.getUseremail().equals(myEmail)) {
            holder.user.setText("Én");
            holder.user.setTextColor(Color.parseColor("#3c0c3e"));

            holder.msg.getBackground().setTint(Color.parseColor("#3c0c3e"));
            holder.msg.setTextColor(Color.WHITE);
            holder.delete.setVisibility(View.VISIBLE);
        } else {
            holder.user.setText(m.getUseremail());

            holder.user.setTextColor(Color.parseColor("#888888"));

            holder.msg.getBackground().setTint(Color.parseColor("#E8E8E8"));
            holder.msg.setTextColor(Color.BLACK);
            holder.delete.setVisibility(View.GONE);
        }

        holder.delete.setOnClickListener(k -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            Message mToDelete = list.get(pos);

            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("messages")
                    .document(mToDelete.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // UI törlés
                        list.remove(pos);
                        notifyItemRemoved(pos);
                        notifyItemRangeChanged(pos, list.size());
                        android.util.Log.d("CHAT_DELETE", "Sikeresen törölve Firestore-ból!");
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("CHAT_DELETE", "Hiba a törlésnél: " + e.getMessage());
                    });
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView msg, user;
        ImageView delete;
        VH(View v) {
            super(v);
            msg = v.findViewById(R.id.text_message);
            user = v.findViewById(R.id.text_user);
            delete = v.findViewById(R.id.chat_delete);
        }
    }
}