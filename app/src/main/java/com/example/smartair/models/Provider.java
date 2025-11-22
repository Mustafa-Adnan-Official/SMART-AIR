package com.example.smartair.models;

import com.google.firebase.Timestamp;

public class Provider {

    private String providerUid;
    private String prefix;       // "Dr.", etc.
    private String name;
    private String email;
    private boolean onboarded;
    private Timestamp createdAt;

    public Provider() {}

    public Provider(String providerUid,
                    String prefix,
                    String name,
                    String email,
                    boolean onboarded,
                    Timestamp createdAt) {
        this.providerUid = providerUid;
        this.prefix = prefix;
        this.name = name;
        this.email = email;
        this.onboarded = onboarded;
        this.createdAt = createdAt;
    }

    public String getProviderUid() {
        return providerUid;
    }

    public void setProviderUid(String providerUid) {
        this.providerUid = providerUid;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isOnboarded() {
        return onboarded;
    }

    public void setOnboarded(boolean onboarded) {
        this.onboarded = onboarded;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
