package org.opendroneid.android.app.network.models.drone;

public class DroneDetectionResponse {
    private String status;
    private String message;
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
