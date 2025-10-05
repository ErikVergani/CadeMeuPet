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

import com.bumptech.glide.Glide;
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
    private TextView title;
    
    private Uri imageUri;
    private FirebaseAuth auth;
    private Pet petToEdit;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);
        
        initComponents();
        loadDataIfEditing();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            petImagePreview.setImageURI(imageUri);
        }
    }
    
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    private void savePet() {
        String name = inputName.getText().toString().trim();
        String desc = inputDesc.getText().toString().trim();
        String data = inputData.getText().toString().trim();
        
        if (name.isEmpty() || desc.isEmpty() || data.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Se for um novo pet, a imagem é obrigatória
        if (petToEdit == null && imageUri == null) {
            Toast.makeText(this, "Selecione uma imagem para o pet", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String petId = (petToEdit != null) ? petToEdit.getId() : UUID.randomUUID().toString();
        String ownerId = auth.getCurrentUser().getUid();
        String ownerEmail = auth.getCurrentUser().getEmail();
        String status = (petToEdit != null) ? petToEdit.getStatus() : Pet.STATUS.MISSING.toString();
        String imageUrl = (petToEdit != null) ? petToEdit.getImageUrl() : ""; // Será atualizado se nova imagem for selecionada
        
        Pet pet = new Pet(petId, name, desc, data, imageUrl, status, ownerId, ownerEmail);
        
        if (imageUri != null) {
            // Lógica de salvar imagem (a mesma que você já tem)
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                File imageFile = saveLocalImage(bitmap);
                if (imageFile != null) {
                    pet.setImageUrl(imageFile.getAbsolutePath());
                    savePetToFirestore(pet);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Nenhuma nova imagem selecionada, apenas salvar as outras informações
            savePetToFirestore(pet);
        }
    }
    
    private void savePetToFirestore(Pet pet) {
        PetService.savePet(pet, o -> {
            Toast.makeText(this, "Pet salvo com sucesso", Toast.LENGTH_SHORT).show();
            finish();
        }, e -> Toast.makeText(this, "Erro ao salvar pet", Toast.LENGTH_SHORT).show());
    }
    
    private File saveLocalImage(Bitmap bitmap) {
        File dir = new File(getExternalFilesDir(null), "Pets");
        if (!dir.exists()) dir.mkdirs();
        String fileName = "pet_" + System.currentTimeMillis() + ".jpg";
        File file = new File(dir, fileName);
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void loadDataIfEditing() {
        petToEdit = (Pet) getIntent().getSerializableExtra("pet_to_edit");
        
        if (petToEdit != null) {
            title.setText("Editar Informações do Pet");
            inputName.setText(petToEdit.getName());
            inputDesc.setText(petToEdit.getDesc());
            inputData.setText(petToEdit.getDtMissing());
            
            // A imagem pode ser um caminho local ou uma URL do Glide
            if (petToEdit.getImageUrl() != null && !petToEdit.getImageUrl().isEmpty()) {
                Glide.with(this).load(petToEdit.getImageUrl()).into(petImagePreview);
            }
        }
    }
    
    
    private void initComponents() {
        petImagePreview = findViewById(R.id.img_pet_preview);
        inputName = findViewById(R.id.tf_pet_name);
        inputDesc = findViewById(R.id.tf_pet_desc);
        inputData = findViewById(R.id.tf_dt_miss);
        selectImageButton = findViewById(R.id.btn_select_img);
        savePetButton = findViewById(R.id.btn_save_pet);
        title = findViewById(R.id.add_pet_title);
        
        auth = FirebaseAuth.getInstance();
        
        selectImageButton.setOnClickListener(v -> openFileChooser());
        savePetButton.setOnClickListener(v -> savePet());
        
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
                    year, month, day);
            datePickerDialog.show();
        });
    }
}