package com.example.stv2;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class MenuActivity extends AppCompatActivity {
    private int where = 1;
    TextView skip ;
    Button next ;
    Button back ;
    ImageView pic ;
    TextView  title ;
    TextView szoveg ;


    //ezt a settup függvényt fogjuk csak meghívni
    protected void setupBottomMenu(Integer selectedItemId) {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (selectedItemId != null) {
            bottomNavigationView.setSelectedItemId(selectedItemId);
        } else {
            bottomNavigationView.getMenu().setGroupCheckable(0, false, true);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent = new Intent(MenuActivity.this, HomeActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_search) {
                Intent intent = new Intent(MenuActivity.this, SearchActivity.class);
                startActivity(intent);
                 return true;
            } else if (id == R.id.nav_clubs) {
                Intent intent = new Intent(MenuActivity.this, ClubsActivity.class);
                startActivity(intent);
                 return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(MenuActivity.this, ProfileActivity.class);
                intent.putExtra("userid",  FirebaseAuth.getInstance().getUid());
                startActivity(intent);
                return true;
            }
            Toast.makeText(this, "OpenAct hiba", Toast.LENGTH_SHORT).show();
            return false;
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, NewBookActivity.class))
        );
    }

  /*  protected void setupBottomMenu() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            Intent intent = null;
            if (id == R.id.nav_home) intent = new Intent(this, HomeActivity.class);
            else if (id == R.id.nav_search) intent = new Intent(this, SearchActivity.class);
            else if (id == R.id.nav_clubs) intent = new Intent(this, ClubsActivity.class);
            else if (id == R.id.nav_profile) intent = new Intent(this, ProfileActivity.class);

            if (intent != null) {
                startActivity(intent);
                finish();
            }
            return true;
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, NewBookActivity.class))
        );
    }*/

    protected void setupTopMenu() {
        //felső menü
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.top_toolbar);
        //kinyithatóság
        androidx.drawerlayout.widget.DrawerLayout drawer = findViewById(R.id.drawer_layout);
        //oldal menü elemei
        com.google.android.material.navigation.NavigationView navigationView = findViewById(R.id.nav_view);

        if (toolbar != null && drawer != null) {
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.other_menu) { // Ez a Toolbaron lévő ikon ID-ja
                    //kinyílik
                    drawer.openDrawer(androidx.core.view.GravityCompat.END);
                    return true;
                }
                return false;
            });
        }

        if (navigationView != null && drawer != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.action_logout_actual) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                if (id == R.id.action_tutorial) {
                    // tuti dialóg megnyitás
                    Dialog dialog = new Dialog(this);
                    dialog.setContentView(R.layout.dialog_tutorial);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    skip = dialog.findViewById(R.id.tutorial_close);
                    next = dialog.findViewById(R.id.tutorial_next);
                    back = dialog.findViewById(R.id.tutorial_back);
                    pic = dialog.findViewById(R.id.tutorial_pic);
                    title = dialog.findViewById(R.id.tutorial_title);
                    szoveg = dialog.findViewById(R.id.tv_tutorial_text);

                    dialog.show();

                    title.setText("Tutorial (1/9)");
                    pic.setImageResource(R.drawable.tutorial_1);
                    szoveg.setText("A plusz jelre kattintva feltölthetünk könyveket vagy létrehozhatunk egy klubot.\nAdhatunk meg címet, szerzőt és borítóképet.\nA kapcsolóra nyomva érjük el a klub feltöltést.");


                    skip.setOnClickListener(v -> {
                        dialog.cancel();
                    });


                    next.setOnClickListener(v -> {
                        where ++;
                        if(where == 10){
                            dialog.cancel();
                        }
                        loadingtutorial();
                    });


                    back.setOnClickListener(v -> {
                        if(where > 1){
                            where--;
                            loadingtutorial();
                        }
                    });
                }

                 drawer.closeDrawer(androidx.core.view.GravityCompat.END);
                return true;
            });
        } else {
             android.util.Log.e("MenuActivity", "Hiba: nav_view vagy drawer_layout nem található a layoutban!");
        }
    }

    protected void loadingtutorial(){
        switch (where){
            case 1:
                title.setText("Tutorial (1/9)");
                pic.setImageResource(R.drawable.tutorial_1);
                szoveg.setText("A plusz jelre kattintva feltölthetünk könyveket vagy létrehozhatunk egy klubot.\nAdhatunk meg címet, szerzőt és borítóképet.\nA kapcsolóra nyomva érjük el a klub feltöltést.");
          break;
            case 2:
                title.setText("Tutorial (2/9)");
                pic.setImageResource(R.drawable.tutorial_2);
                szoveg.setText("Klub létrehozásánál elég a nevét megadnod, későbbiekben módosíthatsz bármit.\nHa privátra állítod a klubot, csak engedéllyel léphetnek be a klubbodba.\n" +
                        "Fejezetekhez számot beírva minden számhoz megfelelő chatszobát hoz létre. pl.: 1. fejezet, 2. fejezet...stb\n" +
                        "Egyedi szobanév bármi lehet, a pipával tudod egyesével beírni őket. pl.: Értékelés\n" +
                        "További segítségért nyomj a kérdőjelekre!");
                break;
                case 3:
                title.setText("Tutorial (3/9)");
                pic.setImageResource(R.drawable.tutorial_3);
                szoveg.setText("A keresés gombra nyomva láthatjuk a felhasználók által feltöltött könyveket, melyek között cím alapján kereshetünk.\n" +
                        "Ha van könyved, itt tudod majd módosítani.\n" +
                        "Nyomjunk a klub gombra kilistázásukhoz!");
                    break;
            case 4:
                title.setText("Tutorial (4/9)");
                pic.setImageResource(R.drawable.tutorial_4);
                szoveg.setText("Itt láthatod az elérhető klubokat és miket olvasnak éppen.\n " +
                        "Ha publikus, akkor csatlakozhatsz a megfelelő gombbal, ha privát akkor küldhetsz " +
                        "kérelmet a klub adminjának.\n" +
                        "Nyomjunk a felhasználók gombra!");

                break; case 5:
                title.setText("Tutorial (5/9)");
                pic.setImageResource(R.drawable.tutorial_5);
                szoveg.setText("Itt láthatod a felhasználókat, és a 'hasonló' négyzetre nyomva leszűkíti " +
                        "azokra akikkel közösek a kedvenc könyveid.\n" +
                        "Profil oldalt a 'Megnyitással' vagy a menüsorban érheted el.");

                break; case 6:
                title.setText("Tutorial (6/9)");
                pic.setImageResource(R.drawable.tutorial_6);
                szoveg.setText("Profiloldalon láthatsz magadról/másokról információkat. Ha a saját oldaladon " +
                        "vagy akkor elérhető a beállítás gomb szerkesztésre.\n" +
                        "Illetve itt tudsz még goodreads alapú ajánlást kapni (több infó a kérdőjelre nyomva)");
                break;  case 7:
                title.setText("Tutorial (7/9)");
                pic.setImageResource(R.drawable.tutorial_7);
                szoveg.setText("Könyvekre kattintva beállíthatsz kedvenc könyvet, fölé nyomva törölheted őket.\n" +
                        "Az oldal alján  van lehetőséged fiókod törlésére.\n" +
                        "A fenti pipával mentheted a változtatásokat.");
                break; case 8:
                title.setText("Tutorial (8/9)");
                pic.setImageResource(R.drawable.tutorial_8);
                szoveg.setText("A menün a Klubok gombra nyomva láthatod a klubokat melynek tagja vagy.\n " +
                        "Nyisd meg az egyiket!");
                next.setText("Tovább ▶");
                break; case 9:
                title.setText("Tutorial (9/9)");
                pic.setImageResource(R.drawable.tutorial_9);
                szoveg.setText("A klub oldalon láthatod az együtt olvasott könyvet és a fejezeteket/egyedi szobákat" +
                        " lenyitva, a chatszobákat.\n" +
                        "A tagok fülön láthatod kik csatlakoztak, itt tudja az admin beengedni a jelentkezőket, és itt tudsz kilépni.\n" +
                        "Az előzmények fülön láthatod az előző könyveket amiket olvasott a klub, és megnyithatod a régi beszélgetéseket illetve használhatod is.\n" +
                        "Az admin a beállítás gombbal tud adatokat szerkeszteni, pl.: fejezet számot megadni.");
                next.setText("Vége");
                break;  default:
                title.setText("Tutorial (1/9)");
                pic.setImageResource(R.drawable.tutorial_1);
                szoveg.setText("A plusz jelre kattintva feltölthetünk könyveket vagy létrehozhatunk egy klubot.\nAdhatunk meg címet, szerzőt és borítóképet.\nA kapcsolóra nyomva érjük el a klub feltöltést.");

        }

    }

}
