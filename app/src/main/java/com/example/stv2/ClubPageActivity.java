package com.example.stv2;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stv2.adapters.ClubRoomAdapter;
import com.example.stv2.adapters.HistoryAdapter;
import com.example.stv2.adapters.MembersAdapter;
import com.example.stv2.adapters.ClubChatAdapter;
import com.example.stv2.model.Book;
import com.example.stv2.model.Club;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClubPageActivity extends MenuActivity {
    //globálisan kell
    private Club club, oldclub;
    private String userEmail, bookid, bookbeforechange;
    private Button members, club_delete, club_history, club_active;
    private Boolean ismoderator = false, oldhappened = false;
    String adminEmail, oldbookid, oldclubid;

    //xml részek
    private TextView clubName, clubBookTitle, statusText, clubBookAuthor;
    private EditText clubNameEdit, chaptersEdit,addcustomEdit ;
    private ImageView clubBookCover, clubAdminPic, clubStatusIcon, Settingbutton, club_book_edit;
    private ToggleButton statusChange;
    private FirebaseFirestore db;

    private List<String> pendingUserIds = new ArrayList<>();
 //   private com.google.firebase.database.ValueEventListener pendingListener;

    //elhelyezés
    private RecyclerView chaptersRecycler, customsRecycler;
    private LinearLayout chaptersHeader, customsHeader;

    private Boolean settingIsOn = false, choosingHappened = false;
    private Boolean isAdmin;


    //saját listener
    public interface OnDeleteCustomClickListener {
        void onDeleteClick(String custom, boolean oldh);
    }

    private OnDeleteCustomClickListener deleteListener = new OnDeleteCustomClickListener() {
        @Override
        public void onDeleteClick(String customKey, boolean oldh) {
                if(oldh){
                    if (oldclub != null && oldclub.getCustoms() != null) {

                        oldclub.deleteCustom(customKey);

                        FirebaseFirestore.getInstance()
                                .collection("oldclub")
                                .document(oldclubid)
                                .update("customs", oldclub.getCustoms())
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("ClubPage", "Sikeres törlés: " + customKey);

                                    setupRecycleruniq(customsRecycler, oldclub.getCustoms());
                                })
                                .addOnFailureListener(e -> Log.e("ClubPage", "Hiba a törlésnél", e));


                    }

                } else {
                    if (club != null && club.getCustoms() != null) {
                        club.deleteCustom(customKey);

                        FirebaseFirestore.getInstance()
                                .collection("club")
                                .document(club.getId())
                                .update("customs", club.getCustoms())
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("ClubPage", "Sikeres törlés: " + customKey);

                                    setupRecycleruniq(customsRecycler, club.getCustoms());
                                })
                                .addOnFailureListener(e -> Log.e("ClubPage", "Hiba a törlésnél", e));
                    }
                }
        }
    };

    //saját listener
    public interface OnChooseBookListener {
        void onChoose(String email);
        //CLUB/PROFILE
    }

    private ClubPageActivity.OnChooseBookListener listener = new ClubPageActivity.OnChooseBookListener() {
        @Override
        public void onChoose(String userid) {
                Intent i = new Intent(ClubPageActivity.this, ProfileActivity.class);
                Log.d("choosemember", "profil megkap userid:" + userid );

                i.putExtra("userid", userid);
                startActivity(i);
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clubpage);

        setupBottomMenu(R.id.nav_clubs);
        setupTopMenu();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = user != null ? user.getEmail() : null;
        bookid = "";


        String clubId = getIntent().getStringExtra("clubId");
        //ha clubid-t nem kap leáll!
        if (clubId == null) { finish(); return; }

        String uid = FirebaseAuth.getInstance().getUid();

        oldbookid = "";
        Log.d("ishistory", "clubpageben history ellenőrzés" );
        if(getIntent().getStringExtra("oldbook")!=null
                && !getIntent().getStringExtra("oldbook").isEmpty()){
            Log.d("ChooseBook", "oldbook történt" );

            oldhappened  = true;
            oldbookid = getIntent().getStringExtra("oldbook");
            getIntent().removeExtra("oldbook");
        }

        //moderator e
        if (uid != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            ismoderator = documentSnapshot.getBoolean("admin") != null && documentSnapshot.getBoolean("admin");
                            if (ismoderator && club != null) {
                                isAdmin = true;
                                admin();

                                setupRecycler(chaptersRecycler, club.getChapters());
                                setupRecycleruniq(customsRecycler, club.getCustoms());
                                Log.d("AdminCheck", "Moderátor jog utólag aktiválva.");
                            }
                        }
                    });
        }

        Log.d("ChooseBook", "clubpageben ellenőrzés" );
        if(getIntent().getStringExtra("chosenbook")!=null
                && !getIntent().getStringExtra("chosenbook").isEmpty()){
            //könyv választás történt
            Log.d("ChooseBook", "clubpagenek szóltak hogy kiválasztás történt" );
            bookid = getIntent().getStringExtra("chosenbook");
            choosingHappened = true;
            Log.d("HISTORY", "3. choosing happened, bookid:" + bookid);
            getIntent().removeExtra("chosenbook");
        }

        //button
        Settingbutton = findViewById(R.id.clubsettingon);
        members = findViewById(R.id.club_members);
        club_delete = findViewById(R.id.club_delete);
        club_history = findViewById(R.id.club_history);
        club_active = findViewById(R.id.club_active);

        //edittext
        clubNameEdit = findViewById(R.id.club_name_edittext);
        statusText = findViewById(R.id.club_status_text);
        statusChange = findViewById(R.id.club_status_change);
        chaptersEdit = findViewById(R.id.chapters_edittext);
        addcustomEdit = findViewById(R.id.addcustom_edittext);

        //elemek
        clubName = findViewById(R.id.club_name); //cím
        clubBookTitle = findViewById(R.id.club_book_title);
        clubBookCover = findViewById(R.id.club_book_cover);
        clubBookAuthor = findViewById(R.id.club_book_author);
        clubAdminPic = findViewById(R.id.club_admin_pic);
        clubStatusIcon = findViewById(R.id.club_status_icon);
        club_book_edit = findViewById(R.id.club_book_edit);

        //lecsukáskor ő jeleníti meg a fejezetek
        chaptersRecycler = findViewById(R.id.chapters_recycler);
        customsRecycler = findViewById(R.id.customs_recycler);

        //erre nyomva csukódik le a recycler
        chaptersHeader = findViewById(R.id.chapters_title_parent);
        customsHeader = findViewById(R.id.customs_title_parent);

        //csak ha rányomunk, alapvetően rejtett
        chaptersRecycler.setVisibility(View.GONE);
        customsRecycler.setVisibility(View.GONE);

        //recycler megjelenítés
        chaptersHeader.setOnClickListener(v -> {
            if (chaptersRecycler.getVisibility() == View.GONE) {
                chaptersRecycler.setVisibility(View.VISIBLE);
            } else {
                chaptersRecycler.setVisibility(View.GONE);
            }
        });

        customsHeader.setOnClickListener(v -> {
            if (customsRecycler.getVisibility() == View.GONE) {
                customsRecycler.setVisibility(View.VISIBLE);
            } else {
                customsRecycler.setVisibility(View.GONE);
            }
        });

        club_history.setOnClickListener( k ->{
            Log.d("clubpage", "Rányomtak az előzmény gombra");
            Intent intent = new Intent(ClubPageActivity.this, HistoryActivity.class);
            intent.putExtra("clubId", clubId);
            if(isAdmin){
                intent.putExtra("admin", "true");
            }else {
                intent.putExtra("admin", "false");
            }

           // intent.putExtra("bookId", bookbeforechange);
            startActivity(intent);
        });

        //tagok gomb
        members.setOnClickListener(v -> {
            setContentView(R.layout.activity_clubpage_members);

            getWindow().getDecorView().post(() -> {
                RecyclerView membersRecycler = findViewById(R.id.members_recycler);
                ImageView backButton = findViewById(R.id.club_backbutton);

                if (membersRecycler != null) {
                    membersRecycler.setLayoutManager(new LinearLayoutManager(this));

                    if (club != null && club.getMembers() != null) {
                        MembersAdapter adapter = new MembersAdapter(
                                club.getMembers(),
                                pendingUserIds,
                                listener,
                                club,
                                userEmail
                        );
                        membersRecycler.setAdapter(adapter);
                        loadPendingRequests(club.getId(), adapter);
                    }
                }

                if (backButton != null) {
                    backButton.setOnClickListener(b -> recreate());
                }

                setupBottomMenu(R.id.nav_clubs);
                setupTopMenu();
            });
        });



        loadClub(clubId);
        //is choosing happened -> update book -> loadclub végén (aszinkron)
    }

    private void loadClub(String clubId) {
        Log.d("HISTORY", "4. loadclub indul");
        //lekérjük a club adatait
        FirebaseFirestore.getInstance()
                .collection("club")
                .document(clubId)
                .get()
                .addOnSuccessListener(docc -> {
                    if (!docc.exists()) {
                        return;
                    }

                    club = docc.toObject(Club.class);
                    if (club == null) return;
                    if(oldhappened && club.getBookId().equals(oldbookid)){
                        oldhappened = false;
                    }

                    if(oldhappened){
                        club_active.setText("Régi");
                        club_active.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.RED
                        ));
                    }else {
                        club_active.setText("Aktív");
                        club_active.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#4CAF50")
                        ));
                    }

                    Log.d("LOAD", "lekért klub adatai, bookid: " + club.getBookId());
                    bookbeforechange = club.getBookId();
                    club.setId(docc.getId());
                    adminEmail = club.getAdmin();
                    isAdmin = (userEmail != null && userEmail.equals(adminEmail)) || ismoderator;
                    clubName.setText(club.getName());
                    club.setId(docc.getId());

                    if (choosingHappened) {
                        if (bookid.equals(club.getBookId())) {
                            Log.d("HISTORY", "Ez a könyv már az aktív, nincs mit tenni.");
                            Toast.makeText(this, "Könyvet már olvassák, keresd ez előzményekben!", Toast.LENGTH_SHORT).show();
                            getIntent().removeExtra("chosenbook");
                            choosingHappened = false;
                            recreate();
                            return;
                        }

                        FirebaseFirestore.getInstance().collection("oldclub")
                                .whereEqualTo("id", club.getId())
                                .whereEqualTo("bookId", bookid)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    if (!querySnapshot.isEmpty()) {
                                        Log.d("HISTORY", "Ez a könyv már volt olvasva, csak váltunk rá.");
                                        //updateBook(bookid);
                                        Intent intent = new Intent(ClubPageActivity.this, ClubPageActivity.class);

                                        intent.putExtra("clubId", clubId);
                                        intent.putExtra("oldbook", bookid);

                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        if (club.getBookId().isEmpty()) {
                                            updateBook(bookid);
                                        } else {
                                            saveclubhistory(bookid);
                                        }
                                    }
                                });
                        return;
                    }
                    if(!choosingHappened){
                        bookid = club.getBookId();
                    }
                    club.setBookId(bookid);
                    Log.d("LOAD", "beállítás utáni, bookid: " + club.getBookId());

                    if (isAdmin) {
                        admin();}

                    if (!club.getIspublic() && !club.isMember(userEmail)) {
                        Toast.makeText(this, "Nincs jogosultságod!", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    //státusz beállítás
                    if (club.getIspublic()){
                        statusChange.setBackgroundResource(R.drawable.ic_lock_open);
                    } else {
                        statusChange.setBackgroundResource(R.drawable.ic_lock);
                    }


                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .whereEqualTo("email", adminEmail)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(query -> {

                                if (!query.isEmpty()) {

                                    DocumentSnapshot doc = query.getDocuments().get(0);
                                    String picUrl = doc.getString("profilepicurl");

                                    if (picUrl != null && !picUrl.isEmpty()) {
                                        Glide.with(this)
                                                .load(picUrl)
                                                .circleCrop()
                                                .placeholder(R.drawable.ic_default_avatar)
                                                .error(R.drawable.ic_default_avatar)
                                                .into(clubAdminPic);
                                    } else {
                                        clubAdminPic.setImageResource(R.drawable.ic_default_avatar);
                                    }

                                } else {
                                    clubAdminPic.setImageResource(R.drawable.ic_default_avatar);
                                }
                            })
                            .addOnFailureListener(e ->
                                    clubAdminPic.setImageResource(R.drawable.ic_default_avatar)
                            );
                    if(isAdmin){
                        admin();
                    }

                    // Status icon
                    clubStatusIcon.setImageResource(club.getIspublic() ? R.drawable.ic_lock_open : R.drawable.ic_lock);


                    if(oldhappened){
                        Log.d("HISTORY", "loadclub ishapppeened ok");
                        FirebaseFirestore.getInstance()
                                .collection("oldclub")
                                .whereEqualTo("id", club.getId())
                                .whereEqualTo( "bookId", oldbookid)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(d -> {

                                    if (d.isEmpty()) {
                                        return;
                                    }

                                    oldclub = d.getDocuments().get(0).toObject(Club.class);
                                    if (oldclub == null) return;

                                    DocumentSnapshot doc = d.getDocuments().get(0);
                                    String ddd = doc.getId();
                                    oldclubid = ddd;

                                    setupRecycler(chaptersRecycler, oldclub.getChapters());
                                    setupRecycleruniq(customsRecycler, oldclub.getCustoms());

                                    //loadBookForClub(oldclub, oldbookid);
                                    //ha van könyv beállítva
                                    String currentBookId = oldbookid;
                                    if(currentBookId != null && !currentBookId.isEmpty()) {
                                        FirebaseFirestore.getInstance()
                                                .collection("books")
                                                .document(currentBookId)
                                                .get()
                                                .addOnSuccessListener(dd -> {
                                                    Book b = dd.toObject(Book.class);
                                                    if(b != null) {
                                                        oldclub.setBook(b);
                                                        clubBookCover.setImageResource(R.drawable.background2);
                                                        if(b.getCoverpic() != null)
                                                            Glide.with(this).load(b.getCoverpic()).centerCrop().into(clubBookCover);
                                                        clubBookTitle.setText(b.getTitle());
                                                        clubBookAuthor.setText(b.getAuthor());
                                                    }
                                                });
                                    } else {
                                        clubBookCover.setImageResource(R.drawable.background2);
                                        clubBookTitle.setText("nincs még könyv");
                                        clubBookAuthor.setText("");
                                    }


                                }).addOnFailureListener(e -> {
                                    Log.e("ClubPage", "Hiba a club betöltésénél", e);
                                    finish();
                                });


                      //  setupRecycler(chaptersRecycler, oldclub.getChapters());
                      //  setupRecycleruniq(customsRecycler, oldclub.getCustoms());




                    } else {
                        //aktív klub
                        Log.d("CLUBPAGE", "BOOKID ÜRES?: " + bookid);

                        setupRecycler(chaptersRecycler, club.getChapters());
                        setupRecycleruniq(customsRecycler, club.getCustoms());

                        String currentBookId = "";
                        if (bookid != null && !bookid.isEmpty()) {
                            currentBookId = bookid;
                        } else if (club != null && club.getBookId() != null) {
                            currentBookId = club.getBookId();
                        }else if (club != null && club.getBookId() != null) {
                            currentBookId = club.getBookId();
                        }

                        //ha van könyv beállítva
                         currentBookId = (bookid != null && !bookid.isEmpty()) ? bookid : club.getBookId();
                        if(currentBookId != null && !currentBookId.isEmpty()) {
                            FirebaseFirestore.getInstance()
                                    .collection("books")
                                    .document(currentBookId)
                                    .get()
                                    .addOnSuccessListener(doc -> {
                                        Book b = doc.toObject(Book.class);
                                        if(b != null) {
                                            club.setBook(b);
                                            clubBookCover.setImageResource(R.drawable.background2);
                                            if(b.getCoverpic() != null)
                                                Glide.with(this).load(b.getCoverpic()).centerCrop().into(clubBookCover);
                                            clubBookTitle.setText(b.getTitle());
                                            clubBookAuthor.setText(b.getAuthor());
                                        }
                                    });
                        } else {
                            clubBookCover.setImageResource(R.drawable.background2);
                            clubBookTitle.setText("nincs még könyv");
                            clubBookAuthor.setText("");
                        }

                    }


                    //ADMIN----------------------------------------------------------------------





                }).addOnFailureListener(e -> {
                    Log.e("ClubPage", "Hiba a club betöltésénél", e);
                    finish();
                });
    }

    private void admin(){
        //feltétel hogy már admin, check -> loadclub
        Settingbutton.setVisibility(View.VISIBLE);

        Settingbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!settingIsOn){
                    if(oldhappened){
                        club_delete.setText("Előzmény törlés");
                    }else {
                        club_delete.setText("Klub törlés");
                    }
                    //be kell kapcsolni

                    //státusz változás
                    statusChange.setOnClickListener(a -> {
                        if(club.getIspublic()){
                            //public -> privát

                            //adatbázisban
                            club.setIspublic(false);
                            //kinézetben
                            statusChange.setBackgroundResource(R.drawable.ic_lock);
                            clubStatusIcon.setImageResource(R.drawable.ic_lock);

                        } else {
                            //priv -> public
                            //adatbázisban
                            club.setIspublic(true);
                            //kinézetben
                            statusChange.setBackgroundResource(R.drawable.ic_lock_open);
                            clubStatusIcon.setImageResource(R.drawable.ic_lock_open);
                        }

                        FirebaseFirestore dba = FirebaseFirestore.getInstance();
                        dba.collection("club").document(club.getId())
                                .update("ispublic", club.getIspublic())
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("ClubPage", "Státusz sikeresen frissítve!");

                                })
                                .addOnFailureListener(e -> Log.e("ClubPage", "Mentési hiba", e));
                    });

                    club_delete.setOnClickListener(l -> {
                        if(oldhappened){
                            new AlertDialog.Builder(ClubPageActivity.this)
                                    .setTitle("Előzmény törlése")
                                    .setMessage("Biztosan törölni szeretnéd az előzményt? A klub aktív része megmarad!")
                                    .setPositiveButton("Igen, törlöm", (dialog, which) -> {


                                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                                        DatabaseReference rtdb = FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

                                        firestore.collection("oldclub").document(oldclubid).delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("DeleteClub", "Firestore dokumentum törölve");

                                                    rtdb.child("messages").child(oldclubid).removeValue();
                                                    rtdb.child("pending_requests").child(oldclubid).removeValue();
                                                    rtdb.child("club_members").child(oldclubid).removeValue();

                                                    Toast.makeText(ClubPageActivity.this, "Klub sikeresen törölve!", Toast.LENGTH_SHORT).show();

                                                    Intent intent = new Intent(ClubPageActivity.this, ClubPageActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    intent.putExtra("clubId", club.getId());
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("DeleteClub", "Hiba a törlésnél", e);
                                                    Toast.makeText(ClubPageActivity.this, "Hiba a törlés során!", Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .setNegativeButton("Mégse", null)
                                    .show();
                        } else {
                            new AlertDialog.Builder(ClubPageActivity.this)
                                    .setTitle("Klub törlése")
                                    .setMessage("Biztosan törölni szeretnéd a klubot? Minden üzenet és tartalom véglegesen megsemmisül!")
                                    .setPositiveButton("Igen, törlöm", (dialog, which) -> {

                                        String clubId = club.getId();
                                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                                        com.google.firebase.database.DatabaseReference rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

                                        firestore.collection("club").document(clubId).delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("DeleteClub", "Firestore dokumentum törölve");

                                                    rtdb.child("messages").child(clubId).removeValue();
                                                    rtdb.child("pending_requests").child(clubId).removeValue();
                                                    rtdb.child("club_members").child(clubId).removeValue();

                                                    if (club.getMembers() != null) {
                                                        for (String memberEmail : club.getMembers()) {
                                                            firestore.collection("users")
                                                                    .whereEqualTo("email", memberEmail)
                                                                    .get()
                                                                    .addOnSuccessListener(querySnapshot -> {
                                                                        for (DocumentSnapshot userDoc : querySnapshot) {
                                                                            String memberUid = userDoc.getId();
                                                                            rtdb.child("connections").child(memberUid).child("clubs").child(clubId).removeValue();
                                                                        }
                                                                    });
                                                        }
                                                    }

                                                    Toast.makeText(ClubPageActivity.this, "Klub sikeresen törölve!", Toast.LENGTH_SHORT).show();

                                                    Intent intent = new Intent(ClubPageActivity.this, HomeActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("DeleteClub", "Hiba a törlésnél", e);
                                                    Toast.makeText(ClubPageActivity.this, "Hiba a törlés során!", Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .setNegativeButton("Mégse", null)
                                    .show();
                        }

                    });

                    //fejezetek módosítása
                    addcustomEdit.setVisibility(View.VISIBLE);
                    chaptersEdit.setVisibility(View.VISIBLE);

                    club_delete.setVisibility(View.VISIBLE);

                    //név edittext megjelent
                    clubName.setVisibility(View.GONE);
                    clubNameEdit.setVisibility(View.VISIBLE);

                    //érték beállítása
                    clubNameEdit.setText(clubName.getText().toString());

                    //publikusság
                    clubStatusIcon.setVisibility(View.GONE);
                    statusChange.setVisibility(View.VISIBLE);
                    statusText.setVisibility(View.VISIBLE);

                    //book picking for club
                    club_book_edit.setVisibility(View.VISIBLE);
                    club_book_edit.setOnClickListener(k -> {
                        Log.d("ChooseBook", "rányomtak a bookchoose gombra, irány a search" );
                        Intent i = new Intent(ClubPageActivity.this, SearchActivity.class);
                        i.putExtra("choose", "true");
                        i.putExtra("clubid", club.getId());
                        startActivity(i);
                    });

                    //mentés gomb lesz
                    Settingbutton.setImageResource(R.drawable.ic_save);
                    settingIsOn = true;

                    //egyből frissítés
                    if(oldhappened){
                        setupRecycler(chaptersRecycler, oldclub.getChapters());
                        setupRecycleruniq(customsRecycler, oldclub.getCustoms());
                    } else {
                        setupRecycler(chaptersRecycler, club.getChapters());
                        setupRecycleruniq(customsRecycler, club.getCustoms());
                    }


                } else {
                    //MENTENEK

                    //klub cím változás mentése
                    if(!clubName.getText().toString().equals(clubNameEdit.getText().toString())){
                        FirebaseFirestore database = FirebaseFirestore.getInstance();

                        database.collection("club").document(club.getId())
                                .update("name", clubNameEdit.getText().toString())
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("ClubPage", "Név sikeresen frissítve!");
                                    clubName.setText(clubNameEdit.getText().toString());
                                    club.setName(clubNameEdit.getText().toString());
                                })
                                .addOnFailureListener(e -> Log.e("ClubPage", "Mentési hiba", e));
                    }

                    //hány fejezet legyen
                    if(getEditTextNumber(chaptersEdit) > 0 &&
                            getEditTextNumber(chaptersEdit)!= club.getChaptersSize()){
                        if(oldhappened){
                            oldclub.setChapters(getEditTextNumber(chaptersEdit));

                            //hány fejezet
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("oldclub").document(oldclubid)
                                    .update("chapters", oldclub.getChapters())
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("ClubPage", "Fejezetek száma sikeresen frissítve Firestore-ban!");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("ClubPage", "Hiba a fejezetek mentésénél", e);
                                    });

                        } else {
                            club.setChapters(getEditTextNumber(chaptersEdit));

                            //hány fejezet
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("club").document(club.getId())
                                    .update("chapters", club.getChapters())
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("ClubPage", "Fejezetek száma sikeresen frissítve Firestore-ban!");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("ClubPage", "Hiba a fejezetek mentésénél", e);
                                    });

                        }

                    }

                    //custom fejezet hozzáadás
                    if(!addcustomEdit.getText().toString().isEmpty()){
                        club.setCustom(addcustomEdit.getText().toString());

                        if(oldhappened){
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("oldclub").document(oldclubid)
                                    .update("customs", oldclub.getCustoms())
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("ClubPage", "Custom száma sikeresen frissítve Firestore-ban!");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("ClubPage", "Hiba a Custom mentésénél", e);
                                    });
                        } else {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("club").document(club.getId())
                                    .update("customs", club.getCustoms())
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("ClubPage", "Custom száma sikeresen frissítve Firestore-ban!");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("ClubPage", "Hiba a Custom mentésénél", e);
                                    });
                        }



                    }


                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    if (oldhappened){
                        db.collection("oldclub").document(oldclubid)
                                .update("customs", oldclub.getCustoms()) // A módosított Map mentése
                                .addOnSuccessListener(aVoid -> Log.d("ClubPage", "Egyedi szobák sikeresen frissítve!"))
                                .addOnFailureListener(e -> Log.e("ClubPage", "Hiba a mentésnél", e));

                    } else {
                        db.collection("club").document(club.getId())
                                .update("customs", club.getCustoms()) // A módosított Map mentése
                                .addOnSuccessListener(aVoid -> Log.d("ClubPage", "Egyedi szobák sikeresen frissítve!"))
                                .addOnFailureListener(e -> Log.e("ClubPage", "Hiba a mentésnél", e));

                    }


                    //név edittext megjelent
                    clubName.setVisibility(View.VISIBLE);
                    clubNameEdit.setVisibility(View.GONE);
                    chaptersEdit.setVisibility(View.GONE);
                    addcustomEdit.setVisibility(View.GONE);

                    club_delete.setVisibility(View.GONE);

                    //újra setting gomb lesz
                    Settingbutton.setImageResource(R.drawable.ic_setting);
                    settingIsOn = false;

                    //publikusság
                    clubStatusIcon.setVisibility(View.VISIBLE);
                    statusChange.setVisibility(View.GONE);
                    statusText.setVisibility(View.GONE);

                    club_book_edit.setVisibility(View.GONE);
                    //frissítés
                    setupRecycler(chaptersRecycler, club.getChapters());
                    setupRecycleruniq(customsRecycler, club.getCustoms());
                }
            }
        });
    }

    private void setupRecycler(RecyclerView recyclerView, Map<String, List<String>> data) {
        List<String> titles = new ArrayList<>(data.keySet());
        RecyclerView.Adapter adapter = new ClubRoomAdapter(titles, data, isAdmin, settingIsOn, false, deleteListener, club.getId());
        if(oldhappened){
             adapter = new ClubRoomAdapter(titles, data, isAdmin, settingIsOn, false, deleteListener, club.getId(), oldclubid);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupRecycleruniq(RecyclerView recyclerView, Map<String, List<String>> data) {
        List<String> titles = new ArrayList<>(data.keySet());
        RecyclerView.Adapter adapter = new ClubRoomAdapter(titles, data, isAdmin, settingIsOn, true, deleteListener, club.getId());
        if(oldhappened){
             adapter = new ClubRoomAdapter(titles, data, isAdmin, settingIsOn, true, deleteListener, club.getId(), oldclubid);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    public int getEditTextNumber(EditText editText) {
        if (editText == null) return 0;

        String text = editText.getText().toString().trim();

        if (text.isEmpty()) return 0;

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ellenőrizzük, hogy van-e új bookid
        String newBookId = getIntent().getStringExtra("chosenbook");
        if (newBookId != null && !newBookId.equals(bookid) && !newBookId.isEmpty()) {
            bookid = newBookId;
            updateBook(bookid);
        }
    }

    private void updateBook(String bookId) {
        choosingHappened = false;
        getIntent().removeExtra("chosenbook");

        if (club == null || club.getId() == null) {
            Log.e("ClubPage", "Még nem töltött be a klub, nem tudok menteni!");
            return;
        }
        Log.d("ClubPage", "update started" );
        FirebaseFirestore.getInstance()
                .collection("books")
                .document(bookId)
                .get()
                .addOnSuccessListener(doc -> {

                    Log.d("ClubPage", "könyv lekérése" );
                    Log.d("ClubPage", "doc.exists(): " + doc.exists());
                    Log.d("ClubPage", "doc data: " + doc.getData());
                    Book b = doc.toObject(Book.class);
                    Log.d("ClubPage", "Book: " + b);

                    if(doc.toObject(Book.class)==null || !doc.exists()){
                        Log.d("ClubPage", "ilyen doc nincs" );
                    }
                    if (doc.exists() && b != null && club != null) {
                        Log.d("ClubPage", "nem null semmi" );
                        club.setBook(b);
                        club.setBookId(bookId);
                        club.setAllChapters(new HashMap<>()); //ürít
                        club.setAllCustom(new HashMap<>());   //ürít

                        // UI frissítés
                        clubBookCover.setImageResource(R.drawable.background2);
                        if(b.getCoverpic()!=null)
                            Glide.with(this).load(b.getCoverpic()).centerCrop().into(clubBookCover);
                        clubBookTitle.setText(b.getTitle());
                        clubBookAuthor.setText(b.getAuthor());
                        Log.d("ClubPage", "ui frissít" );

                        Map<String, List<String>> x = new HashMap<>();
                        Map<String, List<String>> y = new HashMap<>();

                        // Firestore frissítés
                        FirebaseFirestore.getInstance()
                                .collection("club")
                                .document(club.getId())
                                .update("bookId", bookId, "chapters", x, "customs", y)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("ClubPage", "Mentés kész, oldal újratöltése");
                                    recreate();
                                })
                                .addOnFailureListener(e -> Log.e("ClubPage", "Hiba a club könyv frissítésénél", e));



                    }

                });
    }

    private void saveclubhistory(String bookid){
        Log.d("HISTORY", "-1, előzmény jön létre");
        //oldclub kollekcióhoz adás
        Club old = new Club();
        old.setId(club.getId()); //kapcsolat
        Log.d("HISTORY", "-1, előzmény id = mostani id: " + old.getId());
        old.setBookId(club.getBookId());
        if(choosingHappened){
            old.setBookId(bookbeforechange);
            Log.d("HISTORY", "-1, choosehappened");
        }
        Log.d("HISTORY", "-1, old bookid" + old.getBookId());
        //kell setbook simán?
        old.setAllChapters(club.getChapters());
        old.setAllCustom(club.getCustoms());

        db = FirebaseFirestore.getInstance();
        String msgId = db.collection("oldclub").document().getId();
        Log.d("HISTORY", "-1,oldclub id" + msgId);

        //1. mentés új kollekcióba
        db.collection("oldclub").document(msgId).set(old)
                .addOnSuccessListener(aVoid -> {
                    Log.d("history, clubpage", "Előzmény ok: " );
                    Toast.makeText(ClubPageActivity.this, "Klub sikeresen feltöltve előzményként!", Toast.LENGTH_SHORT).show();

                    // 2. üzenetek módosítása
                    db.collection("messages")
                            .whereGreaterThanOrEqualTo("roomPath", club.getId() + "_")
                            .whereLessThanOrEqualTo("roomPath", club.getId() + "_\uf8ff")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                String oldPrefix = club.getId() + "_"; // amit keresünk
                                String newPrefix = msgId + "_";       // amire cseréljük (az oldId)

                                for (QueryDocumentSnapshot doc : querySnapshot) {
                                    String currentPath = doc.getString("roomPath");

                                    if (currentPath != null && currentPath.startsWith(oldPrefix)) {
                                        String updatedPath = currentPath.replaceFirst(oldPrefix, newPrefix);

                                        doc.getReference().update("roomPath", updatedPath)
                                                .addOnSuccessListener(v -> Log.d("history", "Üzenet átrakva: " + doc.getId()));
                                    }
                                }
                            })
                            .addOnFailureListener(e -> Log.e("history", "Hiba az üzenetek frissítésekor", e));

                    //3. új könyv megkezdése, chat ürítése
                    updateBook(bookid);
                })
                    .addOnFailureListener(e -> {
                        Log.e("history, clubpage", e.getMessage(), e);
                        Toast.makeText(ClubPageActivity.this, "Hiba a feltöltés során: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });


    }

    private void loadPendingRequests(String clubId, MembersAdapter adapter) {
        com.google.firebase.database.FirebaseDatabase.getInstance("https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("pending_requests")
                .child(clubId)
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        pendingUserIds.clear();
                        for (com.google.firebase.database.DataSnapshot ds : snapshot.getChildren()) {
                            pendingUserIds.add(ds.getKey()); // Csak a User ID-kat gyűjtjük ki
                        }
                        // Ha az adapter már létezik, frissítjük a nézetet
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                        Log.e("ClubPage", "Hiba a pending lekérésénél", error.toException());
                    }
                });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);


            recreate();
        }
    }
