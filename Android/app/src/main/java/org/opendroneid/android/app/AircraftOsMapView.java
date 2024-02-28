/*
 * Open Drone ID on Open Street Map(OSM) Example.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Writeing: GANCHI
 * TWITTER: @_ganchi
 * https://ganchi.rdy.jp/
 * Produced by Mac@Multiprotocol
 */

package org.opendroneid.android.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.fingerprintjs.android.fingerprint.Fingerprinter;
import com.fingerprintjs.android.fingerprint.FingerprinterFactory;
import com.google.gson.Gson;

import org.opendroneid.android.R;
import org.opendroneid.android.app.network.client.ApiClientDetection;
import org.opendroneid.android.app.network.manager.LogedUserManager;
import org.opendroneid.android.app.network.models.drone.DroneDetectionPost;
import org.opendroneid.android.app.network.models.drone.DroneDetectionResponse;
import org.opendroneid.android.app.network.service.ApiService;
import org.opendroneid.android.data.AircraftObject;
import org.opendroneid.android.data.LocationData;
import org.opendroneid.android.data.SystemData;
import org.opendroneid.android.data.Util;
import org.opendroneid.android.views.OSMCustomColorFilter;
import org.opendroneid.android.views.OSMCustomTilesOverlay;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AircraftOsMapView extends Fragment {
    private static final String TAG = "AircraftOsvMapView";
    private final double P_TOKYO_LATITUDE = 35.681167;
    private final double P_TOKYO_LONGITUDE = 139.767052;
    private final double P_DEFAULT_LATITUDE = 0;
    private final double P_DEFAULT_LONGITUDE = 0;
    private final HashMap<AircraftObject, MapObserver> aircraftObservers = new HashMap<>();
    private Context context;
    private MapView osvMap;
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
    private AircraftViewModel model;

    private void trackAircraft(AircraftObject aircraftObject) {
        MapObserver observer = new MapObserver(aircraftObject);
        aircraftObservers.put(aircraftObject, observer);
    }

    private void stopTrackingAircraft(AircraftObject aircraftObject) {
        MapObserver observer = aircraftObservers.remove(aircraftObject);
        if (observer == null) return;
        observer.stop();
    }

    public void setupModel() {
        model = new ViewModelProvider(requireActivity()).get(AircraftViewModel.class);
        model.getAllAircraft().observe(getViewLifecycleOwner(), allAircraftObserver);
        model.getActiveAircraft().observe(getViewLifecycleOwner(), new Observer<AircraftObject>() {
            MapObserver last = null;

            @Override
            public void onChanged(@Nullable AircraftObject object) {
                if (object == null || object.getLocation() == null || osvMap == null) {
                    return;
                }
                MapObserver observer = aircraftObservers.get(object);
                if (observer == null) {
                    return;
                }
                GeoPoint gp = new GeoPoint(object.getLocation().getLatitude(), object.getLocation().getLongitude());
                Log.i(TAG, "centering on " + object + " at " + gp);
                if (last != null && last.marker != null) {
                    last.marker.setAlpha(0.5f);
                    if (last.markerPilot != null) {
                        last.markerPilot.setAlpha(0.5f);
                    }
                }
                if (observer.marker != null) {
                    observer.marker.setAlpha(1.0f);
                }
                if (observer.markerPilot != null) {
                    observer.markerPilot.setAlpha(1.0f);
                }
                last = observer;
                // center map position
                IMapController mapController = osvMap.getController();
                mapController.animateTo(gp);
            }
        });
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        return layoutInflater.inflate(R.layout.fragment_osm, viewGroup, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        if (osvMap != null) {
            osvMap.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        if (osvMap != null) {
            osvMap.onPause();
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated()");
        context = getContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        osvMap = view.findViewById(R.id.map);
        osvMap.setMultiTouchControls(true);

        MapTileProviderBasic tileProvider = new MapTileProviderBasic(osvMap.getContext());
        tileProvider.setTileSource(TileSourceFactory.MAPNIK);
        OSMCustomTilesOverlay customOverlay = new OSMCustomTilesOverlay(tileProvider, context, osvMap);

        ColorFilter colorFilter = OSMCustomColorFilter.createDarkModeFilter();
        customOverlay.setColorFilter(colorFilter);

        osvMap.getOverlayManager().add(customOverlay);

        MyLocationNewOverlay myLocationoverlay = new MyLocationNewOverlay(osvMap);
        myLocationoverlay.enableMyLocation();
        myLocationoverlay.disableFollowLocation();
        myLocationoverlay.setDrawAccuracyEnabled(true);
        osvMap.getOverlays().add(myLocationoverlay);

        CompassOverlay compassOverlay = new CompassOverlay(requireContext(), osvMap);
        compassOverlay.enableCompass();
        osvMap.getOverlays().add(compassOverlay);

        IMapController mapController = osvMap.getController();
        mapController.setZoom(3.0);
        GeoPoint centerPoint = new GeoPoint(P_DEFAULT_LATITUDE, P_DEFAULT_LONGITUDE);
        mapController.animateTo(centerPoint);

        LogedUserManager logedUserManager = new LogedUserManager(getContext());
        if (logedUserManager.getToken() != null) {
            setupModel();
        }
    }

    public void setMapSettings() {
        if (getActivity() == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
    }

    class MapObserver implements Observer<LocationData> {
        private final List<GeoPoint> polylineData;
        private final AircraftObject aircraft;
        private double lastLatitude = 0.0;
        private double lastLongitude = 0.0;
        private Marker markerPilot;
        private Object makerPilotTag;
        private final Observer<SystemData> systemObserver = new Observer<SystemData>() {
            @Override
            public void onChanged(@Nullable SystemData ignore) {
                SystemData sys = aircraft.getSystem();
                if (sys == null || osvMap == null) {
                    return;
                }
                if (sys.getOperatorLatitude() == 0.0 && sys.getOperatorLongitude() == 0.0) {
                    return;
                }
                GeoPoint geoPoint = new GeoPoint(sys.getOperatorLatitude(), sys.getOperatorLongitude());
                if (markerPilot == null) {
                    String id = "ID missing";
                    if (aircraft.getIdentification1() != null) {
                        id = aircraft.getIdentification1().getUasIdAsString();
                    }
                    markerPilot = new Marker(osvMap);
                    markerPilot.setIcon(context.getDrawable(R.drawable.ic_pilot_target));
                    markerPilot.setPosition(geoPoint);
                    markerPilot.setTitle(sys.getOperatorLocationType().toString() + "\n" + id);
                    if (markerPilot != null) {
                        makerPilotTag = new Pair<>(aircraft, this);
                    }

                    //post initial detection call
                    if (aircraft.location.getValue().getLatitude() != 0 && aircraft.location.getValue().getLongitude() != 0) {
                        lastLatitude = aircraft.location.getValue().getLatitude();
                        lastLongitude = aircraft.location.getValue().getLongitude();
                        sendDroneData(aircraft);

                    }

                    Objects.requireNonNull(markerPilot).setOnMarkerClickListener((marker, mapView) -> {
                        if (marker != null) {
                            Toast.makeText(context, marker.getTitle(), Toast.LENGTH_SHORT).show();
                        }
                        if (makerPilotTag instanceof AircraftObject) {
                            model.setActiveAircraft((AircraftObject) makerPilotTag);
                            return true;
                        }
                        return false;
                    });
                    osvMap.getOverlays().add(markerPilot);
                }
                if (markerPilot != null) {
                    markerPilot.setPosition(geoPoint);
                }
            }
        };
        private Marker marker;
        private Object makerTag;
        private Polyline polyline;

        MapObserver(AircraftObject active) {
            aircraft = active;
            aircraft.location.observe(AircraftOsMapView.this, this);
            aircraft.system.observe(AircraftOsMapView.this, systemObserver);
            polylineData = new ArrayList<>();
        }

        void stop() {
            aircraft.location.removeObserver(this);
            aircraft.system.removeObserver(systemObserver);
            if (marker != null) {
                osvMap.getOverlays().remove(marker);
                marker = null;
            }
            if (markerPilot != null) {
                osvMap.getOverlays().remove(markerPilot);
                markerPilot = null;
            }
            if (polylineData != null) {
                polylineData.clear();
            }
            if (polyline != null) {
                osvMap.getOverlays().remove(polyline);
                polyline = null;
            }
        }

        @Override
        public void onChanged(@Nullable LocationData ignore) {
            boolean zoom = false;
            LocationData loc = aircraft.getLocation();
            if (loc == null || osvMap == null || polylineData == null) {
                return;
            }
            if (loc.getLatitude() == 0.0 && loc.getLongitude() == 0.0) {
                return;
            }

            // Check if coordinates have changed
            if (lastLatitude != 0 && lastLongitude != 0) {
                if (loc.getLatitude() != lastLatitude || loc.getLongitude() != lastLongitude) {
                    //post movement detection call
                    sendDroneData(aircraft);
                    lastLatitude = aircraft.location.getValue().getLatitude();
                    lastLongitude = aircraft.location.getValue().getLongitude();
                }
            }


            GeoPoint geoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
            // make marker
            if (marker == null) {
                String id = "ID missing";
                if (aircraft.getIdentification1() != null) {
                    id = aircraft.getIdentification1().getUasIdAsString();
                }
                marker = new Marker(osvMap);
                marker.setPosition(geoPoint);
                marker.setTitle("aircraft\n" + id);
                marker.setIcon(context.getDrawable(R.drawable.ic_drone_fly));
                if (marker != null) {
                    makerTag = aircraft;
                }
                Objects.requireNonNull(marker).setOnMarkerClickListener((marker, mapView) -> {
                    if (marker != null) {
                        Toast.makeText(context, marker.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                    if (makerTag instanceof AircraftObject) {
                        model.setActiveAircraft((AircraftObject) makerTag);
                        return true;
                    }
                    return false;
                });
                osvMap.getOverlays().add(marker);
                zoom = true;
            }

            // make marker line
            if (polyline != null) {
                osvMap.getOverlays().remove(polyline);
                polyline = null;
            }
            polyline = new Polyline();
            polylineData.add(geoPoint);
            polyline.setPoints(polylineData);
            polyline.getOutlinePaint().setColor(Color.RED);
            osvMap.getOverlays().add(polyline);
            osvMap.invalidate();

            // move map position
            marker.setPosition(geoPoint);
            if (zoom) {
                // moveCamera
                IMapController mapController = osvMap.getController();
                mapController.setZoom(17.0);
                mapController.animateTo(geoPoint);
            }
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


        private void sendDroneData(AircraftObject aircraftObject) {
            ApiService apiService = ApiClientDetection.getClient(requireContext()).create(ApiService.class);

            if (aircraftObject.system.getValue().getSystemTimestamp() == 0) {
                return;
            }

            DroneDetectionPost droneDetectionPost = new DroneDetectionPost();

            long timeStamp = stringToTimeStamp(aircraftObject.system.getValue().getTimestampAsString());
            droneDetectionPost.setTime(timeStamp);

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
                            Log.d("SENDING_DETECTION", response.message());
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
    }
}
