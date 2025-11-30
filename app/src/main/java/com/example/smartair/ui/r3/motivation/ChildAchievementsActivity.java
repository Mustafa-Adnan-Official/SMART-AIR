package com.example.smartair.ui.r3.motivation;

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
import com.example.smartair.services.AchievementService;

/**
 * Full-screen Achievements page for the child.
 *
 * Reached from: ChildHome "View full achievements" button.
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

    // Simple hard-coded "unlocked" colour (blue-ish)
    private static final int UNLOCKED_COLOR = Color.parseColor("#143C8F");
    private static final int LOCKED_COLOR = Color.parseColor("#000000");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_child_achievements);

        // Get child UID from Intent
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
        // Screen is simple, so no progress bar – you could disable cards here if you want.
    }

    @Override
    public void hideLoading() {
        // No-op for now.
    }

    @Override
    public void showAchievements(AchievementSummary summary) {
        // 1) Perfect Controller Week badge
        setCardUnlocked(
                summary.isPerfectControllerWeekBadgeEarned(),
                iconPerfectController,
                textPerfectControllerTitle
        );

        // 2) Technique Master badge
        //    (we’ll treat ANY of the HQ technique badges as unlocking this card)
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

        // 3) Low Rescue Month badge
        boolean lowRescueUnlocked = summary.getLowRescueMonthBadgeCount() > 0;
        setCardUnlocked(
                lowRescueUnlocked,
                iconLowRescue,
                textLowRescueTitle
        );

        // 4) Streak Master
        boolean streakUnlocked = summary.getStreakMasterBadge() != null
                && summary.getStreakMasterBadge().isUnlocked();

        setCardUnlocked(
                streakUnlocked,
                iconStreakMaster,
                textStreakMasterTitle
        );

        // If you have streak numbers on StreakMasterBadge, you can show them here.
        if (summary.getStreakMasterBadge() != null) {
            int current = summary.getStreakMasterBadge().getCurrentStreakDays();
            int highest = summary.getStreakMasterBadge().getHighestStreakDays();

            textStreakCurrent.setText("Current streak: " + current + " days");
            textStreakHighest.setText("Highest streak: " + highest + " days");
        }
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // ---------------- Helper ----------------

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
}
