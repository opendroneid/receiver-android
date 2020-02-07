/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.data;

import java.util.Locale;

public class SystemData extends MessageData {

    private systemFlagsEnum flags;
    private double operatorLatitude;
    private double operatorLongitude;
    private int areaCount;
    private int areaRadius;
    private double areaCeiling;
    private double areaFloor;

    public SystemData() {
        super();
        flags = systemFlagsEnum.TakeOff;
        operatorLatitude = 0;
        operatorLongitude = 0;
        areaCount = 0;
        areaRadius = 0;
        areaCeiling = -1000; // -1000 is the Invalid value in the specification
        areaFloor = -1000; // -1000 is the Invalid value in the specification
    }

    public enum systemFlagsEnum {
        TakeOff,
        LiveGNSS,
    }

    public systemFlagsEnum getFlags() { return flags; }
    public void setFlags(int flags) {
        if (flags == 1)
            this.flags = systemFlagsEnum.LiveGNSS;
        else
            this.flags = systemFlagsEnum.TakeOff;
    }

    public void setOperatorLatitude(double operatorLatitude) {
        if (operatorLatitude < -90 || operatorLatitude > 90) {
            operatorLatitude = 0;
            this.operatorLongitude = 0; // both equal to zero is defined in the specification as the Invalid value
        }
        this.operatorLatitude = operatorLatitude;
    }
    public double getOperatorLatitude() { return operatorLatitude; }
    public String getOperatorLatitudeAsString() {
        if (operatorLatitude == 0 && operatorLongitude == 0)
            return "Unknown";
        return String.format(Locale.US,"%3.7f", operatorLatitude);
    }

    public void setOperatorLongitude(double operatorLongitude) {
        if (operatorLongitude < -180 || operatorLongitude > 180) {
            this.operatorLatitude = 0;
            operatorLongitude = 0; // both equal to zero is defined in the specification as the Invalid value
        }
        this.operatorLongitude = operatorLongitude;
    }
    public double getOperatorLongitude() { return operatorLongitude; }
    public String getOperatorLongitudeAsString() {
        if (operatorLatitude == 0 && operatorLongitude == 0)
            return "Unknown";
        return String.format(Locale.US,"%3.7f", operatorLongitude);
    }

    public void setAreaCount(int areaCount) { this.areaCount = areaCount; }
    public int getAreaCount() { return areaCount; }

    public void setAreaRadius(int areaRadius) { this.areaRadius = areaRadius; }
    public int getAreaRadius() { return areaRadius; }
    public String getAreaRadiusAsString() {
        return String.format(Locale.US,"%d m", areaRadius);
    }

    private String getAltitudeAsString(double altitude) {
        if (altitude == -1000)
            return "Unknown";
        return String.format(Locale.US,"%3.1f m", altitude);
    }

    public void setAreaCeiling(double areaCeiling) { this.areaCeiling = areaCeiling; }
    public double getAreaCeiling() { return areaCeiling; }
    public String getAreaCeilingAsString() { return getAltitudeAsString(areaCeiling); }

    public void setAreaFloor(double areaFloor) { this.areaFloor = areaFloor; }
    public double getAreaFloor() { return areaFloor; }
    public String getAreaFloorAsString() { return getAltitudeAsString(areaFloor); }
}

