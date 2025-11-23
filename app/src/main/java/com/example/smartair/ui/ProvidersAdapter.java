package com.example.smartair.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.models.Provider;

import java.util.List;
/**
 *
 * This class is to connect the Provider UI elements to the Provider database and any data
 */
public class ProvidersAdapter extends RecyclerView.Adapter<ProvidersAdapter.ProviderViewHolder> {

    // List of Providers to display
    private List<Provider> providersList;
    private Context context;

    /**
     * ViewHolder class to hold the child UI elements
     * @param childList
     */




   public ProvidersAdapter(Context context, List<Provider> providersList){
        this.providersList = providersList;
        this.context = context;
    }




    public static class ProviderViewHolder extends RecylcerView.ViewHolder {

        public TextView nameTextView;
        public ImageButton deleteButton;
        public Button sendReportButton;
        public LinearLayout childModulesContainer;

        public ProviderViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.provider_text_name);
            deleteButton = itemView.findViewById(R.id.delete_child_button);
            sendReportButton = itemView.findViewById(R.id.send_realtime_report_button);
            childModulesContainer = itemView.findViewById(R.id.child_modules_list_container);

        }
    }

    @NonNull
    @Override
    public ProviderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.parent_settings_child_card, parent, false); // Ensure this matches your file name
        return new ProviderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProviderViewHolder holder, int position) {
        Provider currentProvider = providersList.get(position);

        holder.nameTextView.setText(currentProvider.getName());
        holder.childModulesContainer.removeAllViews();

        if (currentProvider.getChildren() != null) {
            for (Provider.ChildAccessInfo childInfo : currentProvider.getChildren()) {
                View childView = LayoutInflater.from(context).inflate(R.layout.provider_settings_child_module, holder.childModulesContainer, false);

                Switch accessSwitch = childView.findViewById(R.id.provider_child_access_button);
                Button manageDataBtn = childView.findViewById(R.id.manage_data_button);

                accessSwitch.setText(childInfo.getChildren.childName);
                accessSwitch.setText(childInfo.hasAccess);

                accessSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    childInfo.hasAccess = isChecked;

                    Toast.makeText(context, "Child Access Changed", Toast.LENGTH_SHORT).show();)

                });

                manageDataBtn.setOnClickListener(v -> {
                    Toast.makeText(context, "Manage Data Button Clicked", Toast.LENGTH_SHORT).show();
                });

                holder.childModulesContainer.addView(childView);

            }
        }

        holder.deleteButton.setOnClickListener(v -> {
            showDeleteProviderDialog(v.getContext(), currentProvider, position);

        });

        holder.sendReportButton.setOnClickListener(v -> {
            showEditPBDialog(v.getContext(), currentChild, position);
        });


    }

    @Override
    public int getItemCount() {
        return childList.size();
    }










}


}