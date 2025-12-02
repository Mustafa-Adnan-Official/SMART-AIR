package com.example.smartair.ui.reports;

import android.content.Context;

import com.example.smartair.services.ProviderReportService;
import com.example.smartair.services.PDFReportGenerator;
import com.google.firebase.Timestamp;

import java.util.Calendar;

public class ProviderReportImplementation {

    public interface ReportCallback {
        void onSuccess(String filePath);
        void onError(String error);
    }

    private ProviderReportService reportService;
    private PDFReportGenerator pdfGenerator;

    public ProviderReportImplementation() {
        reportService = new ProviderReportService();
        pdfGenerator = new PDFReportGenerator();
    }

    private Timestamp daysAgo(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -days);
        return new Timestamp(cal.getTime());
    }

    public void generateLast90DaysReport(Context context, String childUid, ReportCallback cb) {
        Timestamp end = Timestamp.now();
        Timestamp start = daysAgo(90);

        reportService.generateReport(childUid, start, end, new ProviderReportService.ReportCallback() {
            @Override
            public void onSuccess(String reportId) {
                pdfGenerator.generatePDF(context, childUid, reportId, new PDFReportGenerator.PDFCallback() {
                    @Override
                    public void onSuccess(java.io.File file) {
                        cb.onSuccess(file.getAbsolutePath());
                    }

                    @Override
                    public void onError(Exception e) {
                        cb.onError(e.getMessage());
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                cb.onError(e.getMessage());
            }
        });
    }
}

