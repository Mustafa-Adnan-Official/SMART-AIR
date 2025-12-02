package com.example.smartair.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartair.R;
import com.example.smartair.models.users.Child;
import com.example.smartair.repositories.ChildRepository;
import com.example.smartair.repositories.ParentRepository;
import com.example.smartair.models.childcollections.InventoryItem;
import com.example.smartair.repositories.InventoryRepository;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

// Imports for Inventory
import com.example.smartair.callbacks.ModelResultCallback;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.text.ParseException;

/**
 * This class is to connect the child UI elements to the child database and any data
 */
public class ChildrenAdapter extends RecyclerView.Adapter<ChildrenAdapter.ChildViewHolder> {

    private ChildRepository childRepository;
    private ParentRepository parentRepository;
    private String childUID;
    private String parentUID;
    private List<Child> childList;

    public ChildrenAdapter(List<Child> childList){
        this.childList = childList;
        childRepository = new ChildRepository();
        parentRepository = new ParentRepository();
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.parent_settings_child_card, parent, false);
        return new ChildViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Child currentChild = childList.get(position);
        childUID = currentChild.getChildUid();
        parentUID = currentChild.getParentUid();

        int childPB = currentChild.getPersonalBest();

        holder.nameTextView.setText(currentChild.getName());
        holder.pbSettingsTextView.setText("PB Setting: " + childPB);
        holder.zoneThresholdGreen.setText("• Green ≥ 80% PB = >" + childPB*0.8);
        holder.zoneThresholdYellow.setText("• Yellow 50-79% PB = " + childPB*0.5 + " to " + childPB*0.79);
        holder.zoneThresholdRed.setText("• Red <50% PB = " + "<" + childPB*0.5);
        holder.manageInventoryButton.setText("Manage Inventory");

        holder.deleteButton.setOnClickListener(v -> {
            showDeleteChildDialog(v.getContext(), currentChild, position);
        });

        holder.pbSettingsButton.setOnClickListener(v -> {
            showEditPBDialog(v.getContext(), currentChild);
        });

        holder.manageInventoryButton.setOnClickListener(v -> {
            showManageInventoryDialog(v.getContext(), currentChild, position);
        });
    }

    @Override
    public int getItemCount() {
        return childList.size();
    }

    public static class ChildViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public ImageButton deleteButton;

        public TextView pbSettingsTextView;
        public ImageButton pbSettingsButton;

        public TextView zoneThresholdGreen;
        public TextView zoneThresholdYellow;
        public TextView zoneThresholdRed;

        public Button manageInventoryButton;

        public ChildViewHolder(View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.child_name_textview);
            deleteButton = itemView.findViewById(R.id.delete_child_button);

            pbSettingsTextView = itemView.findViewById(R.id.pb_setting_textview);
            pbSettingsButton = itemView.findViewById(R.id.pb_setting_button);

            zoneThresholdGreen = itemView.findViewById(R.id.zone_threshold_green_text);
            zoneThresholdYellow = itemView.findViewById(R.id.zone_threshold_yellow_text);
            zoneThresholdRed = itemView.findViewById(R.id.zone_threshold_red_text);

            manageInventoryButton = itemView.findViewById(R.id.manage_inventory_button);
        }
    }

    public void addChild(Child newChild){
        childList.add(newChild);
        notifyItemInserted(childList.size() -1);
    }

    public void removeChild(int position) {
        childList.remove(position);
        notifyItemRemoved(position);
    }

    private void showDeleteChildDialog(Context context, Child currentChild, int position){
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_delete_child, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        Button yesButton = dialogView.findViewById(R.id.dialog_yesdeletechild_button);
        Button noButton = dialogView.findViewById(R.id.dialog_nodeletechild_button);

        yesButton.setOnClickListener(v -> {
            removeChild(position);
            dialog.dismiss();
        });

        noButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showEditPBDialog(Context context, Child currentChild) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_new_pb, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        EditText pbEditText = dialogView.findViewById(R.id.pb_edit_text);
        Button confirmButton = dialogView.findViewById(R.id.dialog_yesconfirmpb_button);
        Button cancelButton = dialogView.findViewById(R.id.dialog_noconfirmpb_button);

        pbEditText.setText(String.valueOf(currentChild.getPersonalBest()));

        confirmButton.setOnClickListener(v -> {
            String newValueString = pbEditText.getText().toString();

            if (!newValueString.isEmpty()){
                try {
                    int newValue = Integer.parseInt(newValueString);
                    currentChild.setPersonalBest(newValue);
                    childRepository.adjustPB(childUID, newValue);
                    notifyItemChanged(childList.indexOf(currentChild));
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Invalid input. Please enter a valid number.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showManageInventoryDialog(Context context, Child currentChild, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_manage_inventory, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        Button controllerButton = dialogView.findViewById(R.id.inventory_controller_button);
        Button rescueButton = dialogView.findViewById(R.id.inventory_rescue_button);
        Button newCanister = dialogView.findViewById(R.id.inventory_new_canister_button);
        ImageButton exitButton = dialogView.findViewById(R.id.inventory_exit_button);

        TextView purchaseDateText = dialogView.findViewById(R.id.purchase_date_text);
        TextView expirationDateText = dialogView.findViewById(R.id.expiration_date_text);
        TextView dailyUsesText = dialogView.findViewById(R.id.inventory_daily_uses_text);
        TextView inventoryAmountLeftText = dialogView.findViewById(R.id.inventory_amount_left_text);

        InventoryRepository inventoryRepository = new InventoryRepository();
        final InventoryItem[] fetchedItems = new InventoryItem[2]; 

        controllerButton.setSelected(true);
        rescueButton.setSelected(false);

        
        inventoryRepository.getInventoryForChild(currentChild.getChildUid(), new ModelResultCallback<List<InventoryItem>>() {
            @Override
            public void onSuccess(List<InventoryItem> result) {
                for (InventoryItem item : result) {
                    if ("controller".equalsIgnoreCase(item.getType())) {
                        fetchedItems[0] = item; // Store Controller
                    } else if ("rescue".equalsIgnoreCase(item.getType())) {
                        fetchedItems[1] = item; // Store Rescue
                    }
                }
                updateInventoryUI(fetchedItems[0], purchaseDateText, expirationDateText, dailyUsesText, inventoryAmountLeftText);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(context, "Failed to load inventory: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
         

        controllerButton.setOnClickListener(v -> {
            controllerButton.setSelected(true);
            rescueButton.setSelected(false);
            updateInventoryUI(fetchedItems[0], purchaseDateText, expirationDateText, dailyUsesText, inventoryAmountLeftText);
        });

        rescueButton.setOnClickListener(v -> {
            rescueButton.setSelected(true);
            controllerButton.setSelected(false);
            updateInventoryUI(fetchedItems[1], purchaseDateText, expirationDateText, dailyUsesText, inventoryAmountLeftText);
        });

        newCanister.setOnClickListener(v -> {
            showNewCanisterDialog(context, currentChild, position);
            dialog.dismiss();
        });

        exitButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }



    private void updateInventoryUI(InventoryItem item, TextView pDateView, TextView eDateView, TextView usesView, TextView amountView) {
        if (item == null) {
            pDateView.setText("Purchase Date: N/A");
            eDateView.setText("Expiration Date: N/A");
            usesView.setText("Expected Daily Uses: N/A");
            amountView.setText("Amount Left: N/A");
            return;
        }

        // Format Timestamp to String
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        String pDateStr = (item.getPurchaseDate() != null) ? sdf.format(item.getPurchaseDate().toDate()) : "N/A";
        String eDateStr = (item.getExpirationDate() != null) ? sdf.format(item.getExpirationDate().toDate()) : "N/A";

        pDateView.setText("Purchase Date: " + pDateStr);
        eDateView.setText("Expiration Date: " + eDateStr);
        usesView.setText("Expected Daily Uses: " + item.getExpectedDailyUses());

        int total = item.getTotalActuations();
        int remaining = item.getDosesRemaining();
        int percentage = (total > 0) ? (remaining * 100) / total : 0;
        amountView.setText("Amount Left: " + percentage + "% (" + remaining + "/" + total + ")");
    }


    private void showNewCanisterDialog(Context context, Child currentChild, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_new_canister, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        
        TextInputEditText purchaseDate = dialogView.findViewById(R.id.canister_purchase_date_input);
        TextInputEditText expirationDate = dialogView.findViewById(R.id.canister_expiration_date_input);
        TextInputEditText dailyUses = dialogView.findViewById(R.id.canister_daily_uses_input);
        TextInputEditText totalActuations = dialogView.findViewById(R.id.canister_total_actuations_input);
        RadioGroup typeRadioGroup = dialogView.findViewById(R.id.canister_type_radio_group);
        
        Button confirmButton = dialogView.findViewById(R.id.confirm_purchase_button);
        Button exitButton = dialogView.findViewById(R.id.exit_canister_purchase_button);

        confirmButton.setOnClickListener(v -> {
            String purchaseDateString = purchaseDate.getText().toString();
            String expirationDateString = expirationDate.getText().toString();
            String dailyUsesString = dailyUses.getText().toString();
            String totalActuationsString = totalActuations.getText().toString();

            int selectedId = typeRadioGroup.getCheckedRadioButtonId();
            String type = "controller";
            if (selectedId == R.id.radio_rescue) {
                type = "rescue";
            }

            if (purchaseDateString.isEmpty() || expirationDateString.isEmpty() || dailyUsesString.isEmpty() || totalActuationsString.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

            try {
                int dailyUsesInt = Integer.parseInt(dailyUsesString);
                int totalActuationsInt = Integer.parseInt(totalActuationsString);
                
                // Parse Dates
                Date pDate = sdf.parse(purchaseDateString);
                Date eDate = sdf.parse(expirationDateString);

                InventoryItem newItem = new InventoryItem();
                newItem.setType(type);
                newItem.setPurchaseDate(new Timestamp(pDate));
                newItem.setExpirationDate(new Timestamp(eDate));
                newItem.setExpectedDailyUses(dailyUsesInt);
                newItem.setTotalActuations(totalActuationsInt);
                newItem.setDosesRemaining(totalActuationsInt);


                InventoryRepository repo = new InventoryRepository();
                repo.saveItem(currentChild.getChildUid(), newItem, new ModelResultCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        Toast.makeText(context, "Canister Saved", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(context, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });


            } catch (NumberFormatException e) {
                Toast.makeText(context, "Invalid number format", Toast.LENGTH_SHORT).show();
            } catch (ParseException e) {
                Toast.makeText(context, "Invalid date format. Use dd/MM/yyyy", Toast.LENGTH_SHORT).show();
            }

        });


        exitButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
