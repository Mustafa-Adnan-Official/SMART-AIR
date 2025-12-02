package com.example.smartair.ui.dashboard;

import com.example.smartair.services.DashboardService;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ParentDashboardImplementation {

    public interface DashboardCallback {
        void onTodayZone(String zone);
        void onLastRescue(String label);
        void onWeeklyRescues(int count);
        void onTrend(List<DashboardService.TrendPoint> points);
        void onError(String msg);
    }

    private DashboardService service;

    public ParentDashboardImplementation() {
        service = new DashboardService();
    }

    public void loadTodayZone(String childUid, DashboardCallback cb) {
        service.getTodayZone(childUid, new DashboardService.ZoneCallback() {
            @Override
            public void onSuccess(String zone) {
                cb.onTodayZone(zone);
            }

            @Override
            public void onError(Exception e) {
                cb.onError(e.getMessage());
            }
        });
    }

    public void loadLastRescue(String childUid, DashboardCallback cb) {
        service.getLastRescueTime(childUid, new DashboardService.LastRescueCallback() {
            @Override
            public void onSuccess(Timestamp ts) {
                if (ts == null) {
                    cb.onLastRescue("No rescue yet");
                } else {
                    SimpleDateFormat f = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
                    cb.onLastRescue(f.format(ts.toDate()));
                }
            }

            @Override
            public void onError(Exception e) {
                cb.onError(e.getMessage());
            }
        });
    }

    public void loadWeeklyRescues(String childUid, DashboardCallback cb) {
        service.getWeeklyRescueCount(childUid, new DashboardService.WeeklyRescueCallback() {
            @Override
            public void onSuccess(int count) {
                cb.onWeeklyRescues(count);
            }

            @Override
            public void onError(Exception e) {
                cb.onError(e.getMessage());
            }
        });
    }

    public void loadTrend(String childUid, int dayRange, DashboardCallback cb) {
        service.getTrendSnippet(childUid, dayRange, new DashboardService.TrendSnippetCallback() {
            @Override
            public void onSuccess(List<DashboardService.TrendPoint> points) {
                cb.onTrend(points);
            }

            @Override
            public void onError(Exception e) {
                cb.onError(e.getMessage());
            }
        });
    }
}
