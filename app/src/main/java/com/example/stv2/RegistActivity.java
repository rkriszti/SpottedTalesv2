package com.example.stv2;

import android.content.Intent;
import android.credentials.GetCredentialRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

       /*  //kollekció tesztelés
       store.collection("debug_test").document("testdoc2")
                .set(new HashMap<String, Object>() {{ put("hello", "world"); }})
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) Log.d("Firestore", "Debug doc létrejött");
                    else Log.e("Firestore", "Debug doc hiba", task.getException());
                });*/

        //Van már fiókom gomb
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

                ImageView giraffe = findViewById(R.id.giraffestart2);
                Animation anim = AnimationUtils.loadAnimation(RegistActivity.this, R.anim.giraffe_move);
                giraffe.startAnimation(anim);
                //Log.d("Firestore", "ProjectID: " + FirebaseFirestore.getInstance().getApp().getOptions().getProjectId());
                //Log.d("RegistActivity", "Regist gombra nyomtak");

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

                if (email.isEmpty() || password.isEmpty() || username.isEmpty() || passwordagain.isEmpty()){
                    Toast.makeText(
                            RegistActivity.this,
                            "Tölts ki minden mezőt!",
                            Toast.LENGTH_LONG
                    ).show();
                }

                if (!password.equals(passwordagain)) {
                    Toast.makeText(RegistActivity.this, "A jelszavak nem egyeznek!",  Toast.LENGTH_LONG).show();
                    return;

                }

                if (password.length() < 6) {
                    Toast.makeText(RegistActivity.this, "A jelszónak legalább 6 karakter hosszúnak kell lennie!", Toast.LENGTH_LONG).show();
                    return;
                }

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

                                user.saveToFirestore(uid,
                                        () -> { // onSuccess
                                            Toast.makeText(RegistActivity.this, "Sikeres regisztráció!", Toast.LENGTH_SHORT).show();

                                                  Intent intent = new Intent(RegistActivity.this, OpenAppActivity.class);
                                                    startActivity(intent);


                                            //startActivity(new Intent(RegistActivity.this, OpenAppActivity.class));
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


    }  //oncreate vége



}//registact vége

