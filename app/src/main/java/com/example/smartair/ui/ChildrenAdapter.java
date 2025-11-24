package com.example.smartair.ui;



import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartair.R; // R is your generated resource file
import com.example.smartair.models.Child; // Assuming Child is in your models package
import com.example.smartair.models.Parent;
import com.example.smartair.repositories.ChildRepository;
import com.example.smartair.repositories.ParentRepository;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;


/**
 *
 * This class is to connect the child UI elements to the child database and any data
 */
public class ChildrenAdapter extends RecyclerView.Adapter<ChildrenAdapter.ChildViewHolder> {


    private ChildRepository childRepository;
    private ParentRepository parentRepository;
    private String childUID;
    private String parentUID;
    // List of children to display
    private List<Child> childList;

    /**
     * ViewHolder class to hold the child UI elements in parent settings
     * @param childList
     */
    public ChildrenAdapter(List<Child> childList){
        this.childList = childList;
        childRepository = new ChildRepository();
        parentRepository = new ParentRepository();


    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.parent_settings_child_card, parent, false);
        return new ChildViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Child currentChild = childList.get(position);
        childUID = currentChild.getChildUid();
        parentUID = currentChild.getParentUid();






        holder.nameTextView.setText(currentChild.getName());
        holder.pbSettingsTextView.setText("PB Setting: " + currentChild.getPersonalBest());
        holder.controllerSettingsTextView.setText("Expected Daily Controller Uses: " + currentChild.getControllerUses());

        holder.deleteButton.setOnClickListener(v -> {
            showDeleteChildDialog(v.getContext(), currentChild, position);

        });

        holder.pbSettingsButton.setOnClickListener(v -> {
            showEditPBDialog(v.getContext(), currentChild);
        });

        holder.controllerButton.setOnClickListener(v -> {
            showEditAdherenceDialog(v.getContext(), currentChild, position);
        });

    }

    @Override
    public int getItemCount() {
        return childList.size();
    }

    public static class ChildViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public ImageButton deleteButton;

        public TextView pbSettingsTextView;
        public ImageButton pbSettingsButton;

        public TextView controllerSettingsTextView;
        public ImageButton controllerButton;

        public ChildViewHolder(View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.child_name_textview);
            deleteButton = itemView.findViewById(R.id.delete_child_button);

            pbSettingsTextView = itemView.findViewById(R.id.pb_setting_textview);
            pbSettingsButton = itemView.findViewById(R.id.pb_setting_button);

            controllerSettingsTextView = itemView.findViewById(R.id.controller_uses_textview);
            controllerButton = itemView.findViewById(R.id.controller_uses_button);


        }

    }
        public void addChild(Child newChild){
            childList.add(newChild);
            notifyItemInserted(childList.size() -1);
        }

        public void removeChild(int position) {
            childList.remove(position);
            notifyItemRemoved(position);
            parentRepository.removeChild(parentUID, childUID);


        }


        private void showDeleteChildDialog(Context context, Child currentChild, int position){
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.dialog_delete_child, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(dialogView);
            final AlertDialog dialog = builder.create();

            Button yesButton = dialogView.findViewById(R.id.dialog_yesdeletechild_button);
            Button noButton = dialogView.findViewById(R.id.dialog_nodeletedhild_button);

            yesButton.setOnClickListener(v -> {

                removeChild(position);
                dialog.dismiss();
            });

            noButton.setOnClickListener(v -> dialog.dismiss());

            dialog.show();


        }
        private void showEditPBDialog(Context context, Child currentChild) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.dialog_new_pb, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(dialogView);

            final AlertDialog dialog = builder.create();

            TextInputEditText pbEditText = dialogView.findViewById(R.id.pb_edit_text);
            Button confirmButton = dialogView.findViewById(R.id.dialog_yesconfirmpb_button);
            Button cancelButton = dialogView.findViewById(R.id.dialog_noconfirmpb_button);


            pbEditText.setText(String.valueOf(currentChild.getPersonalBest()));

            confirmButton.setOnClickListener(v -> {
                String newValueString = pbEditText.getText().toString();

                if (!newValueString.isEmpty()){
                    try {
                        int newValue = Integer.parseInt(newValueString);

                        currentChild.setPersonalBest(newValue);
                        childRepository.adjustPB(childUID, newValue);

                        notifyItemChanged(childList.indexOf(currentChild));
                        dialog.dismiss();

                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Invalid input. Please enter a valid number.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            cancelButton.setOnClickListener(v -> dialog.dismiss());

            dialog.show();


        }

        private void showEditAdherenceDialog(Context context, Child currentChild, int position) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.dialog_new_adherence, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(dialogView);

            final AlertDialog dialog = builder.create();

            TextInputEditText adhEditText = dialogView.findViewById(R.id.adh_edit_text);
            Button confirmButton = dialogView.findViewById(R.id.dialog_yesconfirmadh_button);
            Button cancelButton = dialogView.findViewById(R.id.dialog_noconfirmadh_button);


            adhEditText.setText(String.valueOf(currentChild.getControllerUses()));

            confirmButton.setOnClickListener(v -> {
                String newValueString = adhEditText.getText().toString();

                if (!newValueString.isEmpty()){
                    try {
                        int newValue = Integer.parseInt(newValueString);

                        currentChild.setDailyControllerUses(newValue);
                        childRepository.adjustControllerUses(childUID, newValue);


                        notifyItemChanged(childList.indexOf(currentChild));
                        dialog.dismiss();

                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Invalid input. Please enter a valid number.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            cancelButton.setOnClickListener(v -> dialog.dismiss());

            dialog.show();


        }






    }


