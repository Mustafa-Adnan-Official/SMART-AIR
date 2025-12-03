package com.example.smartair.ui.HomeActivities;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.models.childcollections.Checkin;
import com.example.smartair.models.childcollections.MedLog;
import com.example.smartair.services.AuthService;
import com.example.smartair.services.ZoneService;
import com.example.smartair.ui.checkin.DailyCheckinActivity;
import com.example.smartair.ui.historyBrowser.HistoryBrowserActivity;
import com.example.smartair.ui.meds.ControllerSessionActivity;
import com.example.smartair.ui.meds.RescueSessionActivity;
import com.example.smartair.ui.motivation.ChildAchievementsActivity;
import com.example.smartair.ui.triage.TriageStep1Activity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Child home screen.
 *
 * Responsibilities:
 * - Greet child by name.
 * - Show today's PEF zone tile from latest PEF of today (medLogs, checkins, or incidents).
 * - Show controller streak and technique streak.
 * - Navigate to Rescue / Controller sessions, Triage, and Achievements screen.
 * - Gate med logging based on inventory for children under a parent.
 */
public class ChildHomeActivity extends AppCompatActivity {

    private ImageButton btnSettings;
    private ImageButton btnProfilePicture;

    private Button btnViewHistory;
    private Button btnDailyCheckin;
    private Button btnTroubleBreathing;
    private Button btnRescueInhaler;
    private Button btnControllerMedicine;
    private Button btnAchievements;

    private String childName;

    private TextView textChildName;
    private TextView textControllerStreak;
    private TextView textTechniqueStreak;

    // PEF zone banner
    private LinearLayout bannerPefZone;
    private TextView textPefValue;
    private TextView textPefSubtitle;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String childUid;

    // PB from children/{childUid}.personalBest
    private int personalBest = 0;

    // If true  -> child has own email (no parent inventory, skip checks)
    // If false -> child is under parent (enforce inventory)
    // If null  -> not loaded yet; we treat as true (don’t block child).
    private Boolean hasOwnEmail = null;

    // Helper for per-day usage
    private static class DayUsage {
        boolean controllerUsed;
        boolean techniqueUsed;
    }

    // Colours for zone banner
    private static final int COLOR_GREEN = Color.parseColor("#2E7D32");
    private static final int COLOR_YELLOW = Color.parseColor("#F9A825");
    private static final int COLOR_RED = Color.parseColor("#C62828");
    private static final int COLOR_DEFAULT_BG = Color.parseColor("#143C8F"); // fallback
    private static final int COLOR_TEXT_WHITE = Color.parseColor("#FFFFFF");
    private static final int COLOR_TEXT_MUTED = Color.parseColor("#757575");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen_activity_child);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        bindViews();
        setupClickListeners();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No logged-in child. Please sign in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        childUid = user.getUid();

        // Load name + PB + hasOwnEmail and initial streaks + zone
        loadChildNameAndFlags();
        loadStreaksForChild();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (childUid != null) {
            // Reload name + hasOwnEmail + personalBest, then PEF zone
            loadChildNameAndFlags();

            // Reload streaks too
            loadStreaksForChild();
        }
    }

    // ---------------- Binding & Clicks ----------------

    private void bindViews() {
        // Fetch Child Name
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("children")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            childName = documentSnapshot.getString("name");
                            if (childName != null && !childName.trim().isEmpty()) {
                                textChildName.setText(childName);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(ChildHomeActivity.this, "Failed to load name", Toast.LENGTH_SHORT).show());
        }

        // Top bar
        btnSettings = findViewById(R.id.btn_settings);
        btnProfilePicture = findViewById(R.id.btn_profile_picture);

        btnViewHistory = findViewById(R.id.btn_view_history);
        btnDailyCheckin = findViewById(R.id.btn_daily_checkin);
        btnTroubleBreathing = findViewById(R.id.btn_trouble_breathing);
        btnRescueInhaler = findViewById(R.id.btn_rescue_inhaler);
        btnControllerMedicine = findViewById(R.id.btn_controller_medicine);
        btnAchievements = findViewById(R.id.btn_achievements);

        textChildName = findViewById(R.id.text_child_name);
        textControllerStreak = findViewById(R.id.text_controller_streak);
        textTechniqueStreak = findViewById(R.id.text_technique_streak);

        bannerPefZone = findViewById(R.id.banner_pef_zone);
        textPefValue = findViewById(R.id.text_pef_value);
        textPefSubtitle = findViewById(R.id.text_pef_subtitle);
    }

    private void setupClickListeners() {
        btnSettings.setOnClickListener(v -> {
            // TODO: open child settings screen
            // startActivity(new Intent(ChildHomeActivity.this, ActivitySettingsChild.class));
        });

        btnProfilePicture.setOnClickListener(v -> {
            // TODO: open profile popup later
        });

        btnViewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChildHomeActivity.this, HistoryBrowserActivity.class);
                intent.putExtra("childUid", childUid);
                startActivity(intent);
            }

        });

        btnDailyCheckin.setOnClickListener(v -> {
            FirebaseUser current = auth.getCurrentUser();
            if (current == null) {
                Toast.makeText(ChildHomeActivity.this, "No logged-in child.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(ChildHomeActivity.this, DailyCheckinActivity.class);
            intent.putExtra("USER_ROLE", "Child");
            intent.putExtra("USER_NAME", childName);
            intent.putExtra("USER_ID", current.getUid());
            startActivity(intent);
        });

        btnTroubleBreathing.setOnClickListener(v -> {
            Intent i = new Intent(ChildHomeActivity.this, TriageStep1Activity.class);
            startActivity(i);
        });

        // Rescue + Controller go through inventory gate if child is under parent
        btnRescueInhaler.setOnClickListener(v -> handleMedButtonClick("rescue"));

        btnControllerMedicine.setOnClickListener(v -> handleMedButtonClick("controller"));

        btnAchievements.setOnClickListener(v -> {
            AuthService authService = new AuthService();
            String currentChildUid = authService.getCurrentUserUid();

            if (currentChildUid == null || currentChildUid.trim().isEmpty()) {
                Toast.makeText(ChildHomeActivity.this,
                        "No logged-in child found.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(
                    ChildHomeActivity.this,
                    ChildAchievementsActivity.class
            );
            intent.putExtra("childUid", currentChildUid);
            startActivity(intent);
        });
    }

    // ---------------- Load Child Name + flags + PB ----------------

    /**
     * Reads children/{childUid}:
     *  - name
     *  - hasOwnEmail
     *  - personalBest
     * Then triggers today's PEF zone load.
     */
    private void loadChildNameAndFlags() {
        if (childUid == null) return;

        db.collection("children")
                .document(childUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String name = snapshot.getString("name");
                        if (name != null && !name.trim().isEmpty()) {
                            textChildName.setText(name);
                        }

                        Boolean own = snapshot.getBoolean("hasOwnEmail");
                        hasOwnEmail = (own != null) ? own : Boolean.TRUE;

                        Long pb = snapshot.getLong("personalBest");
                        if (pb != null) {
                            personalBest = pb.intValue();
                        } else {
                            personalBest = 0;
                        }
                    } else {
                        hasOwnEmail = Boolean.TRUE;
                        personalBest = 0;
                    }

                    // Once PB is known, compute zone from today's PEF.
                    loadTodaysPefZone();
                })
                .addOnFailureListener(e -> {
                    hasOwnEmail = Boolean.TRUE;
                    personalBest = 0;
                    // Still try to load today's PEF; zone will fall back to N/A.
                    loadTodaysPefZone();
                });
    }

    // ---------------- Inventory gating ----------------

    private void handleMedButtonClick(String medicineType) {
        if (!Boolean.FALSE.equals(hasOwnEmail)) {
            openMedScreen(medicineType);
            return;
        }

        db.collection("children")
                .document(childUid)
                .collection("inventory")
                .document(medicineType)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        String message;
                        if ("rescue".equals(medicineType)) {
                            message = "Your parent needs to set up a rescue inhaler in Settings before you can log it.";
                        } else {
                            message = "Your parent needs to set up controller medicine in Settings before you can log it.";
                        }
                        Toast.makeText(ChildHomeActivity.this, message, Toast.LENGTH_LONG).show();
                        return;
                    }

                    Long remaining = snapshot.getLong("dosesRemaining");
                    if (remaining != null && remaining <= 0) {
                        Toast.makeText(
                                ChildHomeActivity.this,
                                "Your " + medicineType + " inhaler looks empty.\n" +
                                        "You can still log this dose, but ask your parent " +
                                        "to update the inventory soon.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    openMedScreen(medicineType);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            ChildHomeActivity.this,
                            "Couldn't check inventory. Please try again.",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void openMedScreen(String medicineType) {
        if ("rescue".equals(medicineType)) {
            Intent i = new Intent(ChildHomeActivity.this, RescueSessionActivity.class);
            startActivity(i);
        } else {
            Intent i = new Intent(ChildHomeActivity.this, ControllerSessionActivity.class);
            startActivity(i);
        }
    }

    // ---------------- Today's PEF Zone logic ----------------

    /**
     * Finds the latest PEF of today (from medLogs, checkins, or incidents) and updates the banner.
     * If there is no PEF today or PB is not set, shows N/A and keeps banner safe.
     */
    private void loadTodaysPefZone() {
        if (childUid == null) {
            return;
        }

        if (personalBest <= 0) {
            showPefZoneUnknown("N/A", "Set PB in parent app");
            return;
        }

        final String todayKey = getTodayKey();

        // 1) Try medLogs
        db.collection("children")
                .document(childUid)
                .collection("medLogs")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(medSnapshot -> {

                    Long latestMedPef = null;
                    Timestamp latestMedTime = null;

                    for (QueryDocumentSnapshot doc : medSnapshot) {
                        MedLog log = doc.toObject(MedLog.class);
                        if (log == null) continue;

                        Timestamp ts = log.getCreatedAt();
                        if (ts == null) continue;
                        if (!toDateKey(ts).equals(todayKey)) continue;

                        Long pef = log.getPeakFlow();
                        if (pef == null) continue;

                        if (latestMedTime == null || ts.compareTo(latestMedTime) > 0) {
                            latestMedTime = ts;
                            latestMedPef = pef;
                        }
                    }

                    // hand off to helper for checkins + final decision
                    loadTodaysPefZoneFromCheckins(todayKey, latestMedPef, latestMedTime);
                })
                .addOnFailureListener(e -> {
                    // If medLogs query fails, still try checkins and incidents
                    loadTodaysPefZoneFromCheckins(todayKey, null, null);
                });
    }

    private void loadTodaysPefZoneFromCheckins(String todayKey,
                                               Long latestMedPef,
                                               Timestamp latestMedTime) {

        db.collection("children")
                .document(childUid)
                .collection("checkins")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(checkinSnapshot -> {

                    Long latestCheckinPef = null;
                    Timestamp latestCheckinTime = null;

                    for (QueryDocumentSnapshot doc : checkinSnapshot) {
                        Checkin checkin = doc.toObject(Checkin.class);
                        if (checkin == null) continue;

                        Timestamp ts = checkin.getCreatedAt();
                        if (ts == null) continue;
                        if (!toDateKey(ts).equals(todayKey)) continue;

                        // Convert int to Long for comparison
                        Long pef = (long) checkin.getPeakFlow();
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
                        // No PEF in medLogs or checkins → fallback to incidents
                        loadTodaysPefZoneFromIncidents(todayKey);
                        return;
                    }

                    ZoneService.ZoneResult result =
                            ZoneService.computeZone(latestPef, personalBest);

                    if (result.getZone() == ZoneService.PefZone.UNKNOWN) {
                        loadTodaysPefZoneFromIncidents(todayKey);
                        return;
                    }

                    showPefZone(result);
                })
                .addOnFailureListener(e -> {
                    // checkins query failed → still try incidents
                    loadTodaysPefZoneFromIncidents(todayKey);
                });
    }

    /**
     * Final fallback: look at incidents for today's PEF (e.g., immediate emergency from Step 1).
     */
    private void loadTodaysPefZoneFromIncidents(String todayKey) {
        db.collection("children")
                .document(childUid)
                .collection("incidents")
                .orderBy("timeOfIncident", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(incidentSnapshot -> {

                    Long latestIncidentPef = null;
                    Timestamp latestIncidentTime = null;

                    for (QueryDocumentSnapshot doc : incidentSnapshot) {
                        Timestamp ts = doc.getTimestamp("timeOfIncident");
                        if (ts == null) continue;
                        if (!toDateKey(ts).equals(todayKey)) continue;

                        Long pef = null;
                        Object raw = doc.get("peakFlow");
                        if (raw instanceof Long) {
                            pef = (Long) raw;
                        } else if (raw instanceof Integer) {
                            pef = ((Integer) raw).longValue();
                        }

                        if (pef == null) continue;

                        if (latestIncidentTime == null || ts.compareTo(latestIncidentTime) > 0) {
                            latestIncidentTime = ts;
                            latestIncidentPef = pef;
                        }
                    }

                    if (latestIncidentPef == null) {
                        showPefZoneUnknown("N/A", "No PEF today");
                        return;
                    }

                    ZoneService.ZoneResult result =
                            ZoneService.computeZone(latestIncidentPef, personalBest);

                    if (result.getZone() == ZoneService.PefZone.UNKNOWN) {
                        showPefZoneUnknown("N/A", "No PEF today");
                    } else {
                        showPefZone(result);
                    }
                })
                .addOnFailureListener(e -> {
                    showPefZoneUnknown("N/A", "No PEF today");
                });
    }

    private void showPefZoneUnknown(String valueText, String subtitleText) {
        textPefValue.setText(valueText);
        textPefSubtitle.setText(subtitleText);
        setPefZoneBackgroundColor(COLOR_DEFAULT_BG);
        textPefValue.setTextColor(COLOR_TEXT_WHITE);
        textPefSubtitle.setTextColor(COLOR_TEXT_MUTED);
    }

    private void showPefZone(ZoneService.ZoneResult result) {
        int percent = result.getPercentOfPb();
        textPefValue.setText(percent + "% PEF");
        textPefSubtitle.setText("of PB");

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

        setPefZoneBackgroundColor(bgColor);
        textPefValue.setTextColor(COLOR_TEXT_WHITE);
        textPefSubtitle.setTextColor(COLOR_TEXT_WHITE);
    }

    // ---------------- Streak Logic ----------------

    private void loadStreaksForChild() {
        if (childUid == null) return;

        db.collection("children")
                .document(childUid)
                .collection("medLogs")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(200)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, DayUsage> usageByDay = new HashMap<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        MedLog log = doc.toObject(MedLog.class);
                        if (log == null) continue;

                        Timestamp ts = log.getCreatedAt();
                        if (ts == null) continue;

                        String dateKey = toDateKey(ts);
                        DayUsage usage = usageByDay.get(dateKey);
                        if (usage == null) {
                            usage = new DayUsage();
                            usageByDay.put(dateKey, usage);
                        }

                        String type = log.getMedicineType();
                        Boolean techniqueUsed = log.getTechniqueTrainerUsed();

                        if ("controller".equals(type)) {
                            usage.controllerUsed = true;
                            if (Boolean.TRUE.equals(techniqueUsed)) {
                                usage.techniqueUsed = true;
                            }
                        }
                    }

                    int controllerStreak = computeStreak(usageByDay, true);
                    int techniqueStreak = computeStreak(usageByDay, false);

                    textControllerStreak.setText(
                            "CONTROLLER STREAK: " + controllerStreak + " DAY" + (controllerStreak == 1 ? "" : "S")
                    );
                    textTechniqueStreak.setText(
                            "TECHNIQUE STREAK: " + techniqueStreak + " DAY" + (techniqueStreak == 1 ? "" : "S")
                    );
                })
                .addOnFailureListener(e -> {
                    textControllerStreak.setText("CONTROLLER STREAK: 0 DAYS");
                    textTechniqueStreak.setText("TECHNIQUE STREAK: 0 DAYS");
                });
    }

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

    private int computeStreak(Map<String, DayUsage> usageByDay, boolean isController) {
        int streak = 0;

        Calendar cal = Calendar.getInstance();

        while (true) {
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);

            String key = String.format(Locale.US, "%04d-%02d-%02d", year, month, day);
            DayUsage usage = usageByDay.get(key);

            boolean used;
            if (usage == null) {
                used = false;
            } else {
                used = isController ? usage.controllerUsed : usage.techniqueUsed;
            }

            if (!used) {
                break;
            }

            streak++;
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }

        return streak;
    }

    private void setPefZoneBackgroundColor(int color) {
        Drawable background = bannerPefZone.getBackground();
        if (background instanceof GradientDrawable) {
            ((GradientDrawable) background.mutate()).setColor(color);
        } else {
            // Fallback – should rarely happen
            bannerPefZone.setBackgroundColor(color);
        }
    }
}
