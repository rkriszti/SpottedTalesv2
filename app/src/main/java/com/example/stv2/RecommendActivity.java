package com.example.stv2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RecommendActivity extends MenuActivity {

    private Spinner listSpinner;
    private Button chooseButton;
    private TextView resultText;

    private static final String PREFS_NAME = "profile_cache";
    private static final String IMPORTED_BOOKS_KEY = "imported_books";

    private List<JSONObject> importedBooks = new ArrayList<>();
    private Set<String> uniqueLists = new HashSet<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);
        setupTopMenu();
        setupBottomMenu();

        listSpinner = findViewById(R.id.listSpinner);
        chooseButton = findViewById(R.id.chooseButton);
        resultText = findViewById(R.id.resultText);

        loadImportedBooks();

        if(importedBooks.isEmpty()){
            Toast.makeText(this, "Nincsenek beolvasott könyvek, először importálj CSV-t!", Toast.LENGTH_LONG).show();
            finish(); // vissza a profilra
            return;
        }

        // Spinner feltöltése a listákkal
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>(uniqueLists));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        listSpinner.setAdapter(adapter);

        chooseButton.setOnClickListener(v -> showRandomBook());
    }

    private void loadImportedBooks(){
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String jsonStr = prefs.getString(IMPORTED_BOOKS_KEY, null);
        if(jsonStr != null){
            try {
                JSONArray array = new JSONArray(jsonStr);
                for(int i=0; i<array.length(); i++){
                    JSONObject obj = array.getJSONObject(i);
                    importedBooks.add(obj);

                    JSONArray lists = obj.getJSONArray("lists");
                    for(int j=0; j<lists.length(); j++){
                        uniqueLists.add(lists.getString(j));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void showRandomBook(){
        String selectedList = (String) listSpinner.getSelectedItem();
        if(selectedList == null) return;

        List<String> candidates = new ArrayList<>();
        for(JSONObject book : importedBooks){
            try {
                JSONArray lists = book.getJSONArray("lists");
                for(int i=0; i<lists.length(); i++){
                    if(lists.getString(i).equals(selectedList)){
                        candidates.add(book.getString("title"));
                        break;
                    }
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
        }

        if(candidates.isEmpty()){
            resultText.setText("Nincs könyv ebben a listában!");
        } else {
            Random rand = new Random();
            String chosen = candidates.get(rand.nextInt(candidates.size()));
            resultText.setText(chosen);
        }
    }
}