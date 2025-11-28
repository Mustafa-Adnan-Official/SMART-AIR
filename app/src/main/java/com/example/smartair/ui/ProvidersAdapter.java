package com.example.smartair.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.models.Child;
import com.example.smartair.models.Parent;
import com.example.smartair.models.Provider;
import com.example.smartair.repositories.ChildRepository;
import com.example.smartair.repositories.ProviderRepository;

import java.util.List;

public class ProvidersAdapter extends RecyclerView.Adapter<ProvidersAdapter.ProviderViewHolder> {

    // List of Providers to display
    private List<Provider> providersList;
    private Context context;

    private ProviderRepository providerRepository;
    private ChildRepository childRepository;
    private Parent parent;
    private String childUid;

    public ProvidersAdapter(Context context, List<Provider> providersList, Parent parent){
        this.providersList = providersList;
        this.context = context;
        this.providerRepository = new ProviderRepository();
        this.parent = parent;
        this.childRepository = new ChildRepository();
    }

    public static class ProviderViewHolder extends RecyclerView.ViewHolder {

        public TextView nameTextView;
        public ImageButton deleteButton;
        public Button sendReportButton;
        public LinearLayout childModulesContainer;

        public ProviderViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.provider_text_name);
            deleteButton = itemView.findViewById(R.id.delete_child_button);
            sendReportButton = itemView.findViewById(R.id.send_realtime_report_button);
            childModulesContainer = itemView.findViewById(R.id.child_modules_list_container);
        }
    }

    @NonNull
    @Override
    public ProviderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.parent_settings_provider_card, parent, false); 
        return new ProviderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProviderViewHolder holder, int position) {
        Provider currentProvider = providersList.get(position);

        holder.nameTextView.setText(currentProvider.getName());
        
        if (holder.childModulesContainer != null) {
            holder.childModulesContainer.removeAllViews();
        }

        if (currentProvider.getChildren() != null && holder.childModulesContainer != null) {
            for (Child child : currentProvider.getChildren()) {
                if (parent != null && child.getParentUid() != null && 
                    child.getParentUid().equals(parent.getParentUid())) {
                    
                    childUid = child.getChildUid();
                    View childView = LayoutInflater.from(context).inflate(R.layout.provider_settings_child_module, holder.childModulesContainer, false);

                    Switch accessSwitch = childView.findViewById(R.id.provider_child_access_button);
                    Button manageDataBtn = childView.findViewById(R.id.manage_data_button);

                    accessSwitch.setText(child.getName());
                    
                    accessSwitch.setChecked(false); 

                    accessSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        providerRepository.setAccessBool(childUid, currentProvider.getProviderUid(), isChecked);
                        Toast.makeText(context, "Child Access Changed", Toast.LENGTH_SHORT).show();
                    });

                    manageDataBtn.setOnClickListener(v -> {
                        showManageDataDialog(context, child);
                    });
                    holder.childModulesContainer.addView(childView);
                }
            }
        }

        holder.deleteButton.setOnClickListener(v -> {
            showDeleteProviderDialog(context, currentProvider, position);
        });

        holder.sendReportButton.setOnClickListener(v -> {
            sendRealtimeReport(currentProvider);
        });
    }

    @Override
    public int getItemCount() {
        return providersList.size();
    }

    private void showManageDataDialog(Context context, Child child) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_manage_data, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // FIX: Cast to Button, not Switch. XML defines it as <Button>.
        Button closeButton = dialogView.findViewById(R.id.dialog_close_button);
        
        Switch toggleRescue = dialogView.findViewById(R.id.toggle_rescuelog_data);
        Switch toggleSymptoms = dialogView.findViewById(R.id.toggle_symptoms_tracking);
        Switch toggleTriggers = dialogView.findViewById(R.id.toggle_triggers_tracking);
        Switch togglePeakFlow = dialogView.findViewById(R.id.toggle_peakflow_tracking);
        Switch toggleTriageIncident = dialogView.findViewById(R.id.toggle_triageincidents_tracking);
        Switch toggleSummaryCharts = dialogView.findViewById(R.id.toggle_summarycharts_tracking);
        // Note: toggle_controlleradherence_logs is present in XML but was unused here. 
        // You can add it if needed:
        // Switch toggleController = dialogView.findViewById(R.id.toggle_controlleradherence_logs);

        // Setup dummy listeners
        if (toggleRescue != null) toggleRescue.setOnCheckedChangeListener((buttonView, isChecked) -> {});
        if (toggleSymptoms != null) toggleSymptoms.setOnCheckedChangeListener((buttonView, isChecked) -> {});
        if (toggleTriggers != null) toggleTriggers.setOnCheckedChangeListener((buttonView, isChecked) -> {});
        if (togglePeakFlow != null) togglePeakFlow.setOnCheckedChangeListener((buttonView, isChecked) -> {});
        if (toggleTriageIncident != null) toggleTriageIncident.setOnCheckedChangeListener((buttonView, isChecked) -> {});
        if (toggleSummaryCharts != null) toggleSummaryCharts.setOnCheckedChangeListener((buttonView, isChecked) -> {});

        closeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showDeleteProviderDialog(Context context, Provider provider, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_provider, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Button yesButton = dialogView.findViewById(R.id.dialog_yesdeleteprovider_button);
        Button noButton = dialogView.findViewById(R.id.dialog_nodeleteprovider_button);

        yesButton.setOnClickListener(v -> {
            providersList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, providersList.size());
            Toast.makeText(context, "Provider Removed", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        noButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void sendRealtimeReport(Provider currentProvider) {
        // Placeholder
    }
}
