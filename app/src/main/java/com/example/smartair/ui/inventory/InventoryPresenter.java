package com.example.smartair.ui.inventory;

import com.example.smartair.models.childcollections.Alert;
import com.example.smartair.models.childcollections.InventoryItem;
import com.example.smartair.services.InventoryService;

/**
 * Presenter for the Inventory screen (parent-facing).
 */
public class InventoryPresenter {

    public interface View {
        void showLoading();
        void hideLoading();
        void showInventory(InventoryItem item);
        void showAlert(Alert alert);
        void showError(String message);
    }

    private final View view;
    private final InventoryService inventoryService;

    public InventoryPresenter(View view, InventoryService inventoryService) {
        this.view = view;
        this.inventoryService = inventoryService;
    }

    /**
     * Called by Activity when it wants to refresh inventory data
     * (e.g. onResume or pull-to-refresh).
     */
    public void refreshInventory(String childUid, String medicineType) {
        view.showLoading();

        // You’ll eventually implement a proper "getInventory" in InventoryService.
        // For now, you can stub it or call updateInventoryAfterDose with doseCount=0.
        inventoryService.updateInventoryAfterDose(
                childUid,
                medicineType,
                0,
                new InventoryService.InventoryCallback() {
                    @Override
                    public void onSuccess(InventoryItem updatedItem, Alert maybeNewAlert) {
                        view.hideLoading();
                        if (updatedItem != null) {
                            view.showInventory(updatedItem);
                        }
                        if (maybeNewAlert != null) {
                            view.showAlert(maybeNewAlert);
                        }
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
