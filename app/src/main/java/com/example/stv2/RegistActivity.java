package com.example.stv2;

import android.content.Intent;
import android.credentials.GetCredentialRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stv2.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.annotation.NonNull;
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;


import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class RegistActivity extends AppCompatActivity {

    //regisztrációhoz-------------------------------------------------
    private FirebaseAuth auth= FirebaseAuth.getInstance(); //ezen keresztül érjük a fb függvényeket
    FirebaseFirestore store = FirebaseFirestore.getInstance(); //doksi írás, olvasás
    FirebaseUser fuser; //regisztrélt user obj


    private static final int RC_SIGN_IN = 1001;
    //private GoogleSignInClient googleSignInClient;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);


        //offline
        /*FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        store.setFirestoreSettings(settings);*/


       /* store.collection("debug_test").document("testdoc2")
                .set(new HashMap<String, Object>() {{ put("hello", "world"); }})
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) Log.d("Firestore", "Debug doc létrejött");
                    else Log.e("Firestore", "Debug doc hiba", task.getException());
                });*/


        //Regist oldalon Van már fiókom gombra kattintva a Login oldalra dob
        TextView skip = findViewById(R.id.registtologin);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistActivity.this, com.example.stv2.MainActivity.class);
                startActivity(intent);
            }
        });

        //----------------------------------------------------------------------------------
        //Regist oldalon Regisztráció gombra kattintva a Openapp oldalra dob
        Button registbutton = findViewById(R.id.registbutton);

        registbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Firestore", "ProjectID: " + FirebaseFirestore.getInstance().getApp().getOptions().getProjectId());

                Log.d("RegistActivity", "Regist gombra nyomtak");

                //Regisztráció gombra nyomva
                //beadott adatok
                EditText usernameE = findViewById(R.id.registusername);
                EditText passwordE = findViewById(R.id.registpassword);
                EditText passwordagainE = findViewById(R.id.registpasswordagain);
                EditText emailE = findViewById(R.id.registemail);

                //hogy tudjunk dolgozni velük
                String username = usernameE.getText().toString();
                String password= passwordE.getText().toString();
                String passwordagain= passwordagainE.getText().toString();
                String email= emailE.getText().toString();

                //ellenőrzések
                if (!password.equals(passwordagain)) {
                    Toast.makeText(RegistActivity.this, "A jelszavak nem egyeznek!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                    Toast.makeText(RegistActivity.this, "Tölts ki minden mezőt!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.length() < 6) {
                    Toast.makeText(RegistActivity.this, "A jelszónak legalább 6 karakter hosszúnak kell lennie!", Toast.LENGTH_SHORT).show();
                    return;
                }

                /// username email hiba kiirása

                //firebase regisztráció
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegistActivity.this, task -> {
                            if (task.isSuccessful()) {
                                Log.d("RegistActivity", "FirebaseAuth regisztráció sikeres");

                                FirebaseUser fuser = auth.getCurrentUser();
                                if (fuser == null) {
                                    Toast.makeText(RegistActivity.this, "Hiba: nincs bejelentkezett felhasználó", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Log.d("RegistActivity", "fuser nem üres");

                                String uid = fuser.getUid();
                                User user = new User(username, email);

                                // 🔹 Külön lépés: Firestore mentés
                                user.saveToFirestore(uid,
                                        () -> { // onSuccess
                                            Toast.makeText(RegistActivity.this, "Sikeres regisztráció!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(RegistActivity.this, OpenAppActivity.class));
                                        },
                                        () -> { // onFailure
                                            Toast.makeText(RegistActivity.this, "Hiba a Firestore mentéskor", Toast.LENGTH_SHORT).show();
                                        }
                                );

                            } else {
                                Log.e("RegistActivity", "Auth regisztráció sikertelen", task.getException());
                                Toast.makeText(RegistActivity.this, "Regisztráció sikertelen: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


            }

        });

        //--------------------------------------------------------------------------------------
        /// google regisztrációra nyom
     /* Button registgooglebut = findViewById(R.id.registbuttongoogle);

        registgooglebut.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                auth = FirebaseAuth.getInstance();
                // 1️⃣ Google belépés beállítás
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id)) // ez a google-services.json-ból jön
                        .requestEmail()
                        .build();

                // client indítja a reg ablakot
                googleSignInClient = GoogleSignIn.getClient(RegistActivity.this, gso);

                        //ki kell nyílnia új ablaknak, csak így tdunk adatot kinyerni
                        Intent signInIntent = googleSignInClient.getSignInIntent();
                        startActivityForResult(signInIntent, RC_SIGN_IN);

            }
        });

*/



    }  //oncreate vége



    //google regisztráció-------------------------------------------------------------
  /*  @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN) { //ADAT lekérés
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class); //getresult adja vissza


                //ebből kiszedjük
                String email = account.getEmail();
                String name = account.getDisplayName();
                String idToken = account.getIdToken(); //firebasehez kell

                AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

                //firebase
                auth.signInWithCredential(credential) //firebase, ha van fiókja csak belépteti
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    FirebaseUser fuser = auth.getCurrentUser();

                                    String uid = fuser.getUid();
                                    User user = new User(name, email);

                                    store.collection("users").document(uid)
                                            .set(user, SetOptions.merge())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()) {
                                                        Toast.makeText(RegistActivity.this, "Felhasználó regisztrálva!", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(RegistActivity.this, OpenAppActivity.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(RegistActivity.this, "Hiba: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(RegistActivity.this, "Firebase belépés sikertelen: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            } catch(ApiException e) {
                //sikerül e lekérni az adatokat
                Toast.makeText(this, "Google belépés sikertelen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


*/


}//registact vége

