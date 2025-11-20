package com.example.smartair.ui;
// ChildrenAdapter.java
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChildrenAdapter extends RecyclerView.Adapter<ChildrenAdapter.ChildViewHolder> {

    private List<Child> childList;

    public ChildrenAdapter(List<Child> childList) {
        this.childList = childList;
    }

    // --- 1. ViewHolder: Holds references to the views in a single card ---
    public static class ChildViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView pbSettingTextView;
        public TextView controllerUsesTextView;
        public ImageButton deleteButton;
        public ImageButton pbEditButton;
        // Add more views as needed (e.g., the ImageButtons for editing)

        public ChildViewHolder(View itemView) {
            super(itemView);
            // Link views in the 'parent_settings_child.xml' file to Java variables
            nameTextView = itemView.findViewById(R.id.child_name_textview);
            pbSettingTextView = itemView.findViewById(R.id.pb_setting_textview);
            controllerUsesTextView = itemView.findViewById(R.id.controller_uses_textview);
            deleteButton = itemView.findViewById(R.id.delete_child_button);
            // Assuming you added IDs for the pencil ImageButtons:
            // pbEditButton = itemView.findViewById(R.id.pb_edit_button);
        }
    }

    // --- 2. Inflates the CardView layout for each item ---
    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // R.layout.parent_settings_child refers to your CardView XML file
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.parent_settings_child, parent, false);
        return new ChildViewHolder(itemView);
    }

    // --- 3. Binds data to the views (fills the card with child-specific info) ---
    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Child currentChild = childList.get(position);

        holder.nameTextView.setText(currentChild.getName());
        holder.pbSettingTextView.setText("PB Setting: " + currentChild.getPbSetting());
        holder.controllerUsesTextView.setText("Expected Daily Controller Uses: " + currentChild.getDailyControllerUses());

        // Example: Set up a click listener for the delete button
        holder.deleteButton.setOnClickListener(v -> {
            // Handle the removal logic here
            removeItem(position);
        });
    }

    // --- Utility Methods ---

    @Override
    public int getItemCount() {
        return childList.size();
    }

    // Call this when a new child is created
    public void addChild(Child newChild) {
        childList.add(newChild);
        notifyItemInserted(childList.size() - 1);
    }

    // Call this when a child is removed
    public void removeItem(int position) {
        childList.remove(position);
        notifyItemRemoved(position);
    }
}