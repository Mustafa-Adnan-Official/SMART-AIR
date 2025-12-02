package com.example.smartair.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.callbacks.ResultCallback;
import com.example.smartair.models.childcollections.Alert;
import com.example.smartair.models.childcollections.Incident;
import com.example.smartair.models.childcollections.InventoryItem;
import com.example.smartair.models.childcollections.ProviderAccess;
import com.example.smartair.models.users.Child;
import com.example.smartair.models.users.Parent;
import com.example.smartair.models.users.Provider;
import com.example.smartair.repositories.ChildRepository;
import com.example.smartair.repositories.ProviderRepository;
import com.example.smartair.services.CheckinService;
import com.example.smartair.services.HistoryService;
import com.example.smartair.services.InventoryService;
import com.example.smartair.callbacks.StringListCallback;
import com.example.smartair.models.childcollections.HistoryEntry;
import com.example.smartair.services.IncidentService;
import com.example.smartair.services.ReportService;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class ProvidersAdapter extends RecyclerView.Adapter<ProvidersAdapter.ProviderViewHolder> {

    // List of Providers to display
    private final List<Provider> providersList;
    private final Context context;
    private final HistoryService historyService;
    private final IncidentService incidentService;
    private final ReportService reportService;
    private final InventoryService inventoryService; // <--- ADD THIS
    private ProviderAccess providerAccess;

    private final ProviderRepository providerRepository;
    private final ChildRepository childRepository;
    private final Parent parent;

    // Map to store access state locally: Key = "providerUid_childUid", Value = Boolean
    private final Map<String, Boolean> providerChildAccessMap = new HashMap<>();

    private final List<String> allCheckedSymptoms = new ArrayList<>();
    private final List<String> allCheckedTriggers = new ArrayList<>();

    public ProvidersAdapter(Context context, List<Provider> providersList, Parent parent){
        this.providersList = providersList;
        this.context = context;
        this.providerRepository = new ProviderRepository();
        this.parent = parent;
        this.childRepository = new ChildRepository();

        this.historyService = new HistoryService();
        this.incidentService = new IncidentService();
        this.reportService = new ReportService();
        this.inventoryService = new InventoryService(); // <--- Initialize here

        Toast.makeText(context, "DEBUG: Adapter Initialized", Toast.LENGTH_SHORT).show();
    }

    public static class ProviderViewHolder extends RecyclerView.ViewHolder {

        public TextView nameTextView;
        public ImageButton deleteButton;
        public Button sendReportButton;

        public Button sendHistoryButton;
        public LinearLayout childModulesContainer;

        public ProviderViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.providerName);
            deleteButton = itemView.findViewById(R.id.deleteProvider);
            sendReportButton = itemView.findViewById(R.id.sendReportBtn);
            childModulesContainer = itemView.findViewById(R.id.childrenContainer);
            sendHistoryButton = itemView.findViewById(R.id.sendHistoryBtn);
            Toast.makeText(itemView.getContext(), "DEBUG: ViewHolder Created", Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    @Override
    public ProviderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.manage_item_provider_row_for_parent, parent, false);
        Toast.makeText(itemView.getContext(), "DEBUG: onCreateViewHolder (Inflating Card)", Toast.LENGTH_SHORT).show();
        return new ProviderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProviderViewHolder holder, int position) {

        providerAccess = new ProviderAccess();
        Provider currentProvider = providersList.get(position);

        // 1. SAFEGUARD: Ensure TextView exists before calling setText()
        if (holder.nameTextView != null) {
            holder.nameTextView.setText(currentProvider.getName());
        }

        if (holder.childModulesContainer != null) {
            holder.childModulesContainer.removeAllViews();
        }

        Toast.makeText(context, "DEBUG: onBindViewHolder: Pos " + position + " (" + currentProvider.getName() + ")", Toast.LENGTH_SHORT).show(); // 4. BIND START

        // Retrieve parent UID safely once outside the inner loop
        final String adapterParentUid = (parent != null) ? parent.getParentUid() : null;

        // Check if there are children to process AND if we have a valid Parent UID to compare against
        if (currentProvider.getChildren() != null && holder.childModulesContainer != null && adapterParentUid != null) {

            Toast.makeText(context, "DEBUG: Entering Children Loop", Toast.LENGTH_SHORT).show(); // NEW DEBUG TOAST

            for (Child child : currentProvider.getChildren()) {

                // 2. SAFEGUARD: Ensure Child Parent UID exists before comparison
                String childParentUid = child.getParentUid();

                if (childParentUid != null && childParentUid.equals(adapterParentUid)) {

                    final String currentChildUid = child.getChildUid();

                    // 3. SAFEGUARD: Check if the child row layout can be inflated
                    View childView = LayoutInflater.from(context).inflate(R.layout.manage_item_child_row_for_provider_row, holder.childModulesContainer, false);

                    if (childView != null) {
                        TextView childName = childView.findViewById(R.id.childName);
                        SwitchCompat accessSwitch = childView.findViewById(R.id.switch_child);

                        if (childName != null) {
                            childName.setText(child.getName());
                        }

                        // Key for the map
                        String accessKey = currentProvider.getProviderUid() + "_" + currentChildUid;

                        // Default to false if not in map, or use existing value
                        if (!providerChildAccessMap.containsKey(accessKey)) {
                            providerChildAccessMap.put(accessKey, false);
                        }

                        // 4. SAFEGUARD: Check if the Switch was found
                        if (accessSwitch != null) {
                            Boolean isChecked = providerChildAccessMap.get(accessKey);
                            if (isChecked != null) {
                                accessSwitch.setChecked(isChecked);
                            }

                            accessSwitch.setOnCheckedChangeListener((buttonView, b) -> {
                                providerChildAccessMap.put(accessKey, b);
                                // providerRepository.setAccessBool(currentChildUid, currentProvider.getProviderUid(), isChecked); // Removed immediate call
                                Toast.makeText(context, "Access " + (b ? "Enabled" : "Revoked") + " (Pending Send)", Toast.LENGTH_SHORT).show();
                            });
                            holder.childModulesContainer.addView(childView);
                        } else {
                            Toast.makeText(context, "ERROR: Missing switch_child in provider_child_row!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(context, "ERROR: Failed to inflate provider_child_row!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }

        Toast.makeText(context, "DEBUG: onBindViewHolder: Children Modules Checked", Toast.LENGTH_SHORT).show(); // 5. BIND END

        // 5. SAFEGUARD: Ensure buttons exist before setting listeners
        if (holder.deleteButton == null || holder.sendReportButton == null || holder.sendHistoryButton == null) {
            Toast.makeText(context, "ERROR: Missing action buttons in provider_card!", Toast.LENGTH_LONG).show();
        } else {
            holder.deleteButton.setOnClickListener(v -> showDeleteProviderDialog(context, position));

            holder.sendReportButton.setOnClickListener(v -> showManageDataDialog(context, currentProvider));

            holder.sendHistoryButton.setOnClickListener(v -> showHistorySymptoms(context, currentProvider));
        }
    }

    @Override
    public int getItemCount() {
        return providersList.size();
    }


    private void showHistorySymptoms(Context context, Provider provider) {
        Toast.makeText(context, "DEBUG: Starting showHistorySymptoms", Toast.LENGTH_SHORT).show(); // 6. DIALOG START
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.manage_toggle_symptoms, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Toast.makeText(context, "DEBUG: HistorySymptoms Dialog Inflated", Toast.LENGTH_SHORT).show(); // 7. DIALOG INFLATED

        SwitchCompat toggleCoughing = dialogView.findViewById(R.id.switch_coughing);
        SwitchCompat toggleShortness = dialogView.findViewById(R.id.switch_shortness);
        SwitchCompat toggleTightness = dialogView.findViewById(R.id.switch_chesttightness);

        Button closeButton = dialogView.findViewById(R.id.btnExit);
        Button nextButton = dialogView.findViewById(R.id.btnNext);

        // CRITICAL CHECK for NullPointerException
        if (closeButton == null || nextButton == null || toggleCoughing == null) {
            Toast.makeText(context, "ERROR: Missing view ID in select_symptoms.xml!", Toast.LENGTH_LONG).show();
            return;
        }

        List<String> allDistinctSymptoms = new ArrayList<>(Arrays.asList("Coughing/Wheezing", "Shortness of Breath", "Chest Tightness"));
        List<String> allSymptoms = new ArrayList<>();
        List<String> addedSymptoms = new ArrayList<>();

        final String adapterParentUid = (parent != null) ? parent.getParentUid() : null;

        if (provider.getChildren() != null && adapterParentUid != null) {
            for (Child child : provider.getChildren()) {
                if (child.getParentUid() != null && child.getParentUid().equals(adapterParentUid)) {

                    CheckinService checkin1 = new CheckinService();

                    checkin1.getSymptoms(child.getChildUid(), new StringListCallback() {
                        @Override
                        public void onSuccess(List<String> distinctSymptomsList) {
                            Toast.makeText(context, "DEBUG: HistorySymptoms - Async Success", Toast.LENGTH_SHORT).show(); // 8. ASYNC SUCCESS

                            if (dialog.isShowing()) {
                                allSymptoms.addAll(distinctSymptomsList);

                                for (String symptom : distinctSymptomsList) {
                                    if (!addedSymptoms.contains(symptom) && !allDistinctSymptoms.contains(symptom)) {
                                        addedSymptoms.add(symptom);
                                        addDynamicSymptomSwitch(context, dialogView, symptom);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(context, "Failed to load symptoms: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }

        Toast.makeText(context, "DEBUG: HistorySymptoms - Listeners set", Toast.LENGTH_SHORT).show(); // 10. LISTENERS SET

        toggleCoughing.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                allCheckedSymptoms.add("Coughing/Wheezing");
            } else {
                allCheckedSymptoms.remove("Coughing/Wheezing");
            }
        });

        toggleShortness.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                allCheckedSymptoms.add("Shortness of Breath");
            } else {
                allCheckedSymptoms.remove("Shortness of Breath");
            }
        });

        toggleTightness.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                allCheckedSymptoms.add("Chest Tightness");
            } else {
                allCheckedSymptoms.remove("Chest Tightness");
            }
        });

        closeButton.setOnClickListener(v -> dialog.dismiss());
        nextButton.setOnClickListener(v -> {
            dialog.dismiss(); // Dismiss current dialog before showing next
            showTriggersSymptoms(context, provider);
        });
        dialog.show();
        Toast.makeText(context, "DEBUG: HistorySymptoms - Dialog Shown", Toast.LENGTH_SHORT).show(); // 11. DIALOG SHOWING
    }

    private void addDynamicSymptomSwitch(Context context, View dialogView, String symptom) {
        ViewGroup container = dialogView.findViewById(R.id.containerSymptoms);

        if (container != null) {
            View symptomView = LayoutInflater.from(context).inflate(R.layout.symptomstriggers_child_module, container, false);

            if (symptomView != null) {
                TextView symptomName = symptomView.findViewById(R.id.symptomName);
                SwitchCompat symptomSwitch = symptomView.findViewById(R.id.switch_symptom);

                if (symptomSwitch != null && symptomName != null) {
                    symptomSwitch.setChecked(false);
                    symptomName.setText(symptom);
                    container.addView(symptomView);

                    Toast.makeText(context, "DEBUG: Added dynamic symptom: " + symptom, Toast.LENGTH_SHORT).show();

                    symptomSwitch.setOnCheckedChangeListener((buttonView1, isChecked1) -> {
                        if (isChecked1) {
                            allCheckedSymptoms.add(symptom);
                        } else {
                            allCheckedSymptoms.remove(symptom);
                        }
                    });
                }
            }
        } else {
            Toast.makeText(context, "ERROR: containerSymptoms is NULL!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTriggersSymptoms(Context context, Provider provider) {
        Toast.makeText(context, "DEBUG: Starting showTriggersSymptoms", Toast.LENGTH_SHORT).show(); // 12. DIALOG START
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.manage_toggle_triggers, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Toast.makeText(context, "DEBUG: TriggersSymptoms Dialog Inflated", Toast.LENGTH_SHORT).show(); // 13. DIALOG INFLATED

        SwitchCompat toggleDustMite = dialogView.findViewById(R.id.switch_dustmite);
        SwitchCompat togglePets = dialogView.findViewById(R.id.switch_pets);
        SwitchCompat toggleSmoke = dialogView.findViewById(R.id.switch_smoke);
        SwitchCompat toggleOdor = dialogView.findViewById(R.id.switch_odors);
        SwitchCompat toggleColdAir = dialogView.findViewById(R.id.switch_coldair);
        SwitchCompat toggleIllness = dialogView.findViewById(R.id.switch_illness);
        SwitchCompat toggleExercise = dialogView.findViewById(R.id.switch_exercise);


        Button closeButton = dialogView.findViewById(R.id.btnBack);
        Button nextButton = dialogView.findViewById(R.id.btnNext);

        if (closeButton == null || nextButton == null || toggleDustMite == null) {
            Toast.makeText(context, "ERROR: Missing view ID in select_triggers.xml!", Toast.LENGTH_LONG).show();
            return;
        }


        List<String> allDistinctTriggers = new ArrayList<>(Arrays.asList("Dust Mites", "Pets", "Smoke", "Strong Odors/ Perfumes", "Cold Air", "Illness", "Exercise"));



        List<String> allExtraTriggers = new ArrayList<>();
        List<String> addedExtraTriggers = new ArrayList<>();

        final String adapterParentUid = (parent != null) ? parent.getParentUid() : null;

        if (provider.getChildren() != null && adapterParentUid != null) {
            for (Child child : provider.getChildren()) {
                if (child.getParentUid() != null && child.getParentUid().equals(adapterParentUid)) {

                    CheckinService checkin1 = new CheckinService();

                    checkin1.getTriggers(child.getChildUid(), new StringListCallback() {
                        @Override
                        public void onSuccess(List<String> distinctSymptomsList) {
                            Toast.makeText(context, "DEBUG: TriggersSymptoms - Async Success", Toast.LENGTH_SHORT).show(); // 14. ASYNC SUCCESS
                            if (dialog.isShowing()) {

                                allExtraTriggers.addAll(distinctSymptomsList);

                                for (String symptom : distinctSymptomsList) {
                                    if (!addedExtraTriggers.contains(symptom) && !allDistinctTriggers.contains(symptom)) {
                                        addedExtraTriggers.add(symptom);
                                        addDynamicTriggerSwitch(context, dialogView, symptom);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(context, "Failed to load triggers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }

        allDistinctTriggers.addAll(addedExtraTriggers);

        Toast.makeText(context, "DEBUG: TriggersSymptoms - Listeners set", Toast.LENGTH_SHORT).show(); // 16. LISTENERS SET


        toggleDustMite.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                allCheckedTriggers.add("Dust Mites");
            } else {
                allCheckedTriggers.remove("Dust Mites");
            }

        });

        togglePets.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                allCheckedTriggers.add("Pets");
            } else {
                allCheckedTriggers.remove("Pets");
            }
        });

        toggleSmoke.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                allCheckedTriggers.add("Smoke");
            } else {
                allCheckedTriggers.remove("Smoke");
            }
        });

        toggleOdor.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                allCheckedTriggers.add("Strong Odors/ Perfumes");
            } else {
                allCheckedTriggers.remove("Strong Odors/ Perfumes");
            }
        });

        toggleColdAir.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                allCheckedTriggers.add("Cold Air");
            } else {
                allCheckedTriggers.remove("Cold Air");
            }
        });

        toggleIllness.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                allCheckedTriggers.add("Illness");
            } else {
                allCheckedTriggers.remove("Illness");
            }
        });

        toggleExercise.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                allCheckedTriggers.add("Exercise");
            } else {
                allCheckedTriggers.remove("Exercise");
            }
        });

        closeButton.setOnClickListener(v -> dialog.dismiss());
        nextButton.setOnClickListener(v -> {
            dialog.dismiss(); // Dismiss current dialog before showing next
            showSelectDurationDialog(context, provider, true);
        });

        dialog.show();
        Toast.makeText(context, "DEBUG: TriggersSymptoms - Dialog Shown", Toast.LENGTH_SHORT).show(); // 17. DIALOG SHOWING
    }

    private void addDynamicTriggerSwitch(Context context, View dialogView, String trigger) {
        ViewGroup container = dialogView.findViewById(R.id.containerTriggers);
        if (container != null) {
            View triggerView = LayoutInflater.from(context).inflate(R.layout.symptomstriggers_child_module, container, false);

            if (triggerView != null) {
                TextView triggerName = triggerView.findViewById(R.id.symptomName);
                SwitchCompat triggerSwitch = triggerView.findViewById(R.id.switch_symptom);

                if (triggerSwitch != null && triggerName != null) {
                    triggerSwitch.setChecked(false);
                    triggerName.setText(trigger);
                    container.addView(triggerView);

                    Toast.makeText(context, "DEBUG: Added dynamic trigger: " + trigger, Toast.LENGTH_SHORT).show();

                    triggerSwitch.setOnCheckedChangeListener((buttonView1, isChecked1) -> {
                        if (isChecked1) {
                            allCheckedTriggers.add(trigger);
                        } else {
                            allCheckedTriggers.remove(trigger);
                        }
                    });
                }
            }
        } else {
            Toast.makeText(context, "ERROR: containerTriggers is NULL after async!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showManageDataDialog(Context context, Provider provider) {
        Toast.makeText(context, "DEBUG: Starting showManageDataDialog", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.manage_toggle_data, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Button closeButton = dialogView.findViewById(R.id.btnExit);
        Button nextButton = dialogView.findViewById(R.id.btnNext);

        if (closeButton == null || nextButton == null) {
            Toast.makeText(context, "ERROR: Missing button ID in dialog_manage_data.xml!", Toast.LENGTH_LONG).show();
            return;
        }
        SwitchCompat toggleRescue = dialogView.findViewById(R.id.toggle_rescuelog_data);
        SwitchCompat toggleSymptoms = dialogView.findViewById(R.id.toggle_symptoms_tracking);
        SwitchCompat toggleTriggers = dialogView.findViewById(R.id.toggle_triggers_tracking);
        SwitchCompat togglePeakFlow = dialogView.findViewById(R.id.toggle_peakflow_tracking);
        SwitchCompat toggleTriageIncident = dialogView.findViewById(R.id.toggle_triageincidents_tracking);
        SwitchCompat toggleSummaryCharts = dialogView.findViewById(R.id.toggle_summarycharts_tracking);


        if (toggleRescue != null)
            toggleRescue.setOnCheckedChangeListener((buttonView, isChecked) -> {
                providerAccess.setCanSeeRescueAttempts(isChecked);
            });
        if (toggleSymptoms != null)
            toggleSymptoms.setOnCheckedChangeListener((buttonView, isChecked) -> {
                providerAccess.setCanSeeSymptoms(isChecked);
            });
        if (toggleTriggers != null)
            toggleTriggers.setOnCheckedChangeListener((buttonView, isChecked) -> {
                providerAccess.setCanSeeTriggers(isChecked);
            });
        if (togglePeakFlow != null)
            togglePeakFlow.setOnCheckedChangeListener((buttonView, isChecked) -> {
                providerAccess.setCanSeePeakFlow(isChecked);
            });
        if (toggleTriageIncident != null)
            toggleTriageIncident.setOnCheckedChangeListener((buttonView, isChecked) -> {
                providerAccess.setCanSeeTriageIncidents(isChecked);
            });
        if (toggleSummaryCharts != null)
            toggleSummaryCharts.setOnCheckedChangeListener((buttonView, isChecked) -> {
                providerAccess.setCanSeeTrends(isChecked);
            });

        closeButton.setOnClickListener(v -> dialog.dismiss());
        nextButton.setOnClickListener(v -> {
            showSelectDurationDialog(context, provider, false);
            dialog.dismiss();

        });
        dialog.show();
        Toast.makeText(context, "DEBUG: ManageData Dialog Shown", Toast.LENGTH_SHORT).show();
    }

    private void showSelectDurationDialog(Context context, Provider provider, Boolean isHistory) {
        Toast.makeText(context, "DEBUG: Starting showSelectDurationDialog (isHistory: " + isHistory + ")", Toast.LENGTH_SHORT).show();
        final int[] duration = {0};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.manage_toggle_duration, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Button btn3mo = dialogView.findViewById(R.id.btn3mo);
        Button btn4mo = dialogView.findViewById(R.id.btn4mo);
        Button btn5mo = dialogView.findViewById(R.id.btn5mo);
        Button btn6mo = dialogView.findViewById(R.id.btn6mo);

        Button btnSend = dialogView.findViewById(R.id.btnSend);
        Button btnBack = dialogView.findViewById(R.id.btnBack);

        // CRITICAL CHECK for NullPointerException
        if (btnSend == null || btnBack == null) {
            Toast.makeText(context, "ERROR: Missing button ID in select_duration.xml!", Toast.LENGTH_LONG).show();
            return;
        }

        btn3mo.setOnClickListener(v -> {
            duration[0] = 3;
            btn3mo.setSelected(true);
            btn4mo.setSelected(false);
            btn5mo.setSelected(false);
            btn6mo.setSelected(false);
        });

        btn4mo.setOnClickListener(v -> {
            duration[0] = 4;
            btn3mo.setSelected(false);
            btn4mo.setSelected(true);
            btn5mo.setSelected(false);
            btn6mo.setSelected(false);

        });

        btn5mo.setOnClickListener(v -> {
            duration[0] = 5;
            btn3mo.setSelected(false);
            btn4mo.setSelected(false);
            btn5mo.setSelected(true);
            btn6mo.setSelected(false);

        });

        btn6mo.setOnClickListener(v -> {
            duration[0] = 6;
            btn3mo.setSelected(false);
            btn4mo.setSelected(false);
            btn5mo.setSelected(false);
            btn6mo.setSelected(true);

        });

        btnSend.setOnClickListener(v -> {
            if (isHistory) {
                Toast.makeText(context, "DEBUG: Sending History for " + duration[0] + " months.", Toast.LENGTH_SHORT).show();
                sendHistory(provider, duration[0]);
            } else {
                Toast.makeText(context, "DEBUG: Sending Realtime Report.", Toast.LENGTH_SHORT).show();
                sendRealtimeReport(provider, duration[0]);
            }
            dialog.dismiss();
        });

        btnBack.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        Toast.makeText(context, "DEBUG: SelectDuration Dialog Shown", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteProviderDialog(Context context, int position) {
        Toast.makeText(context, "DEBUG: Starting showDeleteProviderDialog", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_provider, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        Button yesButton = dialogView.findViewById(R.id.dialog_yesdeleteprovider_button);
        Button noButton = dialogView.findViewById(R.id.dialog_nodeleteprovider_button);

        // CRITICAL CHECK for NullPointerException
        if (yesButton == null || noButton == null) {
            Toast.makeText(context, "ERROR: Missing button ID in dialog_delete_provider.xml!", Toast.LENGTH_LONG).show();
            return;
        }

        yesButton.setOnClickListener(v -> {
            providersList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, providersList.size());

            //TODO: DELETE PROVIDER FROM DATABASE

            Toast.makeText(context, "Provider Removed", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        noButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        Toast.makeText(context, "DEBUG: Delete Dialog Shown", Toast.LENGTH_SHORT).show();
    }

    private void sendRealtimeReport(Provider currentProvider, int duration) {
        Toast.makeText(context, "DEBUG: Running sendRealtimeReport logic for " + duration + " months", Toast.LENGTH_SHORT).show();

        // 1. Validate Duration and Calculate Date Range
        if (duration <= 0 || duration > 6) {
            Toast.makeText(context, "Error: Invalid duration selected for report.", Toast.LENGTH_LONG).show();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        Date end = calendar.getTime();
        calendar.add(Calendar.MONTH, -duration);
        Date start = calendar.getTime();
        String durationLabel = duration + " Months";

        // Access map for the CURRENT provider being shared to
        Map<String, ProviderAccess> accessToShare = new HashMap<>();
        // providerAccess holds the toggled data permissions from showManageDataDialog
        accessToShare.put(currentProvider.getProviderUid(), providerAccess);

        if (currentProvider.getChildren() != null) {
            for (Child child : currentProvider.getChildren()) {
                String key = currentProvider.getProviderUid() + "_" + child.getChildUid();
                Boolean hasMasterAccess = providerChildAccessMap.getOrDefault(key, false);

                if (Boolean.TRUE.equals(hasMasterAccess)) {
                    final String childUid = child.getChildUid();
                    final Child currentChild = child;

                    Log.d("ProvidersAdapter", "Starting async fetch for child: " + child.getName() + " (" + childUid + ")");

                    // --- CHAINED ASYNCHRONOUS LOGIC (History -> Incidents -> Inventory -> Report) ---

                    // 2. Fetch History Entries (MedLogs & Checkins)
                    historyService.getAllHistoryLogs(childUid, start, end, new ResultCallback<List<HistoryEntry>>() {
                        @Override
                        public void onSuccess(List<HistoryEntry> historyEntries) {
                            Log.d("ProvidersAdapter", "History entries fetched: " + (historyEntries != null ? historyEntries.size() : "null"));

                            // 3. On History Success, Fetch Incidents
                            incidentService.getAllIncidents(childUid, start, end, new ResultCallback<List<Incident>>() {
                                @Override
                                public void onSuccess(List<Incident> incidents) {
                                    Log.d("ProvidersAdapter", "Incidents fetched: " + (incidents != null ? incidents.size() : "null"));

                                    // 4. Fetch Inventory (Controller)
                                    inventoryService.updateInventoryAfterDose(childUid, "controller", 0, new InventoryService.InventoryCallback() {
                                        @Override
                                        public void onSuccess(@Nullable InventoryItem inventoryItem, @Nullable Alert alert) {
                                            Log.d("ProvidersAdapter", "Inventory fetched: " + (inventoryItem != null ? "Found" : "Null/Standalone"));
                                            
                                            // Fallback to placeholder if null (standalone child or missing data)
                                            InventoryItem finalInventory = inventoryItem != null ? inventoryItem : new InventoryItem();

                                            try {
                                                // 5. Generate and Send Report
                                                String reportDocumentId = reportService.generateReport(
                                                        childUid,
                                                        null, // ChildReport report (Metadata model, often populated by ReportService)
                                                        durationLabel,
                                                        start,
                                                        end,
                                                        accessToShare,
                                                        currentChild,
                                                        finalInventory,
                                                        historyEntries,
                                                        incidents
                                                );
                                                Log.d("ProvidersAdapter", "Report generated successfully: " + reportDocumentId);
                                                Toast.makeText(context, "Realtime Report sent for " + currentChild.getName() + ": " + reportDocumentId, Toast.LENGTH_LONG).show();
                                            } catch (Exception e) {
                                                Log.e("ProvidersAdapter", "Error generating report", e);
                                                e.printStackTrace();
                                                Toast.makeText(context, "Error generating report: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Log.e("ProvidersAdapter", "Error fetching inventory, proceeding with placeholder", e);
                                            
                                            // Proceed with placeholder on error
                                            InventoryItem placeholderInventory = new InventoryItem();
                                            
                                            try {
                                                String reportDocumentId = reportService.generateReport(
                                                        childUid,
                                                        null,
                                                        durationLabel,
                                                        start,
                                                        end,
                                                        accessToShare,
                                                        currentChild,
                                                        placeholderInventory,
                                                        historyEntries,
                                                        incidents
                                                );
                                                Log.d("ProvidersAdapter", "Report generated successfully (with placeholder inv): " + reportDocumentId);
                                                Toast.makeText(context, "Realtime Report sent for " + currentChild.getName() + ": " + reportDocumentId, Toast.LENGTH_LONG).show();
                                            } catch (Exception ex) {
                                                Log.e("ProvidersAdapter", "Error generating report (fallback)", ex);
                                                Toast.makeText(context, "Error generating report: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("ProvidersAdapter", "Error retrieving incidents", e);
                                    Toast.makeText(context, "Failed to retrieve incident data for " + currentChild.getName() + ".", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("ProvidersAdapter", "Error retrieving history logs", e);
                            Toast.makeText(context, "Failed to retrieve history data for " + currentChild.getName() + ".", Toast.LENGTH_LONG).show();
                        }
                    });

                } else {
                    Log.d("ProvidersAdapter", "Access revoked for child: " + child.getName());
                    Toast.makeText(context, "Access revoked for " + child.getName() + ". Report not sent.", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Log.d("ProvidersAdapter", "Provider has no children.");
        }
    }

    private void sendHistory(Provider currentProvider, int duration) {
        Toast.makeText(context, "DEBUG: Running sendHistory logic for " + duration + " months", Toast.LENGTH_SHORT).show();
        if (currentProvider.getChildren() != null) {

            for (Child child : currentProvider.getChildren()) {

                String key = currentProvider.getProviderUid() + "_" + child.getChildUid();
                Boolean hasAccess = providerChildAccessMap.getOrDefault(key, false);
                HistoryService historyService = new HistoryService();

                if (Boolean.TRUE.equals(hasAccess)) {
                    if (duration > 2 && duration < 7){
                        Calendar calendar = Calendar.getInstance();
                        Date end = calendar.getTime(); // Current date (End)
                        calendar.add(Calendar.MONTH, -duration);
                        Date start = calendar.getTime(); // Start date

                        // Call asynchronous getAllHistoryLogs
                        historyService.getAllHistoryLogs(child.getChildUid(), start, end, new ResultCallback<List<HistoryEntry>>() {
                            @Override
                            public void onSuccess(List<HistoryEntry> historyEntries) {
                                List<HistoryEntry> filteredEntries = HistoryService.filterSymptomsTriggers(historyEntries, allCheckedSymptoms, allCheckedTriggers);

                                Map<String, Boolean> providerAccessMap = new HashMap<>();
                                for (Provider prov : providersList) {
                                    String pKey = prov.getProviderUid() + "_" + child.getChildUid();
                                    Boolean access = providerChildAccessMap.getOrDefault(pKey, false);
                                    providerAccessMap.put(prov.getProviderUid(), access);
                                }

                                String reportID = historyService.generateReport(child.getChildUid(), filteredEntries, start, end, duration + " months", providerAccessMap);
                                Toast.makeText(context, "Report sent", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(context, "Failed to send history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        Toast.makeText(context, "Invalid Duration (" + duration + ")", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    // Access revoked logic if needed
                    Toast.makeText(context, "DEBUG: History access revoked for " + child.getName(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
