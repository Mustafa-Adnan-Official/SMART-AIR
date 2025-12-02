package com.example.smartair.callbacks;

import java.util.List;

public interface StringListCallback {
    void onSuccess(List<String> resultList);
    void onFailure(Exception e);
}