package org.opendroneid.android.app.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.fingerprintjs.android.fingerprint.Fingerprinter;
import com.fingerprintjs.android.fingerprint.FingerprinterFactory;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.opendroneid.android.R;
import org.opendroneid.android.SensorUtil;
import org.opendroneid.android.UserFlowUtil;
import org.opendroneid.android.app.DebugActivity;
import org.opendroneid.android.app.network.client.ApiClient;
import org.opendroneid.android.app.network.models.user.UserLogin;
import org.opendroneid.android.app.network.models.user.UserLoginSuccessResponse;
import org.opendroneid.android.app.network.manager.LogedUserManager;
import org.opendroneid.android.app.network.models.sensor.SensorsPostRequest;
import org.opendroneid.android.app.network.service.ApiService;

import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserSignInDialogFragment extends DialogFragment {

    private TextInputEditText emailEditText;
    private TextInputLayout emailInputLayout;
    private TextInputEditText passwordEditText;
    private TextInputLayout passwordInputLayout;
    private AppCompatTextView textRegister;
    private AppCompatTextView textForgotPassword;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_fragment_sign_in_user, null);

        emailEditText = dialogView.findViewById(R.id.edit_text_email);
        emailInputLayout = dialogView.findViewById(R.id.layout_email);
        passwordEditText = dialogView.findViewById(R.id.edit_text_password);
        passwordInputLayout = dialogView.findViewById(R.id.layout_password);
        textRegister = dialogView.findViewById(R.id.text_register);
        textForgotPassword = dialogView.findViewById(R.id.text_forgot_password);

        builder.setView(dialogView)
                .setPositiveButton(getResources().getString(R.string.button_user_sign_in), null)
                .setNegativeButton(R.string.button_user_cancel, (dialog, id) -> {
                    dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button sendButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            sendButton.setOnClickListener(view -> signIn());
        });

        emailEditText.addTextChangedListener(UserFlowUtil.getEmailTextWatcher(emailInputLayout));
        passwordEditText.addTextChangedListener(UserFlowUtil.getPasswordTextWatcher(passwordInputLayout));

        textRegister.setOnClickListener(view -> {
            dismiss();
            openRegisterDialog();
        });

        textForgotPassword.setOnClickListener(view -> {
            dismiss();
            openForgotPasswordDialog();
        });

        setTermsSpan(dialogView);

        return dialog;
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

    private void signIn() {
        String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordEditText.getText()).toString().trim();

        boolean isValidEmail = UserFlowUtil.isValidEmail(email);
        boolean isPasswordValid = UserFlowUtil.isPasswordValid(password);

        if (!isValidEmail) {
            emailInputLayout.setError(getString(R.string.error_invalid_email_address));
            return;
        } else {
            emailInputLayout.setError(null);
        }

        if (!isPasswordValid) {
            passwordInputLayout.setError(getString(R.string.error_invalid_password));
            return;
        } else {
            passwordInputLayout.setError(null);
        }

        if (!isValidEmail || !isPasswordValid) {
            return;
        }

        performSignIn(email, password);
    }

    private void performSignIn(String email, String password) {
        UserLogin userLogin = new UserLogin(email, password);
        ApiService apiService = ApiClient.getClient(requireContext()).create(ApiService.class);

        Call<UserLoginSuccessResponse> call = apiService.postUserLogin(userLogin);
        call.enqueue(new Callback<UserLoginSuccessResponse>() {
            @Override
            public void onResponse(Call<UserLoginSuccessResponse> call, Response<UserLoginSuccessResponse> response) {
                if (response.isSuccessful()) {
                    UserLoginSuccessResponse loginResponse = response.body();
                    if (loginResponse != null) {
                        // Save the token and user
                        try {
                            LogedUserManager logedUserManager = new LogedUserManager(requireContext());
                            logedUserManager.saveToken(loginResponse.getToken());
                            logedUserManager.saveUser(loginResponse.getUser());

                            DebugActivity activity = (DebugActivity) getActivity();
                            if (activity != null) {
                                activity.initialize();
                            }

                            performSilentDeviceRegistration(loginResponse.getToken());
                            dismiss();
                            Toast.makeText(getContext(), getString(R.string.success_sign_in), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), getString(R.string.error_sign_in), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    } else {
                        // unsuccessful login response
                        Toast.makeText(getContext(), getString(R.string.error_sign_in), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // unsuccessful login response
                    Toast.makeText(getContext(), getString(R.string.error_sign_in), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserLoginSuccessResponse> call, Throwable t) {
                // Fail
                Toast.makeText(getContext(), getString(R.string.error_sign_in), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSilentDeviceRegistration(String token) {
        String ipV4 = SensorUtil.getDeviceIpAddress();
        Fingerprinter fingerprinter = FingerprinterFactory.create(requireContext());
        fingerprinter.getDeviceId(Fingerprinter.Version.V_5, deviceIdResult -> {

            String deviceId = deviceIdResult.getDeviceId();
            ApiService apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
            String id = "1796" + deviceId;
            String phoneSensor = deviceId;
            String ref = deviceId;

            Call<ResponseBody> call = apiService.postSensor("Bearer " + token, new SensorsPostRequest(phoneSensor + "-" + id, "#", ref,
                    0, 0, "phone", "PHONE", 1, ipV4, phoneSensor));
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        // Handle success response if needed
                    } else {
                        // Handle failure response if needed
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // Handle failure
                }
            });
            return null;
        });
    }

    private void setTermsSpan(View view) {
        AppCompatTextView termsTextView = view.findViewById(R.id.text_terms_sign_in);
        SpannableString termsText = new SpannableString(termsTextView.getText());

        ClickableSpan termsClick = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                String url = getString(R.string.terms_term_of_use);
                openUrlInBrowser(url);
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
                String url = getString(R.string.terms_acceptable_policy);
                openUrlInBrowser(url);
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
                String url = getString(R.string.terms_privacy_policy);
                openUrlInBrowser(url);
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

    private void openUrlInBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

}