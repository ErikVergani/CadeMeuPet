package com.ev.cademeupet.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.ev.cademeupet.R;
import com.ev.cademeupet.adapters.SightingAdapter;
import com.ev.cademeupet.models.Pet;
import com.ev.cademeupet.models.Sighting;
import com.ev.cademeupet.models.User;
import com.ev.cademeupet.services.EmailService;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PetDetailActivity extends AppCompatActivity {
    
    private static final String TAG = "PetDetailActivity";
    
    private Pet currentPet;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private SightingAdapter sightingAdapter;
    private List<Sighting> sightingList = new ArrayList<>();
    private Button btnReportSighting, btnEditPet, btnDeletePet;
    private LinearLayout ownerActionsContainer;
    
    private ListenerRegistration petListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_details);
        
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        Pet petFromIntent = (Pet) getIntent().getSerializableExtra("pet");
        
        if (petFromIntent == null || petFromIntent.getId() == null) {
            Toast.makeText(this, getString(R.string.error_load_details), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        setupRecyclerView();
        listenToPetUpdates(petFromIntent.getId());
        loadSightings(petFromIntent.getId());
    }
    
    /**
     * Anexa um "ouvinte" ao documento do pet no Firestore.
     * Este método será notificado sobre qualquer alteração nos dados do pet.
     */
    private void listenToPetUpdates(String petId) {
        final DocumentReference petRef = db.collection("pets").document(petId);
        petListener = petRef.addSnapshotListener(this, (snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Falha ao ouvir as atualizações do pet.", e);
                return;
            }
            
            if (snapshot != null && snapshot.exists()) {
                currentPet = snapshot.toObject(Pet.class);
                if (currentPet != null) {
                    updateUI();
                }
            } else {
                Toast.makeText(this, getString(R.string.alert_missing_publi), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    /**
     * Método central que atualiza todos os componentes da tela
     * com base no estado mais recente do objeto currentPet.
     */
    private void updateUI() {
        TextView petName = findViewById(R.id.detail_pet_name);
        TextView petDesc = findViewById(R.id.detail_pet_desc);
        TextView petDate = findViewById(R.id.detail_pet_date);
        ImageView petImage = findViewById(R.id.detail_pet_image);
        
        petName.setText(currentPet.getName());
        petDesc.setText(currentPet.getDesc());
        petDate.setText(getString(R.string.label_missing_at) + currentPet.getDtMissing());
        Glide.with(this).load(currentPet.getImageUrl()).into(petImage);
        
        btnReportSighting = findViewById(R.id.btn_report_sighting);
        ownerActionsContainer = findViewById(R.id.owner_actions_container);
        btnEditPet = findViewById(R.id.btn_edit_pet);
        btnDeletePet = findViewById(R.id.btn_delete_pet);
        
        String currentUserId = auth.getCurrentUser().getUid();
        boolean isOwner = currentUserId != null && currentUserId.equals(currentPet.getOwnerId());
        boolean isFound = currentPet.getStatusEnum() == Pet.STATUS.FOUND;
        
        ownerActionsContainer.setVisibility(isOwner && !isFound ? View.VISIBLE : View.GONE);
        btnReportSighting.setVisibility(isOwner ? View.GONE : View.VISIBLE);
        
        if (isFound) {
            btnReportSighting.setEnabled(false);
            btnReportSighting.setBackgroundColor(Color.rgb(128, 128, 128));
        } else {
            btnReportSighting.setEnabled(true);
            btnReportSighting.setBackgroundColor(Color.parseColor("#6200EE"));
        }
        
        btnReportSighting.setOnClickListener(v -> reportSighting());
        btnEditPet.setOnClickListener(v -> editPet());
        btnDeletePet.setOnClickListener(v -> showDeleteConfirmationDialog());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (petListener != null) {
            petListener.remove();
        }
    }
    
    private void editPet() {
        Intent intent = new Intent(this, AddPetActivity.class);
        intent.putExtra("pet_to_edit", currentPet);
        startActivity(intent);
    }
    
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_delete_title))
                .setMessage(getString(R.string.dialog_delete_msg))
                .setPositiveButton(getString(R.string.dialog_yes), (dialog, which) -> deletePet())
                .setNegativeButton(getString(R.string.dialog_cancel), null)
                .show();
    }
    
    private void deletePet() {
        db.collection("pets").document(currentPet.getId()).delete()
                .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.error_delete), Toast.LENGTH_SHORT).show());
    }
    
    private void setupRecyclerView() {
        RecyclerView rvSightings = findViewById(R.id.rv_sightings);
        rvSightings.setLayoutManager(new LinearLayoutManager(this));
    }
    
    private void loadSightings(String petId) {
        db.collection("sightings")
                .whereEqualTo("petId", petId)
                .orderBy("sightingDate", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Erro ao carregar avistamentos.", e);
                        return;
                    }
                    sightingList.clear();
                    if (snapshots != null) {
                        sightingList.addAll(snapshots.toObjects(Sighting.class));
                    }
                    
                    if (sightingAdapter == null && currentPet != null) {
                        RecyclerView rvSightings = findViewById(R.id.rv_sightings);
                        sightingAdapter = new SightingAdapter(sightingList, currentPet.getOwnerId(), auth.getCurrentUser().getUid(), this);
                        rvSightings.setAdapter(sightingAdapter);
                    } else if (sightingAdapter != null) {
                        sightingAdapter.notifyDataSetChanged();
                    }
                });
    }
    
    private void reportSighting() {
        String currentUserId = auth.getCurrentUser().getUid();
        
        if (currentUserId.equals(currentPet.getOwnerId())) {
            Toast.makeText(this, getString(R.string.text_cant_report_own), Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "A reportar avistamento pelo utilizador: " + currentUserId);
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Log.w(TAG, "O documento do utilizador não existe. O perfil pode não estar completo.");
                        Toast.makeText(this, getString(R.string.text_complete_profile), Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    User reporter = documentSnapshot.toObject(User.class);
                    if (reporter != null && reporter.getFullName() != null && !reporter.getFullName().isEmpty()) {
                        Log.d(TAG, "Perfil do utilizador carregado com sucesso. A criar o avistamento...");
                        Sighting newSighting = new Sighting();
                        newSighting.setId(UUID.randomUUID().toString());
                        newSighting.setPetId(currentPet.getId());
                        newSighting.setReporterId(currentUserId);
                        newSighting.setReporterName(reporter.getFullName());
                        newSighting.setSightingDate(Timestamp.now());
                        newSighting.setLocation(reporter.getFullAddress() != null ? reporter.getFullAddress() : "Localização não informada");
                        newSighting.setMessage("Avistado perto do meu endereço.");
                        newSighting.setStatusEnum(Sighting.STATUS.PENDING);
                        
                        db.collection("sightings").document(newSighting.getId()).set(newSighting)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Avistamento criado com sucesso no Firestore!");
                                    Toast.makeText(this, getString(R.string.sighting_created), Toast.LENGTH_SHORT).show();
                                    
                                    EmailService.sendEmail( this,
                                            currentPet,
                                            documentSnapshot.toObject(User.class));
                                })
                                .addOnFailureListener(err -> {
                                    Log.e(TAG, "Erro ao guardar o avistamento no Firestore.", err);
                                    Toast.makeText(this, getString(R.string.error_report_sighting), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.w(TAG, "Falha ao converter o documento do utilizador ou o nome está vazio.");
                        Toast.makeText(this, getString(R.string.alert_low_profile), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Falha ao buscar os dados do utilizador.", e);
                    Toast.makeText(this, getString(R.string.error_load_profile), Toast.LENGTH_SHORT).show();
                });
    }
}