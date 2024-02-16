package org.opendroneid.android;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import com.google.android.material.textfield.TextInputLayout;

public final class UserFlowUtil {
    private UserFlowUtil() {
        // Private constructor to prevent instantiation
    }

    public static TextWatcher getEmailTextWatcher(TextInputLayout emailInputLayout) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString().trim();
                if (UserFlowUtil.isValidEmail(email)) {
                    emailInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static TextWatcher getPasswordTextWatcher(TextInputLayout passwordInputLayout) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString().trim();
                if (UserFlowUtil.isPasswordValid(password)) {
                    passwordInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    public static boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasSymbol = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSymbol = true;
            }
        }

        return hasUppercase && hasSymbol;
    }

    public static TextWatcher getFullNameTextWatcher(TextInputLayout fullNameInputLayout) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString().trim();
                if (UserFlowUtil.isValidFullName(password)) {
                    fullNameInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }
    public static boolean isValidFullName(String fullName) {
        return !TextUtils.isEmpty(fullName);
    }

    public static TextWatcher getConfirmPasswordTextWatcher(TextInputLayout confirmPasswordInputLayout, String password) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String confirmPassword = s.toString().trim();
                if (isConfirmPasswordValid(password, confirmPassword)) {
                    confirmPasswordInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    public static boolean isConfirmPasswordValid(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }


}