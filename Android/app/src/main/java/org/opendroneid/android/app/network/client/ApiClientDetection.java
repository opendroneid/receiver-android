package org.opendroneid.android.app.network.client;

import android.content.Context;
import android.util.Patterns;

import org.opendroneid.android.app.network.manager.UrlManager;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClientDetection {
    private static String BASE_URL_DETECTION = "http://ec2-52-56-37-226.eu-west-2.compute.amazonaws.com:3378/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        String savedUrl = UrlManager.getDetectionUrl(context);
        if (savedUrl != null) {
            if (isValidUrl(savedUrl)) {
                BASE_URL_DETECTION = savedUrl;
            }
        }
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(BASE_URL_DETECTION).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }

    private static boolean isValidUrl(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }
}
