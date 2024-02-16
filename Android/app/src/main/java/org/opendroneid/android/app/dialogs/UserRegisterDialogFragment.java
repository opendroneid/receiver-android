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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.opendroneid.android.R;
import org.opendroneid.android.UserFlowUtil;

import java.util.Objects;

public class UserRegisterDialogFragment extends DialogFragment {

    private TextInputEditText fullNameEditText;
    private TextInputLayout fullNameInputLayout;
    private TextInputEditText emailEditText;
    private TextInputLayout emailInputLayout;
    private TextInputEditText passwordEditText;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText confirmPasswordEditText;
    private TextInputLayout confirmPasswordInputLayout;

    private AppCompatCheckBox checkboxTerms;
    private AppCompatTextView textSignIn;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_fragment_register_user, null);

        fullNameEditText = dialogView.findViewById(R.id.edit_text_full_name);
        fullNameInputLayout = dialogView.findViewById(R.id.layout_full_name);
        emailEditText = dialogView.findViewById(R.id.edit_text_email);
        emailInputLayout = dialogView.findViewById(R.id.layout_email);
        passwordEditText = dialogView.findViewById(R.id.edit_text_password);
        passwordInputLayout = dialogView.findViewById(R.id.layout_password);
        confirmPasswordEditText = dialogView.findViewById(R.id.edit_text_confirm_password);
        confirmPasswordInputLayout = dialogView.findViewById(R.id.layout_confirm_password);
        textSignIn = dialogView.findViewById(R.id.text_sign_in);
        checkboxTerms = dialogView.findViewById(R.id.checkbox_terms);

        builder.setView(dialogView)
                .setPositiveButton(getResources().getString(R.string.button_user_register), null)
                .setNegativeButton(R.string.button_user_cancel, (dialog, id) -> {
                    dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button sendButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            sendButton.setOnClickListener(view -> {
                signUp();
            });
        });

        fullNameEditText.addTextChangedListener(UserFlowUtil.getFullNameTextWatcher(fullNameInputLayout));
        emailEditText.addTextChangedListener(UserFlowUtil.getEmailTextWatcher(emailInputLayout));
        passwordEditText.addTextChangedListener(UserFlowUtil.getPasswordTextWatcher(passwordInputLayout));
        confirmPasswordEditText.addTextChangedListener(UserFlowUtil.getConfirmPasswordTextWatcher(confirmPasswordInputLayout, Objects.requireNonNull(passwordEditText.getText()).toString().trim()));

        textSignIn.setOnClickListener(view -> {
            dialog.dismiss();
            openSignInDialog();
        });

        setTermsSpan(dialogView);

        return dialog;
    }

    private void openSignInDialog() {
        UserSignInDialogFragment dialog = new UserSignInDialogFragment();
        dialog.show(requireActivity().getSupportFragmentManager(), "UserSignInDialogFragment");
        dialog.setCancelable(false);
    }

    private void signUp() {
        String fullName = Objects.requireNonNull(fullNameEditText.getText()).toString().trim();
        String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordEditText.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(confirmPasswordEditText.getText()).toString().trim();

        boolean isValidFullName = UserFlowUtil.isValidFullName(fullName);
        boolean isValidEmail = UserFlowUtil.isValidEmail(email);
        boolean isPasswordValid = UserFlowUtil.isPasswordValid(password);
        boolean isConfirmPasswordValid = UserFlowUtil.isConfirmPasswordValid(password, confirmPassword);
        boolean isTermsAccepted = checkboxTerms.isChecked();

        if (!isValidFullName) {
            fullNameInputLayout.setError(getString(R.string.error_invalid_full_name));
        } else {
            fullNameInputLayout.setError(null);
        }

        if (!isValidEmail) {
            emailInputLayout.setError(getString(R.string.error_invalid_email_address));
        } else {
            emailInputLayout.setError(null);
        }

        if (!isPasswordValid) {
            passwordInputLayout.setError(getString(R.string.error_invalid_password));
        } else {
            passwordInputLayout.setError(null);
        }

        if (!isConfirmPasswordValid) {
            confirmPasswordInputLayout.setError(getString(R.string.error_password_missmatch));
        } else {
            confirmPasswordInputLayout.setError(null);
        }

        if (!isTermsAccepted) {
            // Terms not accepted, show toast message
            Toast.makeText(getContext(), "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidFullName || !isValidEmail || !isPasswordValid || !isConfirmPasswordValid) {
            return;
        }

        // Perform sign-up logic here
        dismiss();
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