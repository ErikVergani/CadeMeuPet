package com.ev.cademeupet;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ev.cademeupet.activities.AddPetActivity;
import com.ev.cademeupet.activities.EditUser;
import com.ev.cademeupet.adapters.PetAdapter;
import com.ev.cademeupet.models.Pet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private RecyclerView petsRecyclerView;
    private FloatingActionButton addPetFab;
    private List<Pet> petList = new ArrayList<>();
    private List<Pet> allPets = new ArrayList<>();
    private PetAdapter petAdapter;
    
    private FirebaseFirestore db;
    
    @Override
    protected void onCreate( Bundle savedInstanceState ) 
    {
        super.onCreate( savedInstanceState );
        
        setContentView( R.layout.activity_main );
        
        Toolbar toolbar = findViewById( R.id.toolbar );
        toolbar.setBackgroundColor( Color.parseColor( "#6200EE" ) );
        setSupportActionBar(toolbar);
        
        petsRecyclerView = findViewById( R.id.rv_pet);
        addPetFab = findViewById( R.id.float_add);
        
        db = FirebaseFirestore.getInstance();
        
        petAdapter = new PetAdapter( this, petList );
        petsRecyclerView.setLayoutManager( new LinearLayoutManager( this ) );
        petsRecyclerView.setAdapter( petAdapter );
        
        addPetFab.setOnClickListener(v -> startActivity( new Intent( this, AddPetActivity.class ) ) );
        
        loadPets();
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection( "pets" )
                .addSnapshotListener( ( value, error ) -> {
                            if (error != null) {
                                Log.w("FirestoreListener", "Listen failed.", error);
                                return;
                            }
                            
                            List<Pet> petList = new ArrayList<>();
                            
                            for (QueryDocumentSnapshot doc : value) {
                                Pet pet = doc.toObject(Pet.class);
                                petList.add(pet);
                            }
                            
                            Collections.sort(petList, (p1, p2) -> {
                                try 
                                {
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                                    
                                    Date d1 = sdf.parse( p1.getDtMissing() );
                                    Date d2 = sdf.parse( p2.getDtMissing() );
                                    
                                    return d1.compareTo( d2 );
                                } 
                                
                                catch ( Exception e ) 
                                {
                                    return 0;
                                }
                            });
                    
                    this.allPets = petList;
                    petAdapter.updateList( petList );
                });
                
        Spinner spinnerfilter = findViewById( R.id.filter_spinner );
        
        spinnerfilter.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() 
        {
            @Override
            public void onItemSelected( AdapterView<?> parent, View view, int position, long id )
            {
                String selectedFilter = parent.getItemAtPosition( position ).toString();
                appyFilter( selectedFilter );
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) 
    {
        getMenuInflater().inflate( R.menu.menu_main, menu );
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) 
    {
        if ( item.getItemId() == R.id.menu_editar_perfil )
        {
            startActivity( new Intent(this, EditUser.class ) );
            return true;
        }
        
        return super.onOptionsItemSelected( item );
    }
    
    private void appyFilter(String filter ) 
    {
        List<Pet> filteredList = new ArrayList<>();
        
        if ( filter.equals( "Todos" ) )
        {
            petAdapter.updateList( allPets );
            return;
        }
        
        for ( Pet pet : allPets ) 
        {
            if ( pet.getStatus().toString().equalsIgnoreCase( filter ) ) 
            {
                filteredList.add( pet );
            }
        }
        
        petAdapter.updateList( filteredList );
    }
    
    
    private void loadPets() 
    {
        db.collection( "pets" ).get()
          .addOnSuccessListener(queryDocumentSnapshots -> 
          {
            petList.clear();
            
            for ( DocumentSnapshot doc : queryDocumentSnapshots )
            {
                Pet pet = doc.toObject( Pet.class );
                petList.add( pet );
            }
            petAdapter.notifyDataSetChanged();
          });
    }
}
