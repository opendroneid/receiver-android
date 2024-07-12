package org.opendroneid.android.app.network.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.opendroneid.android.app.network.models.user.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class LogedUserManager {

    private static final String TOKEN_PREFS = "token_prefs";
    private static final String TOKEN_KEY = "token_key";
    private static final String USER_KEY = "user_key";

    private final SharedPreferences sharedPreferences;

    public LogedUserManager(Context context) {
        sharedPreferences = context.getSharedPreferences(TOKEN_PREFS, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TOKEN_KEY, token);
        editor.apply();
    }

    public String getToken() {
        return sharedPreferences.getString(TOKEN_KEY, null);
    }

    public void deleteToken() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(TOKEN_KEY);
        editor.apply();
    }

    public void saveUser(User user) throws IOException {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_KEY, serializeUser(user));
        editor.apply();
    }

    public User getUser() throws IOException, ClassNotFoundException {
        String serializedUser = sharedPreferences.getString(USER_KEY, null);
        if (serializedUser != null) {
            return deserializeUser(serializedUser);
        }
        return null;
    }

    public void deleteUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(USER_KEY);
        editor.apply();
    }

    private String serializeUser(User user) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(user);
        objectOutputStream.close();
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

    private User deserializeUser(String serializedUser) throws IOException, ClassNotFoundException {
        byte[] bytes = Base64.decode(serializedUser, Base64.DEFAULT);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return (User) objectInputStream.readObject();
    }
}
