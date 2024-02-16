package org.opendroneid.android.app.network.models.user;

public class UserLogin {
    private String email;
    private String password;
    private String grant_type;

    public UserLogin(String email, String password) {
        this.email = email;
        this.password = password;
        this.grant_type = "password";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGrant_type() {
        return grant_type;
    }

    public void setGrant_type(String grant_type) {
        this.grant_type = grant_type;
    }
}