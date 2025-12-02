package com.example.smartair.services;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

public class PDFReportGenerator {

    private String childrenPath = "children";
    private String reportsPath = "reports";

    private FirebaseFirestore db;

    public interface PDFCallback {
        void onSuccess(File file);
        void onError(Exception e);
    }

    public PDFReportGenerator() {
        db = FirebaseFirestore.getInstance();
    }

    public void generatePDF(Context ctx, String childUid, String reportId, PDFCallback callback) {
        db.collection(childrenPath)
                .document(childUid)
                .collection(reportsPath)
                .document(reportId)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.exists()) {
                        callback.onError(new IllegalStateException("Report not found"));
                        return;
                    }

                    Map<String,Object> data = snap.getData();
                    if (data == null) {
                        callback.onError(new IllegalStateException("Empty report data"));
                        return;
                    }

                    try {
                        File outFile = new File(ctx.getCacheDir(), "provider_report_" + reportId + ".pdf");

                        PdfDocument pdf = new PdfDocument();
                        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                        PdfDocument.Page page = pdf.startPage(pageInfo);
                        Canvas canvas = page.getCanvas();

                        Paint paint = new Paint();
                        paint.setTextSize(14);

                        int y = 40;

                        canvas.drawText("SmartAir Provider Report", 40, y, paint);
                        y += 30;

                        canvas.drawText("Report ID: " + reportId, 40, y, paint);
                        y += 30;

                        canvas.drawText("----------------------------------", 40, y, paint);
                        y += 30;

                        canvas.drawText("Rescue Uses: " + data.get("rescues"), 40, y, paint);
                        y += 25;

                        canvas.drawText("Controller Uses: " + data.get("controllers"), 40, y, paint);
                        y += 25;

                        canvas.drawText("Controller Adherence (%): " + data.get("controllerAdherencePct"), 40, y, paint);
                        y += 25;

                        canvas.drawText("Zone Green: " + data.get("zoneGreen"), 40, y, paint);
                        y += 25;

                        canvas.drawText("Zone Yellow: " + data.get("zoneYellow"), 40, y, paint);
                        y += 25;

                        canvas.drawText("Zone Red: " + data.get("zoneRed"), 40, y, paint);
                        y += 25;

                        canvas.drawText("Triage Count: " + data.get("triageCount"), 40, y, paint);
                        y += 25;

                        canvas.drawText("Rapid Rescue Alerts: " + data.get("rapidRescueAlerts"), 40, y, paint);
                        y += 25;

                        canvas.drawText("Red Zone Alerts: " + data.get("redZoneAlerts"), 40, y, paint);
                        y += 25;

                        canvas.drawText("Inventory Alerts: " + data.get("inventoryAlerts"), 40, y, paint);
                        y += 25;

                        canvas.drawText("Worse After Dose Alerts: " + data.get("worseAfterDoseAlerts"), 40, y, paint);
                        y += 25;

                        canvas.drawText("----------------------------------", 40, y, paint);

                        pdf.finishPage(page);

                        FileOutputStream out = new FileOutputStream(outFile);
                        pdf.writeTo(out);
                        out.close();
                        pdf.close();

                        callback.onSuccess(outFile);

                    } catch (Exception e) {
                        callback.onError(e);
                    }
                })
                .addOnFailureListener(callback::onError);
    }
}
