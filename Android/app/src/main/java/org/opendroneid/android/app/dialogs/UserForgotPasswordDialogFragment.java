package org.opendroneid.android.app.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

import org.opendroneid.android.R;

import java.util.Objects;

public class UserForgotPasswordDialogFragment extends DialogFragment {

    private TextInputEditText emailEditText;
    private AppCompatTextView textSignIn;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_fragment_forgot_password, null);

        emailEditText = dialogView.findViewById(R.id.edit_text_email);
        textSignIn = dialogView.findViewById(R.id.text_sign_in);

        builder.setView(dialogView)
                .setPositiveButton(getResources().getString(R.string.button_user_reset_password), (dialog, id) -> {
                    String email = Objects.requireNonNull(emailEditText.getText()).toString();
                    sendResetEmail(email);
                })
                .setNegativeButton(R.string.button_user_cancel, (dialog, id) -> {
                    // Handle Cancel button click
                });

        textSignIn.setOnClickListener(view -> {
            dismiss();
            openSignInDialog();
        });

        return builder.create();
    }

    private void openSignInDialog() {
        UserSignInDialogFragment dialog = new UserSignInDialogFragment();
        dialog.show(requireActivity().getSupportFragmentManager(), "UserSignInDialogFragment");
        dialog.setCancelable(false);
    }

    private void sendResetEmail(String email) {
        // Perform sign-up logic here
        Toast.makeText(getContext(), "Rest email sent successful", Toast.LENGTH_SHORT).show();
    }

}