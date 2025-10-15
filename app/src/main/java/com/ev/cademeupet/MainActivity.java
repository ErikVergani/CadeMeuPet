package com.ev.cademeupet;

import android.content.Intent;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
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
        
        String selectedStatus = statusSpinner.getSelectedItem().toString();
        if (!selectedStatus.equals("Todos")) {
            filteredList = filteredList.stream()
                    .filter(pet -> selectedStatus.equalsIgnoreCase(pet.getStatus()))
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
        } else if (itemId == R.id.menu_sair) {
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
}