package com.example.smartair.ui.r3.motivation;

import androidx.annotation.NonNull;

import com.example.smartair.models.childcollections.AchievementSummary;
import com.example.smartair.services.AchievementService;

public class MotivationPresenter {

    public interface View {
        void showLoading();
        void hideLoading();
        void showAchievements(@NonNull AchievementSummary summary);
        void showError(@NonNull String message);
    }

    private final String childUid;
    private final AchievementService achievementService;
    private final View view;

    public MotivationPresenter(@NonNull String childUid,
                               @NonNull AchievementService achievementService,
                               @NonNull View view) {
        this.childUid = childUid;
        this.achievementService = achievementService;
        this.view = view;
    }

    /** Just fetch the current summary – no mutation. */
    public void loadAchievements() {
        view.showLoading();

        achievementService.getCurrentSummary(childUid,
                new AchievementService.AchievementCallback() {
                    @Override
                    public void onSuccess(AchievementSummary summary) {
                        view.hideLoading();
                        view.showAchievements(summary);
                    }

                    @Override
                    public void onError(Exception e) {
                        view.hideLoading();
                        view.showError(
                                e != null ? e.getMessage() : "Failed to load achievements"
                        );
                    }
                });
    }
}