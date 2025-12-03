package com.example.smartair.ui.historyBrowser;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;

public class HistoryBrowserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_browser);

        if (savedInstanceState == null) {
            String childUid = getIntent().getStringExtra("childUid");
            if (childUid == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
                childUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }

            FragmentParentHistoryBrowser fragment = FragmentParentHistoryBrowser.newInstance(childUid);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.historyRecyclerView, fragment); // Reusing the RecyclerView container id as fragment container if simple, but better to use a FrameLayout.

            transaction.commit();
        }
    }
}