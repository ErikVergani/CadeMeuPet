package com.ev.cademeupet.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ev.cademeupet.R;
import com.ev.cademeupet.models.Pet;
import com.ev.cademeupet.services.EmailService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {
    
    private Context context;
    private List<Pet> petList;
    
    public PetAdapter( Context context, List<Pet> petList )
    {
        this.context = context;
        this.petList = petList;
    }
    
    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder( @NonNull ViewGroup parent, int viewType ) 
    {
        View view = LayoutInflater.from( context ).inflate( R.layout.item_pet, parent, false );
        return new PetViewHolder( view );
    }
    
    @Override
    public void onBindViewHolder( @NonNull PetViewHolder holder, int position )
    {
        Pet pet = petList.get( position );
        
        holder.petDesc.setText( pet.getDesc() );
        holder.petData.setText( "Desaparecido em: " + pet.getDtMissing() );
        holder.statusTag.setText( pet.getStatus().toString() );
        
        holder.statusTag.setBackgroundColor( pet.getStatusEnum() == Pet.STATUS.FOUND ? Color.parseColor("#4CAF50") : Color.parseColor( "#F44336" ) );
        
        File imgFile = new File( pet.getImageUrl() );
        
        if ( imgFile.exists() )
        {
            Bitmap bitmap = tryFixRotation( imgFile.getAbsolutePath() );
            holder.petImageView.setImageBitmap( bitmap );
        } 
        
        else 
        {
//            holder.petImageView.setImageResource(R.drawable.placeholder);
        }
        
        holder.findButton.setOnClickListener( v -> 
        {
            FirebaseFirestore.getInstance()
                             .collection("users")
                             .document( FirebaseAuth.getInstance().getCurrentUser().getUid() )
                             .get()
                             .addOnSuccessListener(documentSnapshot -> 
                             {
                                if ( documentSnapshot.exists() ) 
                                {
                                    String address = documentSnapshot.getString("address");
                                    String email = documentSnapshot.getString( "email" );
                                    
                                    if ( email != null && address != null )
                                    {
                                        EmailService.sendEmail(
                                                pet.getOwnerEmail(),
                                                pet.getName(),
                                                address
                                        );
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Erro ao buscar usuário: " + e.getMessage());
                            });
                    
            FirebaseFirestore.getInstance()
                             .collection( "pets" )
                             .document( pet.getId() )
                             .update( "status", Pet.STATUS.FOUND.toString() )
                             .addOnSuccessListener( aVoid -> 
                            {
                                Log.d("Firestore", "Status atualizado para Encontrado");
                            } )
                            .addOnFailureListener(e -> 
                            {
                                Log.e("Firestore", "Erro ao atualizar status: " + e.getMessage());
                            } );
                    
            Toast.makeText( context, "O dono será notificado!", Toast.LENGTH_SHORT ).show();
        });
    }
    
    public void updateList(List<Pet> newList) {
        this.petList.clear();
        this.petList.addAll(newList);
        notifyDataSetChanged();
    }
    
    @Override
    public int getItemCount() 
    {
        return petList.size();
    }
    
    static class PetViewHolder extends RecyclerView.ViewHolder 
    {
        ImageView petImageView;
        TextView petDesc, petData, statusTag;
        Button findButton;
        
        public PetViewHolder( @NonNull View itemView ) 
        {
            super( itemView );
            
            petImageView = itemView.findViewById( R.id.petImageView );
            petDesc = itemView.findViewById( R.id.petDescricao );
            petData = itemView.findViewById( R.id.petData );
            statusTag = itemView.findViewById( R.id.statusTag );
            findButton = itemView.findViewById( R.id.encontreiButton );
        }
    }
    
    private Bitmap tryFixRotation( String path ) 
    {
        try {
            int targetW = 300;
            int targetH = 300;
            
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            
            int scaleFactor = Math.min(
                    options.outWidth / targetW,
                    options.outHeight / targetH);
            
            options.inJustDecodeBounds = false;
            options.inSampleSize = scaleFactor;
            options.inPurgeable = true;
            
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            
            // 🔁 Se altura maior que largura, gira 90 graus
            if (bitmap.getHeight() > bitmap.getWidth()) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
            
            return bitmap;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
