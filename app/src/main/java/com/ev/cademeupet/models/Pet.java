package com.ev.cademeupet.models;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class Pet
    implements Serializable 
{
public enum STATUS
{
    MISSING( "Desaparecido" ),
    FOUND( "Encontrado" );
    
    private final String label;
    
    STATUS( String label )
    {
        this.label = label;
    }
    
    @Override
    public String toString() 
    {
        return label;
    }
    
    public static STATUS fromLabel(String label) {
        for (STATUS s : values()) {
            if (s.label.equalsIgnoreCase(label)) return s;
        }
        return MISSING;
    }
}

    private String id;
    
    private String name;
    private String desc;
    private String dtMissing;
    private String imageUrl;
    private String status;
    private String ownerId;
    private String ownerEmail;
    
    public Pet() {}
    
    public Pet( String id, String name, String desc, String dtMissing, String imageUrl, String status, String ownerId, String email )
    {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.dtMissing = dtMissing;
        this.imageUrl = imageUrl;
        this.status = status;
        this.ownerId = ownerId;
        this.ownerEmail = email;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    public String getDtMissing() {
        return dtMissing;
    }
    
    public void setDtLost(String dtLost) {
        this.dtMissing = dtLost;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus( String status ) {
        this.status = status;
    }
    
    public String getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    
    public String getOwnerEmail() {
        return ownerEmail;
    }
    
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
    
    @Exclude
    public STATUS getStatusEnum() {
        if ( "Encontrado".equalsIgnoreCase( status ) ) return STATUS.FOUND;
        return STATUS.MISSING;
    }
    
    @Exclude
    public void setStatusEnum(STATUS statusEnum) 
    {
        this.status = statusEnum.toString();
    }
}

