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
    private EditText txtName, txtPhone, txtStreet, txtHomeNumber, txtDistrict, txtCity, txtUF;
    private FirebaseFirestore db;
    private String uid;
    
    @Override
    protected void onCreate( Bundle savedInstanceState ) 
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_edit_user );
        
        initComponents();
    }
    
    private void loadData() 
    {
        db.collection( "users" )
          .document( uid )
          .get()
          .addOnSuccessListener(doc -> 
          {
              if ( doc.exists() )
              {
                  txtName.setText( doc.getString("fullName" ) );
                  txtPhone.setText( doc.getString("phone" ) );
                  
                  txtStreet.setText( doc.getString("street" ) );
                  txtHomeNumber.setText( doc.getString("homeNumber" ) );
                  txtDistrict.setText( doc.getString("district" ) );
                  txtCity.setText( doc.getString("city" ) );
                  txtUF.setText( doc.getString("UF" ) );
              }
          });
    }
    
    private void saveData() 
    {
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put( "email", FirebaseAuth.getInstance().getCurrentUser().getEmail() );
        updatedData.put( "fullName", txtName.getText().toString().trim().toLowerCase() );
        updatedData.put( "phone", txtPhone.getText().toString() );
        
        updatedData.put( "street", txtStreet.getText().toString().trim().toLowerCase() );
        updatedData.put( "homeNumber", txtHomeNumber.getText().toString().trim().toLowerCase() );
        updatedData.put( "district", txtDistrict.getText().toString().trim().toLowerCase() );
        updatedData.put( "city", txtCity.getText().toString().trim().toLowerCase() );
        updatedData.put( "UF", txtUF.getText().toString().trim().toLowerCase() );
        
        db.collection("users").document( uid )
                .set( updatedData, SetOptions.merge() )
                .addOnSuccessListener( v -> 
                {
                    Toast.makeText(this, getString( R.string.success_profile_update ), Toast.LENGTH_SHORT ).show();
                    finish();
                } )
                .addOnFailureListener(e -> Toast.makeText(this, getString( R.string.error_profile_update ) + " " + e.getMessage(), Toast.LENGTH_SHORT).show() );
    }
    
    private void initComponents()
    {
        Button btnSave = findViewById( R.id.btn_save );
        btnSave.setOnClickListener(v -> saveData() );
        
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        txtName = findViewById( R.id.tf_name);
        txtPhone = findViewById( R.id.tf_phone);
        txtStreet = findViewById( R.id.tf_street );
        txtHomeNumber = findViewById( R.id.tf_home_number );
        txtDistrict = findViewById( R.id.tf_district );
        txtCity = findViewById( R.id.tf_city );
        txtUF = findViewById( R.id.tf_uf );
        
        loadData();
    }
}