package com.example.smartair.models;

import java.util.List;

public class Provider {
    public String providerUID;
    public String prefix;
    public String name;
    public String email;
    public String role;

    public Provider() { }

    public Provider(String providerUID,
                    String prefix,
                    String name,
                    String email,
                    String role) {
        this.providerUID = providerUID;
        this.prefix = prefix;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
