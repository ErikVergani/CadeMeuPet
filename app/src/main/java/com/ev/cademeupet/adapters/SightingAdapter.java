package com.ev.cademeupet.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ev.cademeupet.R;
import com.ev.cademeupet.models.Sighting;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SightingAdapter extends RecyclerView.Adapter<SightingAdapter.SightingViewHolder> {
    
    private List<Sighting> sightingList;
    private String petOwnerId;
    private String currentUserId;
    private Context context;
    
    public SightingAdapter(List<Sighting> sightingList, String petOwnerId, String currentUserId, Context context) {
        this.sightingList = sightingList;
        this.petOwnerId = petOwnerId;
        this.currentUserId = currentUserId;
        this.context = context;
    }
    
    @NonNull
    @Override
    public SightingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sighting, parent, false);
        return new SightingViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SightingViewHolder holder, int position) {
        Sighting sighting = sightingList.get(position);
        holder.bind(sighting);
    }
    
    @Override
    public int getItemCount() {
        return sightingList.size();
    }
    
    class SightingViewHolder extends RecyclerView.ViewHolder {
        TextView reporterName, sightingDate, sightingLocation, sightingStatus;
        Button btnConfirm, btnReject;
        
        public SightingViewHolder(@NonNull View itemView) {
            super(itemView);
            reporterName = itemView.findViewById(R.id.sighting_reporter_name);
            sightingDate = itemView.findViewById(R.id.sighting_date);
            sightingLocation = itemView.findViewById(R.id.sighting_location);
            sightingStatus = itemView.findViewById(R.id.sighting_status);
            btnConfirm = itemView.findViewById(R.id.btn_confirm_sighting);
            btnReject = itemView.findViewById(R.id.btn_reject_sighting);
        }
        
        void bind(Sighting sighting) {
            reporterName.setText("Reportado por: " + sighting.getReporterName());
            sightingLocation.setText("Local: " + sighting.getLocation());
            sightingStatus.setText("Status: " + sighting.getStatus());
            
            if (sighting.getSightingDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());
                sightingDate.setText(sdf.format(sighting.getSightingDate().toDate()));
            }
            
            // Mostra os botões de confirmação apenas para o dono do pet e se o status for "Pendente"
            if (currentUserId.equals(petOwnerId) && "Pendente".equals(sighting.getStatus())) {
                btnConfirm.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);
                
                btnConfirm.setOnClickListener(v -> updateSightingStatus(sighting, "Confirmado"));
                btnReject.setOnClickListener(v -> updateSightingStatus(sighting, "Rejeitado"));
            } else {
                btnConfirm.setVisibility(View.GONE);
                btnReject.setVisibility(View.GONE);
            }
        }
        
        private void updateSightingStatus(Sighting sighting, String newStatus) {
            FirebaseFirestore.getInstance().collection("sightings").document(sighting.getId())
                    .update("status", newStatus)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Status do avistamento atualizado!", Toast.LENGTH_SHORT).show();
                        if("Confirmado".equals(newStatus)){
                            // Opcional: Atualizar o status do Pet para "Encontrado"
                            FirebaseFirestore.getInstance().collection("pets").document(sighting.getPetId())
                                    .update("status", "Encontrado");
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Erro ao atualizar status.", Toast.LENGTH_SHORT).show());
        }
    }
}
