package com.example.smartair.ui.triage;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.smartair.R;

/**

 Full-screen dim overlay using triage_dialog_emergency.xml.
 Shows "PLEASE CALL EMERGENCY IMMEDIATELY" with a Close button.*/
public class TriageEmergencyDialog extends Dialog {

    public TriageEmergencyDialog(@NonNull Context context) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.triage_dialog_emergency);

        Button btnClose = findViewById(R.id.buttonCloseEmergency);
        btnClose.setOnClickListener(v -> dismiss());
    }

    public static void show(Context context) {
        TriageEmergencyDialog dialog = new TriageEmergencyDialog(context);
        dialog.setCancelable(false);
        dialog.show();
    }
}
