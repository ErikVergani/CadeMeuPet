package com.ev.cademeupet.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ev.cademeupet.R;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity 
{
    private EditText email, password;
    private FirebaseAuth mAuth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate( savedInstanceState );
        EdgeToEdge.enable( this );
        setContentView( R.layout.activity_register );
        
        initComponents(); 
        
        ViewCompat.setOnApplyWindowInsetsListener( findViewById( R.id.main ), ( v, insets ) -> 
        {
            Insets systemBars = insets.getInsets( WindowInsetsCompat.Type.systemBars() );
            v.setPadding( systemBars.left, systemBars.top, systemBars.right, systemBars.bottom );
            
            return insets;
        } );
    }
    
    private void registerUser() 
    {
        String userEmail = email.getText().toString();
        String userPassword = password.getText().toString();
        
        if ( userEmail.isEmpty() || userPassword.isEmpty() )
        {
            Toast.makeText( this, "Preencha todos os campos", Toast.LENGTH_SHORT ).show();
            return;
        }
        
        mAuth.createUserWithEmailAndPassword( userEmail, userPassword )
             .addOnCompleteListener(task -> 
             {
                if ( task.isSuccessful() ) 
                {
                    Toast.makeText(this, "Usuário cadastrado com sucesso", Toast.LENGTH_SHORT ).show();
                    finish();
                }
                
                else
                {
                    Toast.makeText(this, "Erro ao cadastrar", Toast.LENGTH_SHORT ).show();
                }
            });
    }
    
    private void initComponents()
    {
        email = findViewById( R.id.txt_email );
        password = findViewById( R.id.txt_pass );
        Button registerBtn = findViewById( R.id.btn_register);
        
        mAuth = FirebaseAuth.getInstance();
        
        registerBtn.setOnClickListener(l -> registerUser() );
    }
}