/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.app;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import org.opendroneid.android.data.AircraftObject;
import org.opendroneid.android.data.LocationData;
import org.opendroneid.android.data.SystemData;
import org.opendroneid.android.data.Util;

import java.util.Collection;
import java.util.HashMap;

public class AircraftMapView extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
private static final String TAG = "AircraftMapView";

    private GoogleMap googleMap;
    private AircraftViewModel model;

    private HashMap<AircraftObject, MapObserver> aircraftObservers = new HashMap<>();

    private Util.DiffObserver<AircraftObject> allAircraftObserver = new Util.DiffObserver<AircraftObject>() {
        @Override
        public void onAdded(Collection<AircraftObject> added) {
            for (AircraftObject aircraftObject : added) {
                trackAircraft(aircraftObject);
            }
        }

        @Override
        public void onRemoved(Collection<AircraftObject> removed) {
            for (AircraftObject aircraftObject : removed) {
                stopTrackingAircraft(aircraftObject);
            }
        }
    };

    private void trackAircraft(AircraftObject aircraftObject) {
        MapObserver observer = new MapObserver(aircraftObject);
        aircraftObservers.put(aircraftObject, observer);
    }

    private void stopTrackingAircraft(AircraftObject aircraftObject) {
        MapObserver observer = aircraftObservers.remove(aircraftObject);
        if (observer == null) return;
        observer.stop();
    }

    private static final int DESIRED_ZOOM = 17;
    private static final int ALLOWED_ZOOM_MARGIN = 2;

    private void setupModel() {
        if (getActivity() == null)
            return;

        model = ViewModelProviders.of(getActivity()).get(AircraftViewModel.class);

        model.getAllAircraft().observe(this, allAircraftObserver);
        model.getActiveAircraft().observe(this, new Observer<AircraftObject>() {
            MapObserver last = null;
            @Override
            public void onChanged(@Nullable AircraftObject object) {
                if (object == null || object.getLocation() == null || googleMap == null)
                    return;
                MapObserver observer = aircraftObservers.get(object);
                if (observer == null)
                    return;

                if (object.getLocation().getLatitude() == 0.0 && object.getLocation().getLongitude() == 0.0)
                    return;

                LatLng ll = new LatLng(object.getLocation().getLatitude(), object.getLocation().getLongitude());
                Log.i(TAG, "centering on "+object+" at "+ll);

                if (last != null && last.marker != null) {
                    last.marker.setAlpha(0.5f);
                    if (last.markerPilot != null)
                        last.markerPilot.setAlpha(0.5f);
                }
                if (observer.marker != null)
                    observer.marker.setAlpha(1.0f);
                if (observer.markerPilot != null)
                    observer.markerPilot.setAlpha(1.0f);

                last = observer;

                CameraPosition position = googleMap.getCameraPosition();
                if (position.zoom < DESIRED_ZOOM - ALLOWED_ZOOM_MARGIN ||
                    position.zoom > DESIRED_ZOOM + ALLOWED_ZOOM_MARGIN)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, DESIRED_ZOOM));
                else
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(ll));
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if (marker != null) {
            Object tag = marker.getTag();
            if (tag instanceof AircraftObject) {
                model.setActiveAircraft((AircraftObject) tag);
                return true;
            }
        }
        return false;
    }

    class MapObserver implements Observer<LocationData> {
        private Marker marker;
        private Marker markerPilot;
        private Polyline polyline;
        private PolylineOptions polylineOptions;

        private final AircraftObject aircraft;

        MapObserver(AircraftObject active) {
            aircraft = active;
            aircraft.location.observe(AircraftMapView.this, this);
            aircraft.system.observe(AircraftMapView.this, systemObserver);
            polylineOptions = new PolylineOptions()
                    .color(Color.RED)
                    .clickable(true);
        }

        void stop() {
            aircraft.location.removeObserver(this);
            aircraft.system.removeObserver(systemObserver);
            if (marker != null) {
                marker.remove();
                marker = null;
            }
            if (markerPilot != null) {
                markerPilot.remove();
                markerPilot = null;
            }
            if (polyline != null) {
                polyline.remove();
                polyline = null;
            }
            polylineOptions = null;
        }

        private final Observer<SystemData> systemObserver = new Observer<SystemData>() {
            @Override
            public void onChanged(@Nullable SystemData ignore) {
                SystemData sys = aircraft.getSystem();
                // todo, why is google maps null? fix this
                if (sys == null || googleMap == null)
                    return;

                // filter out zero data
                if (sys.getOperatorLatitude() == 0.0 && sys.getOperatorLongitude() == 0.0)
                    return;

                LatLng latLng = new LatLng(sys.getOperatorLatitude(), sys.getOperatorLongitude());
                if (markerPilot == null) {
                    String id = "ID missing";
                    if (aircraft.getIdentification() != null)
                        id = aircraft.getIdentification().getUasIdAsString();
                    markerPilot = googleMap.addMarker(
                            new MarkerOptions()
                                    .alpha(0.5f)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                    .position(latLng)
                                    .title("pilot " + id));
                    markerPilot.setTag(new Pair<>(aircraft, this));
                }
                markerPilot.setPosition(latLng);
            }
        };

        @Override
        public void onChanged(@Nullable LocationData ignore) {
            boolean zoom = false;
            LocationData loc = aircraft.getLocation();
            // todo, why is google maps null? fix this
            if (loc == null || googleMap == null || polylineOptions == null)
                return;

            // filter out zero data
            if (loc.getLatitude() == 0.0 && loc.getLongitude() == 0.0)
                return;

            LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
            if (marker == null) {
                String id = "ID missing";
                if (aircraft.getIdentification() != null)
                    id = aircraft.getIdentification().getUasIdAsString();
                marker = googleMap.addMarker(
                        new MarkerOptions()
                                .alpha(0.5f)
                                .position(latLng)
                        .title("aircraft " + id));
                marker.setTag(aircraft);
                zoom = true;
            }

            polylineOptions.add(latLng);
            if (polyline != null) {
                polyline.remove();
                polyline = null;
            }
            polyline = googleMap.addPolyline(polylineOptions);

            marker.setPosition(latLng);
            if (zoom) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = super.onCreateView(layoutInflater, viewGroup, bundle);
        getMapAsync(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        setupModel();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (getActivity() == null)
            return;

        this.googleMap = googleMap;

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling ActivityCompat#requestPermissions
            // to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e("XX", "##################### can't make the right permissions");
            return;
        }

        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.setMyLocationEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.setOnMarkerClickListener(this);
    }
}
