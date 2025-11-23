package com.example.smartair.models;

import java.util.List;

public class Child {
    public String childUID;
    public String parentUID;
    public String name;
    public String email;
    public String role;
    public int personalBest;
    public int controllerUses;
    public int reportDuration;
    public boolean hasOwnEmail;
    public List<ProviderSharing> providerSharingList;

    public Child() {}

    public Child(String childUID,
                 String parentUID,
                 String name,
                 String email,
                 String role,
                 int personalBest,
                 int controllerUses,
                 int reportDuration,
                 boolean hasOwnEmail,
                 List<ProviderSharing> providerSharingList) {
        this.childUID = childUID;
        this.parentUID = parentUID;
        this.name = name;
        this.email = email;
        this.role = role;
        this.personalBest = personalBest;
        this.controllerUses = controllerUses;
        this.reportDuration = reportDuration;
        this.hasOwnEmail = hasOwnEmail;
        this.providerSharingList = providerSharingList;
    }
}
