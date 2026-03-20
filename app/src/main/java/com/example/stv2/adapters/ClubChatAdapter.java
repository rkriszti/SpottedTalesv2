package com.example.stv2.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.stv2.R;
import com.example.stv2.model.Message;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ClubChatAdapter extends RecyclerView.Adapter<ClubChatAdapter.VH> {
    private List<Message> list;
    private String myEmail;
    private String currentTheme;
    private Boolean ismoderator = false;

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
        holder.msg.setText(m.getMessage());
        boolean isown = m.getUseremail() != null && m.getUseremail().equals(myEmail);;

        if (m.getUseremail() != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("email", m.getUseremail())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String url = queryDocumentSnapshots.getDocuments().get(0).getString("profilepicurl");
                            Glide.with(holder.itemView.getContext())
                                    .load(url)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(R.drawable.default_profile)
                                    .circleCrop()
                                    .into(holder.profilepic);
                        } else {
                            holder.profilepic.setImageResource(R.drawable.default_profile);
                        }
                    });
        }

        String themeColor = "#3c0c3e";
        if (currentTheme != null) {
            if (currentTheme.trim().equals("Romantasy")) {
                themeColor = "#14366b";
            } else if (currentTheme.trim().equals("Romantikus")) {
                themeColor = "#69216e";
            }
        }

            Log.d("Chat", themeColor);
        if (m.getUseremail() != null && m.getUseremail().equals(myEmail)) {
            holder.user.setText("Én");
            holder.user.setTextColor(Color.parseColor("#d9a9db"));
            holder.msg.getBackground().setTint(Color.parseColor(themeColor));
            holder.msg.setTextColor(Color.WHITE);
            //holder.delete.setVisibility(View.VISIBLE);
        } else {
            holder.user.setText(m.getUseremail());
            holder.user.setTextColor(Color.parseColor("#d9a9db"));
            holder.msg.getBackground().setTint(Color.parseColor(themeColor));
            holder.msg.setTextColor(Color.WHITE);
            holder.delete.setVisibility(View.GONE);
        }
        holder.egyeb.setOnClickListener( l -> {
            if (isown ||ismoderator){
                holder.delete.setVisibility(View.VISIBLE);
            }
            holder.egyeb.setVisibility(View.GONE);
            holder.egyebhide.setVisibility(View.VISIBLE);
        });
        holder.egyebhide.setOnClickListener( ll->{
            holder.delete.setVisibility(View.GONE);
            holder.egyeb.setVisibility(View.VISIBLE);
            holder.egyebhide.setVisibility(View.GONE);
        });


        holder.delete.setOnClickListener(k -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            Message mToDelete = list.get(pos);
            FirebaseFirestore.getInstance()
                    .collection("messages")
                    .document(mToDelete.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        list.remove(pos);
                        notifyItemRemoved(pos);
                        notifyItemRangeChanged(pos, list.size());
                    });
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView msg, user;
        ImageView delete, profilepic, egyeb, egyebhide;
        VH(View v) {
            super(v);
            msg = v.findViewById(R.id.text_message);
            user = v.findViewById(R.id.text_user);
            delete = v.findViewById(R.id.chat_delete);
            profilepic = v.findViewById(R.id.chat_profilpic);
            egyeb = v.findViewById((R.id.chat_egyeb_start));
            egyebhide = v.findViewById((R.id.chat_egyeb_hide));
        }
    }
}