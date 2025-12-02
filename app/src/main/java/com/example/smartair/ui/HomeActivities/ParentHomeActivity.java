package com.example.smartair.ui.HomeActivities;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.smartair.services.ZoneService;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private Button btnChildSelector;
    private Button historyBrowserButton;

    private TextView parentNameText;
    private String parentName;
    private String uid;

    private String selectedChildUid;

    // Incident + medicine log views
    private TextView textIncidentLog;
    private TextView textMedicineLog;

    // Weekly rescue + last rescue views
    private TextView textWeeklyRescueValue;
    private TextView textLastRescueValue;

    // PEF zone banner (parent)
    private LinearLayout bannerPefZoneParent;
    private TextView textPefValueParent;
    private TextView textPefSubtitleParent;

    // Firestore
    private FirebaseFirestore db;

    // Child PB (for PEF zone calculation)
    private int personalBest = 0;

    // Auto-refresh every 30 seconds
    private static final long LOG_REFRESH_INTERVAL_MS = 30_000L;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable logsRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isFinishing() && !isDestroyed()) {
                loadDailyLogsForSelectedChild();
                loadRescueSummaryForSelectedChild();
                loadTodaysPefZoneForParent();
                refreshHandler.postDelayed(this, LOG_REFRESH_INTERVAL_MS);
            }
        }
    };

    // Colours for zone banner (same as child)
    private static final int COLOR_GREEN = Color.parseColor("#2E7D32");
    private static final int COLOR_YELLOW = Color.parseColor("#F9A825");
    private static final int COLOR_RED = Color.parseColor("#C62828");
    private static final int COLOR_DEFAULT_BG = Color.parseColor("#143C8F");
    private static final int COLOR_TEXT_WHITE = Color.parseColor("#FFFFFF");
    private static final int COLOR_TEXT_MUTED = Color.parseColor("#757575");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen_activity_parent);

        db = FirebaseFirestore.getInstance();

        // --- Find views from XML ---
        lineChartTriggers = findViewById(R.id.linechart_triggers);
        toggle1W = findViewById(R.id.toggle_1w);
        toggle1M = findViewById(R.id.toggle_1m);
        btnSettings = findViewById(R.id.btn_settings);

        parentNameText = findViewById(R.id.text_parent_name);

        textIncidentLog = findViewById(R.id.text_incident_log);
        textMedicineLog = findViewById(R.id.text_medicine_log);

        textWeeklyRescueValue = findViewById(R.id.text_weekly_rescue_value);
        textLastRescueValue = findViewById(R.id.text_last_rescue_value);

        bannerPefZoneParent = findViewById(R.id.banner_pef_zone_parent);
        textPefValueParent = findViewById(R.id.text_pef_value_parent);
        textPefSubtitleParent = findViewById(R.id.text_pef_subtitle_parent);

        parentNameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        try {
            parentNameText.setTypeface(ResourcesCompat.getFont(this, R.font.garet_heavy), Typeface.BOLD);
        } catch (Exception e) {
            parentNameText.setTypeface(Typeface.DEFAULT_BOLD);
        }
        parentNameText.setTextColor(Color.parseColor("#000000"));
        parentNameText.setAlpha(1.0f);

        checkInButton = findViewById(R.id.btn_daily_checkin_parent);
        historyBrowserButton = findViewById(R.id.btn_view_history_parent);
        btnChildSelector = findViewById(R.id.btn_child_selector);

        // Fetch Parent Name
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

        // --- Basic chart styling ---
        setupTriggersChart();

        // Default: show last 7 days (1W)
        toggle1W.setAlpha(1.0f);
        toggle1M.setAlpha(0.5f);
        updateChartData();

        // --- Toggle listeners ---
        toggle1W.setOnClickListener(v -> {
            toggle1W.setAlpha(1.0f);
            toggle1M.setAlpha(0.5f);
            updateChartData();
        });

        toggle1M.setOnClickListener(v -> {
            toggle1M.setAlpha(0.5f);
            toggle1W.setAlpha(0.5f);
            toggle1M.setAlpha(1.0f);
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

        // --- Settings Button Listener ---
        if (btnSettings != null) {
            btnSettings.setOnClickListener(this::showSettingsMenu);
        }

        // --- Child Selector Listener ---
        if (btnChildSelector != null) {
            btnChildSelector.setOnClickListener(v -> showSelectChildDialog());
        }

        // --- Auto-select first child if available ---
        if (uid != null) {
            ChildRepository childRepo = new ChildRepository();
            childRepo.getChildrenForParent(uid, new ResultCallback<List<Child>>() {
                @Override
                public void onSuccess(List<Child> children) {
                    if (children != null && !children.isEmpty() && selectedChildUid == null) {
                        Child firstChild = children.get(0);
                        selectedChildUid = firstChild.getChildUid();
                        if (btnChildSelector != null) {
                            btnChildSelector.setText(firstChild.getName() + " ▼");
                        }
                        updateChartData();
                        loadDailyLogsForSelectedChild();
                        loadRescueSummaryForSelectedChild();
                        loadChildPbAndPefZone();
                    }
                }

                @Override
                public void onError(Exception e) {
                    // Ignore; keep default UI
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start auto-refresh
        refreshHandler.post(logsRefreshRunnable);
        // Immediate refresh
        loadDailyLogsForSelectedChild();
        loadRescueSummaryForSelectedChild();
        loadTodaysPefZoneForParent();
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(logsRefreshRunnable);
    }

    // ===================== Chart Data ======================

    private void updateChartData() {
        int days = (toggle1W.getAlpha() == 1.0f) ? 7 : 30;

        if (selectedChildUid != null) {
            loadRealSymptomData(days);
        } else {
            loadDummyTriggersData(days);
        }
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
        for (int i = 0; i < days; i++) {
            entries.add(new Entry(i, dayCounts.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Symptoms per day");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);

        if (lineChartTriggers != null) {
            lineChartTriggers.setData(lineData);
            lineChartTriggers.invalidate();
        }
    }

    // ===================== POPUP MENU ======================

    private void showSettingsMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.settings_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_manage_children) {
                startActivity(new Intent(this, ActivitySettingsChild.class));
                return true;
            } else if (id == R.id.action_manage_providers) {
                startActivity(new Intent(this, ActivitySettingsProvider.class));
                return true;
            } else if (id == R.id.action_sign_out) {
                startActivity(new Intent(this, ParentLoginActivity.class));
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

    // ===================== MPAndroidChart setup ======================

    private void setupTriggersChart() {
        if (lineChartTriggers == null) {
            return;
        }

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
        if (lineChartTriggers == null) return;

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

    // ===================== INCIDENT & MEDICINE LOGS ======================

    private void loadDailyLogsForSelectedChild() {
        if (selectedChildUid == null || db == null) {
            return;
        }

        Calendar startCal = Calendar.getInstance();
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = (Calendar) startCal.clone();
        endCal.add(Calendar.DAY_OF_MONTH, 1);

        Timestamp startTs = new Timestamp(startCal.getTime());
        Timestamp endTs = new Timestamp(endCal.getTime());

        loadIncidentLogForDay(selectedChildUid, startTs, endTs);
        loadMedicineLogForDay(selectedChildUid, startTs, endTs);
    }

    private void loadIncidentLogForDay(String childUid, Timestamp startTs, Timestamp endTs) {
        db.collection("children")
                .document(childUid)
                .collection("incidents")
                .whereGreaterThanOrEqualTo("timeOfIncident", startTs)
                .whereLessThan("timeOfIncident", endTs)
                .orderBy("timeOfIncident", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (textIncidentLog == null) return;

                    if (querySnapshot.isEmpty()) {
                        textIncidentLog.setText("No incidents logged today.");
                        return;
                    }

                    StringBuilder sb = new StringBuilder();
                    SimpleDateFormat timeFormat =
                            new SimpleDateFormat("h:mma", Locale.getDefault());

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Timestamp ts = doc.getTimestamp("timeOfIncident");
                        Date date = (ts != null) ? ts.toDate() : null;
                        String timeStr = (date != null)
                                ? timeFormat.format(date).toLowerCase(Locale.getDefault())
                                : "N/A";

                        List<String> symptoms =
                                (List<String>) doc.get("symptoms");
                        Long peakFlow = doc.getLong("peakFlow");
                        Long puffsOrMeasures = doc.getLong("puffsOrMeasures");
                        String type = doc.getString("type");
                        Boolean escalated = doc.getBoolean("escalatedIncident");

                        sb.append("Time: ").append(timeStr).append("\n");

                        sb.append("Flags Present:\n");
                        if (symptoms != null && !symptoms.isEmpty()) {
                            for (String s : symptoms) {
                                sb.append(" • ").append(formatSymptomLabel(s)).append("\n");
                            }
                        } else {
                            sb.append(" • None\n");
                        }

                        String actionPlan;
                        if (Boolean.TRUE.equals(escalated)) {
                            actionPlan = "RED; emergency";
                        } else {
                            actionPlan = "YELLOW; managed at home";
                        }
                        sb.append("Action Plan: ").append(actionPlan).append("\n");

                        if (Boolean.TRUE.equals(escalated)) {
                            sb.append("Emergency Card Shown\n");
                        } else {
                            sb.append("Emergency Card Not Shown\n");
                        }

                        if (type != null && type.contains("rescue") && puffsOrMeasures != null) {
                            sb.append("Number of Rescue Attempts: ")
                                    .append(puffsOrMeasures)
                                    .append("\n");
                        } else {
                            sb.append("Number of Rescue Attempts: N/A\n");
                        }

                        sb.append("PEF: ")
                                .append(peakFlow != null ? peakFlow : "N/A");

                        sb.append("\n\n");
                    }

                    if (sb.length() >= 2) {
                        sb.setLength(sb.length() - 2);
                    }

                    textIncidentLog.setText(sb.toString());
                })
                .addOnFailureListener(e -> {
                    if (textIncidentLog != null) {
                        textIncidentLog.setText("Unable to load incidents.");
                    }
                });
    }

    private void loadMedicineLogForDay(String childUid, Timestamp startTs, Timestamp endTs) {
        db.collection("children")
                .document(childUid)
                .collection("medLogs")
                .whereGreaterThanOrEqualTo("createdAt", startTs)
                .whereLessThan("createdAt", endTs)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (textMedicineLog == null) return;

                    if (querySnapshot.isEmpty()) {
                        textMedicineLog.setText("No medicine taken today.");
                        return;
                    }

                    StringBuilder sb = new StringBuilder();
                    SimpleDateFormat timeFormat =
                            new SimpleDateFormat("h:mma", Locale.getDefault());

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Timestamp ts = doc.getTimestamp("createdAt");
                        Date date = (ts != null) ? ts.toDate() : null;
                        String timeStr = (date != null)
                                ? timeFormat.format(date).toLowerCase(Locale.getDefault())
                                : "N/A";

                        String medicineType = doc.getString("medicineType");
                        Long puffs = doc.getLong("puffsOrMeasures");

                        String typeLabel;
                        if ("controller".equalsIgnoreCase(medicineType)) {
                            typeLabel = "Controller";
                        } else if ("rescue".equalsIgnoreCase(medicineType)) {
                            typeLabel = "Rescue";
                        } else {
                            typeLabel = "Medicine";
                        }

                        long puffCount = (puffs != null) ? puffs : 0;

                        sb.append(typeLabel)
                                .append(" – ")
                                .append(timeStr)
                                .append(" – ")
                                .append(puffCount)
                                .append(" puff(s)")
                                .append("\n");
                    }

                    if (sb.length() >= 1) {
                        sb.setLength(sb.length() - 1);
                    }

                    textMedicineLog.setText(sb.toString());
                })
                .addOnFailureListener(e -> {
                    if (textMedicineLog != null) {
                        textMedicineLog.setText("Unable to load medicine log.");
                    }
                });
    }

    private String formatSymptomLabel(String code) {
        if (code == null) return "Unknown";
        switch (code) {
            case "chest_pulling_retractions":
                return "Chest pulling";
            case "blue_grey_lips_or_nails":
                return "Blue nails";
            case "cant_speak_full_sentences":
                return "Can't speak full sentences";
            default:
                return code.replace('_', ' ');
        }
    }

    // ===================== WEEKLY RESCUE + LAST RESCUE ======================

    private void loadRescueSummaryForSelectedChild() {
        if (selectedChildUid == null || db == null) {
            return;
        }

        // Start of 7-day window (today + previous 6 days)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6);
        Timestamp weekStart = new Timestamp(cal.getTime());

        db.collection("children")
                .document(selectedChildUid)
                .collection("medLogs")
                .whereGreaterThanOrEqualTo("createdAt", weekStart)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {

                    int weeklyRescueCount = 0;
                    Timestamp latestRescueTs = null;

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String medicineType = doc.getString("medicineType");
                        if (medicineType == null) {
                            continue;
                        }

                        // Only count rescue meds
                        if (!"rescue".equalsIgnoreCase(medicineType)) {
                            continue;
                        }

                        weeklyRescueCount++;

                        Timestamp ts = doc.getTimestamp("createdAt");
                        if (ts != null && (latestRescueTs == null || ts.compareTo(latestRescueTs) > 0)) {
                            latestRescueTs = ts;
                        }
                    }

                    // --- Update Weekly Rescue Count ---
                    if (textWeeklyRescueValue != null) {
                        textWeeklyRescueValue.setText(weeklyRescueCount + " per week");
                    }

                    // --- Update Last Rescue Time ---
                    if (textLastRescueValue != null) {
                        if (latestRescueTs == null) {
                            textLastRescueValue.setText("No rescue use yet");
                        } else {
                            Date d = latestRescueTs.toDate();
                            SimpleDateFormat dateFormat =
                                    new SimpleDateFormat("MMMM d yyyy", Locale.getDefault());
                            SimpleDateFormat timeFormat =
                                    new SimpleDateFormat("h:mma", Locale.getDefault());

                            String dateStr = dateFormat.format(d);
                            String timeStr = timeFormat.format(d).toLowerCase(Locale.getDefault());

                            textLastRescueValue.setText(dateStr + " -\n" + timeStr);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (textWeeklyRescueValue != null) {
                        textWeeklyRescueValue.setText("N/A");
                    }
                    if (textLastRescueValue != null) {
                        textLastRescueValue.setText("N/A");
                    }
                });
    }

    // ===================== PEF ZONE (PARENT) ======================

    private void loadChildPbAndPefZone() {
        if (selectedChildUid == null) return;

        db.collection("children")
                .document(selectedChildUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Long pb = snapshot.getLong("personalBest");
                    if (pb != null) {
                        personalBest = pb.intValue();
                    } else {
                        personalBest = 0;
                    }
                    loadTodaysPefZoneForParent();
                })
                .addOnFailureListener(e -> {
                    personalBest = 0;
                    showPefZoneUnknownParent("N/A", "Set PB in parent app");
                });
    }

    /**
     * Same logic as child home: latest PEF of today from medLogs/checkins.
     */
    private void loadTodaysPefZoneForParent() {
        if (selectedChildUid == null) {
            return;
        }

        if (personalBest <= 0) {
            showPefZoneUnknownParent("N/A", "Set PB in parent app");
            return;
        }

        final String todayKey = getTodayKey();

        db.collection("children")
                .document(selectedChildUid)
                .collection("medLogs")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(medSnapshot -> {
                    Long latestMedPef = null;
                    Timestamp latestMedTime = null;

                    for (QueryDocumentSnapshot doc : medSnapshot) {
                        Timestamp ts = doc.getTimestamp("createdAt");
                        if (ts == null) continue;
                        if (!toDateKey(ts).equals(todayKey)) continue;

                        Long pef = doc.getLong("peakFlow");
                        if (pef == null) continue;

                        if (latestMedTime == null || ts.compareTo(latestMedTime) > 0) {
                            latestMedTime = ts;
                            latestMedPef = pef;
                        }
                    }

                    loadTodaysPefZoneFromCheckinsForParent(todayKey, latestMedPef, latestMedTime);
                })
                .addOnFailureListener(e -> {
                    showPefZoneUnknownParent("N/A", "No PEF today");
                });
    }

    private void loadTodaysPefZoneFromCheckinsForParent(
            String todayKey,
            Long latestMedPef,
            Timestamp latestMedTime
    ) {
        db.collection("children")
                .document(selectedChildUid)
                .collection("checkins")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(checkinSnapshot -> {
                    Long latestCheckinPef = null;
                    Timestamp latestCheckinTime = null;

                    for (QueryDocumentSnapshot doc : checkinSnapshot) {
                        Timestamp ts = doc.getTimestamp("createdAt");
                        if (ts == null) continue;
                        if (!toDateKey(ts).equals(todayKey)) continue;

                        Long pef = doc.getLong("peakFlow");
                        if (pef == null) continue;

                        if (latestCheckinTime == null || ts.compareTo(latestCheckinTime) > 0) {
                            latestCheckinTime = ts;
                            latestCheckinPef = pef;
                        }
                    }

                    Long latestPef = null;
                    if (latestMedTime != null &&
                            (latestCheckinTime == null || latestMedTime.compareTo(latestCheckinTime) >= 0)) {
                        latestPef = latestMedPef;
                    } else if (latestCheckinTime != null) {
                        latestPef = latestCheckinPef;
                    }

                    if (latestPef == null) {
                        showPefZoneUnknownParent("N/A", "No PEF today");
                        return;
                    }

                    ZoneService.ZoneResult result =
                            ZoneService.computeZone(latestPef, personalBest);

                    if (result.getZone() == ZoneService.PefZone.UNKNOWN) {
                        showPefZoneUnknownParent("N/A", "No PEF today");
                        return;
                    }

                    showPefZoneParent(result);
                })
                .addOnFailureListener(e -> {
                    showPefZoneUnknownParent("N/A", "No PEF today");
                });
    }

    private void showPefZoneUnknownParent(String valueText, String subtitleText) {
        if (textPefValueParent == null || textPefSubtitleParent == null || bannerPefZoneParent == null) {
            return;
        }
        textPefValueParent.setText(valueText);
        textPefSubtitleParent.setText(subtitleText);
        setPefZoneBackgroundColorParent(COLOR_DEFAULT_BG);
        textPefValueParent.setTextColor(COLOR_TEXT_WHITE);
        textPefSubtitleParent.setTextColor(COLOR_TEXT_MUTED);
    }

    private void showPefZoneParent(ZoneService.ZoneResult result) {
        if (textPefValueParent == null || textPefSubtitleParent == null || bannerPefZoneParent == null) {
            return;
        }

        int percent = result.getPercentOfPb();
        textPefValueParent.setText(percent + "% PEF");
        textPefSubtitleParent.setText("of PB");

        int bgColor;
        switch (result.getZone()) {
            case GREEN:
                bgColor = COLOR_GREEN;
                break;
            case YELLOW:
                bgColor = COLOR_YELLOW;
                break;
            case RED:
                bgColor = COLOR_RED;
                break;
            default:
                bgColor = COLOR_DEFAULT_BG;
                break;
        }

        setPefZoneBackgroundColorParent(bgColor);
        textPefValueParent.setTextColor(COLOR_TEXT_WHITE);
        textPefSubtitleParent.setTextColor(COLOR_TEXT_WHITE);
    }

    private void setPefZoneBackgroundColorParent(int color) {
        if (bannerPefZoneParent == null) return;

        Drawable background = bannerPefZoneParent.getBackground();
        if (background instanceof GradientDrawable) {
            ((GradientDrawable) background.mutate()).setColor(color);
        } else {
            bannerPefZoneParent.setBackgroundColor(color);
        }
    }

    // ---------------- Date helpers ----------------

    private String toDateKey(Timestamp ts) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(ts.toDate());
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day);
    }

    private String getTodayKey() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day);
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
            startActivity(new Intent(this, ActivitySettingsChild.class));
            return true;
        } else if (id == R.id.action_manage_providers) {
            startActivity(new Intent(this, ActivitySettingsProvider.class));
            return true;
        } else if (id == R.id.action_sign_out) {
            startActivity(new Intent(this, ParentLoginActivity.class));
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
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        PacGenerator pacGenerator = new PacGenerator();
        String initialCode = pacGenerator.getPacCode();
        tvCode.setText(initialCode);

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
            ClipboardManager clipboard =
                    (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
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
                            Toast.makeText(ParentHomeActivity.this,
                                    "Selected: " + child.getName(),
                                    Toast.LENGTH_SHORT).show();
                            updateChartData();
                            loadDailyLogsForSelectedChild();
                            loadRescueSummaryForSelectedChild();
                            loadChildPbAndPefZone();
                            dialog.dismiss();
                        });
                        container.addView(childBtn);
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(ParentHomeActivity.this,
                            "Error loading children", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }

        dialog.show();
    }
}