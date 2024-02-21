package org.opendroneid.android.app.network.models.sensor;

import com.google.gson.annotations.SerializedName;

public class PostSensorFailureResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("error")
    private String error;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }
}
