package com.example.smartair.ui.HomeActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.ui.login.ParentLoginActivity;

// MPAndroidChart imports
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Parent home screen.
 */
public class ParentHomeActivity extends AppCompatActivity {

    // Chart + toggle buttons
    private LineChart lineChartTriggers;
    private Button toggle1W;
    private Button toggle1M;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_home);

        // --- Find views from XML ---
        lineChartTriggers = findViewById(R.id.linechart_triggers);
        toggle1W = findViewById(R.id.toggle_1w);
        toggle1M = findViewById(R.id.toggle_1m);

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
            // TODO: Re-enable when ActivitySettingsChild screen exists again
            // Intent intent = new Intent(this, ActivitySettingsChild.class);
            // startActivity(intent);
            return true;
        } else if (id == R.id.action_manage_providers) {
            // TODO: Re-enable when ActivitySettingsProvider screen exists again
            // Intent intent = new Intent(this, ActivitySettingsProvider.class);
            // startActivity(intent);
            return true;
        } else if (id == R.id.action_sign_out) {
            Intent intent = new Intent(this, ParentLoginActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
