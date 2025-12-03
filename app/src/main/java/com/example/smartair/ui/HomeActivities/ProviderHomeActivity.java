package com.example.smartair.ui.HomeActivities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProviderHomeActivity extends AppCompatActivity {

    FirebaseFirestore db;
    String providerUid;
    String selectedParentUid;
    String selectedChildUid;
    TextView providerNameText;
    RecyclerView reportsRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen_activity_provider);

        db = FirebaseFirestore.getInstance();
        providerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        providerNameText = findViewById(R.id.textView10);
        reportsRecycler = findViewById(R.id.recyclerView);
        reportsRecycler.setLayoutManager(new LinearLayoutManager(this));

        loadProviderName();

        findViewById(R.id.settingsButton).setOnClickListener(v -> showSettingsDialog());
        findViewById(R.id.button18).setOnClickListener(v -> showSelectParentDialog());
        findViewById(R.id.button14).setOnClickListener(v -> showSelectChildDialog());

        clearReports();
    }

    void loadProviderName() {
        db.collection("providers")
                .document(providerUid)
                .get()
                .addOnSuccessListener(d -> {
                    String prefix = d.getString("prefix");
                    String name = d.getString("name");
                    if (prefix == null) prefix = "";
                    if (name == null) name = "";
                    providerNameText.setText("Hello " + prefix + " " + name);
                });
    }

    void showSettingsDialog() {
        View v = LayoutInflater.from(this)
                .inflate(R.layout.settings_dialog_provider, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(v)
                .create();

        v.findViewById(R.id.button4).setOnClickListener(x -> {
            dialog.dismiss();
            showManageParents();
        });

        v.findViewById(R.id.button3).setOnClickListener(x -> {
            dialog.dismiss();
            Intent i = new Intent(this, WelcomeRoleSelectionActivity.class);
            startActivity(i);
            finish();
        });

        dialog.show();
    }

    void showManageParents() {
        View v = LayoutInflater.from(this)
                .inflate(R.layout.manage_activity_parents_for_provider, null);
        AlertDialog screen = new AlertDialog.Builder(
                this,
                android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen
        ).setView(v).create();

        RecyclerView recycler = v.findViewById(R.id.parents_recyclerview);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        loadParentsList(recycler);

        Button add = v.findViewById(R.id.add_new_parent_button);
        add.setOnClickListener(x -> showEnterCodeDialog(recycler));

        screen.show();
    }

    void loadParentsList(RecyclerView recycler) {
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

                    final int[] remaining = {snaps.size()};

                    for (QueryDocumentSnapshot doc : snaps) {
                        String parentUidField = doc.getString("parentUid");
                        if (parentUidField == null || parentUidField.isEmpty()) {
                            parentUidField = doc.getId();
                        }
                        String parentName = doc.getString("parentName");
                        String parentUidFinal = parentUidField;

                        db.collection("parents")
                                .document(parentUidFinal)
                                .get()
                                .addOnSuccessListener(parentDoc -> {
                                    List<String> childUids = (List<String>) parentDoc.get("childUIDs");
                                    if (childUids == null) childUids = new ArrayList<>();
                                    rows.add(new ParentRow(parentUidFinal, parentName, childUids));
                                    remaining[0]--;
                                    if (remaining[0] == 0) {
                                        recycler.setAdapter(new ParentsAdapter(rows));
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    remaining[0]--;
                                    if (remaining[0] == 0) {
                                        recycler.setAdapter(new ParentsAdapter(rows));
                                    }
                                });
                    }
                });
    }

    void showEnterCodeDialog(RecyclerView recycler) {
        View v = LayoutInflater.from(this)
                .inflate(R.layout.manage_dialog_enter_code, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(v)
                .create();

        EditText codeInput = v.findViewById(R.id.access_code_input);
        Button submit = v.findViewById(R.id.submit_button);
        Button back = v.findViewById(R.id.back_button);

        submit.setOnClickListener(x -> {
            String code = codeInput.getText().toString().trim();
            dialog.dismiss();
            findParentByPAC(code, recycler);
        });

        back.setOnClickListener(x -> dialog.dismiss());

        dialog.show();
    }

    void findParentByPAC(String code, RecyclerView recycler) {
        db.collection("parents")
                .get()
                .addOnSuccessListener(snaps -> {
                    for (QueryDocumentSnapshot d : snaps) {
                        String pac = d.getString("parentAccessCode");
                        if (pac != null && pac.equals(code)) {
                            String parentUid = d.getId();
                            String parentName = d.getString("name");
                            String parentEmail = d.getString("email");
                            linkParent(parentUid, parentName, parentEmail, recycler);
                            return;
                        }
                    }
                });
    }

    void linkParent(String parentUid, String parentName, String parentEmail, RecyclerView recycler) {
        Map<String, Object> data = new HashMap<>();
        data.put("parentUid", parentUid);
        data.put("parentName", parentName);
        data.put("parentEmail", parentEmail);

        db.collection("providers")
                .document(providerUid)
                .collection("parentLinks")
                .document(parentUid)
                .set(data)
                .addOnSuccessListener(x ->
                        db.collection("parents")
                                .document(parentUid)
                                .update("providerUIDs", FieldValue.arrayUnion(providerUid))
                                .addOnSuccessListener(y -> loadParentsList(recycler)));
    }

    void showSelectParentDialog() {
        View v = LayoutInflater.from(this)
                .inflate(R.layout.selector_activity_select_parent, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(v)
                .create();

        RecyclerView recycler = v.findViewById(R.id.providersRecyclerView);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        db.collection("providers")
                .document(providerUid)
                .collection("parentLinks")
                .get()
                .addOnSuccessListener(snaps -> {
                    List<ParentButtonRow> rows = new ArrayList<>();
                    for (QueryDocumentSnapshot d : snaps) {
                        String parentUidField = d.getString("parentUid");
                        if (parentUidField == null || parentUidField.isEmpty()) {
                            parentUidField = d.getId();
                        }
                        String parentName = d.getString("parentName");
                        rows.add(new ParentButtonRow(parentUidField, parentName));
                    }
                    ParentSelectorAdapter adapter = new ParentSelectorAdapter(rows, row -> {
                        selectedParentUid = row.parentUid;
                        selectedChildUid = null;
                        clearReports();
                        dialog.dismiss();
                    });
                    recycler.setAdapter(adapter);
                });

        dialog.show();
    }

    void showSelectChildDialog() {
        if (selectedParentUid == null) return;

        View v = LayoutInflater.from(this)
                .inflate(R.layout.selector_activity_select_child, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(v)
                .create();

        RecyclerView recycler = v.findViewById(R.id.providersRecyclerView);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        db.collection("parents")
                .document(selectedParentUid)
                .get()
                .addOnSuccessListener(parentDoc -> {
                    List<String> childIds = (List<String>) parentDoc.get("childUIDs");
                    if (childIds == null) childIds = new ArrayList<>();

                    List<ChildButtonRow> rows = new ArrayList<>();
                    if (childIds.isEmpty()) {
                        recycler.setAdapter(new ChildSelectorAdapter(rows, row -> {}));
                        dialog.show();
                        return;
                    }

                    final int[] remaining = {childIds.size()};

                    for (String cUid : childIds) {
                        db.collection("children").document(cUid).get()
                                .addOnSuccessListener(cd -> {
                                    rows.add(new ChildButtonRow(cd.getId(), cd.getString("name")));
                                    remaining[0]--;
                                    if (remaining[0] == 0) {
                                        ChildSelectorAdapter adapter = new ChildSelectorAdapter(rows, row -> {
                                            selectedChildUid = row.childUid;
                                            dialog.dismiss();
                                            loadReportsForSelectedChild();
                                        });
                                        recycler.setAdapter(adapter);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    remaining[0]--;
                                    if (remaining[0] == 0) {
                                        ChildSelectorAdapter adapter = new ChildSelectorAdapter(rows, row -> {});
                                        recycler.setAdapter(adapter);
                                    }
                                });
                    }
                });

        dialog.show();
    }

    void clearReports() {
        reportsRecycler.setAdapter(new ReportsAdapter(new ArrayList<>()));
    }

    void loadReportsForSelectedChild() {
        if (selectedChildUid == null) return;

        db.collection("children")
                .document(selectedChildUid)
                .collection("reports")
                .get()
                .addOnSuccessListener(snaps -> {
                    List<ReportItem> list = new ArrayList<>();
                    if (snaps.isEmpty()) {
                        reportsRecycler.setAdapter(new ReportsAdapter(list));
                        return;
                    }

                    final int[] remaining = {snaps.size()};

                    for (QueryDocumentSnapshot rpt : snaps) {
                        String rptId = rpt.getId();
                        db.collection("children")
                                .document(selectedChildUid)
                                .collection("reports")
                                .document(rptId)
                                .collection("providerAccess")
                                .document(providerUid)
                                .get()
                                .addOnSuccessListener(acc -> {
                                    String access = acc.getString("access");
                                    if ("shared".equals(access)) {
                                        Timestamp ts = rpt.getTimestamp("generatedAt");
                                        String type = rpt.getString("reportType");
                                        String date = "";
                                        if (ts != null) {
                                            date = android.text.format.DateFormat
                                                    .format("dd/MM/yyyy", ts.toDate())
                                                    .toString();
                                        }
                                        list.add(new ReportItem(date, type));
                                    }
                                    remaining[0]--;
                                    if (remaining[0] == 0) {
                                        reportsRecycler.setAdapter(new ReportsAdapter(list));
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    remaining[0]--;
                                    if (remaining[0] == 0) {
                                        reportsRecycler.setAdapter(new ReportsAdapter(list));
                                    }
                                });
                    }
                });
    }

    class ParentRow {
        String parentUid;
        String parentName;
        List<String> childUids;

        ParentRow(String u, String n, List<String> children) {
            parentUid = u;
            parentName = n;
            childUids = children;
        }
    }

    class ParentsAdapter extends RecyclerView.Adapter<ParentsAdapter.Holder> {
        List<ParentRow> data;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        ParentsAdapter(List<ParentRow> d) {
            data = d;
        }

        @Override
        public Holder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.manage_item_parent_row_for_provider, parent, false);
            return new Holder(row);
        }

        @Override
        public void onBindViewHolder(Holder h, int position) {
            ParentRow row = data.get(position);
            h.parentName.setText(row.parentName);
            h.childrenContainer.removeAllViews();

            for (String childUid : row.childUids) {
                db.collection("children").document(childUid).get()
                        .addOnSuccessListener(snap -> {
                            String cName = snap.getString("name");
                            if (cName != null) {
                                View childRow = LayoutInflater.from(h.itemView.getContext())
                                        .inflate(R.layout.manage_item_child_row_for_parent, h.childrenContainer, false);
                                TextView tv = childRow.findViewById(R.id.child_name_textview);
                                tv.setText(cName);
                                h.childrenContainer.addView(childRow);
                            }
                        });
            }
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

    class ParentButtonRow {
        String parentUid;
        String parentName;

        ParentButtonRow(String u, String n) {
            parentUid = u;
            parentName = n;
        }
    }

    interface ParentSelectListener {
        void select(ParentButtonRow row);
    }

    class ParentSelectorAdapter extends RecyclerView.Adapter<ParentSelectorAdapter.Holder> {
        List<ParentButtonRow> data;
        ParentSelectListener listener;

        ParentSelectorAdapter(List<ParentButtonRow> d, ParentSelectListener l) {
            data = d;
            listener = l;
        }

        @Override
        public Holder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.selector_item_parent_row, parent, false);
            return new Holder(row);
        }

        @Override
        public void onBindViewHolder(Holder h, int position) {
            ParentButtonRow row = data.get(position);
            h.btn.setText(row.parentName);
            h.btn.setOnClickListener(v -> listener.select(row));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            Button btn;

            Holder(View v) {
                super(v);
                btn = v.findViewById(R.id.parent_button);
            }
        }
    }

    class ChildButtonRow {
        String childUid;
        String childName;

        ChildButtonRow(String u, String n) {
            childUid = u;
            childName = n;
        }
    }

    interface ChildSelectListener {
        void select(ChildButtonRow row);
    }

    class ChildSelectorAdapter extends RecyclerView.Adapter<ChildSelectorAdapter.Holder> {
        List<ChildButtonRow> data;
        ChildSelectListener listener;

        ChildSelectorAdapter(List<ChildButtonRow> d, ChildSelectListener l) {
            data = d;
            listener = l;
        }

        @Override
        public Holder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.selector_item_child_row, parent, false);
            return new Holder(row);
        }

        @Override
        public void onBindViewHolder(Holder h, int position) {
            ChildButtonRow row = data.get(position);
            h.btn.setText(row.childName);
            h.btn.setOnClickListener(v -> listener.select(row));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            Button btn;

            Holder(View v) {
                super(v);
                btn = v.findViewById(R.id.child_button);
            }
        }
    }

    class ReportItem {
        String date;
        String type;

        ReportItem(String d, String t) {
            date = d;
            type = t;
        }
    }

    class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.Holder> {
        List<ReportItem> data;

        ReportsAdapter(List<ReportItem> d) {
            data = d;
        }

        @Override
        public Holder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.homescreen_item_provider_report, parent, false);
            return new Holder(row);
        }

        @Override
        public void onBindViewHolder(Holder h, int position) {
            ReportItem r = data.get(position);
            h.date.setText(r.date);
            h.type.setText("[" + r.type + "]");
            h.csv.setOnClickListener(v -> {});
            h.pdf.setOnClickListener(v -> {});
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView date;
            TextView type;
            Button csv;
            Button pdf;

            Holder(View v) {
                super(v);
                date = v.findViewById(R.id.date_textview);
                type = v.findViewById(R.id.report_type_textview);
                csv = v.findViewById(R.id.csv_button);
                pdf = v.findViewById(R.id.pdf_button);
            }
        }
    }
}
