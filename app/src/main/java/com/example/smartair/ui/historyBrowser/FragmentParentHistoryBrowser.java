package com.example.smartair.ui.historyBrowser;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.callbacks.ResultCallback;
import com.example.smartair.callbacks.StringListCallback;
import com.example.smartair.models.childcollections.HistoryEntry;
import com.example.smartair.models.childcollections.MedLog;
import com.example.smartair.services.CheckinService;
import com.example.smartair.services.HistoryService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class FragmentParentHistoryBrowser extends Fragment {

    private static final String TAG = "HistoryBrowser";
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<HistoryEntry> historyList = new ArrayList<>();
    private List<HistoryEntry> fullHistoryList = new ArrayList<>();
    private HistoryService historyService;
    private String childUid;

    private final List<String> allCheckedSymptoms = new ArrayList<>();
    private final List<String> allCheckedTriggers = new ArrayList<>();
    

    private static final String ARG_CHILD_UID = "childUid";

    public FragmentParentHistoryBrowser() {
        // Required empty public constructor
    }

    public static FragmentParentHistoryBrowser newInstance(String childUid) {
        FragmentParentHistoryBrowser fragment = new FragmentParentHistoryBrowser();
        Bundle args = new Bundle();
        args.putString(ARG_CHILD_UID, childUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            childUid = getArguments().getString(ARG_CHILD_UID);
        }
        
        // Handle fallback if childUid is missing from arguments but passed via intent extras (legacy/activity context)
        if (childUid == null && getActivity() != null && getActivity().getIntent() != null) {
            if (getActivity().getIntent().hasExtra("childUid")) {
                childUid = getActivity().getIntent().getStringExtra("childUid");
            }
        }

        // Fallback to current user if not provided
        if ((childUid == null || childUid.isEmpty()) && FirebaseAuth.getInstance().getCurrentUser() != null) {
             childUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        
        try {
            historyService = new HistoryService();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing HistoryService", e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.history_activity_browser, container, false);
        } catch (Exception e) {
            Log.e(TAG, "Error inflating layout", e);
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            recyclerView = view.findViewById(R.id.historyRecyclerView);
            if (recyclerView == null) {
                Log.e(TAG, "RecyclerView not found with ID R.id.historyRecyclerView");
                return;
            }
            
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            
            adapter = new HistoryAdapter(historyList);
            recyclerView.setAdapter(adapter);

            Button filterButton = view.findViewById(R.id.select_filter_button);
            if (filterButton != null) {
                filterButton.setOnClickListener(v -> showFilterSymptomsDialog());
            }

            if (childUid != null && !childUid.isEmpty() && historyService != null) {
                loadHistoryData();
            } else {
                Log.w(TAG, "Cannot load real data. childUid=" + childUid + ", historyService=" + historyService);
                if (childUid == null || childUid.isEmpty()) {
                    loadMockData();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing view in onViewCreated", e);
        }
    }

    private void loadHistoryData() {
        try {
            // Default: Load last 1 year of history
            Calendar cal = Calendar.getInstance();
            Date endDate = cal.getTime();
            cal.add(Calendar.YEAR, -1); 
            Date startDate = cal.getTime();

            historyService.getAllHistoryLogs(childUid, startDate, endDate, new ResultCallback<List<HistoryEntry>>() {
                @Override
                public void onSuccess(List<HistoryEntry> entries) {
                    if (getContext() == null) {
                        return; 
                    }

                    try {
                        historyList.clear();
                        fullHistoryList.clear();
                        if (entries != null && !entries.isEmpty()) {
                            historyList.addAll(entries);
                            fullHistoryList.addAll(entries);
                        } else {
                            // No entries found - Load Mock Data as requested
                            loadMockData();
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e(TAG, "Error in onSuccess", e);
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "getAllHistoryLogs onError", e);
                    if (getContext() == null) return; 
                    
                    // Fallback to mock data on error
                    Toast.makeText(getContext(), "Error loading history, showing mock data", Toast.LENGTH_SHORT).show();
                    loadMockData();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadHistoryData setup", e);
            loadMockData();
        }
    }

    private void loadMockData() {
        try {
            // Clear existing list to avoid duplicates
            historyList.clear();
            fullHistoryList.clear();

            List<HistoryEntry> mockEntries = new ArrayList<>();

            // --- Existing Mock Data (2 entries) ---
            List<String> mockSymptoms1 = new ArrayList<>(Arrays.asList("Coughing/Wheezing"));
            List<String> mockTriggers1 = new ArrayList<>(Arrays.asList("Dust", "Pollen"));
            mockEntries.add(new HistoryEntry("2023-10-27", null, null, mockSymptoms1, mockTriggers1));

            List<String> mockTriggers2 = new ArrayList<>(Arrays.asList("Cold Air"));
            mockEntries.add(new HistoryEntry("2023-10-26", null, null, new ArrayList<>(), mockTriggers2));

            // --- New Mock Data (40 entries starting from 2023-10-25 and going backwards) ---

            // Helper lists for rotation
            List<List<String>> symptomSets = Arrays.asList(
                    Arrays.asList("Chest Tightness"),
                    Arrays.asList("Shortness of Breath"),
                    Arrays.asList("Coughing/Wheezing", "Fatigue"),
                    new ArrayList<>(), // Empty symptom list
                    Arrays.asList("Coughing/Wheezing", "Chest Tightness"),
                    Arrays.asList("Shortness of Breath", "Fatigue")
            );

            List<List<String>> triggerSets = Arrays.asList(
                    Arrays.asList("Exercise"),
                    Arrays.asList("Dust", "Smoke"),
                    Arrays.asList("Pollen"),
                    Arrays.asList("Cold Air", "Perfume"),
                    new ArrayList<>(), // Empty trigger list
                    Arrays.asList("Pet Dander")
            );

            // Generate 40 entries from 2023-10-25 down to 2023-09-16
            for (int i = 0; i < 40; i++) {
                // Calculate date (Starting from 2023-10-25 and going backward by i days)
                Calendar cal = Calendar.getInstance();
                cal.set(2023, Calendar.OCTOBER, 25); // Set start date: October 25, 2023
                cal.add(Calendar.DAY_OF_YEAR, -i); // Subtract i days

                // Format date as "YYYY-MM-DD"
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                String dateString = dateFormat.format(cal.getTime());

                // Rotate through symptom and trigger sets
                List<String> currentSymptoms = symptomSets.get(i % symptomSets.size());
                List<String> currentTriggers = triggerSets.get(i % triggerSets.size());

                mockEntries.add(new HistoryEntry(
                        dateString,
                        null,
                        null,
                        currentSymptoms,
                        currentTriggers
                ));
            }

            historyList.addAll(mockEntries);
            fullHistoryList.addAll(mockEntries);
            
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating mock data", e);
        }
    }

    private void showFilterSymptomsDialog() {
        Context context = getContext();
        if (context == null) return;

        allCheckedSymptoms.clear();
        allCheckedTriggers.clear();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.manage_toggle_symptoms, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();


        SwitchCompat toggleCoughing = dialogView.findViewById(R.id.switch_coughing);
        SwitchCompat toggleShortness = dialogView.findViewById(R.id.switch_shortness);
        SwitchCompat toggleTightness = dialogView.findViewById(R.id.switch_chesttightness);

        Button closeButton = dialogView.findViewById(R.id.btnExit);
        Button nextButton = dialogView.findViewById(R.id.btnNext);

        if (closeButton == null || nextButton == null || toggleCoughing == null) {
            return;
        }

        List<String> allDistinctSymptoms = new ArrayList<>(Arrays.asList("Coughing/Wheezing", "Shortness of Breath", "Chest Tightness"));
        List<String> allSymptoms = new ArrayList<>();
        List<String> addedSymptoms = new ArrayList<>();

        // Manually add symptoms from fullHistoryList as well
        for (HistoryEntry entry : fullHistoryList) {
            if (entry.getSymptoms() != null) {
                for (String symptom : entry.getSymptoms()) {
                    if (!allDistinctSymptoms.contains(symptom) && !allSymptoms.contains(symptom)) {
                        allSymptoms.add(symptom);
                    }
                }
            }
        }

        // Add extracted local symptoms immediately
        for (String symptom : allSymptoms) {
            if (!addedSymptoms.contains(symptom) && !allDistinctSymptoms.contains(symptom)) {
                addedSymptoms.add(symptom);
                addDynamicSymptomSwitch(context, dialogView, symptom);
            }
        }

        CheckinService checkinService = new CheckinService();
        checkinService.getSymptoms(childUid, new StringListCallback() {
            @Override
            public void onSuccess(List<String> distinctSymptomsList) {

                if (dialog.isShowing()) {
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
                Log.e(TAG, "Failed to load symptoms: " + e.getMessage());
            }
        });


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
            dialog.dismiss();
            showFilterTriggersDialog();
        });
        dialog.show();
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


                    symptomSwitch.setOnCheckedChangeListener((buttonView1, isChecked1) -> {
                        if (isChecked1) {
                            allCheckedSymptoms.add(symptom);
                        } else {
                            allCheckedSymptoms.remove(symptom);
                        }
                    });
                }
            }
        }
    }

    private void showFilterTriggersDialog() {
        Context context = getContext();
        if (context == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.manage_toggle_triggers, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();


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
            return;
        }

        List<String> allDistinctTriggers = new ArrayList<>(Arrays.asList("Dust Mites", "Pets", "Smoke", "Strong Odors/ Perfumes", "Cold Air", "Illness", "Exercise"));
        List<String> allExtraTriggers = new ArrayList<>();
        List<String> addedExtraTriggers = new ArrayList<>();
        
        // Manually add triggers from fullHistoryList as well
        // This ensures that even if CheckinService.getTriggers fails or is empty, we see local data triggers
        for (HistoryEntry entry : fullHistoryList) {
            if (entry.getTriggers() != null) {
                for (String trigger : entry.getTriggers()) {
                    if (!allDistinctTriggers.contains(trigger) && !allExtraTriggers.contains(trigger)) {
                        allExtraTriggers.add(trigger);
                    }
                }
            }
        }
        
        // Add extracted local triggers immediately
        for (String trigger : allExtraTriggers) {
             if (!addedExtraTriggers.contains(trigger) && !allDistinctTriggers.contains(trigger)) {
                addedExtraTriggers.add(trigger);
                addDynamicTriggerSwitch(context, dialogView, trigger);
             }
        }

        CheckinService checkinService = new CheckinService();
        checkinService.getTriggers(childUid, new StringListCallback() {
            @Override
            public void onSuccess(List<String> distinctTriggersList) {
                if (dialog.isShowing()) {
                    
                    for (String trigger : distinctTriggersList) {
                        // Only add if not already in standard list AND not already added from local history
                        if (!addedExtraTriggers.contains(trigger) && !allDistinctTriggers.contains(trigger)) {
                            addedExtraTriggers.add(trigger);
                            addDynamicTriggerSwitch(context, dialogView, trigger);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load triggers: " + e.getMessage());
            }
        });


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
            dialog.dismiss();
            applyFilters();
        });

        dialog.show();
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


                    triggerSwitch.setOnCheckedChangeListener((buttonView1, isChecked1) -> {
                        if (isChecked1) {
                            allCheckedTriggers.add(trigger);
                        } else {
                            allCheckedTriggers.remove(trigger);
                        }
                    });
                }
            }
        }
    }

    private void applyFilters() {
        
        // If no filters selected, maybe show all? 
        // But the user requirement is "filter to only shows those symptoms and triggers".
        // HistoryService.filterSymptomsTriggers logic: if entry has ALL selected symptoms AND ALL selected triggers.
        
        List<HistoryEntry> filtered = HistoryService.filterSymptomsTriggers(fullHistoryList, allCheckedSymptoms, allCheckedTriggers);
        
        historyList.clear();
        historyList.addAll(filtered);
        adapter.notifyDataSetChanged();
        
    }

    // --- Inner Adapter Class ---
    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private final List<HistoryEntry> entries;

        public HistoryAdapter(List<HistoryEntry> entries) {
            this.entries = entries;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.history_item_parent_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                HistoryEntry entry = entries.get(position);
                
                if (entry.getDate() != null) {
                    holder.dateText.setText(entry.getDate());
                } else {
                    holder.dateText.setText("Unknown Date");
                }

                holder.viewDataButton.setOnClickListener(v -> {
                    showDetailDialog(v.getContext(), entry);
                });
            } catch (Exception e) {
                Log.e("HistoryAdapter", "Error in onBindViewHolder at position " + position, e);
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return entries.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView dateText;
            Button viewDataButton;

            public ViewHolder(View itemView) {
                super(itemView);
                dateText = itemView.findViewById(R.id.textDate);
                viewDataButton = itemView.findViewById(R.id.buttonViewData);
            }
        }

        private void showDetailDialog(Context context, HistoryEntry entry) {
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.history_dialog_day_details, null);
                builder.setView(dialogView);
                
                // Bind views
                TextView textDate = dialogView.findViewById(R.id.textDetailDate);
                TextView textRescue = dialogView.findViewById(R.id.textDetailRescueItems);
                TextView textController = dialogView.findViewById(R.id.textDetailControllerItems);
                TextView textSymptoms = dialogView.findViewById(R.id.textDetailSymptomsItems);
                TextView textTriggers = dialogView.findViewById(R.id.textDetailTriggersItems);

                // Populate Data
                textDate.setText(entry.getDate() != null ? entry.getDate() : "Unknown Date");

                // Rescue Logic
                MedLog rescueLog = entry.getMedlogRescue();
                if (rescueLog != null) {
                    String puffs = rescueLog.getPuffsOrMeasures() != null ? rescueLog.getPuffsOrMeasures().toString() : "?";
                    textRescue.setText("• Rescue (" + puffs + " puffs)");
                } else {
                    textRescue.setText("• None");
                }

                // Controller Logic
                MedLog controllerLog = entry.getMedlogController();
                if (controllerLog != null) {
                    String puffs = controllerLog.getPuffsOrMeasures() != null ? controllerLog.getPuffsOrMeasures().toString() : "?";
                    textController.setText("• Controller (" + puffs + " puffs)");
                } else {
                    textController.setText("• None");
                }

                // Symptoms Logic
                List<String> symptoms = entry.getSymptoms();
                if (symptoms != null && !symptoms.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (String s : symptoms) {
                        sb.append("• ").append(s).append("\n");
                    }
                    textSymptoms.setText(sb.toString().trim());
                } else {
                    textSymptoms.setText("• None");
                }

                // Triggers Logic
                List<String> triggers = entry.getTriggers();
                if (triggers != null && !triggers.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (String t : triggers) {
                        sb.append("• ").append(t).append("\n");
                    }
                    textTriggers.setText(sb.toString().trim());
                } else {
                    textTriggers.setText("• None");
                }

                AlertDialog dialog = builder.create();

                
                dialog.show();

            } catch (Exception e) {
                Log.e("HistoryAdapter", "Error showing detail dialog", e);
                Toast.makeText(context, "Error showing details", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
