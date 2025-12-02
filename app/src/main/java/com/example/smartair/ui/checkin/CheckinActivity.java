package com.example.smartair.ui.checkin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.callbacks.ResultCallback;
import com.example.smartair.models.users.Child;
import com.example.smartair.repositories.ChildRepository;
import com.example.smartair.ui.ChildrenAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class CheckinActivity extends AppCompatActivity {

    private RecyclerView childrenRecyclerView;
    private ChildrenAdapter childrenAdapter;

    private List<Child> childList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_activity_children_for_parent);

        childrenRecyclerView = findViewById(R.id.children_recycler_view);

        setupChildrenRecyclerView();
        loadChildrenData();
    }

    private void loadChildrenData() {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String parentUid = currentUser.getUid();
            ChildRepository childRepository = new ChildRepository();
            childRepository.getChildrenForParent(parentUid, new ResultCallback<List<Child>>() {
                @Override
                public void onSuccess(List<Child> result) {
                    childList.clear();
                    childList.addAll(result);
                    if (childrenAdapter != null) {
                        childrenAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(CheckinActivity.this, "Error loading children", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }

    }

    private void setupChildrenRecyclerView() {
        childrenAdapter = new ChildrenAdapter(childList);
        childrenRecyclerView.setAdapter(childrenAdapter);
    }
}
