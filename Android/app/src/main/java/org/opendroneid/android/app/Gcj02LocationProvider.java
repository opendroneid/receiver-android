/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * osmdroid location provider that converts device GPS (WGS-84) fixes into
 * GCJ-02 before handing them to MyLocationNewOverlay.
 *
 * Needed whenever the base map is drawn from GCJ-02 tiles (AMap): without
 * this, the blue "my position" dot would be offset a few hundred meters from
 * the user's real position on the map.
 */

package org.opendroneid.android.app;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;

import org.opendroneid.android.data.CoordTransform;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import java.util.List;

public class Gcj02LocationProvider implements IMyLocationProvider {

    private static final long UPDATE_INTERVAL_MS = 1000;
    private static final float UPDATE_DISTANCE_M = 1.0f;

    private final LocationManager locationManager;
    private IMyLocationConsumer consumer;
    private Location lastKnown;
    private boolean running;

    private final LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Location converted = toGcj02(location);
            lastKnown = converted;
            if (consumer != null) {
                consumer.onLocationChanged(converted, Gcj02LocationProvider.this);
            }
        }

        @Override
        public void onLocationChanged(@NonNull List<Location> locations) {
            if (!locations.isEmpty()) {
                onLocationChanged(locations.get(locations.size() - 1));
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // not used by osmdroid
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
        }
    };

    public Gcj02LocationProvider(Context context) {
        locationManager = (LocationManager) context.getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);
    }

    private static Location toGcj02(Location input) {
        Location out = new Location(input);
        double[] gcj = CoordTransform.wgs84ToGcj02(input.getLatitude(), input.getLongitude());
        out.setLatitude(gcj[0]);
        out.setLongitude(gcj[1]);
        return out;
    }

    @Override
    public boolean startLocationProvider(IMyLocationConsumer locationConsumer) {
        this.consumer = locationConsumer;
        boolean started = false;
        if (locationManager == null) {
            return false;
        }
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        UPDATE_INTERVAL_MS, UPDATE_DISTANCE_M, listener);
                started = true;
            }
        } catch (SecurityException | IllegalArgumentException ignored) {
        }
        try {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        UPDATE_INTERVAL_MS, UPDATE_DISTANCE_M, listener);
                started = true;
            }
        } catch (SecurityException | IllegalArgumentException ignored) {
        }
        running = started;
        return started;
    }

    @Override
    public void stopLocationProvider() {
        running = false;
        if (locationManager != null) {
            locationManager.removeUpdates(listener);
        }
    }

    @Override
    public Location getLastKnownLocation() {
        if (locationManager == null) {
            return null;
        }
        Location best = null;
        try {
            for (String provider : locationManager.getAllProviders()) {
                Location l = locationManager.getLastKnownLocation(provider);
                if (l != null && (best == null || l.getTime() > best.getTime())) {
                    best = l;
                }
            }
        } catch (SecurityException ignored) {
        }
        if (best != null) {
            return toGcj02(best);
        }
        return lastKnown == null ? null : new Location(lastKnown);
    }

    @Override
    public void destroy() {
        stopLocationProvider();
    }

    public GeoPoint getLastKnownGeoPoint() {
        Location l = getLastKnownLocation();
        return l == null ? null : new GeoPoint(l.getLatitude(), l.getLongitude());
    }
}
