package com.example.smartair.ui.HomeActivities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.smartair.R;
import com.example.smartair.callbacks.CheckinListCallback;
import com.example.smartair.callbacks.ResultCallback;
import com.example.smartair.models.childcollections.Checkin;
import com.example.smartair.models.users.Child;
import com.example.smartair.repositories.ChildRepository;
import com.example.smartair.repositories.ParentRepository;
import com.example.smartair.services.CheckinService;
import com.example.smartair.services.AlertService;
import com.example.smartair.services.InAppAlertsListener;
import com.example.smartair.services.LocalNotificationHelper;
import com.example.smartair.ui.ActivitySettingsChild;
import com.example.smartair.ui.ActivitySettingsProvider;
import com.example.smartair.ui.checkin.DailyCheckinActivityParent;
import com.example.smartair.ui.historyBrowser.HistoryBrowserActivity;
import com.example.smartair.ui.login.ParentLoginActivity;
import com.example.smartair.utils.PacGenerator;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ParentHomeActivity extends AppCompatActivity {

    private LineChart lineChartTriggers;
    private Button toggle1W;
    private Button toggle1M;
    private ImageButton btnSettings;
    private Button checkInButton;
    private Button btnChildSelector;
    private Button historyBrowserButton;
    private TextView parentNameText;
    private String parentName;
    private String uid;
    private String selectedChildUid;
    private InAppAlertsListener alertsListener;
    private LocalNotificationHelper notificationHelper;
    private AlertService alertService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen_activity_parent);

        lineChartTriggers = findViewById(R.id.linechart_triggers);
        toggle1W = findViewById(R.id.toggle_1w);
        toggle1M = findViewById(R.id.toggle_1m);
        btnSettings = findViewById(R.id.btn_settings);
        parentNameText = findViewById(R.id.text_parent_name);
        checkInButton = findViewById(R.id.btn_daily_checkin_parent);
        historyBrowserButton = findViewById(R.id.btn_view_history_parent);
        btnChildSelector = findViewById(R.id.btn_child_selector);

        alertsListener = new InAppAlertsListener();
        notificationHelper = new LocalNotificationHelper(this);
        alertService = new AlertService();

        parentNameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        try {
            parentNameText.setTypeface(ResourcesCompat.getFont(this, R.font.garet_heavy), Typeface.BOLD);
        } catch (Exception e) {
            parentNameText.setTypeface(Typeface.DEFAULT_BOLD);
        }
        parentNameText.setTextColor(Color.parseColor("#000000"));
        parentNameText.setAlpha(1.0f);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
            FirebaseFirestore.getInstance().collection("parents")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            parentName = documentSnapshot.getString("name");
                            if (parentNameText != null) {
                                parentNameText.setText(parentName);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(ParentHomeActivity.this, "Failed to load name", Toast.LENGTH_SHORT).show());
        }

        setupTriggersChart();
        updateChartData();
        toggle1W.setAlpha(1.0f);
        toggle1M.setAlpha(0.5f);

        toggle1W.setOnClickListener(v -> {
            toggle1W.setAlpha(1.0f);
            toggle1M.setAlpha(0.5f);
            updateChartData();
        });

        toggle1M.setOnClickListener(v -> {
            toggle1M.setAlpha(1.0f);
            toggle1W.setAlpha(0.5f);
            updateChartData();
        });

        if (historyBrowserButton != null) {
            historyBrowserButton.setOnClickListener(v -> {
                Intent intent = new Intent(ParentHomeActivity.this, HistoryBrowserActivity.class);
                intent.putExtra("childUid", selectedChildUid);
                startActivity(intent);
            });
        }

        if (checkInButton != null) {
            checkInButton.setOnClickListener(v -> {
                Intent intent = new Intent(ParentHomeActivity.this, DailyCheckinActivityParent.class);
                intent.putExtra("USER_ROLE", "Parent");
                intent.putExtra("USER_NAME", parentName);
                intent.putExtra("CHILD_UID", selectedChildUid);
                startActivity(intent);
            });
        }

        if (btnSettings != null) {
            btnSettings.setOnClickListener(this::showSettingsMenu);
        }

        if (btnChildSelector != null) {
            btnChildSelector.setOnClickListener(v -> showSelectChildDialog());
        }

        if (uid != null) {
            ChildRepository childRepo = new ChildRepository();
            childRepo.getChildrenForParent(uid, new ResultCallback<List<Child>>() {
                @Override
                public void onSuccess(List<Child> children) {
                    if (children != null && !children.isEmpty() && selectedChildUid == null) {
                        Child firstChild = children.get(0);
                        selectedChildUid = firstChild.getChildUid();
                        btnChildSelector.setText(firstChild.getName() + " ▼");
                        updateChartData();
                        restartAlertsListener();
                    }
                }

                @Override
                public void onError(Exception e) {
                }
            });
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @Override
    protected void onStart() {
        super.onStart();
        restartAlertsListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (alertsListener != null) alertsListener.stopListening();
        if (alertService != null) alertService.stop();
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private void restartAlertsListener() {
        if (selectedChildUid == null) return;

        if (alertsListener != null) alertsListener.stopListening();
        if (alertService != null) alertService.stop();

        alertService.start(selectedChildUid);

        alertsListener.startListening(selectedChildUid, alertData -> {
            Boolean red = getBoolean(alertData, "redZoneDay");
            Boolean rapid = getBoolean(alertData, "rapidRescueRepeats");
            Boolean triage = getBoolean(alertData, "triageEscalation");
            Boolean worse = getBoolean(alertData, "worseAfterDose");
            Boolean inventory = getBoolean(alertData, "inventoryLowOrExpired");

            String title = "SmartAir Alert";
            String body = "There is a new alert for your child.";

            if (red != null && red) {
                title = "Red Zone Alert";
                body = "Your child is in the red zone today.";
            } else if (rapid != null && rapid) {
                title = "Frequent Rescue Use";
                body = "Your child used their rescue inhaler several times recently.";
            } else if (triage != null && triage) {
                title = "Triage Alert";
                body = "Your child's triage has escalated.";
            } else if (worse != null && worse) {
                title = "Worsening Symptoms";
                body = "Your child feels worse after their dose.";
            } else if (inventory != null && inventory) {
                title = "Medication Inventory";
                body = "Your child's medication inventory is low or expired.";
            }

            notificationHelper.show(title, body);
        });
    }

    private Boolean getBoolean(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Boolean) return (Boolean) value;
        return null;
    }

    private void updateChartData() {
        int days = (toggle1W.getAlpha() == 1.0f) ? 7 : 30;
        if (selectedChildUid != null) loadRealSymptomData(days);
        else loadDummyTriggersData(days);
    }

    private void loadRealSymptomData(int days) {
        if (selectedChildUid == null) return;
        CheckinService checkinService = new CheckinService();
        checkinService.getCheckins(selectedChildUid, new CheckinListCallback() {
            @Override
            public void onSuccess(List<Checkin> checkins) {
                processCheckinDataForChart(checkins, days);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ParentHomeActivity.this, "Failed to load chart data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processCheckinDataForChart(List<Checkin> checkins, int days) {
        Map<Integer, Integer> dayCounts = new HashMap<>();
        for (int i = 0; i < days; i++) dayCounts.put(i, 0);

        long nowMillis = System.currentTimeMillis();

        for (Checkin checkin : checkins) {
            if (checkin.getCreatedAt() == null) continue;
            long time = checkin.getCreatedAt().toDate().getTime();
            long diff = nowMillis - time;
            long diffDays = TimeUnit.MILLISECONDS.toDays(diff);
            if (diffDays < days && diffDays >= 0) {
                int index = (days - 1) - (int) diffDays;
                int currentCount = dayCounts.get(index);
                int symptomCount = (checkin.getSymptoms() != null) ? checkin.getSymptoms().size() : 0;
                dayCounts.put(index, currentCount + symptomCount);
            }
        }

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < days; i++) entries.add(new Entry(i, dayCounts.get(i)));

        LineDataSet dataSet = new LineDataSet(entries, "Symptoms per day");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        lineChartTriggers.setData(lineData);
        lineChartTriggers.invalidate();
    }

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
                finish();
                return true;
            } else if (id == R.id.action_code_generator_parent) {
                showAccessCodeDialog();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void setupTriggersChart() {
        lineChartTriggers.getDescription().setEnabled(false);
        lineChartTriggers.getLegend().setEnabled(false);

        XAxis xAxis = lineChartTriggers.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setAxisMinimum(0f);

        YAxis leftAxis = lineChartTriggers.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);

        YAxis rightAxis = lineChartTriggers.getAxisRight();
        rightAxis.setEnabled(false);

        lineChartTriggers.animateX(600);
    }

    private void loadDummyTriggersData(int days) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            float triggersCount = (float) (Math.random() * 5.0);
            entries.add(new Entry(i, triggersCount));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Symptoms per day");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        lineChartTriggers.setData(lineData);
        lineChartTriggers.invalidate();
    }

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

        if (uid != null) repo.updateAccessCode(uid, initialCode);

        btnRefresh.setOnClickListener(v -> {
            String newCode = pacGenerator.getPacCode();
            tvCode.setText(newCode);
            if (uid != null) repo.regenerateAccessCode(uid, newCode);
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

    private void showSelectChildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_select_child, null);
        builder.setView(dialogView);

        LinearLayout container = dialogView.findViewById(R.id.child_list_container);
        AlertDialog dialog = builder.create();

        if (uid == null) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) uid = user.getUid();
        }

        if (uid != null) {
            ChildRepository childRepo = new ChildRepository();
            childRepo.getChildrenForParent(uid, new ResultCallback<List<Child>>() {
                @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
                @Override
                public void onSuccess(List<Child> children) {
                    container.removeAllViews();
                    for (Child child : children) {
                        Button childBtn = new Button(ParentHomeActivity.this);
                        childBtn.setText(child.getName());
                        childBtn.setTextSize(18f);
                        childBtn.setPadding(20, 20, 20, 20);
                        childBtn.setOnClickListener(v -> {
                            btnChildSelector.setText(child.getName() + " ▼");
                            selectedChildUid = child.getChildUid();
                            Toast.makeText(ParentHomeActivity.this, "Selected: " + child.getName(), Toast.LENGTH_SHORT).show();
                            updateChartData();
                            restartAlertsListener();
                            dialog.dismiss();
                        });
                        container.addView(childBtn);
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(ParentHomeActivity.this, "Error loading children", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }

        dialog.show();
    }
}
