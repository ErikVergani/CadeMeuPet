package com.ev.cademeupet.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class Sighting implements Serializable {
    
    private String id;
    private String petId;
    private String reporterId;
    private String reporterName; // Adicionado para facilitar a exibição
    private Timestamp sightingDate;
    private String location; // Pode ser um endereço ou coordenadas
    private String message;
    private String status; // "Pendente", "Confirmado", "Rejeitado"
    
    public Sighting() {
        // Construtor vazio necessário para o Firestore
    }
    
    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getPetId() { return petId; }
    public void setPetId(String petId) { this.petId = petId; }
    
    public String getReporterId() { return reporterId; }
    public void setReporterId(String reporterId) { this.reporterId = reporterId; }
    
    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    
    public Timestamp getSightingDate() { return sightingDate; }
    public void setSightingDate(Timestamp sightingDate) { this.sightingDate = sightingDate; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
