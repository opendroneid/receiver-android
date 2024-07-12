package org.opendroneid.android.app.network.models.user;

public class UserForgotPasswordResponse {
    private boolean success;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
