package com.ev.cademeupet.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class Sighting implements Serializable {
    
    public enum STATUS {
        PENDING,
        CONFIRMED,
        REJECTED;
    }
    
    private String id;
    private String petId;
    private String reporterId;
    private String reporterName;
    private Timestamp sightingDate;
    private String location;
    private String message;
    private String status; 
    
    public Sighting() {
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
    
    @Exclude
    public STATUS getStatusEnum() {
        if (status == null) return STATUS.PENDING;

        if (status.equalsIgnoreCase("Pendente")) return STATUS.PENDING;
        if (status.equalsIgnoreCase("Confirmado")) return STATUS.CONFIRMED;
        if (status.equalsIgnoreCase("Rejeitado")) return STATUS.REJECTED;

        try {
            return STATUS.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return STATUS.PENDING;
        }
    }

    @Exclude
    public void setStatusEnum(STATUS statusEnum) {
        this.status = statusEnum.name();
    }
}
