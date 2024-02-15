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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

import org.opendroneid.android.R;

import java.util.Objects;

public class UserSignInDialogFragment extends DialogFragment {

    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private AppCompatTextView textRegister;

    private AppCompatTextView textForgotPassword;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_fragment_sign_in_user, null);

        emailEditText = dialogView.findViewById(R.id.edit_text_email);
        passwordEditText = dialogView.findViewById(R.id.edit_text_password);
        textRegister = dialogView.findViewById(R.id.text_register);
        textForgotPassword = dialogView.findViewById(R.id.text_forgot_password);

        builder.setView(dialogView)
                .setPositiveButton(getResources().getString(R.string.button_user_sign_in), (dialog, id) -> {
                    String email = Objects.requireNonNull(emailEditText.getText()).toString();
                    String password = Objects.requireNonNull(passwordEditText.getText()).toString();
                    signIn(email, password);
                })
                .setNegativeButton(R.string.button_user_cancel, (dialog, id) -> {
                    // Handle Cancel button click
                });

        setTermsSpan(dialogView);

        textRegister.setOnClickListener(view -> {
            dismiss();
            openRegisterDialog();
        });

        textForgotPassword.setOnClickListener(view -> {
            dismiss();
            openForgotPasswordDialog();
        });

        return builder.create();
    }

    private void openRegisterDialog() {
        UserRegisterDialogFragment dialog = new UserRegisterDialogFragment();
        dialog.show(requireActivity().getSupportFragmentManager(), "UserRegisterDialogFragment");
        dialog.setCancelable(false);
    }

    private void openForgotPasswordDialog() {
        UserForgotPasswordDialogFragment dialog = new UserForgotPasswordDialogFragment();
        dialog.show(requireActivity().getSupportFragmentManager(), "UserForgotPasswordDialogFragment");
        dialog.setCancelable(false);
    }

    private void signIn(String email, String password) {
        // Perform sign-up logic here
        Toast.makeText(getContext(), "Sign in successful", Toast.LENGTH_SHORT).show();
    }

    private void setTermsSpan(View view) {
        AppCompatTextView termsTextView = view.findViewById(R.id.text_terms_sign_in);
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

        termsText.setSpan(termsClick, 46, 59, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        termsText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireActivity(), R.color.sky)), 46, 59, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        termsText.setSpan(acceptableUsePolicyClick, 60, 82, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        termsText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireActivity(), R.color.sky)), 60, 82, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        termsText.setSpan(privacyPolicyClick, 84, termsText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        termsText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireActivity(), R.color.sky)), 84, termsText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        termsTextView.setText(termsText);
        termsTextView.setMovementMethod(LinkMovementMethod.getInstance());
        termsTextView.setHighlightColor(Color.TRANSPARENT);
    }

}