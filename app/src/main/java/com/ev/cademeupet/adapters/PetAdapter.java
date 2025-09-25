package com.ev.cademeupet.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

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
            
            statusTag.setBackgroundColor(pet.getStatusEnum() == Pet.STATUS.FOUND ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));
            
            // Mostra os botões corretos dependendo se o utilizador é o dono
            if (currentUserId != null && currentUserId.equals(pet.getOwnerId())) {
                ownerActionsContainer.setVisibility(View.VISIBLE);
                findButton.setVisibility(View.GONE);
            } else {
                ownerActionsContainer.setVisibility(View.GONE);
                findButton.setVisibility(pet.getStatusEnum() == Pet.STATUS.MISSING ? View.VISIBLE : View.GONE);
            }
            
            // Ação de clique para ir aos detalhes
            itemView.setOnClickListener(c -> {
                Intent intent = new Intent(context, PetDetailActivity.class);
                intent.putExtra("pet", pet);
                context.startActivity(intent);
            });
            
            // Ação do botão "Editar"
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(context, AddPetActivity.class);
                intent.putExtra("pet_to_edit", pet);
                context.startActivity(intent);
            });
            
            // Ação do botão "Excluir"
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
    }
}

