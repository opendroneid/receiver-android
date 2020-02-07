/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.data;

import java.util.Locale;

public class LocationData extends MessageData {

    private StatusEnum status;
    private heightTypeEnum heightType;
    private double direction;
    private double speedHorizontal;
    private double speedVertical;
    private double latitude;
    private double longitude;
    private double altitudePressure;
    private double altitudeGeodetic;
    private double height;
    private HorizontalAccuracyEnum horizontalAccuracy;
    private VerticalAccuracyEnum verticalAccuracy;
    private VerticalAccuracyEnum baroAccuracy;
    private SpeedAccuracyEnum speedAccuracy;
    private double locationTimestamp;
    private double timeAccuracy;

    public LocationData() {
        super();
        status = StatusEnum.Undeclared;
        heightType = heightTypeEnum.Takeoff;
        direction = 361; // 361 is the Invalid value in the specification
        speedHorizontal = 255; // 255 is the Invalid value in the specification
        speedVertical = 63; // 63 is the Invalid value in the specification
        latitude = 0;
        longitude = 0;
        altitudePressure = -1000; // -1000 is the Invalid value in the specification
        altitudeGeodetic = -1000; // -1000 is the Invalid value in the specification
        height = -1000; // -1000 is the Invalid value in the specification
        horizontalAccuracy = HorizontalAccuracyEnum.Unknown;
        verticalAccuracy = VerticalAccuracyEnum.Unknown;
        baroAccuracy = VerticalAccuracyEnum.Unknown;
        speedAccuracy = SpeedAccuracyEnum.Unknown;
        locationTimestamp = 0;
        timeAccuracy = 0;
    }

    public enum StatusEnum {
        Undeclared,
        Ground,
        Airborne,
    }
    public StatusEnum getStatus() { return status; }
    public void setStatus(int status) {
        switch(status) {
            case 1: this.status = StatusEnum.Ground; break;
            case 2: this.status = StatusEnum.Airborne; break;
            default: this.status = StatusEnum.Undeclared; break;
        }
    }

    public enum heightTypeEnum {
        Takeoff,
        Ground,
    }
    public heightTypeEnum getHeightType() { return heightType; }
    public void setHeightType(int heightType) {
        if (heightType == 1)
            this.heightType = heightTypeEnum.Ground;
        else
            this.heightType = heightTypeEnum.Takeoff;
    }

    public double getDirection() { return direction; }
    public String getDirectionAsString() {
        if (direction != 361)
            return String.format(Locale.US,"%3.0f deg", direction);
        else
            return ("Unknown");
    }
    public void setDirection(double direction) {
        if (direction < 0 || direction > 360)
            direction = 361; // 361 is defined in the specification as the Invalid value
        this.direction = direction;
    }

    public double getSpeedHorizontal() { return speedHorizontal; }
    public String getSpeedHorizontalAsString() {
        if (speedHorizontal != 255)
            return String.format(Locale.US,"%3.2f m/s", speedHorizontal);
        else
            return ("Unknown");
    }
    public void setSpeedHorizontal(double speedHorizontal) {
        if (speedHorizontal < 0 || speedHorizontal > 254.25)
            speedHorizontal = 255; // 255 is defined in the specification as the Invalid value
        this.speedHorizontal = speedHorizontal;
    }

    public double getSpeedVertical() { return speedVertical; }
    public String getSpeedVerticalAsString() {
        if (speedVertical != 63)
            return String.format(Locale.US,"%3.2f m/s", speedVertical);
        else
            return ("Unknown");
    }
    public void setSpeedVertical(double speedVertical) {
        if (speedVertical < -62 || speedVertical > 62)
            speedVertical = 63; // 63 is defined in the specification as the Invalid value
        this.speedVertical = speedVertical;
    }

    public double getLatitude() { return latitude; }
    public String getLatitudeAsString() {
        if (latitude == 0 && longitude == 0)
            return "Unknown";
        return String.format(Locale.US,"%3.7f", latitude);
    }
    public void setLatitude(double latitude) {
        if (latitude < -90 || latitude > 90) {
            latitude = 0;
            this.longitude = 0; // both equal to zero is defined in the specification as the Invalid value
        }
        this.latitude = latitude;
    }

    public double getLongitude() { return longitude; }
    public String getLongitudeAsString() {
        if (latitude == 0 && longitude == 0)
            return "Unknown";
        return String.format(Locale.US,"%3.7f", longitude);
    }
    public void setLongitude(double longitude) {
        if (longitude < -180 || longitude > 180) {
            this.latitude = 0;
            longitude = 0; // both equal to zero is defined in the specification as the Invalid value
        }
        this.longitude = longitude;
    }

    private String getAltitudeAsString(double altitude) {
        if (altitude == -1000)
            return "Unknown";
        return String.format(Locale.US,"%3.1f m", altitude);
    }
    public double getAltitudePressure() { return altitudePressure; }
    public String getAltitudePressureAsString() { return getAltitudeAsString(altitudePressure); }
    public void setAltitudePressure(double altitudePressure) {
        if (altitudePressure < -1000 || altitudePressure > 31767)
            altitudePressure = -1000; // -1000 is defined in the specification as the Invalid value
        this.altitudePressure = altitudePressure;
    }
    public double getAltitudeGeodetic() { return altitudeGeodetic; }
    public String getAltitudeGeodeticAsString() { return getAltitudeAsString(altitudeGeodetic); }
    public void setAltitudeGeodetic(double altitudeGeodetic) {
        if (altitudeGeodetic < -1000 || altitudeGeodetic > 31767)
            altitudeGeodetic = -1000; // -1000 is defined in the specification as the Invalid value
        this.altitudeGeodetic = altitudeGeodetic;
    }
    public double getHeight() { return height; }
    public String getHeightAsString() { return getAltitudeAsString(height); }
    public void setHeight(double height) {
        if (height < -1000 || height > 31767)
            height = -1000; // -1000 is defined in the specification as the Invalid value
        this.height = height;
    }

    public enum HorizontalAccuracyEnum {
        Unknown,
        kilometers_18_52,
        kilometers_7_408,
        kilometers_3_704,
        kilometers_1_852,
        meters_926,
        meters_555_6,
        meters_185_2,
        meters_92_6,
        meters_30,
        meters_10,
        meters_3,
        meters_1,
    }
    public HorizontalAccuracyEnum getHorizontalAccuracy() { return horizontalAccuracy; }
    public String getHorizontalAccuracyAsString() {
        switch(horizontalAccuracy) {
            case kilometers_18_52: return "< 18.52 km";
            case kilometers_7_408: return "< 7.408 km";
            case kilometers_3_704: return "< 3.704 km";
            case kilometers_1_852: return "< 1.852 km";
            case meters_926: return "< 926 m";
            case meters_555_6: return "< 555.6 m";
            case meters_185_2: return "< 185.2 m";
            case meters_92_6: return "< 92.6 m";
            case meters_30: return "< 30 m";
            case meters_10: return "< 10 m";
            case meters_3: return "< 3 m";
            case meters_1: return "< 1 m";
            default: return "Unknown";
        }
    }
    public void setHorizontalAccuracy(int horizontalAccuracy) {
        switch(horizontalAccuracy) {
            case 1: this.horizontalAccuracy = HorizontalAccuracyEnum.kilometers_18_52; break;
            case 2: this.horizontalAccuracy = HorizontalAccuracyEnum.kilometers_7_408; break;
            case 3: this.horizontalAccuracy = HorizontalAccuracyEnum.kilometers_3_704; break;
            case 4: this.horizontalAccuracy = HorizontalAccuracyEnum.kilometers_1_852; break;
            case 5: this.horizontalAccuracy = HorizontalAccuracyEnum.meters_926; break;
            case 6: this.horizontalAccuracy = HorizontalAccuracyEnum.meters_555_6; break;
            case 7: this.horizontalAccuracy = HorizontalAccuracyEnum.meters_185_2; break;
            case 8: this.horizontalAccuracy = HorizontalAccuracyEnum.meters_92_6; break;
            case 9: this.horizontalAccuracy = HorizontalAccuracyEnum.meters_30; break;
            case 10: this.horizontalAccuracy = HorizontalAccuracyEnum.meters_10; break;
            case 11: this.horizontalAccuracy = HorizontalAccuracyEnum.meters_3; break;
            case 12: this.horizontalAccuracy = HorizontalAccuracyEnum.meters_1; break;
            default: this.horizontalAccuracy = HorizontalAccuracyEnum.Unknown; break;
        }
    }

    public enum VerticalAccuracyEnum {
        Unknown,
        meters_150,
        meters_45,
        meters_25,
        meters_10,
        meters_3,
        meters_1,
    }
    public VerticalAccuracyEnum getVerticalAccuracy() { return verticalAccuracy; }
    public String getVerticalAccuracyAsString(VerticalAccuracyEnum accuracy) {
        switch(accuracy) {
            case meters_150: return "< 150 m";
            case meters_45: return "< 45 m";
            case meters_25: return "< 25 m";
            case meters_10: return "< 10 m";
            case meters_3: return "< 3 m";
            case meters_1: return "< 1 m";
            default: return "Unknown";
        }
    }
    private VerticalAccuracyEnum intToVerticalAccuracy(int verticalAccuracy) {
        switch(verticalAccuracy) {
            case 1: return VerticalAccuracyEnum.meters_150;
            case 2: return VerticalAccuracyEnum.meters_45;
            case 3: return VerticalAccuracyEnum.meters_25;
            case 4: return VerticalAccuracyEnum.meters_10;
            case 5: return VerticalAccuracyEnum.meters_3;
            case 6: return VerticalAccuracyEnum.meters_1;
            default: return VerticalAccuracyEnum.Unknown;
        }
    }
    public void setVerticalAccuracy(int verticalAccuracy) {
        this.verticalAccuracy = intToVerticalAccuracy(verticalAccuracy);
    }
    public VerticalAccuracyEnum getBaroAccuracy() { return baroAccuracy; }
    public void setBaroAccuracy(int verticalAccuracy) {
        this.baroAccuracy = intToVerticalAccuracy(verticalAccuracy);
    }

    public enum SpeedAccuracyEnum {
        Unknown,
        meter_per_second_10,
        meter_per_second_3,
        meter_per_second_1,
        meter_per_second_0_3,
    }
    public SpeedAccuracyEnum getSpeedAccuracy() { return speedAccuracy; }
    public String getSpeedAccuracyAsString() {
        switch(speedAccuracy) {
            case meter_per_second_10: return "< 10 m/s";
            case meter_per_second_3: return "< 3 m/s";
            case meter_per_second_1: return "< 1 m/s";
            case meter_per_second_0_3: return "< 0.3 m/s";
            default: return "Unknown";
        }
    }
    public void setSpeedAccuracy(int speedAccuracy) {
        switch(speedAccuracy) {
            case 1: this.speedAccuracy = SpeedAccuracyEnum.meter_per_second_10; break;
            case 2: this.speedAccuracy = SpeedAccuracyEnum.meter_per_second_3; break;
            case 3: this.speedAccuracy = SpeedAccuracyEnum.meter_per_second_1; break;
            case 4: this.speedAccuracy = SpeedAccuracyEnum.meter_per_second_0_3; break;
            default: this.speedAccuracy = SpeedAccuracyEnum.Unknown; break;
        }
    }

    public double getLocationTimestamp() { return locationTimestamp; }
    private double getTimeStampMinutes() { return ((int) (locationTimestamp / 10)) / 60; }
    private double getTimeStampSeconds() { return (locationTimestamp/10) % 60; }
    public String getLocationTimestampAsString() {
        return String.format(Locale.US,"%02.0f:%02.0f", getTimeStampMinutes(), getTimeStampSeconds());
    }
    public void setLocationTimestamp(double locationTimestamp) {
        if (locationTimestamp < 0)
            locationTimestamp = 0;
        if (locationTimestamp > 3600)
            locationTimestamp = 3600; // Max one hour is allowed
        this.locationTimestamp = locationTimestamp;
    }

    public double getTimeAccuracy() { return timeAccuracy; }
    public String getTimeAccuracyAsString() {
        return String.format(Locale.US,"< %1.1f s", timeAccuracy);
    }
    public void setTimeAccuracy(double timeAccuracy) {
        if (timeAccuracy < 0)
            timeAccuracy = 0;
        if (timeAccuracy > 1.5)
            timeAccuracy = 1.5; // 1.5s is the maximum value in the specification
        this.timeAccuracy = timeAccuracy;
    }
}
