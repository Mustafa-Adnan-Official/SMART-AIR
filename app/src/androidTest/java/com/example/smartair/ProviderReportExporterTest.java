package com.example.smartair;

import android.content.Context;
import android.os.Environment;
import com.example.smartair.ProviderReportExporterTestUtil;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ProviderReportExporterTest {

    @Test
    public void writePdf_createsPdfFile() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        assertNotNull(context);

        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        assertNotNull(dir);

        int beforeCount = ProviderReportExporterTestUtil.countPdfs(dir);

        Map<String, Object> mockData = new HashMap<>();
        mockData.put("Generated At", "2025-02-10");
        mockData.put("Controller Times Used", 5);
        mockData.put("Rescue Attempts", 2);

        ProviderReportExporterTestUtil.callWritePdf(
                context,
                mockData,
                false
        );

        int afterCount = beforeCount;
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < 2000) {
            afterCount = ProviderReportExporterTestUtil.countPdfs(dir);
            if (afterCount > beforeCount) break;
            Thread.sleep(100);
        }

        assertTrue(
                "PDF was not created within timeout",
                afterCount > beforeCount
        );
    }

}

