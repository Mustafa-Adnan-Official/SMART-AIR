package com.example.smartair;

import android.content.Context;
import java.io.File;

import com.example.smartair.services.ProviderReportExporter;

import java.lang.reflect.Method;
import java.util.Map;

public final class ProviderReportExporterTestUtil {

    private ProviderReportExporterTestUtil() {}

    @SuppressWarnings("unchecked")
    public static void callWritePdf(Context context,
                                    Map<String, Object> data,
                                    boolean isHistory) {
        try {
            Method method = ProviderReportExporter.class.getDeclaredMethod(
                    "writePdf",
                    Context.class,
                    Map.class,
                    boolean.class
            );

            method.setAccessible(true);
            method.invoke(null, context, data, isHistory);

        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke writePdf()", e);
        }
    }

    public static int countPdfs(File dir) {
        File[] files = dir.listFiles(
                (d, name) -> name.toLowerCase().endsWith(".pdf")
        );
        return files == null ? 0 : files.length;
    }

}
