package com.example.smartair.ui.HomeActivities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProviderHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen_activity_provider);

        findViewById(R.id.settingsButton).setOnClickListener(v -> showSettingsDialog());
        findViewById(R.id.button18).setOnClickListener(v -> showSelectParentDialog());
        findViewById(R.id.button14).setOnClickListener(v -> showSelectChildDialog());

        loadReportsScroll();
    }

    private void showSettingsDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.settings_dialog_provider, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.button4).setOnClickListener(v -> {
            showManageParentsScreen();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.button3).setOnClickListener(v -> {
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
                this, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen
        ).setView(screenView).create();

        RecyclerView recycler = screenView.findViewById(R.id.parents_recyclerview);
        Button addNewParentButton = screenView.findViewById(R.id.add_new_parent_button);

        String providerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("providers")
                .document(providerUid)
                .collection("parentLinks")
                .get()
                .addOnSuccessListener(parentSnaps -> {
                    List<ParentRow> rows = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : parentSnaps) {
                        String parentName = doc.getString("parentName");
                        String parentUid2 = doc.getString("parentUID");
                        List<String> childUids = (List<String>) doc.get("childUIDs");

                        rows.add(new ParentRow(parentName, parentUid2, childUids));
                    }

                    recycler.setAdapter(new ParentsAdapter(rows));
                });

        addNewParentButton.setOnClickListener(v -> showEnterCodeDialog());

        fullScreen.show();
    }

    private void showEnterCodeDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.manage_dialog_enter_code, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        EditText codeInput = dialogView.findViewById(R.id.access_code_input);
        Button submitBtn = dialogView.findViewById(R.id.submit_button);
        Button backBtn = dialogView.findViewById(R.id.back_button);

        submitBtn.setOnClickListener(v -> {
            String code = codeInput.getText().toString().trim();
            dialog.dismiss();
        });

        backBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showSelectChildDialog() {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.selector_activity_select_child, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view).create();
        dialog.show();
    }

    private void showSelectParentDialog() {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.selector_activity_select_parent, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view).create();
        dialog.show();
    }

    private void loadReportsScroll() {
        RecyclerView reportsRecycler = findViewById(R.id.recyclerView);
        if (reportsRecycler == null) return;

        reportsRecycler.setLayoutManager(new LinearLayoutManager(this));

        String providerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<ReportItem> allReports = new ArrayList<>();

        db.collection("providers")
                .document(providerUid)
                .collection("parentLinks")
                .get()
                .addOnSuccessListener(parentSnaps -> {
                    for (QueryDocumentSnapshot parentDoc : parentSnaps) {
                        String parentUid = parentDoc.getString("parentUID");

                        db.collection("providers")
                                .document(providerUid)
                                .collection("parentLinks")
                                .document(parentUid)
                                .collection("children")
                                .get()
                                .addOnSuccessListener(childSnaps -> {

                                    for (QueryDocumentSnapshot childDoc : childSnaps) {
                                        List<String> sharedReports =
                                                (List<String>) childDoc.get("sharedReports");

                                        if (sharedReports != null) {
                                            for (String r : sharedReports) {
                                                allReports.add(new ReportItem(r));
                                            }
                                        }
                                    }

                                    reportsRecycler.setAdapter(new ReportsAdapter(allReports));
                                });
                    }
                });
    }

    class ReportItem {
        String reportId;
        ReportItem(String reportId) { this.reportId = reportId; }
    }

    class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportHolder> {

        List<ReportItem> data;

        ReportsAdapter(List<ReportItem> data) { this.data = data; }

        @Override
        public ReportHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.homescreen_item_provider_report, parent, false);
            return new ReportHolder(row);
        }

        @Override
        public void onBindViewHolder(ReportHolder h, int position) {
            ReportItem r = data.get(position);

            h.dateText.setText("Report");
            h.typeText.setText("[REPORT]");

            h.csvButton.setOnClickListener(v -> {});
            h.pdfButton.setOnClickListener(v -> {});
        }

        @Override
        public int getItemCount() { return data.size(); }

        class ReportHolder extends RecyclerView.ViewHolder {
            TextView dateText, typeText;
            Button csvButton, pdfButton;

            ReportHolder(View item) {
                super(item);
                dateText = item.findViewById(R.id.date_textview);
                typeText = item.findViewById(R.id.report_type_textview);
                csvButton = item.findViewById(R.id.csv_button);
                pdfButton = item.findViewById(R.id.pdf_button);
            }
        }
    }

    class ParentRow {
        String parentName;
        String parentUid;
        List<String> childUids;

        ParentRow(String parentName, String parentUid, List<String> childUids) {
            this.parentName = parentName;
            this.parentUid = parentUid;
            this.childUids = childUids;
        }
    }

    class ParentsAdapter extends RecyclerView.Adapter<ParentsAdapter.ParentHolder> {

        List<ParentRow> data;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        ParentsAdapter(List<ParentRow> data) { this.data = data; }

        @Override
        public ParentHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.manage_item_parent_row_for_provider, parent, false);
            return new ParentHolder(row);
        }

        @Override
        public void onBindViewHolder(ParentHolder holder, int position) {
            ParentRow row = data.get(position);

            holder.parentName.setText(row.parentName);
            holder.childrenContainer.removeAllViews();

            if (row.childUids != null) {
                for (String childUid : row.childUids) {
                    db.collection("children")
                            .document(childUid)
                            .get()
                            .addOnSuccessListener(snap -> {
                                String childName = snap.getString("name");
                                if (childName != null) {
                                    TextView tv = new TextView(holder.itemView.getContext());
                                    tv.setText("- " + childName);
                                    tv.setTextSize(16f);
                                    tv.setPadding(8, 8, 8, 8);
                                    holder.childrenContainer.addView(tv);
                                }
                            });
                }
            }
        }

        @Override
        public int getItemCount() { return data.size(); }

        class ParentHolder extends RecyclerView.ViewHolder {
            TextView parentName;
            LinearLayout childrenContainer;
            ImageButton deleteButton;

            ParentHolder(View itemView) {
                super(itemView);
                parentName = itemView.findViewById(R.id.parent_name_textview);
                childrenContainer = itemView.findViewById(R.id.children_container);
                deleteButton = itemView.findViewById(R.id.delete_button);
            }
        }
    }
}
