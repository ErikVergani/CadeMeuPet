package com.ev.cademeupet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ev.cademeupet.MainActivity;
import com.ev.cademeupet.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity 
{
    private EditText txtEmail, txtPass;
    private FirebaseAuth firebaseAuth;
    
    @Override
    protected void onCreate( Bundle savedInstanceState ) 
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login );
        
        initComponents();
        
        ViewCompat.setOnApplyWindowInsetsListener( findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    
    private void doLogin()
    {
        String email = txtEmail.getText().toString();
        String pass = txtPass.getText().toString();
        
        if ( email.isEmpty() || pass.isEmpty() )
        {
            Toast.makeText( this, "Preencha todos os campos", Toast.LENGTH_SHORT ).show();
            return;
        }
        
        firebaseAuth.signInWithEmailAndPassword( email, pass )
                    .addOnCompleteListener(task -> 
                    {
                        if ( task.isSuccessful() ) {
                            startActivity( new Intent(this, MainActivity.class ) );
                        } else {
                            Toast.makeText(this, "Erro ao fazer login", Toast.LENGTH_SHORT).show();
                        }
                    } );
    }
    
    private void initComponents()
    {
        firebaseAuth = FirebaseAuth.getInstance();
        
        txtEmail = findViewById( R.id.txt_email );
        txtPass = findViewById( R.id.txt_pass );
        
        Button loginButton = findViewById( R.id.btn_login );
        TextView registerLink = findViewById( R.id.btn_register );
        
        loginButton.setOnClickListener(l -> doLogin() );
        registerLink.setOnClickListener(l -> startActivity( new Intent( LoginActivity.this, RegisterActivity.class ) ) );
    }
}