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
import com.google.firebase.firestore.FirebaseFirestore;
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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_details);
        
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        currentPet = (Pet) getIntent().getSerializableExtra("pet");
        
        if (currentPet == null) {
            Toast.makeText(this, "Erro ao carregar detalhes do pet.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        setupPetDetails();
        setupRecyclerView();
        loadSightings();
        setupButtons();
    }
    
    private void setupButtons() {
        btnReportSighting = findViewById(R.id.btn_report_sighting);
        ownerActionsContainer = findViewById(R.id.owner_actions_container);
        btnEditPet = findViewById(R.id.btn_edit_pet);
        btnDeletePet = findViewById(R.id.btn_delete_pet);
        
        String currentUserId = auth.getCurrentUser().getUid();
        
        if (currentUserId != null && currentUserId.equals(currentPet.getOwnerId())) {
            btnReportSighting.setVisibility(View.GONE);
            ownerActionsContainer.setVisibility(View.VISIBLE);
        } else {
            btnReportSighting.setVisibility(View.VISIBLE);
            ownerActionsContainer.setVisibility(View.GONE);
        }
        
        if (currentPet.getStatus().equalsIgnoreCase("Encontrado")) {
            btnReportSighting.setEnabled(false);
            btnReportSighting.setBackgroundColor(Color.rgb(128, 128, 128));
        }
        btnReportSighting.setOnClickListener(v -> reportSighting());
        btnEditPet.setOnClickListener(v -> editPet());
        btnDeletePet.setOnClickListener(v -> showDeleteConfirmationDialog());
    }
    
    private void editPet() {
        Intent intent = new Intent(this, AddPetActivity.class);
        intent.putExtra("pet_to_edit", currentPet);
        startActivity(intent);
    }
    
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Publicação")
                .setMessage("Tem a certeza de que deseja excluir esta publicação?")
                .setPositiveButton("Sim, Excluir", (dialog, which) -> deletePet())
                .setNegativeButton("Cancelar", null)
                .show();
    }
    
    private void deletePet() {
        db.collection("pets").document(currentPet.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Publicação excluída com sucesso.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao excluir a publicação.", Toast.LENGTH_SHORT).show());
    }
    
    
    private void setupPetDetails() {
        TextView petName = findViewById(R.id.detail_pet_name);
        TextView petDesc = findViewById(R.id.detail_pet_desc);
        TextView petDate = findViewById(R.id.detail_pet_date);
        ImageView petImage = findViewById(R.id.detail_pet_image);
        
        petName.setText(currentPet.getName());
        petDesc.setText(currentPet.getDesc());
        petDate.setText("Desaparecido em: " + currentPet.getDtMissing());
        
        Glide.with(this).load(currentPet.getImageUrl()).into(petImage);
    }
    
    private void setupRecyclerView() {
        RecyclerView rvSightings = findViewById(R.id.rv_sightings);
        rvSightings.setLayoutManager(new LinearLayoutManager(this));
        sightingAdapter = new SightingAdapter(sightingList, currentPet.getOwnerId(), auth.getCurrentUser().getUid(), this);
        rvSightings.setAdapter(sightingAdapter);
    }
    
    private void loadSightings() {
        Log.d(TAG, "A carregar avistamentos para o pet ID: " + currentPet.getId());
        db.collection("sightings")
                .whereEqualTo("petId", currentPet.getId())
                .orderBy("sightingDate", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Erro ao carregar avistamentos.", e);
                        Toast.makeText(this, "Erro ao carregar avistamentos.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sightingList.clear();
                    if (snapshots != null) {
                        sightingList.addAll(snapshots.toObjects(Sighting.class));
                        Log.d(TAG, "Foram encontrados " + sightingList.size() + " avistamentos.");
                    }
                    sightingAdapter.notifyDataSetChanged();
                });
    }
    
    private void reportSighting() {
        String currentUserId = auth.getCurrentUser().getUid();
        
        // Evita que o próprio dono reporte um avistamento do seu animal
        if (currentUserId.equals(currentPet.getOwnerId())) {
            Toast.makeText(this, "Não pode reportar um avistamento do seu próprio pet.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "A reportar avistamento pelo utilizador: " + currentUserId);
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Log.w(TAG, "O documento do utilizador não existe. O perfil pode não estar completo.");
                        Toast.makeText(this, "Complete o seu perfil antes de reportar um avistamento.", Toast.LENGTH_LONG).show();
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
                        newSighting.setStatus("Pendente");
                        
                        db.collection("sightings").document(newSighting.getId()).set(newSighting)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Avistamento criado com sucesso no Firestore!");
                                    Toast.makeText(this, "Avistamento reportado com sucesso!", Toast.LENGTH_SHORT).show();
                                    
                                    sightingList.add(0, newSighting);
                                    sightingAdapter.notifyItemInserted(0);
                                    RecyclerView rvSightings = findViewById(R.id.rv_sightings);
                                    rvSightings.scrollToPosition(0);
                                    
                                    EmailService.sendEmail(
                                            currentPet,
                                            documentSnapshot.toObject(User.class));
                                })
                                .addOnFailureListener(err -> {
                                    Log.e(TAG, "Erro ao guardar o avistamento no Firestore.", err);
                                    Toast.makeText(this, "Erro ao reportar avistamento.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.w(TAG, "Falha ao converter o documento do utilizador ou o nome está vazio.");
                        Toast.makeText(this, "Parece que o seu perfil está incompleto. Por favor, atualize os seus dados.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Falha ao buscar os dados do utilizador.", e);
                    Toast.makeText(this, "Erro ao buscar os seus dados. Tente novamente.", Toast.LENGTH_SHORT).show();
                });
    }
}