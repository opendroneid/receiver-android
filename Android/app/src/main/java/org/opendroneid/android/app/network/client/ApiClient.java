package org.opendroneid.android.app.network.client;

import android.content.Context;
import android.util.Patterns;

import org.opendroneid.android.app.network.manager.UrlManager;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static String BASE_URL = "http://ec2-18-135-187-150.eu-west-2.compute.amazonaws.com/api/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        String savedUrl = UrlManager.getUrl(context);
        if (savedUrl != null) {
            if (isValidUrl(savedUrl)) {
                BASE_URL = savedUrl;
            }
        }
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }

    private static boolean isValidUrl(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }
}
