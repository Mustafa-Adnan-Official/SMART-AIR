package com.example.smartair.models.providercollections;

import java.util.List;

/**
 * Purpose: Represents a child document stored under a parent link:
 *          providers/{providerUid}/parentLinks/{parentUid}/children/{childUid}.
 * Layer: Model
 * Used For: Showing which children a provider can view for a given parent,
 *           and which reports have been shared.
 */
public class LinkedChild {

    private String childUid;
    private String childName;
    private List<SharedReportRef> sharedReports;

    /** Required for Firebase deserialization. */
    public LinkedChild() {
    }

    public LinkedChild(String childUid,
                       String childName,
                       List<SharedReportRef> sharedReports) {
        this.childUid = childUid;
        this.childName = childName;
        this.sharedReports = sharedReports;
    }

    public String getChildUid() {
        return childUid;
    }

    public void setChildUid(String childUid) {
        this.childUid = childUid;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public List<SharedReportRef> getSharedReports() {
        return sharedReports;
    }

    public void setSharedReports(List<SharedReportRef> sharedReports) {
        this.sharedReports = sharedReports;
    }
}