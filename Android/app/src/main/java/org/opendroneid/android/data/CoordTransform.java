/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * WGS-84 <-> GCJ-02 coordinate transformation.
 *
 * Remote ID broadcasts carry raw WGS-84 coordinates (plain GPS).
 * Chinese online map services (AMap/Gaode, Tencent, Google.cn) render their
 * map tiles in the GCJ-02 ("Mars") reference frame. Plotting WGS-84
 * coordinates directly on GCJ-02 tiles produces a position error of roughly
 * 300-600 meters inside mainland China. This class converts the received
 * Remote ID coordinates so that markers land on the correct spot of a
 * GCJ-02 base map.
 */

package org.opendroneid.android.data;

public final class CoordTransform {

    private static final double PI = Math.PI;
    private static final double SEMI_MAJOR_AXIS = 6378245.0; // Krasovsky 1940
    private static final double ECC_SQUARED = 0.00669342162296594323;

    private CoordTransform() { }

    /** @return true when the point is inside the rough bounding box of mainland China. */
    public static boolean outOfChina(double lat, double lon) {
        return lon < 72.004 || lon > 137.8347 || lat < 0.8293 || lat > 55.8271;
    }

    /**
     * Convert a WGS-84 coordinate to GCJ-02.
     * Outside mainland China the input is returned unchanged.
     *
     * @return double[]{latitude, longitude}
     */
    public static double[] wgs84ToGcj02(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new double[]{lat, lon};
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - ECC_SQUARED * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((SEMI_MAJOR_AXIS * (1 - ECC_SQUARED)) / (magic * sqrtMagic) * PI);
        dLon = (dLon * 180.0) / (SEMI_MAJOR_AXIS / sqrtMagic * Math.cos(radLat) * PI);
        return new double[]{lat + dLat, lon + dLon};
    }

    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
                + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y
                + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }
}
