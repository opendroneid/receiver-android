package org.opendroneid.android.app.network.models.user;

public class UserRegistration {
    private String name;
    private String email;
    private String password;
    private String password_confirm;
    private int tandc;

    public UserRegistration(String name, String email, String password, String password_confirm, int tandc) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.password_confirm = password_confirm;
        this.tandc = tandc;
    }
}