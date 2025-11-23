package com.example.smartair.models;

import java.util.List;

public class Parent {
    public String parentUID;
    public String name;
    public String email;
    public String role;
    public String parentAccessCode;
    public List<String> childUIDs;
    public List<String> providerUIDs;

    public Parent() {}

    public Parent(String parentUID,
                  String name,
                  String email,
                  String role,
                  String parentAccessCode,
                  List<String> childUIDs,
                  List<String> providerUIDs) {
        this.parentUID = parentUID;
        this.name = name;
        this.email = email;
        this.role = role;
        this.parentAccessCode = parentAccessCode;
        this.childUIDs = childUIDs;
        this.providerUIDs = providerUIDs;
    }
}