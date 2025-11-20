package com.ev.cademeupet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ev.cademeupet.MainActivity;
import com.ev.cademeupet.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private EditText txtEmail, txtPass;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Toast.makeText(this, getString( R.string.error_google_login ), Toast.LENGTH_SHORT).show();
                    }
                }
            });
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        initComponents();
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        createGoogleRequest();
    }
    
    private void createGoogleRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, R.string.error_auth, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void doLogin() {
        String email = txtEmail.getText().toString();
        String pass = txtPass.getText().toString();
        
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, getString( R.string.error_fill_fields ), Toast.LENGTH_SHORT).show();
            return;
        }
        
        firebaseAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this, MainActivity.class));
                    } else {
                        Toast.makeText(this, getString( R.string.error_login ), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void initComponents() {
        firebaseAuth = FirebaseAuth.getInstance();
        txtEmail = findViewById(R.id.txt_email);
        txtPass = findViewById(R.id.txt_pass);
        Button loginButton = findViewById(R.id.btn_login);
        TextView registerLink = findViewById(R.id.btn_add_user);
        Button googleLoginButton = findViewById(R.id.btn_google_login);
        
        loginButton.setOnClickListener(l -> doLogin());
        registerLink.setOnClickListener(l -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        googleLoginButton.setOnClickListener(l -> signInWithGoogle());
    }
    
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }
}