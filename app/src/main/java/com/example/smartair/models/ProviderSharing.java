package com.example.smartair.models;

public class ProviderSharing {
    public String providerUID;
    public ChildToggles toggles;

    public ProviderSharing() {}

    public ProviderSharing(String providerUID, ChildToggles toggles) {
        this.providerUID = providerUID;
        this.toggles = toggles;
    }
}
