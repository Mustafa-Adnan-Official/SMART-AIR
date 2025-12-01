package com.example.smartair.ui.HomeActivities;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.repositories.ParentRepository;
import com.example.smartair.ui.ActivitySettingsChild;
import com.example.smartair.ui.ActivitySettingsProvider;
import com.example.smartair.ui.checkin.DailyCheckinActivity;
import com.example.smartair.ui.checkin.DailyCheckinActivityParent;
import com.example.smartair.ui.login.ParentLoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.example.smartair.utils.PacGenerator;

// MPAndroidChart imports
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Parent home screen.
 */
public class ParentHomeActivity extends AppCompatActivity {

    // Chart + toggle buttons
    private LineChart lineChartTriggers;
    private Button toggle1W;
    private Button toggle1M;
    private ImageButton btnSettings;
    private Button checkInButton;

    private String parentName;
    private String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        // Fetch Child Name
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("parents") // Should be parents collection for parent name
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            parentName = documentSnapshot.getString("name");
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(ParentHomeActivity.this, "Failed to load name", Toast.LENGTH_SHORT).show());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_home);

        // --- Find views from XML ---
        lineChartTriggers = findViewById(R.id.linechart_triggers);
        toggle1W = findViewById(R.id.toggle_1w);
        toggle1M = findViewById(R.id.toggle_1m);
        btnSettings = findViewById(R.id.btn_settings);
        // FIX: Use the correct ID from activity_parent_home.xml
        checkInButton = findViewById(R.id.btn_daily_checkin_parent);


        // --- Basic chart styling ---
        setupTriggersChart();

        // Default: show last 7 days (1W)
        loadDummyTriggersData(7);
        toggle1W.setAlpha(1.0f);   // selected
        toggle1M.setAlpha(0.5f);   // not selected

        // --- Toggle listeners ---
        toggle1W.setOnClickListener(v -> {
            loadDummyTriggersData(7);
            toggle1W.setAlpha(1.0f);
            toggle1M.setAlpha(0.5f);
        });

        toggle1M.setOnClickListener(v -> {
            loadDummyTriggersData(30);
            toggle1M.setAlpha(1.0f);
            toggle1W.setAlpha(0.5f);
        });

        if (checkInButton != null) {
            checkInButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Use DailyCheckinActivityParent for parents
                 Intent intent = new Intent(ParentHomeActivity.this, DailyCheckinActivityParent.class);
                 intent.putExtra("USER_ROLE", "Parent");
                 intent.putExtra("USER_NAME", parentName);
                 startActivity(intent);
             }
            });
        }

        // --- Settings Button Listener ---
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v-> {
                showSettingsMenu(v);
            });
        }
    }

    // ===================== POPUP MENU ======================
    private void showSettingsMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.settings_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_manage_children) {
                Intent intent = new Intent(this, ActivitySettingsChild.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.action_manage_providers) {
                Intent intent = new Intent(this, ActivitySettingsProvider.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.action_sign_out) {
                Intent intent = new Intent(this, ParentLoginActivity.class);
                startActivity(intent);
                finish(); // Close this activity
                return true;
            } else if (id == R.id.action_code_generator_parent) {
                showAccessCodeDialog();
                return true;
            }
            return false;
        });
        popup.show();
    }

    // ===================== MPAndroidChart setup ======================

    private void setupTriggersChart() {
        if (lineChartTriggers == null) {
            return; // defensive in case XML id mismatch
        }

        // Turn off default description text
        lineChartTriggers.getDescription().setEnabled(false);

        // No legend for now
        lineChartTriggers.getLegend().setEnabled(false);

        // X axis (bottom)
        XAxis xAxis = lineChartTriggers.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // step = 1 day
        xAxis.setAxisMinimum(0f);

        // Left Y axis
        YAxis leftAxis = lineChartTriggers.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // triggers can't be negative
        leftAxis.setGranularity(1f);

        // Disable right Y axis
        YAxis rightAxis = lineChartTriggers.getAxisRight();
        rightAxis.setEnabled(false);

        // Simple X animation
        lineChartTriggers.animateX(600);
    }

    private void loadDummyTriggersData(int days) {
        if (lineChartTriggers == null) {
            return; // defensive
        }

        // Later: replace this with real Firestore data.
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            // x = day index (0,1,2,...)
            // y = number of triggers that day (0–5 for now)
            float triggersCount = (float) (Math.random() * 5.0);
            entries.add(new Entry(i, triggersCount));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Triggers per day");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false); // no value labels on each point
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // smooth line

        LineData lineData = new LineData(dataSet);
        lineChartTriggers.setData(lineData);
        lineChartTriggers.invalidate(); // refresh/redraw
    }

    // ===================== MENU (existing logic) ======================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_manage_children) {
             Intent intent = new Intent(this, ActivitySettingsChild.class);
             startActivity(intent);
            return true;
        } else if (id == R.id.action_manage_providers) {
             Intent intent = new Intent(this, ActivitySettingsProvider.class);
             startActivity(intent);
            return true;
        } else if (id == R.id.action_sign_out) {
            Intent intent = new Intent(this, ParentLoginActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_code_generator_parent) {
            showAccessCodeDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAccessCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.menu_item_code_generator, null);
        builder.setView(dialogView);

        TextView tvCode = dialogView.findViewById(R.id.tv_generated_code);
        ImageButton btnCopy = dialogView.findViewById(R.id.btn_copy_code);
        ImageButton btnRefresh = dialogView.findViewById(R.id.btn_refresh_code);

        ParentRepository repo = new ParentRepository();
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        PacGenerator pacGenerator = new PacGenerator();


        String initialCode = pacGenerator.getPacCode();
        tvCode.setText(initialCode);

        // Update in DB if user is logged in
        if (uid != null) {
            repo.updateAccessCode(uid, initialCode);
        }

        btnRefresh.setOnClickListener(v -> {
            String newCode = pacGenerator.getPacCode();
            tvCode.setText(newCode);
            if (uid != null) {
                repo.regenerateAccessCode(uid, newCode);
            }
        });

        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Access Code", tvCode.getText());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Code copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
