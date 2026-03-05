package com.example.stv2.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stv2.R;
import com.example.stv2.ProfileActivity;
import com.example.stv2.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.VH> {

    private List<User> users = new ArrayList<>();

    public void setUsers(List<User> list) {
        users = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        User u = users.get(pos);

        h.username.setText(u.getUsername());
        h.pic.setImageResource(R.drawable.background2);

        h.button.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("username", u.getUsername())
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String realId = querySnapshot.getDocuments().get(0).getId();
                            Intent i = new Intent(v.getContext(), ProfileActivity.class);
                            i.putExtra("userid", realId);
                            v.getContext().startActivity(i);
                        }
                    });
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView username;
        ImageView pic;
        Button button;

        VH(View v) {
            super(v);
            username = v.findViewById(R.id.menu_username);
            pic = v.findViewById(R.id.menu_userpic);
            button = v.findViewById(R.id.user_button);
        }
    }
}
