package com.ev.cademeupet.adapters;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.ev.cademeupet.R;
import com.ev.cademeupet.activities.AddPetActivity;
import com.ev.cademeupet.activities.PetDetailActivity;
import com.ev.cademeupet.models.Pet;
import com.ev.cademeupet.models.Sighting;
import com.ev.cademeupet.models.User;
import com.ev.cademeupet.services.EmailService;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.UUID;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {
    
    private Context context;
    private List<Pet> petList;
    private String currentUserId;
    
    public PetAdapter(Context context, List<Pet> petList) {
        this.context = context;
        this.petList = petList;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }
    
    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pet, parent, false);
        return new PetViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = petList.get(position);
        holder.bind(pet);
    }
    
    public void updateList(List<Pet> newList) {
        this.petList.clear();
        this.petList.addAll(newList);
        notifyDataSetChanged();
    }
    
    @Override
    public int getItemCount() {
        return petList.size();
    }
    
    class PetViewHolder extends RecyclerView.ViewHolder {
        ImageView petImageView;
        TextView name, data, statusTag;
        Button findButton, btnEdit, btnDelete;
        LinearLayout ownerActionsContainer;
        
        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            petImageView = itemView.findViewById(R.id.img_pet);
            name = itemView.findViewById(R.id.txt_name);
            data = itemView.findViewById(R.id.dt_miss);
            statusTag = itemView.findViewById(R.id.pet_status);
            findButton = itemView.findViewById(R.id.btn_found);
            ownerActionsContainer = itemView.findViewById(R.id.owner_actions_item_container);
            btnEdit = itemView.findViewById(R.id.btn_edit_item);
            btnDelete = itemView.findViewById(R.id.btn_delete_item);
        }
        
        void bind(final Pet pet) {
            name.setText(pet.getName());
            data.setText("Desaparecido em: " + pet.getDtMissing());
            statusTag.setText(pet.getStatus());
            
            if (pet.getImageUrl() != null && !pet.getImageUrl().isEmpty()) {
                Glide.with(context).load(pet.getImageUrl()).into(petImageView);
            }
            
            boolean found = pet.getStatusEnum() == Pet.STATUS.MISSING;
            statusTag.setBackgroundColor( found ?  Color.parseColor("#F44336") : Color.parseColor("#4CAF50") );
            
            int visibility = found ? View.VISIBLE : View.GONE;
            
            if (currentUserId != null && currentUserId.equals(pet.getOwnerId()) ) {
                ownerActionsContainer.setVisibility( visibility );
                findButton.setVisibility(View.GONE);
            } else {
                ownerActionsContainer.setVisibility(View.GONE);
                findButton.setVisibility( visibility );
            }
            
            itemView.setOnClickListener(c -> {
                Intent intent = new Intent(context, PetDetailActivity.class);
                intent.putExtra("pet", pet);
                context.startActivity(intent);
            });
            findButton.setOnClickListener(v -> reportSightingFromMain(pet));
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(context, AddPetActivity.class);
                intent.putExtra("pet_to_edit", pet);
                context.startActivity(intent);
            });
            
            btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog(pet));
        }
        
        private void showDeleteConfirmationDialog(Pet pet) {
            new AlertDialog.Builder(context)
                    .setTitle("Excluir Publicação")
                    .setMessage("Tem a certeza de que deseja excluir esta publicação?")
                    .setPositiveButton("Sim, Excluir", (dialog, which) -> deletePet(pet))
                    .setNegativeButton("Cancelar", null)
                    .show();
        }
        
        private void deletePet(Pet pet) {
            FirebaseFirestore.getInstance().collection("pets").document(pet.getId()).delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Publicação excluída.", Toast.LENGTH_SHORT).show();
                        // A lista será atualizada automaticamente pelo snapshot listener na MainActivity
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Erro ao excluir.", Toast.LENGTH_SHORT).show());
        }
        private void reportSightingFromMain(Pet pet) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            
            if (currentUserId == null) {
                Toast.makeText(context, "Faça login para reportar um avistamento.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (currentUserId.equals(pet.getOwnerId())) {
                Toast.makeText(context, "Você não pode reportar um avistamento do seu próprio pet.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            db.collection("users").document(currentUserId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!documentSnapshot.exists()) {
                            Toast.makeText(context, "Complete seu perfil para reportar um avistamento.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        
                        User reporter = documentSnapshot.toObject(User.class);
                        if (reporter != null && reporter.getFullName() != null && !reporter.getFullName().isEmpty()) {
                            Sighting newSighting = new Sighting();
                            newSighting.setId(UUID.randomUUID().toString());
                            newSighting.setPetId(pet.getId());
                            newSighting.setReporterId(currentUserId);
                            newSighting.setReporterName(reporter.getFullName());
                            newSighting.setSightingDate(Timestamp.now());
                            newSighting.setLocation(reporter.getFullAddress() != null ? reporter.getFullAddress() : "Localização não informada");
                            newSighting.setMessage("Avistado perto do meu endereço.");
                            newSighting.setStatus("Pendente");
                            
                            db.collection("sightings").document(newSighting.getId()).set(newSighting)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Avistamento criado com sucesso no Firestore!");
                                        Toast.makeText(context, "Avistamento reportado com sucesso!", Toast.LENGTH_SHORT).show();
                                        EmailService.sendEmail(pet, reporter);
                                    })
                                    .addOnFailureListener(err -> {
                                        Log.e(TAG, "Erro ao guardar o avistamento no Firestore.", err);
                                        Toast.makeText(context, "Erro ao reportar avistamento.", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(context, "Seu perfil está incompleto. Por favor, atualize seus dados.", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Falha ao buscar os dados do utilizador.", e);
                        Toast.makeText(context, "Erro ao buscar seus dados. Tente novamente.", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}

