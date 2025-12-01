package com.example.smartair.ui.HomeActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.models.childcollections.MedLog;
import com.example.smartair.ui.r3.meds.ControllerSessionActivity;
import com.example.smartair.ui.r3.meds.RescueSessionActivity;
import com.example.smartair.ui.r3.motivation.ChildAchievementsActivity;
import com.example.smartair.services.AuthService;
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
 * - Greet child by name (from children/{childUid}.name).
 * - Show controller streak and technique streak (computed from medLogs).
 * - Navigate to Rescue / Controller sessions and Achievements screen.
 * - If child is under a parent (hasOwnEmail == false), gate med logging
 *   based on inventory docs.
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

    private TextView textChildName;
    private TextView textControllerStreak;
    private TextView textTechniqueStreak;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String childUid;

    // If true  -> child has own email (no parent inventory, skip checks)
    // If false -> child is under parent (enforce inventory)
    // If null  -> not loaded yet; we treat as true (don’t block child).
    private Boolean hasOwnEmail = null;

    // Helper for per-day usage
    private static class DayUsage {
        boolean controllerUsed;
        boolean techniqueUsed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen_activity_child);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        bindViews();
        setupClickListeners();

        // Get currently logged-in Firebase user (child)
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No logged-in child. Please sign in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        childUid = user.getUid();

        // Load name + hasOwnEmail and initial streaks
        loadChildNameAndFlags();
        loadStreaksForChild();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // When coming back from logging meds / technique, recompute streaks
        if (childUid != null) {
            loadStreaksForChild();
        }
    }

    // ---------------- Binding & Clicks ----------------

    private void bindViews() {
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
    }

    private void setupClickListeners() {
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: open child settings screen
                // startActivity(new Intent(ChildHomeActivity.this, ActivitySettingsChild.class));
            }
        });

        btnProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: open profile popup later
            }
        });

        btnViewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: go to history screen
            }
        });

        btnDailyCheckin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: open daily check-in flow
            }
        });

        btnTroubleBreathing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: open emergency instructions
            }
        });

        // Rescue + Controller now go through inventory gate if child is under parent
        btnRescueInhaler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleMedButtonClick("rescue");
            }
        });

        btnControllerMedicine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleMedButtonClick("controller");
            }
        });

        btnAchievements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });
    }

    // ---------------- Load Child Name + hasOwnEmail ----------------

    /**
     * Reads children/{childUid}:
     *  - name → text_child_name
     *  - hasOwnEmail → decides if inventory checks are enforced
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
                        // Default to true (standalone child) if field missing
                        hasOwnEmail = (own != null) ? own : Boolean.TRUE;
                    } else {
                        hasOwnEmail = Boolean.TRUE;
                    }
                })
                .addOnFailureListener(e -> {
                    // On failure, assume standalone child so we don't block them
                    hasOwnEmail = Boolean.TRUE;
                });
    }

    // ---------------- Inventory gating ----------------

    /**
     * Handles click on Rescue / Controller button.
     *
     * If hasOwnEmail == true or not loaded yet → open screen directly.
     * If hasOwnEmail == false → check inventory doc first.
     */
    private void handleMedButtonClick(String medicineType) {
        // Standalone child → no parent-managed inventory, skip check
        if (!Boolean.FALSE.equals(hasOwnEmail)) {
            openMedScreen(medicineType);
            return;
        }

        // Child under parent → enforce inventory existence
        db.collection("children")
                .document(childUid)
                .collection("inventory")
                .document(medicineType) // "rescue" or "controller"
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

    // ---------------- Streak Logic (unchanged) ----------------

    /**
     * Loads medLogs for this child and computes:
     * - Controller streak (days in a row with any controller dose)
     * - Technique streak (days in a row with any controller dose that used technique trainer)
     *
     * Both streaks reset when a day is missed.
     */
    private void loadStreaksForChild() {
        if (childUid == null) return;

        db.collection("children")
                .document(childUid)
                .collection("medLogs")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(200) // plenty for recent streaks
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, DayUsage> usageByDay = new HashMap<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        MedLog log = doc.toObject(MedLog.class);
                        if (log == null) continue;

                        Timestamp ts = log.getCreatedAt();
                        if (ts == null) continue;

                        String dateKey = toDateKey(ts); // e.g., "2025-11-30"
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

    /**
     * Converts a Timestamp to a simple yyyy-MM-dd key in device local time.
     */
    private String toDateKey(Timestamp ts) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(ts.toDate());
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // 0-based
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day);
    }

    /**
     * Computes a streak ending today.
     *
     * @param usageByDay map from "yyyy-MM-dd" -> DayUsage
     * @param isController true  -> use controllerUsed
     *                     false -> use techniqueUsed
     */
    private int computeStreak(Map<String, DayUsage> usageByDay, boolean isController) {
        int streak = 0;

        Calendar cal = Calendar.getInstance(); // today

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
                // No logs on this day for the type we care about -> streak ends
                break;
            }

            streak++;
            // Move to previous day
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }

        return streak;
    }
}