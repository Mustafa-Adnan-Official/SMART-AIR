package com.example.smartair.ui.r3.meds;

import androidx.annotation.Nullable;

import com.example.smartair.models.childcollections.AchievementSummary;
import com.example.smartair.models.childcollections.Alert;
import com.example.smartair.models.childcollections.InventoryItem;
import com.example.smartair.models.childcollections.MedLog;
import com.example.smartair.services.AchievementService;
import com.example.smartair.services.InventoryService;
import com.example.smartair.services.MedLogService;
import com.google.firebase.Timestamp;

import java.util.List;

/**
 * Presenter/logic for the "Log Medicine" screens (rescue & controller).
 * Talks to MedLogService + InventoryService + AchievementService.
 */
public class ChildMedLogPresenter {

    public interface View {
        void showLoading();
        void hideLoading();
        void showSuccess(MedLog log);
        void showError(String message);
    }

    private final View view;
    private final MedLogService medLogService;
    private final InventoryService inventoryService;
    private final AchievementService achievementService;

    public ChildMedLogPresenter(
            View view,
            MedLogService medLogService,
            InventoryService inventoryService,
            AchievementService achievementService
    ) {
        this.view = view;
        this.medLogService = medLogService;
        this.inventoryService = inventoryService;
        this.achievementService = achievementService;
    }

    // --- RESCUE DOSE ---

    public void logRescueDose(
            String childUid,
            long doseCount,
            @Nullable Long peakFlow,
            @Nullable List<String> symptoms,
            @Nullable String feelingBefore,
            @Nullable String feelingAfter,
            @Nullable String breathRating
    ) {
        view.showLoading();

        medLogService.logRescueDose(
                childUid,
                doseCount,
                peakFlow,
                symptoms,
                feelingBefore,
                feelingAfter,
                breathRating,
                new MedLogService.MedLogCallback() {
                    @Override
                    public void onSuccess(MedLog savedLog) {
                        // Update inventory
                        inventoryService.updateInventoryAfterDose(
                                childUid,
                                "rescue",
                                doseCount,
                                new InventoryService.InventoryCallback() {
                                    @Override
                                    public void onSuccess(InventoryItem item, @Nullable Alert alert) {
                                        // Update motivation (low-rescue month, etc.)
                                        achievementService.updateAfterRescueDose(
                                                childUid,
                                                Timestamp.now(),
                                                new AchievementService.AchievementCallback() {
                                                    @Override
                                                    public void onSuccess(AchievementSummary summary) {
                                                        view.hideLoading();
                                                        view.showSuccess(savedLog);
                                                    }

                                                    @Override
                                                    public void onError(Exception e) {
                                                        view.hideLoading();
                                                        view.showError(e.getMessage());
                                                    }
                                                }
                                        );
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        view.hideLoading();
                                        view.showError(e.getMessage());
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(Exception e) {
                        view.hideLoading();
                        view.showError(e.getMessage());
                    }
                }
        );
    }

    // --- CONTROLLER DOSE ---

    public void logControllerDose(
            String childUid,
            long doseCount,
            boolean techniqueUsed,
            @Nullable Long peakFlow,
            @Nullable List<String> symptoms,
            @Nullable String feelingBefore,
            @Nullable String feelingAfter,
            @Nullable String breathRating
    ) {
        view.showLoading();

        medLogService.logControllerDose(
                childUid,
                doseCount,
                techniqueUsed,
                peakFlow,
                symptoms,
                feelingBefore,
                feelingAfter,
                breathRating,
                new MedLogService.MedLogCallback() {
                    @Override
                    public void onSuccess(MedLog savedLog) {
                        inventoryService.updateInventoryAfterDose(
                                childUid,
                                "controller",
                                doseCount,
                                new InventoryService.InventoryCallback() {
                                    @Override
                                    public void onSuccess(InventoryItem item, @Nullable Alert alert) {
                                        achievementService.updateAfterControllerDose(
                                                childUid,
                                                Timestamp.now(),
                                                new AchievementService.AchievementCallback() {
                                                    @Override
                                                    public void onSuccess(AchievementSummary summary) {
                                                        view.hideLoading();
                                                        view.showSuccess(savedLog);
                                                    }

                                                    @Override
                                                    public void onError(Exception e) {
                                                        view.hideLoading();
                                                        view.showError(e.getMessage());
                                                    }
                                                }
                                        );
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        view.hideLoading();
                                        view.showError(e.getMessage());
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(Exception e) {
                        view.hideLoading();
                        view.showError(e.getMessage());
                    }
                }
        );
    }
}