package com.example.smartair.models.childcollections;


import java.util.List;

public class HistoryEntry {

    private String date; // Added date field
    private MedLog medlogRescue;
    private MedLog medlogController;

    private List<String> Symptoms;
    private List<String> Triggers;
    
    public HistoryEntry() {} // Default constructor for potential Firebase use

    public HistoryEntry(String date, MedLog medlogRescue, MedLog medlogController, List<String> Symptoms, List<String> Triggers){
        this.date = date;
        this.medlogRescue = medlogRescue;
        this.medlogController = medlogController;
        this.Symptoms = Symptoms;
        this.Triggers = Triggers;

    }

    // Overloaded constructor to maintain backward compatibility if needed, though I'll update usages
    public HistoryEntry(MedLog medlogRescue, MedLog medlogController, List<String> Symptoms, List<String> Triggers){
        this.medlogRescue = medlogRescue;
        this.medlogController = medlogController;
        this.Symptoms = Symptoms;
        this.Triggers = Triggers;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public MedLog getMedlogRescue(){
        return medlogRescue;
    }

    public void setMedlogRescue(MedLog medlogRescue){
        this.medlogRescue = medlogRescue;
    }


    public MedLog getMedlogController(){
        return medlogController;
    }

    public void setMedLogController(MedLog medlogController){
        this.medlogController = medlogController;
    }

    public List<String> getSymptoms(){
        return Symptoms;
    }

    public List<String> getTriggers(){
        return Triggers;
    }



}
