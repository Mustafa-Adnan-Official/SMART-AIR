package com.example.smartair.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.services.AuthService;

/**
 * Parent home screen.
 */
public class ParentHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView tv = findViewById(R.id.text_home);
        // tv.setText("..."); // Already set in XML, or update here
    }

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
        }

        return super.onOptionsItemSelected(item);
    }
}
