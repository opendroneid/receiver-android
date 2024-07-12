package org.opendroneid.android.app.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.opendroneid.android.R;
import org.opendroneid.android.app.network.models.user.User;

public class UserDialogFragment extends DialogFragment {

    private User user;
    public UserDialogFragment(User user) {
        this.user = user;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_fragment_user, null);

        builder.setView(dialogView)
                .setPositiveButton(getResources().getString(R.string.button_close), null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button sendButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            sendButton.setTextColor(getResources().getColor(R.color.paleSky));
            sendButton.setOnClickListener(view -> dismiss());
        });

        TextView textName = dialogView.findViewById(R.id.text_name);
        textName.setText(String.format("%s %s", textName.getText(), user.getName()));

        TextView textEmail = dialogView.findViewById(R.id.text_email);
        textEmail.setText(String.format("%s %s", textEmail.getText(), user.getEmail()));

        TextView textDroneRange = dialogView.findViewById(R.id.text_drone_range);
        textDroneRange.setText(String.format("%s %s", textDroneRange.getText(), user.getDrone_range()));

        TextView textUnits = dialogView.findViewById(R.id.text_units);
        textUnits.setText(String.format("%s %s", textUnits.getText(), user.getUnits()));

        TextView textMeasurements = dialogView.findViewById(R.id.text_measurements);
        textMeasurements.setText(String.format("%s %s", textMeasurements.getText(), user.getMeasurements()));

        return dialog;
    }

}