/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.app;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;

import com.fingerprintjs.android.fingerprint.Fingerprinter;
import com.fingerprintjs.android.fingerprint.FingerprinterFactory;
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

import org.opendroneid.android.app.network.client.ApiClientDetection;
import org.opendroneid.android.app.network.models.drone.DroneDetectionPost;
import org.opendroneid.android.app.network.models.drone.DroneDetectionResponse;
import org.opendroneid.android.app.network.service.ApiService;
import org.opendroneid.android.data.AircraftObject;
import org.opendroneid.android.data.LocationData;
import org.opendroneid.android.data.SystemData;
import org.opendroneid.android.data.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AircraftMapView extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final String TAG = "AircraftMapView";
    private GoogleMap googleMap;
    private AircraftViewModel model;

    private double lastLatitude = 0.0;
    private double lastLongitude = 0.0;

    private final HashMap<AircraftObject, MapObserver> aircraftObservers = new HashMap<>();

    private final Util.DiffObserver<AircraftObject> allAircraftObserver = new Util.DiffObserver<AircraftObject>() {
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

        model = new ViewModelProvider(getActivity()).get(AircraftViewModel.class);
        model.getAllAircraft().observe(getViewLifecycleOwner(), allAircraftObserver);
        model.getActiveAircraft().observe(getViewLifecycleOwner(), new Observer<AircraftObject>() {
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
                Log.i(TAG, "centering on " + object + " at " + ll);

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
                if (position.zoom < DESIRED_ZOOM - ALLOWED_ZOOM_MARGIN || position.zoom > DESIRED_ZOOM + ALLOWED_ZOOM_MARGIN)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, DESIRED_ZOOM));
                else
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(ll));
            }
        });
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {

        Object tag = marker.getTag();
        if (tag instanceof AircraftObject) {
            model.setActiveAircraft((AircraftObject) tag);
            return true;
        }
        return false;
    }

    public class MapObserver implements Observer<LocationData> {
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

        public void stop() {
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
                if (sys == null || googleMap == null)
                    return;

                // filter out zero data
                if (sys.getOperatorLatitude() == 0.0 && sys.getOperatorLongitude() == 0.0)
                    return;

                LatLng latLng = new LatLng(sys.getOperatorLatitude(), sys.getOperatorLongitude());
                if (markerPilot == null) {
                    String id = "ID missing";
                    if (aircraft.getIdentification1() != null)
                        id = aircraft.getIdentification1().getUasIdAsString();
                    markerPilot = googleMap.addMarker(
                            new MarkerOptions()
                                    .alpha(0.5f)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                    .position(latLng)
                                    .title(sys.getOperatorLocationType().toString() + ": " + id));
                    if (markerPilot != null)
                        markerPilot.setTag(new Pair<>(aircraft, this));
                }


                //post initial detection call
                if (aircraft.location.getValue().getLatitude() != 0 && aircraft.location.getValue().getLongitude() != 0) {
                    lastLatitude = aircraft.location.getValue().getLatitude();
                    lastLongitude = aircraft.location.getValue().getLongitude();
                    sendDroneData(aircraft);

                }

                if (markerPilot != null)
                    markerPilot.setPosition(latLng);
            }
        };

        @Override
        public void onChanged(@Nullable LocationData ignore) {
            boolean zoom = false;
            LocationData loc = aircraft.getLocation();
            if (loc == null || googleMap == null || polylineOptions == null)
                return;

            // filter out zero data
            if (loc.getLatitude() == 0.0 && loc.getLongitude() == 0.0)
                return;

            // Check if coordinates have changed
            if (lastLatitude != 0 && lastLongitude != 0) {
                if (loc.getLatitude() != lastLatitude || loc.getLongitude() != lastLongitude) {
                    //post movement detection call
                    sendDroneData(aircraft);
                    lastLatitude = aircraft.location.getValue().getLatitude();
                    lastLongitude = aircraft.location.getValue().getLongitude();
                }
            }

            LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
            if (marker == null) {
                String id = "ID missing";
                if (aircraft.getIdentification1() != null)
                    id = aircraft.getIdentification1().getUasIdAsString();
                marker = googleMap.addMarker(
                        new MarkerOptions()
                                .alpha(0.5f)
                                .position(latLng)
                                .title("aircraft " + id));
                if (marker != null)
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

    @Override @NonNull
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
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
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (getActivity() == null)
            return;

        this.googleMap = googleMap;
        setMapSettings();
    }

    public boolean changeMapType(MenuItem item) {
        if (googleMap == null)
            return false;
        /* When the flag org.gradle.project.map in gradle.properties is defined to google_map,
           the below code needs to be uncommented:
        if (item.getItemId() == R.id.maptypeHYBRID) {
            item.setChecked(!item.isChecked());
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (item.getItemId() == R.id.maptypeNONE) {
            item.setChecked(!item.isChecked());
            googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        } else if (item.getItemId() == R.id.maptypeNORMAL) {
            item.setChecked(!item.isChecked());
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (item.getItemId() == R.id.maptypeSATELLITE) {
            item.setChecked(!item.isChecked());
            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (item.getItemId() == R.id.maptypeTERRAIN) {
            item.setChecked(!item.isChecked());
            googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else {
            return false;
        }*/
        return true;
    }

    public void setMapSettings() {
        if (getActivity() == null || googleMap == null)
            return;

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.setOnMarkerClickListener(this);
    }

    private void sendDroneData(AircraftObject aircraftObject) {
        ApiService apiService = ApiClientDetection.getClient(requireContext()).create(ApiService.class);

        DroneDetectionPost droneDetectionPost = new DroneDetectionPost();

        if (aircraftObject.system.getValue().getSystemTimestamp() == 0) {
            long timeStamp = System.currentTimeMillis();
            droneDetectionPost.setTime(timeStamp);
        }else{
            long timeStamp = stringToTimeStamp(aircraftObject.system.getValue().getTimestampAsString());
            droneDetectionPost.setTime(timeStamp);
        }



        Fingerprinter fingerprinter = FingerprinterFactory.create(requireContext());
        fingerprinter.getDeviceId(Fingerprinter.Version.V_5, deviceIdResult -> {

            String phoneSensor = deviceIdResult.getDeviceId();
            droneDetectionPost.setSensorId(phoneSensor);

            ArrayList metaDataList = new ArrayList<DroneDetectionPost.Metadata>();

            DroneDetectionPost.Metadata objectType = new DroneDetectionPost.Metadata();
            objectType.setKey("type");
            objectType.setVal("drone");
            metaDataList.add(objectType);

            DroneDetectionPost.Metadata macAddress = new DroneDetectionPost.Metadata();
            macAddress.setKey("mac_address");
            macAddress.setVal(Objects.requireNonNull(aircraftObject.connection.getValue()).macAddress);
            macAddress.setType("primary");
            metaDataList.add(macAddress);

            DroneDetectionPost.Metadata source = new DroneDetectionPost.Metadata();
            source.setKey("source");
            source.setVal(Objects.requireNonNull(aircraftObject.connection.getValue()).macAddress);
            source.setType("primary");
            metaDataList.add(source);

            DroneDetectionPost.Metadata registration = new DroneDetectionPost.Metadata();
            registration.setKey("registration");
            registration.setVal(Objects.requireNonNull(aircraftObject.connection.getValue()).macAddress);
            registration.setType("primary");
            metaDataList.add(registration);

            DroneDetectionPost.Metadata icao = new DroneDetectionPost.Metadata();
            icao.setKey("icao");
            icao.setVal(Objects.requireNonNull(aircraftObject.connection.getValue()).macAddress);
            icao.setType("primary");
            metaDataList.add(icao);

            DroneDetectionPost.Metadata sensorLatitude = new DroneDetectionPost.Metadata();
            sensorLatitude.setKey("sensor_latitude");
            sensorLatitude.setVal(aircraftObject.location.getValue().getLatitudeAsString(getResources()));
            sensorLatitude.setType("primary");
            metaDataList.add(sensorLatitude);

            DroneDetectionPost.Metadata sensorLongitude = new DroneDetectionPost.Metadata();
            sensorLongitude.setKey("sensor_longitude");
            sensorLongitude.setVal(aircraftObject.location.getValue().getLongitudeAsString(getResources()));
            sensorLongitude.setType("primary");
            metaDataList.add(sensorLongitude);

            DroneDetectionPost.Metadata operatorLocation = new DroneDetectionPost.Metadata();
            operatorLocation.setKey("Operator Location");
            operatorLocation.setVal(aircraftObject.location.getValue().getLatitudeAsString(getResources()) + ", " + aircraftObject.location.getValue().getLongitudeAsString(getResources()));
            operatorLocation.setType("volatile");
            metaDataList.add(operatorLocation);

            DroneDetectionPost.Metadata altitude = new DroneDetectionPost.Metadata();
            altitude.setKey("alt");
            altitude.setVal(aircraftObject.location.getValue().getAltitudeGeodeticAsString(getResources()));
            altitude.setType("volatile");
            metaDataList.add(altitude);

            droneDetectionPost.setPosition(new DroneDetectionPost.Position(
                    aircraftObject.location.getValue().getLatitude(),
                    aircraftObject.location.getValue().getLongitude(),
                    aircraftObject.location.getValue().getAltitudeGeodetic(),
                    aircraftObject.location.getValue().getTimeAccuracy(), // accuracy
                    aircraftObject.location.getValue().getSpeedHorizontal(), // speed-horizontal
                    aircraftObject.location.getValue().getDirection()  // bearing
            ));

            droneDetectionPost.setMetadata(metaDataList);

            Call<DroneDetectionResponse> call = apiService.postDetection(droneDetectionPost);
            call.enqueue(new Callback<DroneDetectionResponse>() {
                @Override
                public void onResponse(Call<DroneDetectionResponse> call, Response<DroneDetectionResponse> response) {
                    if (response.isSuccessful()) {
                        Log.d("SENDING_DETECTION_DRONE", response.message());
                    } else {
                        Log.e("SENDING_DETECTION_ERROR", response.message());
                    }
                    metaDataList.clear();
                }

                @Override
                public void onFailure(Call<DroneDetectionResponse> call, Throwable t) {
                    Log.e("SENDING_DETECTION_ERROR", t.getMessage());
                }
            });

            return null;
        });
    }

    public long stringToTimeStamp(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date date = dateFormat.parse(dateString);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
