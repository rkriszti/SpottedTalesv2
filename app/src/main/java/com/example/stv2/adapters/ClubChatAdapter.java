package com.example.stv2.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        // Az üzenet buborék layoutját fújjuk fel
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Message m = list.get(position);
        holder.msg.setText(m.getMessage());

        // Logika: Színváltás a küldő alapján, de minden balra marad
        if (m.getUseremail() != null && m.getUseremail().equals(myEmail)) {
            holder.user.setText("Én");
            holder.user.setTextColor(Color.parseColor("#3c0c3e"));
            // Lila buborék, fehér szöveg neked
            holder.msg.getBackground().setTint(Color.parseColor("#3c0c3e"));
            holder.msg.setTextColor(Color.WHITE);
        } else {
            holder.user.setText(m.getUseremail());

            holder.user.setTextColor(Color.parseColor("#888888"));

            holder.msg.getBackground().setTint(Color.parseColor("#E8E8E8"));
            holder.msg.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView msg, user;
        VH(View v) {
            super(v);
            msg = v.findViewById(R.id.text_message);
            user = v.findViewById(R.id.text_user);
        }
    }
}