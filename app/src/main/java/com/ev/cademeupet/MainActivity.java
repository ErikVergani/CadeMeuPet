package com.ev.cademeupet;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ev.cademeupet.activities.AddPetActivity;
import com.ev.cademeupet.activities.EditUser;
import com.ev.cademeupet.activities.LoginActivity;
import com.ev.cademeupet.adapters.PetAdapter;
import com.ev.cademeupet.models.Pet;
import com.ev.cademeupet.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    
    private RecyclerView petsRecyclerView;
    private List<Pet> allPets = new ArrayList<>();
    private PetAdapter petAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private GoogleSignInClient mGoogleSignInClient;
    
    private Spinner statusSpinner;
    private SwitchCompat myPostsSwitch;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#6200EE"));
        setSupportActionBar(toolbar);
        
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        
        petsRecyclerView = findViewById(R.id.rv_pet);
        FloatingActionButton addPetFab = findViewById(R.id.float_add);
        statusSpinner = findViewById(R.id.filter_spinner);
        myPostsSwitch = findViewById(R.id.switch_my_posts);
        
        petAdapter = new PetAdapter(this, new ArrayList<>());
        petsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        petsRecyclerView.setAdapter(petAdapter);
        
        addPetFab.setOnClickListener(v -> startActivity(new Intent(this, AddPetActivity.class)));
        
        checkUserLanguage();
        setupFilters();
        listenToPetUpdates();
    }
    
    private void setupFilters() {
        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        
        statusSpinner.setOnItemSelectedListener(filterListener);
        myPostsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> applyFilters());
    }
    
    private void listenToPetUpdates() {
        db.collection("pets")
                .orderBy("dtMissing", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("FirestoreListener", "Listen failed.", error);
                        return;
                    }
                    allPets.clear();
                    if (value != null) {
                        allPets.addAll(value.toObjects(Pet.class));
                    }
                    applyFilters();
                });
    }
    
    private void applyFilters() {
        if (auth.getCurrentUser() == null) return;
        
        List<Pet> filteredList = new ArrayList<>(allPets);
        String currentUserId = auth.getCurrentUser().getUid();
        
        if (myPostsSwitch.isChecked()) {
            filteredList = filteredList.stream()
                    .filter(pet -> currentUserId.equals(pet.getOwnerId()))
                    .collect(Collectors.toList());
        }
        int selectedPosition = statusSpinner.getSelectedItemPosition();
        
        if (selectedPosition == 1) { 
            filteredList = filteredList.stream()
                    .filter(pet -> pet.getStatusEnum() == Pet.STATUS.MISSING)
                    .collect(Collectors.toList());
        } else if (selectedPosition == 2) {
            filteredList = filteredList.stream()
                    .filter(pet -> pet.getStatusEnum() == Pet.STATUS.FOUND)
                    .collect(Collectors.toList());
        }
        
        petAdapter.updateList(filteredList);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_editar_perfil) {
            startActivity(new Intent(this, EditUser.class));
            return true;
        } 
        else if (itemId == R.id.lang_pt) {
            setLocale("pt", true);
            return true;
        } else if (itemId == R.id.lang_en) {
            setLocale("en",true);
            return true;
        }
        else if (itemId == R.id.menu_sair) {
            auth.signOut();
            mGoogleSignInClient.signOut().addOnCompleteListener(this,
                    task -> {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
     private void setLocale(String langCode, boolean saveToDb) {
        if (saveToDb && auth.getCurrentUser() != null) {
            updateLanguageInFirestore(langCode);
        }

        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
    
    private void checkUserLanguage() {
        if (auth.getCurrentUser() == null) return;
        
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null && user.getLanguage() != null) {
                    String savedLang = user.getLanguage();
                    String currentLang = getResources().getConfiguration().getLocales().get(0).getLanguage();
                    
                    if (!savedLang.equals(currentLang)) {
                        setLocale(savedLang, false);
                    }
                }
            }
        });
    }
        
    private void updateUserLanguage(String langCode) 
    {
        String uid = auth.getCurrentUser().getUid();
        Map<String, Object> data = new HashMap<>();
        data.put("language", langCode);

        db.collection("users").document(uid)
                .set(data, SetOptions.merge())
                .addOnFailureListener(e -> Log.e("MainActivity", "Erro ao salvar idioma", e));
    }
    
    private void updateLanguageInFirestore(String langCode) {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        Map<String, Object> data = new HashMap<>();
        data.put("language", langCode);

        db.collection("users").document(uid)
                .set(data, SetOptions.merge())
                .addOnFailureListener(e -> Log.e("MainActivity", "Erro ao salvar idioma", e));
    }
}