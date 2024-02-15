package org.opendroneid.android.app.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

import org.opendroneid.android.R;

import java.util.Objects;

public class UserRegisterDialogFragment extends DialogFragment {

    private TextInputEditText fullNameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private AppCompatTextView textSignIn;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_fragment_register_user, null);

        fullNameEditText = dialogView.findViewById(R.id.edit_text_full_name);
        emailEditText = dialogView.findViewById(R.id.edit_text_email);
        passwordEditText = dialogView.findViewById(R.id.edit_text_password);
        confirmPasswordEditText = dialogView.findViewById(R.id.edit_text_confirm_password);
        textSignIn = dialogView.findViewById(R.id.text_sign_in);

        builder.setView(dialogView)
                .setPositiveButton(getResources().getString(R.string.button_user_register), (dialog, id) -> {
                    String fullName = Objects.requireNonNull(fullNameEditText.getText()).toString();
                    String email = Objects.requireNonNull(emailEditText.getText()).toString();
                    String password = Objects.requireNonNull(passwordEditText.getText()).toString();
                    String confirmPassword = Objects.requireNonNull(confirmPasswordEditText.getText()).toString();

                    if (password.equals(confirmPassword)) {
                        // Passwords match, perform sign-up logic here
                        signUp(fullName, email, password);
                    } else {
                        // Passwords do not match, show error message
                        Toast.makeText(getContext(), R.string.error_password_missmatch, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.button_user_cancel, (dialog, id) -> {
                    // Handle Cancel button click
                });

        setTermsSpan(dialogView);

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

    private void signUp(String fullName, String email, String password) {
        // Perform sign-up logic here
        Toast.makeText(getContext(), "Sign up successful", Toast.LENGTH_SHORT).show();
    }

    private void setTermsSpan(View view) {
        AppCompatTextView termsTextView = view.findViewById(R.id.text_terms);
        SpannableString termsText = new SpannableString(termsTextView.getText());

        ClickableSpan termsClick = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // Handle click action for Terms of Use
                Toast.makeText(getContext(), "Terms of Use clicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(ContextCompat.getColor(requireActivity(), R.color.sky));
            }
        };

        ClickableSpan acceptableUsePolicyClick = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // Handle click action for Acceptable Use Policy
                Toast.makeText(getContext(), "Acceptable Use Policy clicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(ContextCompat.getColor(requireActivity(), R.color.sky));
            }
        };

        ClickableSpan privacyPolicyClick = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // Handle click action for Privacy Policy
                Toast.makeText(getContext(), "Privacy Policy clicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(ContextCompat.getColor(requireActivity(), R.color.sky));
            }
        };

        termsText.setSpan(termsClick, 29, 41, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        termsText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireActivity(), R.color.sky)), 29, 41, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        termsText.setSpan(acceptableUsePolicyClick, 43, 64, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        termsText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireActivity(), R.color.sky)), 43, 64, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        termsText.setSpan(privacyPolicyClick, 66, termsText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        termsText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireActivity(), R.color.sky)), 66, termsText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        termsTextView.setText(termsText);
        termsTextView.setMovementMethod(LinkMovementMethod.getInstance());
        termsTextView.setHighlightColor(Color.TRANSPARENT);

        CheckBox checkBox = view.findViewById(R.id.checkbox_terms);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Handle checkbox state change
            if (isChecked) {
                // Checkbox is checked
                // Perform action
            } else {
                // Checkbox is unchecked
                // Perform action
            }
        });
    }

}