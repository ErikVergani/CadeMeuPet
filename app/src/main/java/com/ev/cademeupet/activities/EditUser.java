package com.ev.cademeupet.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ev.cademeupet.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class EditUser extends AppCompatActivity 
{
    private EditText txtName, txtAddress, txtPhone;
    private FirebaseFirestore db;
    private String uid;
    
    @Override
    protected void onCreate( Bundle savedInstanceState ) 
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_edit_user );
        
        initComponents();
    }
    
    private void loadData() {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if ( doc.exists() )
                    {
                        txtName.setText( doc.getString("fullName" ) );
                        txtAddress.setText( doc.getString("address" ) );
                        txtPhone.setText( doc.getString("phone" ) );
                    }
                });
    }
    
    private void saveData() 
    {
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put( "fullName", txtName.getText().toString() );
        updatedData.put( "address", txtAddress.getText().toString() );
        updatedData.put( "phone", txtPhone.getText().toString() );
        updatedData.put( "email", FirebaseAuth.getInstance().getCurrentUser().getEmail() );
        
        db.collection("users").document( uid )
                .set( updatedData, SetOptions.merge() )
                .addOnSuccessListener( v -> 
                {
                    Toast.makeText(this, "Perfil atualizado com sucesso", Toast.LENGTH_SHORT).show();
                    finish();
                } )
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao atualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show() );
    }
    
    private void initComponents()
    {
        Button btnSave = findViewById( R.id.btnSalvar );
        btnSave.setOnClickListener(v -> saveData() );
        
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        txtName = findViewById( R.id.etNome );
        txtAddress = findViewById( R.id.etEndereco );
        txtPhone = findViewById( R.id.etTelefone );
        
        loadData();
    }
}