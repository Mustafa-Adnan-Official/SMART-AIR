package com.example.smartair.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Placeholder parent home screen for R1.
 */
public class ParentHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(android.R.layout.simple_list_item_1);

        TextView tv = findViewById(android.R.id.text1);
        tv.setText("Parent Home (TODO – replace with real layout)");
    }
}