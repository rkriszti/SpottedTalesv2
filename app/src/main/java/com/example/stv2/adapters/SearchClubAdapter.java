package com.example.stv2.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stv2.ClubPageActivity;
import com.example.stv2.R;
import com.example.stv2.model.Club;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchClubAdapter extends RecyclerView.Adapter<SearchClubAdapter.VH> {

    private List<Club> clubs = new ArrayList<>();
    private String useremail;
    private boolean isUserMember = false, isClubPublic = false;

    public void setClubs(List<Club> list, String email) {
        clubs = list;
        useremail = email;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_club, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();

        if (currentUserId == null) return;

        Club c = clubs.get(pos);

        h.name.setText(c.getName());
        h.members.setText(String.valueOf(c.getMembers().size()));
        h.pic.setImageResource(R.drawable.background2);

        isClubPublic = c.getIspublic();
        isUserMember = c.isMember(useremail);

        int type;
        if (isUserMember){
            //ha tagja akkor mindegy hogy publik vagy sem
            h.button.setText("Tovább");
            type = 1;
        } else {
            //ha nem tag
            if(isClubPublic){
                h.button.setText("Csatlakozás");
                type = 2;
            } else {
                //privát
                h.button.setText("Kérelem");
                type = 3;
            }
        }

        h.button.setOnClickListener(v -> {
            switch (type){
                case 1 :
                    Intent i = new Intent(v.getContext(), ClubPageActivity.class);
                    i.putExtra("clubId", c.getId());
                    v.getContext().startActivity(i);
                    break;
                case 2:
                    //belép
                    c.addMember(useremail);

                    FirebaseFirestore.getInstance()
                            .collection("club")
                            .document(c.getId())
                            .update("members", FieldValue.arrayUnion(useremail))
                            .addOnSuccessListener(aVoid -> {

                                // Realtime DB update
                                String userId = FirebaseAuth.getInstance().getUid();
                                FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/")
                                        .getReference("connections")
                                        .child(userId)
                                        .child("clubs")
                                        .child(c.getId())
                                        .setValue(true); // vagy a szükséges info


                                Intent ii = new Intent(v.getContext(), ClubPageActivity.class);
                                ii.putExtra("clubId", c.getId());
                                v.getContext().startActivity(ii);

                            })
                            .addOnFailureListener(e ->
                                    Log.e("FIRESTORE", "Nem sikerült a csatlakozás", e)
                            );


                    Intent ii = new Intent(v.getContext(), ClubPageActivity.class);
                    ii.putExtra("clubId", c.getId());
                    v.getContext().startActivity(ii);
                    break;
                case 3:
                    com.google.firebase.database.DatabaseReference dbRef =
                            com.google.firebase.database.FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/")
                                    .getReference("pending_requests");

                    //pending_requests -> clubId -> userId : true
                    dbRef.child(c.getId()).child(currentUserId).setValue(true)
                            .addOnSuccessListener(aVoid -> {
                                h.button.setText("Várakozás");
                                h.button.setEnabled(false);
                                Log.d("searchclub", "kérelem elküldve");
                                android.widget.Toast.makeText(v.getContext(), "Kérelem elküldve!", android.widget.Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                android.util.Log.e("RTDB", "Hiba a kérelemnél", e);
                            });
                    break;
            }

        });
    }

    @Override
    public int getItemCount() {
        return clubs.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, members;
        ImageView pic;
        Button button;

        VH(View v) {
            super(v);
            name = v.findViewById(R.id.menu_clubname);
            members = v.findViewById(R.id.menu_clubtags);
            pic = v.findViewById(R.id.menu_clubpic);
            button = v.findViewById(R.id.clubs_button);
        }
    }
}
