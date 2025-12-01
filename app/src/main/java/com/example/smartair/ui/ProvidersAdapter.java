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
import com.example.smartair.models.users.Child;
import com.example.smartair.models.users.Parent;
import com.example.smartair.models.users.Provider;
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

        public Button sendHistoryButton;
        public LinearLayout childModulesContainer;

        public ProviderViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.providerHeader);
            deleteButton = itemView.findViewById(R.id.delete_child_button);
            sendReportButton = itemView.findViewById(R.id.sendReportBtn);
            childModulesContainer = itemView.findViewById(R.id.childrenContainer);
            sendHistoryButton = itemView.findViewById(R.id.sendHistoryBtn);
        }
    }

    @NonNull
    @Override
    public ProviderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.provider_card, parent, false);
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
                    View childView = LayoutInflater.from(context).inflate(R.layout.provider_child_row, holder.childModulesContainer, false);

                    TextView childName = childView.findViewById(R.id.childName);

                    Switch accessSwitch = childView.findViewById(R.id.switch_child);

                    childName.setText(child.getName());

                    accessSwitch.setChecked(false); 

                    accessSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        providerRepository.setAccessBool(childUid, currentProvider.getProviderUid(), isChecked);
                        Toast.makeText(context, "Child Access Changed", Toast.LENGTH_SHORT).show();
                    });
                    holder.childModulesContainer.addView(childView);
                }
            }
        }

        holder.deleteButton.setOnClickListener(v -> {
            showDeleteProviderDialog(context, currentProvider, position);
        });

        holder.sendReportButton.setOnClickListener(v -> {
            showManageDataDialog(context, currentProvider);
        });

        holder.sendHistoryButton.setOnClickListener(v -> {
            showHistorySymptoms(context, currentProvider);
        });
    }

    @Override
    public int getItemCount() {
        return providersList.size();
    }


    private void showHistorySymptoms(Context context, Provider provider) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.select_symptoms, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Switch toggleCoughing = dialogView.findViewById(R.id.switch_coughing);
        Switch toggleShortness = dialogView.findViewById(R.id.switch_shortness);
        Switch toggleTightness = dialogView.findViewById(R.id.switch_chesttightness);

        Button closeButton = dialogView.findViewById(R.id.btnExit);
        Button nextButton = dialogView.findViewById(R.id.btnNext);

        if (provider.getChildren() != null) {
            for (Child child : provider.getChildren()) {
                if (parent != null && child.getParentUid() != null &&
                        child.getParentUid().equals(parent.getParentUid())) {

                    childUid = child.getChildUid();
                    View symptomView = LayoutInflater.from(context).inflate(R.layout.symptoms_child_module, dialogView.findViewById(R.id.containerSymptoms), false);

                    //Get CheckIn Data into list from Child


                    TextView symptomName = symptomView.findViewById(R.id.symptomName);
                    Switch symptomSwitch = symptomView.findViewById(R.id.switch_symptom);

                    symptomSwitch.setChecked(false);

                }
            }
        }


        toggleCoughing.setOnCheckedChangeListener((buttonView, isChecked) -> {
            //TODO: Implementation
        });

        toggleShortness.setOnCheckedChangeListener((buttonView, isChecked) -> {

            //TODO: Implementation
        });

        toggleTightness.setOnCheckedChangeListener((buttonView, isChecked) -> {

            //TODO: Implementation
        });

        closeButton.setOnClickListener(v -> dialog.dismiss());
        nextButton.setOnClickListener(v -> {

            showTriggersSymptoms(context, provider);
        });
        dialog.show();


    }
    private void showTriggersSymptoms(Context context, Provider provider) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.select_triggers, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Switch toggleDustMite = dialogView.findViewById(R.id.switch_dustmite);
        Switch togglePets = dialogView.findViewById(R.id.switch_pets);
        Switch toggleSmoke = dialogView.findViewById(R.id.switch_smoke);
        Switch toggleOdor = dialogView.findViewById(R.id.switch_odors);
        Switch toggleColdAir = dialogView.findViewById(R.id.switch_coldair);
        Switch toggleIllness = dialogView.findViewById(R.id.switch_illness);
        Switch toggleExercise = dialogView.findViewById(R.id.switch_exercise);


        Button closeButton = dialogView.findViewById(R.id.btnBack);
        Button nextButton = dialogView.findViewById(R.id.btnBack);

        toggleDustMite.setOnCheckedChangeListener((buttonView, isChecked) -> {
            //TODO: Implementation
        });

        togglePets.setOnCheckedChangeListener((buttonView, isChecked) -> {

            //TODO: Implementation
        });

        toggleSmoke.setOnCheckedChangeListener((buttonView, isChecked) -> {

            //TODO: Implementation
        });

        toggleOdor.setOnCheckedChangeListener((buttonView, isChecked) -> {

            //TODO: Implementation
        });

        toggleColdAir.setOnCheckedChangeListener((buttonView, isChecked) -> {

            //TODO: Implementation
        });

        toggleIllness.setOnCheckedChangeListener((buttonView, isChecked) -> {

            //TODO: Implementation
        });

        toggleExercise.setOnCheckedChangeListener((buttonView, isChecked) -> {

            //TODO: Implementation
        });

        closeButton.setOnClickListener(v -> dialog.dismiss());
        nextButton.setOnClickListener(v -> {

            showSelectDurationDialog(context, provider, true);
        });




    }
    private void showManageDataDialog(Context context, Provider provider) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_manage_data, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // FIX: Cast to Button, not Switch. XML defines it as <Button>.
        Button closeButton = dialogView.findViewById(R.id.dialog_close_button);
        Button nextButton = dialogView.findViewById(R.id.dialog_next_button);
        
        Switch toggleRescue = dialogView.findViewById(R.id.toggle_rescuelog_data);
        Switch toggleSymptoms = dialogView.findViewById(R.id.toggle_symptoms_tracking);
        Switch toggleTriggers = dialogView.findViewById(R.id.toggle_triggers_tracking);
        Switch togglePeakFlow = dialogView.findViewById(R.id.toggle_peakflow_tracking);
        Switch toggleTriageIncident = dialogView.findViewById(R.id.toggle_triageincidents_tracking);
        Switch toggleSummaryCharts = dialogView.findViewById(R.id.toggle_summarycharts_tracking);

        if (toggleRescue != null)
            toggleRescue.setOnCheckedChangeListener((buttonView, isChecked) -> {
                //TODO: Set Report Toggle
            });
        if (toggleSymptoms != null)
            toggleSymptoms.setOnCheckedChangeListener((buttonView, isChecked) -> {
                //TODO: Set Report Toggle
            });
        if (toggleTriggers != null)
            toggleTriggers.setOnCheckedChangeListener((buttonView, isChecked) -> {
                //TODO: Set Report Toggle
            });
        if (togglePeakFlow != null)
            togglePeakFlow.setOnCheckedChangeListener((buttonView, isChecked) -> {
                //TODO: Set Report Toggle
            });
        if (toggleTriageIncident != null)
            toggleTriageIncident.setOnCheckedChangeListener((buttonView, isChecked) -> {
                //TODO: Set Report Toggle
            });
        if (toggleSummaryCharts != null)
            toggleSummaryCharts.setOnCheckedChangeListener((buttonView, isChecked) -> {
                //TODO: Set Report Toggle
            });

        closeButton.setOnClickListener(v -> dialog.dismiss());
        nextButton.setOnClickListener(v -> {
            showSelectDurationDialog(context, provider, false);
            dialog.dismiss();

        });
        dialog.show();
    }

    private void showSelectDurationDialog(Context context, Provider provider, Boolean isHistory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.select_duration, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Button btn3mo = dialogView.findViewById(R.id.btn3mo);
        Button btn4mo = dialogView.findViewById(R.id.btn4mo);
        Button btn5mo = dialogView.findViewById(R.id.btn5mo);
        Button btn6mo = dialogView.findViewById(R.id.btn6mo);

        Button btnSend = dialogView.findViewById(R.id.btnSend);
        Button btnBack = dialogView.findViewById(R.id.btnBack);

        btn3mo.setOnClickListener(v -> {
            btn3mo.setSelected(true);
            btn4mo.setSelected(false);
            btn5mo.setSelected(false);
            btn6mo.setSelected(false);
        });

        btn4mo.setOnClickListener(v -> {
            btn3mo.setSelected(false);
            btn4mo.setSelected(true);
            btn5mo.setSelected(false);
            btn6mo.setSelected(false);

        });

        btn5mo.setOnClickListener(v -> {
            btn3mo.setSelected(false);
            btn4mo.setSelected(false);
            btn5mo.setSelected(true);
            btn6mo.setSelected(false);

        });

        btn6mo.setOnClickListener(v -> {
            btn3mo.setSelected(false);
            btn4mo.setSelected(false);
            btn5mo.setSelected(false);
            btn6mo.setSelected(true);

        });

        if (isHistory){
            sendHistory(provider);
            dialog.dismiss();
        } else {
            btnSend.setOnClickListener(v -> {
                sendRealtimeReport(provider);
                dialog.dismiss();
            });

        }
        btnBack.setOnClickListener(v -> dialog.dismiss());
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


    private void sendHistory(Provider currentProvider){
        //TODO: Send History
    }
    private void sendRealtimeReport(Provider currentProvider) {
        //TODO: Do Implementation
    }
}
