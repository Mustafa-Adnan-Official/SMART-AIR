package com.example.smartair.ui.HomeActivities;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen_activity_parent);

        // --- Find views from XML ---
        lineChartTriggers = findViewById(R.id.linechart_triggers);
        toggle1W = findViewById(R.id.toggle_1w);
        toggle1M = findViewById(R.id.toggle_1m);
        btnSettings = findViewById(R.id.btn_settings);

        parentNameText = findViewById(R.id.text_parent_name);
        
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

        // Fetch Child Name (Async) - Move here so we can update UI inside callback
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
        updateChartData();
        toggle1W.setAlpha(1.0f);   // selected
        toggle1M.setAlpha(0.5f);   // not selected

        // --- Toggle listeners ---
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
            historyBrowserButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ParentHomeActivity.this, HistoryBrowserActivity.class);
                    intent.putExtra("childUid", selectedChildUid);
                    startActivity(intent);
                }

            });

        }

        if (checkInButton != null) {
            checkInButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Use DailyCheckinActivityParent for parents
                 Intent intent = new Intent(ParentHomeActivity.this, DailyCheckinActivityParent.class);
                 intent.putExtra("USER_ROLE", "Parent");
                 intent.putExtra("USER_NAME", parentName);
                 intent.putExtra("CHILD_UID", selectedChildUid);
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
                    }
                }

                @Override
                public void onError(Exception e) {
                    // Handle error or keep default state
                }
            });
        }
    }

    private void updateChartData() {
        // Check which toggle is active to decide days
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
        // Initialize all days to 0
        for(int i=0; i<days; i++) dayCounts.put(i, 0);

        long nowMillis = System.currentTimeMillis();
        
        for (Checkin checkin : checkins) {
            if (checkin.getCreatedAt() == null) continue;
            
            long time = checkin.getCreatedAt().toDate().getTime();
            long diff = nowMillis - time;
            long diffDays = TimeUnit.MILLISECONDS.toDays(diff);
            
            if (diffDays < days && diffDays >= 0) {
                // Index 0 = oldest day, Index (days-1) = today
                // diffDays 0 = today -> index = days-1
                // diffDays days-1 = oldest -> index = 0
                
                int index = (days - 1) - (int)diffDays; 
                
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

    //  POPUP MENU
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

    //  MPAndroidChart setup

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

        LineDataSet dataSet = new LineDataSet(entries, "Symptoms per day");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false); // no value labels on each point
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // smooth line

        LineData lineData = new LineData(dataSet);
        lineChartTriggers.setData(lineData);
        lineChartTriggers.invalidate(); // refresh/redraw
    }

    //  MENU (existing logic)

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
                     // Clear any existing views just in case, though it's a new dialog instance
                     container.removeAllViews();
                     
                     for (Child child : children) {
                         Button childBtn = new Button(ParentHomeActivity.this);
                         childBtn.setText(child.getName());
                         childBtn.setTextSize(18f);
                         childBtn.setPadding(20, 20, 20, 20);
                         // Optional: Style the button
                         
                         childBtn.setOnClickListener(v -> {
                             btnChildSelector.setText(child.getName() + " ▼");
                             selectedChildUid = child.getChildUid();
                             Toast.makeText(ParentHomeActivity.this, "Selected: " + child.getName(), Toast.LENGTH_SHORT).show();
                             updateChartData(); // <-- Add this line to update chart
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
