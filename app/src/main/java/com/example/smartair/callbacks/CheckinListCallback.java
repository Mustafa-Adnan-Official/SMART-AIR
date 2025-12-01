package com.example.smartair.callbacks;
import java.util.List;
import com.example.smartair.models.childcollections.Checkin;

public interface CheckinListCallback {
    void onSuccess(List<Checkin> checkins);
    void onFailure(Exception e);
}
