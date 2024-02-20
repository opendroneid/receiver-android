package org.opendroneid.android.app.network.models.user;

import com.google.gson.annotations.SerializedName;

public class PostSensorSuccessResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("source")
    private Source source;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public Source getSource() {
        return source;
    }

    public String getMessage() {
        return message;
    }

    public static class Source {
        @SerializedName("service")
        private String service;

        @SerializedName("url")
        private String url;

        @SerializedName("ref")
        private String ref;

        @SerializedName("public")
        private int isPublic;

        @SerializedName("static")
        private int isStatic;

        @SerializedName("icon")
        private String icon;

        @SerializedName("type")
        private String type;

        @SerializedName("ip")
        private String ip;

        @SerializedName("master_name")
        private String masterName;

        @SerializedName("updated_at")
        private String updatedAt;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("id")
        private int id;

        @SerializedName("master_id")
        private int masterId;

        // Getters for all fields
    }
}
