package com.example.smartair.callbacks;

import com.example.smartair.models.RoleType;

public interface AuthResultCallback {
    void onSuccess(String uid);
    void onFailure(String errorMessage);
}
