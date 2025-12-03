package com.example.smartair.ui.HomeActivities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smartair.R;
import com.example.smartair.ui.HomeActivities.WelcomeRoleSelectionActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProviderHomeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String providerUid;

    // UI on main provider home screen
    private TextView greetingText;
    private Button selectParentButton;
    private Button selectChildButton;
    private RecyclerView reportsRecycler;
    private ReportsAdapter reportsAdapter;
    private List<ReportItem> reportItems = new ArrayList<>();

    // Currently selected parent/child
    private String selectedParentUid = null;
    private String selectedParentName = null;
    private String selectedChildUid = null;
    private String selectedChildName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen_activity_provider);

        db = FirebaseFirestore.getInstance();
        providerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        greetingText = findViewById(R.id.textView10);
        selectParentButton = findViewById(R.id.button18);
        selectChildButton = findViewById(R.id.button14);
        reportsRecycler = findViewById(R.id.recyclerView);

        if (reportsRecycler != null) {
            reportsRecycler.setLayoutManager(new LinearLayoutManager(this));
            reportsAdapter = new ReportsAdapter(reportItems);
            reportsRecycler.setAdapter(reportsAdapter);
        }

        findViewById(R.id.settingsButton).setOnClickListener(v -> showSettingsDialog());
        selectParentButton.setOnClickListener(v -> showSelectParentDialog());
        selectChildButton.setOnClickListener(v -> showSelectChildDialog());

        loadProviderGreeting();
        clearReportsList();
    }

    // ---------------- HEADER ----------------

    private void loadProviderGreeting() {
        db.collection("providers")
                .document(providerUid)
                .get()
                .addOnSuccessListener(snap -> {
                    String prefix = snap.getString("prefix");
                    String name = snap.getString("name");

                    StringBuilder sb = new StringBuilder("Hello");
                    if (prefix != null && !prefix.trim().isEmpty()) {
                        sb.append(" ").append(prefix.trim());
                    }
                    if (name != null && !name.trim().isEmpty()) {
                        sb.append(" ").append(name.trim());
                    }
                    greetingText.setText(sb.toString());
                })
                .addOnFailureListener(e -> {
                    greetingText.setText("Hello Provider");
                });
    }

    // ---------------- SETTINGS + MANAGE PARENTS ----------------

    private void showSettingsDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.settings_dialog_provider, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Manage Parents
        dialogView.findViewById(R.id.button4).setOnClickListener(v -> {
            showManageParentsScreen();
            dialog.dismiss();
        });

        // Sign out
        dialogView.findViewById(R.id.button3).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(ProviderHomeActivity.this, WelcomeRoleSelectionActivity.class);
            startActivity(i);
            finish();
        });

        dialog.show();
    }

    private void showManageParentsScreen() {
        View screenView = LayoutInflater.from(this)
                .inflate(R.layout.manage_activity_parents_for_provider, null);

        AlertDialog fullScreen = new AlertDialog.Builder(
                this,
                android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen
        ).setView(screenView).create();

        RecyclerView recycler = screenView.findViewById(R.id.parents_recyclerview);
        Button addNewParentButton = screenView.findViewById(R.id.add_new_parent_button);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        loadParentsIntoRecycler(recycler);

        addNewParentButton.setOnClickListener(v -> showEnterCodeDialog(recycler));
        fullScreen.show();
    }

    private void showEnterCodeDialog(RecyclerView recycler) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.manage_dialog_enter_code, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        EditText codeInput = dialogView.findViewById(R.id.access_code_input);
        Button submit = dialogView.findViewById(R.id.submit_button);
        Button back = dialogView.findViewById(R.id.back_button);

        submit.setOnClickListener(v -> {
            String code = codeInput.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(this, "Please enter a code", Toast.LENGTH_SHORT).show();
                return;
            }
            findParentByAccessCode(code, recycler);
            dialog.dismiss();
        });

        back.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void findParentByAccessCode(String code, RecyclerView recycler) {
        db.collection("parents")
                .get()
                .addOnSuccessListener(snaps -> {
                    for (QueryDocumentSnapshot doc : snaps) {
                        String pac = doc.getString("parentAccessCode");
                        if (pac != null && pac.equals(code)) {
                            String parentUid = doc.getId();
                            String parentName = doc.getString("name");
                            String parentEmail = doc.getString("email");
                            linkProviderAndParent(parentUid, parentName, parentEmail, recycler);
                            return;
                        }
                    }
                    Toast.makeText(this, "No parent found for that code",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to look up code",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void linkProviderAndParent(String parentUid,
                                       String parentName,
                                       String parentEmail,
                                       RecyclerView recycler) {

        Map<String, Object> data = new HashMap<>();
        data.put("parentUid", parentUid);
        data.put("parentEmail", parentEmail);
        data.put("parentName", parentName);

        db.collection("providers")
                .document(providerUid)
                .collection("parentLinks")
                .document(parentUid)
                .set(data)
                .addOnSuccessListener(a -> {
                    db.collection("parents")
                            .document(parentUid)
                            .update("providerUIDs",
                                    com.google.firebase.firestore.FieldValue.arrayUnion(providerUid))
                            .addOnSuccessListener(b -> {
                                loadParentsIntoRecycler(recycler);
                            });
                });
    }

    private void loadParentsIntoRecycler(RecyclerView recycler) {
        db.collection("providers")
                .document(providerUid)
                .collection("parentLinks")
                .get()
                .addOnSuccessListener(snaps -> {
                    List<ParentRow> rows = new ArrayList<>();

                    if (snaps.isEmpty()) {
                        recycler.setAdapter(new ParentsAdapter(rows));
                        return;
                    }

                    for (QueryDocumentSnapshot doc : snaps) {
                        final String parentUid2 = doc.getId();
                        final String parentName = doc.getString("parentName");

                        db.collection("parents")
                                .document(parentUid2)
                                .get()
                                .addOnSuccessListener(parentSnap -> {
                                    List<String> childUids =
                                            (List<String>) parentSnap.get("childUIDs");
                                    if (childUids == null) {
                                        childUids = new ArrayList<>();
                                    }
                                    rows.add(new ParentRow(parentName, parentUid2, childUids));
                                    recycler.setAdapter(new ParentsAdapter(new ArrayList<>(rows)));
                                });
                    }
                });
    }

    // ---------------- SELECT PARENT / CHILD DIALOGS ----------------

    private void showSelectParentDialog() {
        View v = LayoutInflater.from(this)
                .inflate(R.layout.selector_activity_select_parent, null);

        RecyclerView rv = v.findViewById(R.id.providersRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(v)
                .create();

        db.collection("providers")
                .document(providerUid)
                .collection("parentLinks")
                .get()
                .addOnSuccessListener(snaps -> {
                    List<ParentChoice> choices = new ArrayList<>();
                    for (QueryDocumentSnapshot d : snaps) {
                        String parentUid = d.getId();
                        String name = d.getString("parentName");
                        choices.add(new ParentChoice(parentUid, name));
                    }
                    rv.setAdapter(new ParentChoiceAdapter(choices, parentChoice -> {
                        selectedParentUid = parentChoice.parentUid;
                        selectedParentName = parentChoice.parentName;
                        selectedChildUid = null;
                        selectedChildName = null;

                        selectParentButton.setText(
                                selectedParentName == null
                                        ? "Select Parent"
                                        : selectedParentName
                        );
                        selectChildButton.setText("Select Child");
                        clearReportsList();
                        dialog.dismiss();
                    }));
                });

        dialog.show();
    }

    private void showSelectChildDialog() {
        if (selectedParentUid == null) {
            Toast.makeText(this, "Select a parent first", Toast.LENGTH_SHORT).show();
            return;
        }

        View v = LayoutInflater.from(this)
                .inflate(R.layout.selector_activity_select_child, null);

        RecyclerView rv = v.findViewById(R.id.providersRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(v)
                .create();

        db.collection("parents")
                .document(selectedParentUid)
                .get()
                .addOnSuccessListener(parentSnap -> {
                    List<String> childUids =
                            (List<String>) parentSnap.get("childUIDs");
                    if (childUids == null) childUids = new ArrayList<>();

                    if (childUids.isEmpty()) {
                        Toast.makeText(this, "No children linked to this parent",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }

                    List<ChildChoice> choices = new ArrayList<>();

                    for (String childUid : childUids) {
                        final String uid = childUid;
                        db.collection("children")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(childSnap -> {
                                    String name = childSnap.getString("name");
                                    choices.add(new ChildChoice(uid, name));
                                    rv.setAdapter(new ChildChoiceAdapter(choices, childChoice -> {
                                        selectedChildUid = childChoice.childUid;
                                        selectedChildName = childChoice.childName;

                                        selectChildButton.setText(
                                                selectedChildName == null
                                                        ? "Select Child"
                                                        : selectedChildName
                                        );
                                        dialog.dismiss();
                                        loadReportsForSelection();
                                    }));
                                });
                    }
                });

        dialog.show();
    }

    // ---------------- REPORTS LOADING ----------------

    private void clearReportsList() {
        if (reportItems != null) {
            reportItems.clear();
            if (reportsAdapter != null) {
                reportsAdapter.notifyDataSetChanged();
            }
        }
    }

    private void loadReportsForSelection() {
        clearReportsList();

        if (selectedParentUid == null || selectedChildUid == null) {
            // Requirement: show nothing if parent or child not selected
            return;
        }

        if (reportsRecycler == null) return;

        db.collection("children")
                .document(selectedChildUid)
                .collection("reports")
                .get()
                .addOnSuccessListener(reportSnaps -> {
                    for (QueryDocumentSnapshot rpt : reportSnaps) {
                        final String reportId = rpt.getId();
                        final String type = rpt.getString("reportType");
                        Timestamp ts = rpt.getTimestamp("generatedAt");
                        String date = "";
                        if (ts != null) {
                            date = android.text.format.DateFormat
                                    .format("dd/MM/yyyy", ts.toDate())
                                    .toString();
                        }
                        final String fDate = date;
                        final String fType = type == null ? "" : type;
                        final boolean isHistory =
                                "History".equalsIgnoreCase(fType);

                        db.collection("children")
                                .document(selectedChildUid)
                                .collection("reports")
                                .document(reportId)
                                .collection("providerAccess")
                                .document(providerUid)
                                .get()
                                .addOnSuccessListener(accessSnap -> {
                                    if (accessSnap.exists()) {
                                        String access = accessSnap.getString("access");
                                        if (access != null &&
                                                access.equalsIgnoreCase("shared")) {
                                            reportItems.add(new ReportItem(
                                                    reportId,
                                                    fDate,
                                                    fType,
                                                    isHistory
                                            ));
                                            reportsAdapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                    }
                });
    }

    // ---------------- MODEL / ADAPTER CLASSES ----------------

    class ReportItem {
        String reportId;
        String date;
        String type;
        boolean isHistory;

        ReportItem(String reportId, String date, String type, boolean isHistory) {
            this.reportId = reportId;
            this.date = date;
            this.type = type;
            this.isHistory = isHistory;
        }
    }

    class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.Holder> {
        List<ReportItem> data;

        ReportsAdapter(List<ReportItem> data) {
            this.data = data;
        }

        @Override
        public Holder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.homescreen_item_provider_report, parent, false);
            return new Holder(row);
        }

        @Override
        public void onBindViewHolder(Holder h, int pos) {
            ReportItem r = data.get(pos);
            h.dateText.setText(r.date);
            h.typeText.setText("[" + r.type + "]");
            // You can hook up CSV / PDF export later
            h.csvButton.setOnClickListener(v -> {
                // TODO: wire ProviderReportExporter with ReportExportRequest
            });
            h.pdfButton.setOnClickListener(v -> {
                // TODO: wire ProviderReportExporter with ReportExportRequest
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView dateText;
            TextView typeText;
            Button csvButton;
            Button pdfButton;

            Holder(View v) {
                super(v);
                dateText = v.findViewById(R.id.date_textview);
                typeText = v.findViewById(R.id.report_type_textview);
                csvButton = v.findViewById(R.id.csv_button);
                pdfButton = v.findViewById(R.id.pdf_button);
            }
        }
    }

    class ParentRow {
        String parentName;
        String parentUid;
        List<String> childUids;

        ParentRow(String n, String u, List<String> c) {
            parentName = n;
            parentUid = u;
            childUids = c;
        }
    }

    class ParentsAdapter extends RecyclerView.Adapter<ParentsAdapter.Holder> {
        List<ParentRow> data;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        ParentsAdapter(List<ParentRow> data) {
            this.data = data;
        }

        @Override
        public Holder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.manage_item_parent_row_for_provider, parent, false);
            return new Holder(row);
        }

        @Override
        public void onBindViewHolder(Holder h, int pos) {
            ParentRow row = data.get(pos);
            h.parentName.setText(row.parentName);
            h.childrenContainer.removeAllViews();

            for (String childUid : row.childUids) {
                final String uid = childUid;
                db.collection("children")
                        .document(uid)
                        .get()
                        .addOnSuccessListener(snap -> {
                            String name = snap.getString("name");
                            if (name != null) {
                                TextView t = new TextView(h.itemView.getContext());
                                t.setText("- " + name);
                                t.setTextSize(16f);
                                t.setPadding(8, 8, 8, 8);
                                h.childrenContainer.addView(t);
                            }
                        });
            }

            // Delete button (optional – can be left blank if not needed)
            h.deleteButton.setOnClickListener(v -> {
                // Optional: remove link both ways
                db.collection("providers")
                        .document(providerUid)
                        .collection("parentLinks")
                        .document(row.parentUid)
                        .delete()
                        .addOnSuccessListener(a -> {
                            data.remove(pos);
                            notifyDataSetChanged();
                        });
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView parentName;
            LinearLayout childrenContainer;
            ImageButton deleteButton;

            Holder(View v) {
                super(v);
                parentName = v.findViewById(R.id.parent_name_textview);
                childrenContainer = v.findViewById(R.id.children_container);
                deleteButton = v.findViewById(R.id.delete_button);
            }
        }
    }

    // ---------- Choice models + adapters for dialogs ----------

    static class ParentChoice {
        String parentUid;
        String parentName;

        ParentChoice(String uid, String name) {
            this.parentUid = uid;
            this.parentName = name;
        }
    }

    interface OnParentChosen {
        void onChosen(ParentChoice parentChoice);
    }

    class ParentChoiceAdapter extends RecyclerView.Adapter<ParentChoiceAdapter.Holder> {

        List<ParentChoice> data;
        OnParentChosen callback;

        ParentChoiceAdapter(List<ParentChoice> data, OnParentChosen cb) {
            this.data = data;
            this.callback = cb;
        }

        @Override
        public Holder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.selector_item_parent_row, parent, false);
            return new Holder(row);
        }

        @Override
        public void onBindViewHolder(Holder h, int pos) {
            ParentChoice p = data.get(pos);
            h.button.setText(p.parentName == null ? "[Parent]" : p.parentName);
            h.button.setOnClickListener(v -> callback.onChosen(p));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            Button button;

            Holder(View v) {
                super(v);
                button = v.findViewById(R.id.parent_button);
            }
        }
    }

    static class ChildChoice {
        String childUid;
        String childName;

        ChildChoice(String uid, String name) {
            this.childUid = uid;
            this.childName = name;
        }
    }

    interface OnChildChosen {
        void onChosen(ChildChoice childChoice);
    }

    class ChildChoiceAdapter extends RecyclerView.Adapter<ChildChoiceAdapter.Holder> {

        List<ChildChoice> data;
        OnChildChosen callback;

        ChildChoiceAdapter(List<ChildChoice> data, OnChildChosen cb) {
            this.data = data;
            this.callback = cb;
        }

        @Override
        public Holder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.selector_item_child_row, parent, false);
            return new Holder(row);
        }

        @Override
        public void onBindViewHolder(Holder h, int pos) {
            ChildChoice c = data.get(pos);
            h.button.setText(c.childName == null ? "[Child]" : c.childName);
            h.button.setOnClickListener(v -> callback.onChosen(c));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            Button button;

            Holder(View v) {
                super(v);
                button = v.findViewById(R.id.child_button);
            }
        }
    }
}