package org.opendroneid.android.app.network.models.user;

public class UserLoginSuccessResponse {
    private boolean success;
    private String token;
    private User user;
    private boolean ga;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isGa() {
        return ga;
    }

    public void setGa(boolean ga) {
        this.ga = ga;
    }
}