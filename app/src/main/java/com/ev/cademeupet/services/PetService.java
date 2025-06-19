package com.ev.cademeupet.services;

import android.widget.Toast;

import com.ev.cademeupet.models.Pet;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class PetService 
{
    public static void savePet(Pet pet, OnSuccessListener successListener, OnFailureListener failureListener )
    {
        FirebaseFirestore.getInstance().collection("pets")
                         .document( pet.getId() )
                         .set( pet )
                         .addOnSuccessListener( successListener )
                         .addOnFailureListener( failureListener );
    }
    
}
