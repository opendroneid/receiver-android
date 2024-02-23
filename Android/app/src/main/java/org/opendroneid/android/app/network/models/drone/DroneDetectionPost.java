package org.opendroneid.android.app.network.models.drone;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DroneDetectionPost {

    private long time;
    @SerializedName("sensor-id")
    private String sensorId;
    private Position position;
    private List<Metadata> metadata;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public List<Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<Metadata> metadata) {
        this.metadata = metadata;
    }

    public static class Position {
        private double latitude;
        private double longitude;
        private double altitude;
        private double accuracy;
        @SerializedName("speed-horizontal")
        private double speedHorizontal;
        private double bearing;

        public Position(double v, double v1, double v2, double v3, double v4, double v5) {
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getAltitude() {
            return altitude;
        }

        public void setAltitude(double altitude) {
            this.altitude = altitude;
        }

        public double getAccuracy() {
            return accuracy;
        }

        public void setAccuracy(double accuracy) {
            this.accuracy = accuracy;
        }

        public double getSpeedHorizontal() {
            return speedHorizontal;
        }

        public void setSpeedHorizontal(double speedHorizontal) {
            this.speedHorizontal = speedHorizontal;
        }

        public double getBearing() {
            return bearing;
        }

        public void setBearing(double bearing) {
            this.bearing = bearing;
        }
    }

    public static class Metadata {
        private String key;
        private String val;
        private String type;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getVal() {
            return val;
        }

        public void setVal(String val) {
            this.val = val;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
