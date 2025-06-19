package com.ev.cademeupet.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ev.cademeupet.R;
import com.ev.cademeupet.models.Pet;
import com.ev.cademeupet.services.PetService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

public class AddPetActivity extends AppCompatActivity {
    
    private static final int PICK_IMAGE_REQUEST = 1;
    
    private ImageView petImagePreview;
    private EditText inputDesc, inputData, inputName;
    private Button selectImageButton, savePetButton;
    
    private Uri imageUri;
    private FirebaseAuth auth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);
        
        initComponents();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        
        if ( requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null )
        {
            imageUri = data.getData();
            petImagePreview.setImageURI( imageUri );
        }
    }
    
    private void openFileChooser() 
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    private void savePet() 
    {
        String name = inputName.getText().toString();
        String desc = inputDesc.getText().toString();
        String data = inputData.getText().toString();
        
        if ( name.isEmpty() || desc.isEmpty() || data.isEmpty() || imageUri == null )
        {
            Toast.makeText(this, "Preencha todos os campos e selecione uma imagem", Toast.LENGTH_SHORT ).show();
            return;
        }
        
        try 
        {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap( getContentResolver(), imageUri );
            File imageFile = saveLocalImage( bitmap );
            
            if ( imageFile != null )
            {
                Pet pet = new Pet( UUID.randomUUID().toString(),
                                   name,
                                   desc,
                                   data,
                                   imageFile.getAbsolutePath(),
                                   Pet.STATUS.MISSING.toString(),
                                   auth.getCurrentUser().getUid(),
                                    auth.getCurrentUser().getEmail() );
                
                PetService.savePet( pet, o -> {
                    Toast.makeText(this, "Pet cadastrado com sucesso", Toast.LENGTH_SHORT).show();
                    finish();
                },
                 e -> Toast.makeText(this, "Erro ao salvar pet", Toast.LENGTH_SHORT).show() );
            }
        }
        
        catch ( IOException e ) 
        {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show();
        }
    }
    
    private File saveLocalImage( Bitmap bitmap ) 
    {
        File dir = new File( getExternalFilesDir( null ), "Pets" );
        if ( !dir.exists() ) dir.mkdirs();
        
        String fileName = "pet_" + System.currentTimeMillis() + ".jpg";
        File file = new File( dir, fileName );
        
        try ( FileOutputStream out = new FileOutputStream( file ) ) 
        {
            bitmap.compress( Bitmap.CompressFormat.JPEG, 90, out );
            
            return file;
        } 
        
        catch ( IOException e )
        {
            e.printStackTrace();
            return null;
        }
    }
    
    private void initComponents()
    {
        petImagePreview = findViewById( R.id.petImagePreview );
        inputName = findViewById( R.id.input_name );
        inputDesc = findViewById( R.id.input_desc );
        inputData = findViewById( R.id.input_dt );
        selectImageButton = findViewById( R.id.selectImageButton );
        savePetButton = findViewById( R.id.savePetButton );
        
        auth = FirebaseAuth.getInstance();
        
        selectImageButton.setOnClickListener( v -> openFileChooser() );
        savePetButton.setOnClickListener( v -> savePet() );
        
        inputData.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AddPetActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String dtSelected = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                        inputData.setText(dtSelected);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }
}
