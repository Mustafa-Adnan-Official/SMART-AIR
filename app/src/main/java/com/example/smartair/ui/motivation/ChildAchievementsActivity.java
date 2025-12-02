package com.example.smartair.ui.motivation;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.models.childcollections.AchievementSummary;
import com.example.smartair.models.childcollections.StreakMasterBadge;
import com.example.smartair.services.AchievementService;

/**
 * Full-screen Achievements page for the child.
 *
 * Reached from: ChildHome "See All Achievements" button.
 */
public class ChildAchievementsActivity extends AppCompatActivity
        implements MotivationPresenter.View {

    // Cards + icons + titles
    private LinearLayout cardPerfectController;
    private ImageView iconPerfectController;
    private TextView textPerfectControllerTitle;

    private LinearLayout cardTechniqueMaster;
    private ImageView iconTechniqueMaster;
    private TextView textTechniqueMasterTitle;

    private LinearLayout cardLowRescueMonth;
    private ImageView iconLowRescue;
    private TextView textLowRescueTitle;

    private LinearLayout cardStreakMaster;
    private ImageView iconStreakMaster;
    private TextView textStreakMasterTitle;

    private TextView textStreakCurrent;
    private TextView textStreakHighest;

    private ImageButton buttonBack;

    private MotivationPresenter presenter;
    private String childUid;

    // Colours
    private static final int UNLOCKED_COLOR = Color.parseColor("#143C8F"); // blue
    private static final int LOCKED_COLOR   = Color.parseColor("#000000"); // black
    private static final int ALERT_COLOR    = Color.parseColor("#D32F2F"); // red

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.achievements_activity);

        childUid = getIntent().getStringExtra("childUid");
        if (childUid == null) {
            Toast.makeText(this, "Missing child UID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();

        presenter = new MotivationPresenter(
                childUid,
                new AchievementService(),
                this
        );

        buttonBack.setOnClickListener(v -> finish());

        presenter.loadAchievements();
    }

    private void bindViews() {
        cardPerfectController = findViewById(R.id.cardPerfectController);
        iconPerfectController = findViewById(R.id.iconPerfectController);
        textPerfectControllerTitle = findViewById(R.id.textPerfectControllerTitle);

        cardTechniqueMaster = findViewById(R.id.cardTechniqueMaster);
        iconTechniqueMaster = findViewById(R.id.iconTechniqueMaster);
        textTechniqueMasterTitle = findViewById(R.id.textTechniqueMasterTitle);

        cardLowRescueMonth = findViewById(R.id.cardLowRescueMonth);
        iconLowRescue = findViewById(R.id.iconLowRescue);
        textLowRescueTitle = findViewById(R.id.textLowRescueTitle);

        cardStreakMaster = findViewById(R.id.cardStreakMaster);
        iconStreakMaster = findViewById(R.id.iconStreakMaster);
        textStreakMasterTitle = findViewById(R.id.textStreakMasterTitle);

        textStreakCurrent = findViewById(R.id.textStreakCurrent);
        textStreakHighest = findViewById(R.id.textStreakHighest);

        buttonBack = findViewById(R.id.buttonBack);
    }

    // ---------------- MotivationPresenter.View ----------------

    @Override
    public void showLoading() {
        // No spinner for now.
    }

    @Override
    public void hideLoading() {
        // No-op.
    }

    @Override
    public void showAchievements(AchievementSummary summary) {
        // --- Streak Master data from badge ---
        StreakMasterBadge badge = summary.getStreakMasterBadge();
        int currentStreak = 0;
        int highestStreak = 0;

        if (badge != null) {
            currentStreak = badge.getCurrentStreakDays();
            highestStreak = badge.getHighestStreakDays();
        }

        // 1) Perfect Controller Week: X / 7  (uses current streak, capped at 7)
        int perfectWeekProgress = Math.min(currentStreak, 7);
        String weekText = "Perfect Controller Week " + perfectWeekProgress + "/7";
        textPerfectControllerTitle.setText(weekText);

        boolean perfectWeekUnlocked =
                summary.isPerfectControllerWeekBadgeEarned() || highestStreak >= 7;

        setCardUnlocked(
                perfectWeekUnlocked,
                iconPerfectController,
                textPerfectControllerTitle
        );

        // 2) Technique Master (unchanged)
        boolean techniqueUnlocked =
                summary.isTenHQTechniqueSessionsBadgeEarned()
                        || summary.isThirtyHQTechniqueSessionsBadgeEarned()
                        || summary.isHundredHQTechniqueSessionsBadgeEarned()
                        || summary.isYearHundredHQTechniqueSessionsBadgeEarned();

        setCardUnlocked(
                techniqueUnlocked,
                iconTechniqueMaster,
                textTechniqueMasterTitle
        );

        // 3) Low Rescue Month:
        // lowRescueMonthBadgeCount now stores "rescue days in last 30 days"
        int rescueDaysLast30 = summary.getLowRescueMonthBadgeCount();
        if (rescueDaysLast30 <= 4) {
            // Good month => blue
            setCardUnlocked(
                    true,
                    iconLowRescue,
                    textLowRescueTitle
            );
        } else {
            // Too many rescue days => turn red
            textLowRescueTitle.setTextColor(Color.RED);
            iconLowRescue.setColorFilter(Color.RED);
        }

        // 4) Streak Master card
        boolean streakUnlocked = highestStreak >= 1;

        setCardUnlocked(
                streakUnlocked,
                iconStreakMaster,
                textStreakMasterTitle
        );

        textStreakCurrent.setText("Current streak: " + currentStreak + " days");
        textStreakHighest.setText("Highest streak: " + highestStreak + " days");
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // ---------------- Helpers ----------------

    private void setCardUnlocked(boolean unlocked,
                                 ImageView icon,
                                 TextView title) {
        if (unlocked) {
            title.setTextColor(UNLOCKED_COLOR);
            icon.setColorFilter(UNLOCKED_COLOR);
        } else {
            title.setTextColor(LOCKED_COLOR);
            icon.clearColorFilter();
        }
    }

    /**
     * Low rescue month:
     *  - <=4 rescue days in last 30 days  -> BLUE (good)
     *  - >4 rescue days                  -> RED (warning)
     */
    private void setLowRescueCardState(int rescueDaysLast30) {
        boolean healthy = rescueDaysLast30 <= 4;

        if (healthy) {
            textLowRescueTitle.setTextColor(UNLOCKED_COLOR);
            iconLowRescue.setColorFilter(UNLOCKED_COLOR);
        } else {
            textLowRescueTitle.setTextColor(ALERT_COLOR);
            iconLowRescue.setColorFilter(ALERT_COLOR);
        }
    }
}