package com.example.smartair.models.providercollections;

/**
 * Purpose: Represents a parent link document stored in
 *          providers/{providerUid}/parentLinks/{parentUid}.
 * Layer: Model
 * Used For: Listing parents connected to a provider and
 *           loading their linked children.
 */
public class ParentLink {

    private String parentUid;
    private String parentEmail;
    private String parentName;

    /** Required for Firebase deserialization. */
    public ParentLink() {
    }

    public ParentLink(String parentUid, String parentEmail, String parentName) {
        this.parentUid = parentUid;
        this.parentEmail = parentEmail;
        this.parentName = parentName;
    }

    public String getParentUid() {
        return parentUid;
    }

    public void setParentUid(String parentUid) {
        this.parentUid = parentUid;
    }

    public String getParentEmail() {
        return parentEmail;
    }

    public void setParentEmail(String parentEmail) {
        this.parentEmail = parentEmail;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
}