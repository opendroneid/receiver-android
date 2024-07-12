package org.opendroneid.android.app.network.models.drone;

import com.google.gson.annotations.SerializedName;

public class DroneDetectionResponse {
    @SerializedName("Status")
    private String status;
    @SerializedName("Message")
    private String message;
    @SerializedName("DetectionData")
    private DetectionData detectionData;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DetectionData getDetectionData() {
        return detectionData;
    }

    public void setDetectionData(DetectionData detectionData) {
        this.detectionData = detectionData;
    }

}
