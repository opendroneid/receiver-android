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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import org.opendroneid.android.BuildConfig;
import org.opendroneid.android.R;
import org.opendroneid.android.data.AircraftObject;
import org.opendroneid.android.data.CoordTransform;
import org.opendroneid.android.data.LocationData;
import org.opendroneid.android.data.SystemData;
import org.opendroneid.android.data.Util;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AircraftOsMapView extends Fragment {
    private static final String TAG = "AircraftOsvMapView";
    private Context context;
    private MapView osvMap;
    private AircraftViewModel model;
    private final HashMap<AircraftObject, MapObserver> aircraftObservers = new HashMap<>();
    private MyLocationCenterOverlay myLocationCenterOverlay;
    private MyLocationNewOverlay myLocationOverlay;

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
                GeoPoint gp = geoPoint(object.getLocation().getLatitude(), object.getLocation().getLongitude());
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
        if ("amap".equals(BuildConfig.BASE_MAP)) {
            // AMap vector map - GCJ-02, paired with CoordTransform / Gcj02LocationProvider
            osvMap.setTileSource(new AMapTileSource(AMapTileSource.TYPE_VECTOR));
        } else if ("amap_sat".equals(BuildConfig.BASE_MAP)) {
            // AMap satellite imagery + transparent road/label overlay on top
            osvMap.setTileSource(new AMapTileSource(AMapTileSource.TYPE_SATELLITE));
            addAmapRoadOverlay();
        } else if ("google_sat".equals(BuildConfig.BASE_MAP)) {
            // Google imagery - WGS-84, no coordinate conversion
            osvMap.setTileSource(new GoogleSatTileSource());
        } else {
            osvMap.setTileSource(TileSourceFactory.MAPNIK);
        }
        osvMap.setMultiTouchControls(true);

        setupModel();
        setMapSettings();
    }

    public void setMapSettings() {
        if (osvMap == null) return;

        // osmdroid's built-in +/- zoom buttons are not wanted: zooming is done
        // with a pinch, and the "my location" button jumps straight to street level.
        osvMap.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        if (BuildConfig.NEEDS_GCJ02) {
            myLocationOverlay = new MyLocationNewOverlay(new Gcj02LocationProvider(requireContext()), osvMap);
        } else {
            myLocationOverlay = new MyLocationNewOverlay(osvMap);
        }
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.disableFollowLocation();
        myLocationOverlay.setDrawAccuracyEnabled(true);
        // osmdroid's defaults are a yellow person (no bearing) and a white
        // navigation arrow (once the fix carries a heading). Replace both with
        // the classic blue "my location" dot so the receiver position always
        // looks the same.
        Bitmap dot = bitmapFromDrawable(R.drawable.ic_my_location_dot);
        myLocationOverlay.setDirectionArrow(dot, dot);
        myLocationOverlay.setPersonHotspot(dot.getWidth() * 0.5f, dot.getHeight() * 0.5f);
        osvMap.getOverlays().add(myLocationOverlay);

        CompassOverlay compassOverlay = new CompassOverlay(requireContext(), osvMap);
        compassOverlay.enableCompass();
        osvMap.getOverlays().add(compassOverlay);

        myLocationCenterOverlay = new MyLocationCenterOverlay(requireContext(), myLocationOverlay);
        osvMap.getOverlays().add(myLocationCenterOverlay);

        IMapController mapController = osvMap.getController();
        mapController.setZoom(3.0);
        // Start centred on the receiver's last known position when one exists,
        // so the blue "my location" dot is on screen right away instead of the
        // user having to find it from the default (0, 0) world view.
        GeoPoint centerPoint = myLocationOverlay == null ? null : myLocationOverlay.getMyLocation();
        if (centerPoint == null) {
            centerPoint = new GeoPoint(0, 0);
        }
        mapController.animateTo(centerPoint);
    }

    /**
     * Stack the AMap transparent road/label layer (style=8) on top of the
     * satellite imagery. Its "loading" background must be transparent, otherwise
     * osmdroid paints opaque grey placeholders over the imagery while the
     * overlay tiles are still downloading.
     */
    private void addAmapRoadOverlay() {
        MapTileProviderBasic provider = new MapTileProviderBasic(requireContext(),
                new AMapTileSource(AMapTileSource.TYPE_ROAD_NET));
        TilesOverlay overlay = new TilesOverlay(provider, requireContext());
        overlay.setLoadingBackgroundColor(Color.TRANSPARENT);
        overlay.setLoadingLineColor(Color.TRANSPARENT);
        // index 0 keeps the road layer below the location/compass/marker overlays
        osvMap.getOverlays().add(0, overlay);
    }

    /**
     * Rasterise a vector drawable into a Bitmap (osmdroid's my-location icons
     * are Bitmaps, not Drawables).
     */
    private Bitmap bitmapFromDrawable(int resId) {
        Drawable d = AppCompatResources.getDrawable(context, resId);
        int w = d != null && d.getIntrinsicWidth() > 0 ? d.getIntrinsicWidth() : 24;
        int h = d != null && d.getIntrinsicHeight() > 0 ? d.getIntrinsicHeight() : 24;
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        if (d != null) {
            d.setBounds(0, 0, w, h);
            d.draw(canvas);
        }
        return bmp;
    }

    /**
     * Build a GeoPoint for the currently selected base map.
     * Remote ID data is WGS-84; AMap tiles are GCJ-02, so convert when needed.
     */
    private GeoPoint geoPoint(double latitude, double longitude) {
        if (BuildConfig.NEEDS_GCJ02) {
            double[] gcj = CoordTransform.wgs84ToGcj02(latitude, longitude);
            return new GeoPoint(gcj[0], gcj[1]);
        }
        return new GeoPoint(latitude, longitude);
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
                GeoPoint geoPoint = geoPoint(sys.getOperatorLatitude(), sys.getOperatorLongitude());
                if (markerPilot == null) {
                    String pId = "ID missing";
                    if (aircraft.getIdentification1() != null) {
                        pId = aircraft.getIdentification1().getUasIdAsString();
                    }
                    markerPilot = new Marker(osvMap);
                    markerPilot.setIcon(AppCompatResources.getDrawable(context, R.drawable.ic_pilot));
                    markerPilot.setPosition(geoPoint);
                    markerPilot.setTitle(sys.getOperatorLocationType().toString() + "\n" + pId);
                    if (markerPilot != null) {
                        makerPilotTag = new Pair<>(aircraft, this);
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
            GeoPoint geoPoint = geoPoint(loc.getLatitude(), loc.getLongitude());
            // make marker
            if (marker == null) {
                String aircraftId = "ID missing";
                if (aircraft.getIdentification1() != null) {
                    aircraftId = aircraft.getIdentification1().getUasIdAsString();
                }
                marker = new Marker(osvMap);
                marker.setPosition(geoPoint);
                marker.setTitle("aircraft\n" + aircraftId);
                if (marker != null) {
                    makerTag = aircraft;
                }
                Objects.requireNonNull(marker).setOnMarkerClickListener((marker1, mapView) -> {
                    if (marker1 != null) {
                        Toast.makeText(context, marker1.getTitle(), Toast.LENGTH_SHORT).show();
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
    }
}
