package org.opendroneid.android.app.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.opendroneid.android.R;
import org.opendroneid.android.UserFlowUtil;
import org.opendroneid.android.app.network.ApiClient;
import org.opendroneid.android.app.network.models.user.UserForgotPassword;
import org.opendroneid.android.app.network.models.user.UserForgotPasswordResponse;
import org.opendroneid.android.app.network.models.user.UserLoginSuccessResponse;
import org.opendroneid.android.app.network.models.user.UserManager;
import org.opendroneid.android.app.network.service.ApiService;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserForgotPasswordDialogFragment extends DialogFragment {

    private TextInputEditText emailEditText;
    private TextInputLayout emailInputLayout;
    private AppCompatTextView textSignIn;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_fragment_forgot_password, null);

        emailEditText = dialogView.findViewById(R.id.edit_text_email);
        emailInputLayout = dialogView.findViewById(R.id.layout_email);
        textSignIn = dialogView.findViewById(R.id.text_sign_in);

        builder.setView(dialogView)
                .setPositiveButton(getResources().getString(R.string.button_user_reset_password), null)
                .setNegativeButton(R.string.button_user_cancel, (dialog, id) -> {
                    dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button sendButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            sendButton.setOnClickListener(view -> sendResetEmail());
        });

        emailEditText.addTextChangedListener(UserFlowUtil.getEmailTextWatcher(emailInputLayout));

        textSignIn.setOnClickListener(view -> {
            dismiss();
            openSignInDialog();
        });

        return dialog;
    }

    private void openSignInDialog() {
        UserSignInDialogFragment dialog = new UserSignInDialogFragment();
        dialog.show(requireActivity().getSupportFragmentManager(), "UserSignInDialogFragment");
        dialog.setCancelable(false);
    }

    private void sendResetEmail() {
        String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
        if (!UserFlowUtil.isValidEmail(email)) {
            emailInputLayout.setError(getString(R.string.error_invalid_email_address));
        } else {
            emailInputLayout.setError(null);
            performForgotPassword(email);
        }
    }

    private void performForgotPassword(String email){
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        UserForgotPassword userForgotPassword = new UserForgotPassword(email);
        Call<UserForgotPasswordResponse> call = apiService.postUserForgotPassword(userForgotPassword);

        call.enqueue(new Callback<UserForgotPasswordResponse>() {
            @Override
            public void onResponse(Call<UserForgotPasswordResponse> call, Response<UserForgotPasswordResponse> response) {
                if (response.isSuccessful()) {
                    UserForgotPasswordResponse userForgotPasswordResponse = response.body();
                    if (userForgotPasswordResponse.isSuccess()) {
                        Toast.makeText(getContext(), getString(R.string.success_reset_email_sent), Toast.LENGTH_SHORT).show();
                        dismiss();
                    } else {
                        // unsuccessful login response
                        Toast.makeText(getContext(), getString(R.string.error_forgot_password), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // unsuccessful login response
                    Toast.makeText(getContext(), getString(R.string.error_forgot_password), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserForgotPasswordResponse> call, Throwable t) {
                // Fail
                Toast.makeText(getContext(), getString(R.string.error_forgot_password), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
