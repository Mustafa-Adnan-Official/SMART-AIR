// File: ProviderReportExporter.java
package com.example.smartair.services;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.smartair.models.providercollections.ReportExportRequest;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Map;

public final class ProviderReportExporter {

    private static final String DISCLAIMER =
            "This is guidance, not a diagnosis. If in doubt, call emergency.";

    private ProviderReportExporter() {}

    public enum Format { CSV, PDF }

    public static void export(
            Context context,
            ReportExportRequest request,
            Format format
    ) {

        String providerUid = FirebaseAuth.getInstance().getUid();
        if (providerUid == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference reportRef =
                db.collection("children")
                        .document(request.getChildUid())
                        .collection("reports")
                        .document(request.getReportId());

        DocumentReference accessRef =
                reportRef.collection("providerAccess")
                        .document(providerUid);

        Tasks.whenAllSuccess(reportRef.get(), accessRef.get())
                .addOnSuccessListener(result -> {

                    DocumentSnapshot reportSnap = (DocumentSnapshot) result.get(0);
                    DocumentSnapshot accessSnap = (DocumentSnapshot) result.get(1);

                    if (!reportSnap.exists()) return;

                    if (!"shared".equalsIgnoreCase(accessSnap.getString("access"))) {
                        safeToast(context, "Access to this report was revoked.");
                        return;
                    }

                    Map<String, Object> visibleData =
                            ReportVisibilityFilter.apply(reportSnap, accessSnap);

                    if (format == Format.CSV) {
                        writeCsv(context, visibleData, request.isHistory());
                    } else {
                        writePdf(context, visibleData, request.isHistory());
                    }
                });
    }

    // ---------------- CSV ----------------

    private static void writeCsv(Context context,
                                 Map<String, Object> data,
                                 boolean isHistory) {
        try {
            File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (dir == null) return;

            File file = new File(
                    dir,
                    "smartair_" +
                            (isHistory ? "history_" : "report_") +
                            new Date().getTime() + ".csv"
            );

            FileOutputStream fos = new FileOutputStream(file);

            for (String key : data.keySet()) {
                fos.write((key + "," + data.get(key) + "\n").getBytes());
            }

            fos.write(("\nDisclaimer," + DISCLAIMER).getBytes());
            fos.close();

            safeToast(context, "CSV saved to Downloads");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- PDF ----------------

    private static void writePdf(Context context,
                                 Map<String, Object> data,
                                 boolean isHistory) {

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();

        final int PAGE_WIDTH = 595;
        final int PAGE_HEIGHT = 842;
        final int MARGIN_X = 40;
        final int MARGIN_Y = 50;
        final int LINE_HEIGHT = 20;

        int pageNumber = 1;
        int y = MARGIN_Y;

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(
                        PAGE_WIDTH, PAGE_HEIGHT, pageNumber
                ).create();

        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText(
                isHistory ? "SMART AIR – History Report"
                        : "SMART AIR – Provider Report",
                MARGIN_X, y, paint
        );

        y += LINE_HEIGHT * 2;
        paint.setTextSize(12);
        paint.setFakeBoldText(false);

        for (Map.Entry<String, Object> entry : data.entrySet()) {

            if (y > PAGE_HEIGHT - MARGIN_Y) {
                pdfDocument.finishPage(page);

                pageNumber++;
                pageInfo = new PdfDocument.PageInfo.Builder(
                        PAGE_WIDTH, PAGE_HEIGHT, pageNumber
                ).create();

                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                y = MARGIN_Y;
            }

            canvas.drawText(
                    entry.getKey() + ": " + entry.getValue(),
                    MARGIN_X,
                    y,
                    paint
            );
            y += LINE_HEIGHT;
        }

        y += LINE_HEIGHT;

        if (y > PAGE_HEIGHT - MARGIN_Y) {
            pdfDocument.finishPage(page);

            pageNumber++;
            pageInfo = new PdfDocument.PageInfo.Builder(
                    PAGE_WIDTH, PAGE_HEIGHT, pageNumber
            ).create();

            page = pdfDocument.startPage(pageInfo);
            canvas = page.getCanvas();
            y = MARGIN_Y;
        }

        paint.setFakeBoldText(true);
        canvas.drawText("Disclaimer:", MARGIN_X, y, paint);
        y += LINE_HEIGHT;

        paint.setFakeBoldText(false);
        canvas.drawText(DISCLAIMER, MARGIN_X, y, paint);

        pdfDocument.finishPage(page);

        try {
            File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (dir == null) return;

            File file = new File(
                    dir,
                    "smartair_" +
                            (isHistory ? "history_" : "report_") +
                            System.currentTimeMillis() + ".pdf"
            );

            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();

            safeToast(context, "PDF saved to Downloads");

            openPdf(context, file);

        } catch (Exception e) {
            e.printStackTrace();
            safeToast(context, "Failed to generate PDF");
        }
    }

    // ---------------- Open PDF ----------------

    private static void openPdf(Context context, File file) {
        try {
            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
            safeToast(context, "No app found to open PDF");
        }
    }

    // ---------------- Toast Safety ----------------

    private static void safeToast(Context context, String message) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }
}
