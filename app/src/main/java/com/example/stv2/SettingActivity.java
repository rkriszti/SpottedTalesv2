package com.example.stv2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.List;


public class SettingActivity extends MenuActivity{
    private FirebaseFirestore firestore;
    private DatabaseReference rtDb;
    private ImageView back;
    private AutoCompleteTextView autocomplete_tema;
    private com.google.android.material.textfield.TextInputLayout tema;
    private Button lang_english, lang_magyar;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setupBottomMenu(null);
        setupTopMenu();

        firestore = FirebaseFirestore.getInstance();
        String dbUrl = "https://stv2-84ad0-default-rtdb.europe-west1.firebasedatabase.app/";
        rtDb = FirebaseDatabase.getInstance(dbUrl).getReference();
        String currentUid = FirebaseAuth.getInstance().getUid();

        autocomplete_tema = findViewById(R.id.autocomplete_tema);
        back = findViewById(R.id.backbutton);
        tema = findViewById(R.id.tema);
        lang_english= findViewById(R.id.lang_english);
        lang_magyar = findViewById(R.id.lang_magyar);

Theme();
        String[] themes = getResources().getStringArray(R.array.app_themes);
        ArrayAdapter<String> adaptere = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, themes);
        autocomplete_tema.setAdapter(adaptere);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String mentettTema = prefs.getString("selected_theme", "Purple");

        SharedPreferences prefss = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String mentettTemaa = prefss.getString("language", "magyar");
        boolean lang = mentettTemaa.equals("magyar");
        boolean theme = mentettTema.endsWith("Purple");

        int a =  ContextCompat.getColor(this, R.color.grey);
        int b = ContextCompat.getColor(this, R.color.blue);
        int c = ContextCompat.getColor(this, R.color.bordo);

        lang_english.setBackgroundColor( lang ? a : (theme ? c : b));
        lang_magyar.setBackgroundColor(lang ? (theme ? c : b) : a);


        //nyelvek
        lang_english.setOnClickListener(k->{

            int color = mentettTema.endsWith("Blue") ? R.color.blue  : R.color.bordo;

            lang_english.setBackgroundColor(ContextCompat.getColor(this, color));
            lang_magyar.setBackgroundColor(ContextCompat.getColor(this, R.color.grey));

            rtDb.child("settings").child(currentUid).child("language")
                    .setValue("english")
                    .addOnSuccessListener(aVoid -> {
                        getSharedPreferences("AppPrefs", MODE_PRIVATE)
                                .edit()
                                .putString("language", "english")
                                .apply();
                        Toast.makeText(this, "Language setting have changed.", Toast.LENGTH_SHORT).show();
                    });

        });

        lang_magyar.setOnClickListener(k->{

            int color = mentettTema.endsWith("Blue") ? R.color.blue  : R.color.bordo;

            lang_magyar.setBackgroundColor(ContextCompat.getColor(this, color));
            lang_english.setBackgroundColor(ContextCompat.getColor(this, R.color.grey));

            rtDb.child("settings").child(currentUid).child("language")
                    .setValue("magyar")
                    .addOnSuccessListener(aVoid -> {
                        getSharedPreferences("AppPrefs", MODE_PRIVATE)
                                .edit()
                                .putString("language", "magyar")
                                .apply();
                        Toast.makeText(this, "Nyelv sikeresen átállítva.", Toast.LENGTH_SHORT).show();
                    });

        });

        //téma
        autocomplete_tema.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTheme = autocomplete_tema.getText().toString();
            autocomplete_tema.setText(selectedTheme, false);


            if(!selectedTheme.isEmpty() && !selectedTheme.equals("Választás...")){
                rtDb.child("settings").child(currentUid).child("theme")
                        .setValue(selectedTheme)
                        .addOnSuccessListener(aVoid -> {
                            // SharedPreferences mentés
                            getSharedPreferences("AppPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("selected_theme", selectedTheme)
                                    .apply();
                            Toast.makeText(this, "Téma módosítva erre: " + selectedTheme, Toast.LENGTH_SHORT).show();
                        });

            }
        });

        /*

        String selectedTheme = autocomplete_tema.getText().toString();

        if (!selectedTheme.isEmpty() &&
                !selectedTheme.equals("Válassz egy témát...") &&
                !selectedTheme.equals(currentSavedTheme)) {

            rtdb.child("club_settings").child(club.getId()).child("theme")
                    .setValue(selectedTheme)
                    .addOnSuccessListener(aVoid -> {
                        currentSavedTheme = selectedTheme;
                        setBackgroundTheme(currentSavedTheme, false);
                        Log.d("RTDB", "Új téma mentve: " + selectedTheme);
                    });
        }
*/

        back.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }


    private void Theme(){
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String mentettTema = prefs.getString("selected_theme", "Purple");

        ConstraintLayout layout = findViewById(R.id.layout);
        layout.setBackgroundResource(mentettTema.equals("Blue") ? R.drawable.img_3_blue : R.drawable.background_new);

        TextView textView4 = findViewById(R.id.textView4);
        TextView textView6  = findViewById(R.id.textView6);

        int szinId = mentettTema.equals("Blue") ? R.color.lightblue : R.color.lightlighttbordo;
        textView4.setTextColor(ContextCompat.getColor(this, szinId));
        textView6.setTextColor(ContextCompat.getColor(this, szinId));

    }

}
