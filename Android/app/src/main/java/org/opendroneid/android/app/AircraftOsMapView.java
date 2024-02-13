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
import android.graphics.ColorMatrixColorFilter;
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

import org.opendroneid.android.R;
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
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AircraftOsMapView extends Fragment {
    private final double P_TOKYO_LATITUDE = 35.681167;
    private final double P_TOKYO_LONGITUDE = 139.767052;
    private final double P_DEFAULT_LATITUDE = 0;
    private final double P_DEFAULT_LONGITUDE = 0;

    private static final String TAG = "AircraftOsvMapView";
    private Context context;
    private MapView osvMap;
    private AircraftViewModel model;
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

    private void setupModel() {
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

        ColorFilter colorFilter = OSMCustomColorFilter.createDarkModeFilter() ;
        customOverlay.setColorFilter(colorFilter);

        osvMap.getOverlayManager().add(customOverlay);

        MyLocationNewOverlay myLocationoverlay = new MyLocationNewOverlay(osvMap);
        myLocationoverlay.enableMyLocation();
        myLocationoverlay.disableFollowLocation();
        myLocationoverlay.setDrawAccuracyEnabled(true);

        CompassOverlay compassOverlay = new CompassOverlay(requireContext(), osvMap);
        compassOverlay.enableCompass();
        osvMap.getOverlays().add(compassOverlay);

        IMapController mapController = osvMap.getController();
        mapController.setZoom(3.0);
        GeoPoint centerPoint = new GeoPoint(P_DEFAULT_LATITUDE, P_DEFAULT_LONGITUDE);
        mapController.animateTo(centerPoint);

        setupModel();
    }

    public void setMapSettings() {
        if (getActivity() == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    class MapObserver implements Observer<LocationData> {
        private Marker markerPilot;
        private Object makerPilotTag;
        private Marker marker;
        private Object makerTag;
        private final List<GeoPoint> polylineData;
        private Polyline polyline;
        private final AircraftObject aircraft;

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
                    markerPilot.setIcon(context.getDrawable(R.drawable.ic_pilot));
                    markerPilot.setPosition(geoPoint);
                    markerPilot.setTitle(sys.getOperatorLocationType().toString() + "\n" + id);
                    if (markerPilot != null) {
                        makerPilotTag = new Pair<>(aircraft, this);
                    }
                    Objects.requireNonNull(markerPilot).setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker, MapView mapView) {
                            if (marker != null) {
                                Toast.makeText(context, marker.getTitle(), Toast.LENGTH_SHORT).show();
                            }
                            if (makerPilotTag instanceof AircraftObject) {
                                model.setActiveAircraft((AircraftObject) makerPilotTag);
                                return true;
                            }
                            return false;
                        }
                    });
                    osvMap.getOverlays().add(markerPilot);
                }
                if (markerPilot != null) {
                    markerPilot.setPosition(geoPoint);
                }
            }
        };

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
                if (marker != null) {
                    makerTag = aircraft;
                }
                Objects.requireNonNull(marker).setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView) {
                        if (marker != null) {
                            Toast.makeText(context, marker.getTitle(), Toast.LENGTH_SHORT).show();
                        }
                        if (makerTag instanceof AircraftObject) {
                            model.setActiveAircraft((AircraftObject) makerTag);
                            return true;
                        }
                        return false;
                    }
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
    }
}
