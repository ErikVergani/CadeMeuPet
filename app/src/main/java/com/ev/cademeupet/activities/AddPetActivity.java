package com.ev.cademeupet.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ev.cademeupet.R;
import com.ev.cademeupet.models.Pet;
import com.ev.cademeupet.services.PetService;
import com.google.firebase.auth.FirebaseAuth;

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
        loadData();
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
        String name = inputName.getText().toString().trim().toLowerCase();
        String desc = inputDesc.getText().toString().trim().toLowerCase();
        String data = inputData.getText().toString().trim().toLowerCase();
        
        if ( name.isEmpty() || desc.isEmpty() || data.isEmpty() || imageUri == null )
        {
            Toast.makeText(this, "Preencha todos os campos e selecione uma imagem", Toast.LENGTH_SHORT ).show();
            return;
        }
        
        try 
        {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap( getContentResolver(), imageUri );
            
            Matrix matrix = new Matrix();
            matrix.postRotate( 90 );
            bitmap = Bitmap.createBitmap(
                    bitmap,
                    0, 0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    matrix,
                    true );
                    
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
                    Toast.makeText(this,  "Pet cadastrado com sucesso", Toast.LENGTH_SHORT ).show();
                    finish();
                },
                 e -> Toast.makeText( this, "Erro ao salvar pet", Toast.LENGTH_SHORT ).show() );
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
    
    private void loadData()
    {
        Pet pet = Pet.class.cast( getIntent().getSerializableExtra( "pet" ) );
        
        if ( pet != null )
        {
            ((TextView) findViewById(R.id.add_pet_title)).setText( "Informações do PET" );
            inputName.setText( pet.getName() );
            inputDesc.setText( pet.getDesc() );
            inputData.setText( pet.getDtMissing() );
            
            File imgFile = new File( pet.getImageUrl() );
            
            if ( imgFile.exists() )
            {
                Bitmap bitmap = BitmapFactory.decodeFile( imgFile.getAbsolutePath() );
                
                petImagePreview.setImageBitmap( bitmap );
            }
        }
    }
    
    
    private void initComponents()
    {
        petImagePreview = findViewById( R.id.img_pet_preview);
        inputName = findViewById( R.id.tf_pet_name);
        inputDesc = findViewById( R.id.tf_pet_desc);
        inputData = findViewById( R.id.tf_dt_miss);
        selectImageButton = findViewById( R.id.btn_select_img);
        savePetButton = findViewById( R.id.btn_save_pet);
        
        boolean viewMode = getIntent().getBooleanExtra( "view_mode", false );
        
        if ( viewMode )
        {
            savePetButton.setVisibility( View.GONE );
            selectImageButton.setVisibility( View.GONE );
            
            inputName.setEnabled( false );
            inputDesc.setEnabled( false );
            inputData.setEnabled( false );
            
            ViewGroup.LayoutParams params = petImagePreview.getLayoutParams();
            params.height = (int)( 475 * getResources().getDisplayMetrics().density + 0.5f );
            petImagePreview.setLayoutParams( params );
        }
        
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
