/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.data;

import android.content.res.Resources;

import org.opendroneid.android.R;

import java.sql.Timestamp;
import java.util.Locale;

public class SystemData extends MessageData {

    private operatorLocationTypeEnum operatorLocationType;
    private classificationTypeEnum classificationType;
    private double operatorLatitude;
    private double operatorLongitude;
    private int areaCount;
    private int areaRadius;
    private double areaCeiling;
    private double areaFloor;
    private categoryEnum category;
    private classValueEnum classValue;
    private double operatorAltitudeGeo;
    private long systemTimestamp;

    public SystemData() {
        super();
        operatorLocationType = operatorLocationTypeEnum.Invalid;
        classificationType = classificationTypeEnum.Undeclared;
        operatorLatitude = 0;
        operatorLongitude = 0;
        areaCount = 0;
        areaRadius = 0;
        areaCeiling = -1000; // -1000 is the Invalid value in the specification
        areaFloor = -1000; // -1000 is the Invalid value in the specification
        category = categoryEnum.Undeclared;
        classValue = classValueEnum.Undeclared;
        operatorAltitudeGeo = -1000; // -1000 is the Invalid value in the specification
        systemTimestamp = 0;
    }

    // These apply both to operator Latitude/Longitude and to AltitudeGeo
    public enum operatorLocationTypeEnum {
        TakeOff,
        Dynamic, // Live GNSS Location
        Fixed,   // Fixed Location
        Invalid,
    }

    public operatorLocationTypeEnum getOperatorLocationType() { return operatorLocationType; }
    public void setOperatorLocationType(int operatorLocationType) {
        switch(operatorLocationType) {
            case 0: this.operatorLocationType = operatorLocationTypeEnum.TakeOff; break;
            case 1: this.operatorLocationType = operatorLocationTypeEnum.Dynamic; break;
            case 2: this.operatorLocationType = operatorLocationTypeEnum.Fixed; break;
            default: this.operatorLocationType = operatorLocationTypeEnum.Invalid; break;
        }
    }

    public enum classificationTypeEnum {
        Undeclared,
        EU, // European Union
    }

    public classificationTypeEnum getclassificationType() { return classificationType; }
    public void setClassificationType(int classificationType) {
        if (classificationType == 1) {
            this.classificationType = classificationTypeEnum.EU;
        } else {
            this.classificationType = classificationTypeEnum.Undeclared;
        }
    }

    public void setOperatorLatitude(double operatorLatitude) {
        if (operatorLatitude < -90 || operatorLatitude > 90) {
            operatorLatitude = 0;
            this.operatorLongitude = 0; // both equal to zero is defined in the specification as the Invalid value
        }
        this.operatorLatitude = operatorLatitude;
    }
    public double getOperatorLatitude() { return operatorLatitude; }
    public String getOperatorLatitudeAsString(Resources res) {
        if (operatorLatitude == 0 && operatorLongitude == 0)
            return res.getString(R.string.unknown);
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
    public String getOperatorLongitudeAsString(Resources res) {
        if (operatorLatitude == 0 && operatorLongitude == 0)
            return res.getString(R.string.unknown);
        return String.format(Locale.US,"%3.7f", operatorLongitude);
    }

    public void setAreaCount(int areaCount) { this.areaCount = areaCount; }
    public int getAreaCount() { return areaCount; }

    public void setAreaRadius(int areaRadius) { this.areaRadius = areaRadius; }
    public int getAreaRadius() { return areaRadius; }
    public String getAreaRadiusAsString() {
        return String.format(Locale.US,"%d m", areaRadius);
    }

    private String getAltitudeAsString(double altitude, Resources res) {
        if (altitude == -1000)
            return res.getString(R.string.unknown);
        return String.format(Locale.US,"%3.1f m", altitude);
    }

    public void setAreaCeiling(double areaCeiling) { this.areaCeiling = areaCeiling; }
    public double getAreaCeiling() { return areaCeiling; }
    public String getAreaCeilingAsString(Resources res) { return getAltitudeAsString(areaCeiling, res); }

    public void setAreaFloor(double areaFloor) { this.areaFloor = areaFloor; }
    public double getAreaFloor() { return areaFloor; }
    public String getAreaFloorAsString(Resources res) { return getAltitudeAsString(areaFloor, res); }

    public enum categoryEnum {
        Undeclared,
        EU_Open,
        EU_Specific,
        EU_Certified,
    }

    public categoryEnum getCategory() { return category; }
    public void setCategory(int category) {
        if (classificationType == classificationTypeEnum.EU) {
            switch(category) {
                case 1: this.category = categoryEnum.EU_Open; break;
                case 2: this.category = categoryEnum.EU_Specific; break;
                case 3: this.category = categoryEnum.EU_Certified; break;
                default: this.category = categoryEnum.Undeclared; break;
            }
        } else {
            this.category = categoryEnum.Undeclared;
        }
    }

    public enum classValueEnum {
        Undeclared,
        EU_Class_0,
        EU_Class_1,
        EU_Class_2,
        EU_Class_3,
        EU_Class_4,
        EU_Class_5,
        EU_Class_6,
    }

    public classValueEnum getClassValue() { return classValue; }
    public void setClassValue(int classValue) {
        if (classificationType == classificationTypeEnum.EU) {
            switch(classValue) {
                case 1: this.classValue = classValueEnum.EU_Class_0; break;
                case 2: this.classValue = classValueEnum.EU_Class_1; break;
                case 3: this.classValue = classValueEnum.EU_Class_2; break;
                case 4: this.classValue = classValueEnum.EU_Class_3; break;
                case 5: this.classValue = classValueEnum.EU_Class_4; break;
                case 6: this.classValue = classValueEnum.EU_Class_5; break;
                case 7: this.classValue = classValueEnum.EU_Class_6; break;
                default: this.classValue = classValueEnum.Undeclared; break;
            }
        } else {
            this.classValue = classValueEnum.Undeclared;
        }
    }

    public void setOperatorAltitudeGeo(double operatorAltitudeGeo) {
        this.operatorAltitudeGeo = operatorAltitudeGeo;
    }
    public double getOperatorAltitudeGeo() { return operatorAltitudeGeo; }
    public String getOperatorAltitudeGeoAsString(Resources res) {
        return getAltitudeAsString(operatorAltitudeGeo, res);
    }

    long getSystemTimestamp() { return systemTimestamp; }
    public String getSystemTimestampAsString(Resources res) {
        if (systemTimestamp == 0)
            return res.getString(R.string.unknown);
        Timestamp time = new Timestamp((1546300800L + systemTimestamp) * 1000);
        return time.toString();
    }
    public void setSystemTimestamp(long systemTimestamp) { this.systemTimestamp = systemTimestamp; }
}