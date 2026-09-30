/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.app;

import android.Manifest;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.snackbar.Snackbar;

import org.opendroneid.android.BuildConfig;
import org.opendroneid.android.Constants;
import org.opendroneid.android.PermissionUtils;
import org.opendroneid.android.R;
import org.opendroneid.android.log.LogWriter;
import org.opendroneid.android.bluetooth.BluetoothScanner;
import org.opendroneid.android.bluetooth.WiFiNaNScanner;
import org.opendroneid.android.bluetooth.WiFiBeaconScanner;
import org.opendroneid.android.bluetooth.OpenDroneIdDataManager;
import org.opendroneid.android.data.AircraftObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

public class DebugActivity extends AppCompatActivity {
    BluetoothScanner btScanner;
    WiFiNaNScanner wiFiNaNScanner;
    WiFiBeaconScanner wiFiBeaconScanner;

    private AircraftViewModel mModel;
    OpenDroneIdDataManager dataManager;

    public LocationRequest locationRequest;
    public LocationCallback locationCallback;
    public FusedLocationProviderClient mFusedLocationClient;

    private static final String TAG = DebugActivity.class.getSimpleName();

    public static final String SHARED_PREF_NAME = "DebugActivity";
    public static final String SHARED_PREF_ENABLE_LOG = "EnableLog";
    private MenuItem mMenuLogItem;

    private AircraftMapView mMapView;

    private File loggerFile;
    private LogWriter logger;

    private Handler handler;
    private Runnable runnableCode;

    private ActivityResultLauncher<Intent> bluetoothEnableLauncher;
    private ActivityResultLauncher<Intent> wifiEnableLauncher;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mMenuLogItem = menu.findItem(R.id.menu_log);
        mMenuLogItem.setChecked(getLogEnabled());
        MenuItem mapTypeItem = menu.findItem(R.id.maptype);
        if (mapTypeItem != null) {
            mapTypeItem.setVisible(BuildConfig.USE_GOOGLE_MAPS);
        }
        if (BuildConfig.USE_GOOGLE_MAPS) {
            MenuItem hybridItem = menu.findItem(R.id.maptypeHYBRID);
            if (hybridItem != null) {
                hybridItem.setChecked(true);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            checkBluetoothSupport(menu);
            checkNaNSupport(menu);
        }
        checkWiFiSupport(menu);
        markCurrentLanguage(menu);
        return true;
    }

    /** Tick the language entry matching the locale currently applied to the app. */
    private void markCurrentLanguage(Menu menu) {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        String tags = locales.isEmpty() ? "" : locales.toLanguageTags();
        int item;
        if (tags.startsWith("zh")) {
            item = R.id.lang_zh;
        } else if (tags.startsWith("ja")) {
            item = R.id.lang_ja;
        } else if (tags.startsWith("fr")) {
            item = R.id.lang_fr;
        } else if (tags.startsWith("en")) {
            item = R.id.lang_en;
        } else {
            item = R.id.lang_system;
        }
        MenuItem menuItem = menu.findItem(item);
        if (menuItem != null) {
            menuItem.setChecked(true);
        }
    }

    /**
     * Switch the app language at runtime. Passing null or an empty tag resets the app
     * to the device (system) language. The choice is stored by AppCompat and restored
     * automatically, so it survives a restart of the application.
     */
    private void setAppLanguage(String languageTag) {
        LocaleListCompat locales = (languageTag == null || languageTag.isEmpty())
                ? LocaleListCompat.getEmptyLocaleList()
                : LocaleListCompat.forLanguageTags(languageTag);
        AppCompatDelegate.setApplicationLocales(locales);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void checkBluetoothSupport(Menu menu) {
        Object object = getSystemService(BLUETOOTH_SERVICE);
        if (object == null)
            return;
        BluetoothAdapter bluetoothAdapter = ((android.bluetooth.BluetoothManager) object).getAdapter();

        if (bluetoothAdapter.isLeCodedPhySupported()) {
            menu.findItem(R.id.coded_phy).setTitle(getString(R.string.coded_phy_supported));
        }
        if (bluetoothAdapter.isLeExtendedAdvertisingSupported()) {
            menu.findItem(R.id.extended_advertising).setTitle(getString(R.string.ea_supported));
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void checkNaNSupport(Menu menu) {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)) {
            menu.findItem(R.id.wifi_nan).setTitle(getString(R.string.nan_supported));
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private void checkWiFiSupport(Menu menu) {
        menu.findItem(R.id.wifi_beacon_scan).setTitle(getString(R.string.wifi_beacon_scan_supported));
    }

    private void showHelpMenu() {
        HelpMenu helpMenu = HelpMenu.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        helpMenu.show(transaction, getString(R.string.Help));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.clear) {
            dataManager.getAircraft().clear();
            mModel.setAllAircraft(dataManager.getAircraft());
            LogWriter.bumpSession();
            return true;
        } else if (id == R.id.help) {
            showHelpMenu();
            return true;
        } else if (id == R.id.menu_log) {
            boolean enabled = !getLogEnabled();
            setLogEnabled(enabled);
            mMenuLogItem.setChecked(enabled);
            if (enabled) {
                createNewLogfile();
                if (wiFiNaNScanner != null)
                    wiFiNaNScanner.setLogger(logger);
                if (wiFiBeaconScanner != null)
                    wiFiBeaconScanner.setLogger(logger);
            } else {
                if (logger != null)
                    logger.close();
                btScanner.setLogger(null);
                if (wiFiNaNScanner != null)
                    wiFiNaNScanner.setLogger(null);
                if (wiFiBeaconScanner != null)
                    wiFiBeaconScanner.setLogger(null);
            }
            return true;
        } else if (id == R.id.log_location) {
            String message;
            if (getLogEnabled())
                message = getString(R.string.Logging_to) + loggerFile;
            else
                message = getString(R.string.Logging_not_activated);
            showToast(message);
            return true;
        } else if (id == R.id.lang_zh) {
            setAppLanguage("zh-CN");
            return true;
        } else if (id == R.id.lang_en) {
            setAppLanguage("en");
            return true;
        } else if (id == R.id.lang_ja) {
            setAppLanguage("ja");
            return true;
        } else if (id == R.id.lang_fr) {
            setAppLanguage("fr");
            return true;
        } else if (id == R.id.lang_system) {
            setAppLanguage(null);
            return true;
        }

        if (BuildConfig.USE_GOOGLE_MAPS && mMapView != null) {
            return mMapView.changeMapType(item);
        }
        return super.onOptionsItemSelected(item);
    }

    boolean getLogEnabled() {
        SharedPreferences pref = getSharedPreferences(SHARED_PREF_NAME, 0);
        return pref.getBoolean(SHARED_PREF_ENABLE_LOG, true);
    }

    void setLogEnabled(boolean enabled) {
        SharedPreferences pref = getSharedPreferences(SHARED_PREF_NAME, 0);
        pref.edit().putBoolean(SHARED_PREF_ENABLE_LOG, enabled).apply();
    }

    private File getLoggerFileDir(String name) {
        File documentsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "OpenDroneID");
        File dir = (documentsDir.exists() || documentsDir.mkdirs()) ? documentsDir : getExternalFilesDir(null);

        String pattern = "yyyy-MM-dd_HH-mm-ss.SSS";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.US);
        return new File(dir, "log_" + Build.MODEL + "_" + name + "_" + simpleDateFormat.format(new Date()) + ".csv");
    }

    private void createNewLogfile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "createNewLogfile:  Did not get BLUETOOTH_SCAN or BLUETOOTH_CONNECT");
                showToast(getString(R.string.nearby_not_granted));
                forceStopApp();
                return;
            }
        }
        loggerFile = getLoggerFileDir(btScanner.getBluetoothAdapter().getName());

        try {
            logger = new LogWriter(loggerFile);
        } catch (IOException e) {
            Log.e(TAG, "createNewLogfile: failed", e);
        }
        btScanner.setLogger(logger);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothEnableLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "bluetoothEnableLauncher: call request permission");
                            requestLocationPermission(Constants.FINE_LOCATION_PERMISSION_REQUEST_CODE);
                        } else {
                            initialize();
                        }
                    } else {
                        Log.e(TAG, "bluetoothEnableLauncher: User declined to enable Bluetooth, exit the app.");
                        showToast(getString(R.string.bt_not_enabled_leaving));
                        forceStopApp();
                    }
                }
        );

        wifiEnableLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if (!wifiManager.isWifiEnabled()) {
                        Log.e(TAG, "wifiEnableLauncher: User declined to enable WiFi, exit the app.");
                        showToast(getString(R.string.wifi_not_enabled_leaving));
                        forceStopApp();
                    }
                }
        );

        setContentView(R.layout.activity_debug);
        TextView attribution = findViewById(R.id.attribution);
        if (BuildConfig.NEEDS_GCJ02) {
            attribution.setText(R.string.copyright_amap);
        } else if ("google_sat".equals(BuildConfig.BASE_MAP)) {
            attribution.setText(R.string.copyright_google_sat);
        }
        mModel = new ViewModelProvider(this).get(AircraftViewModel.class);

        dataManager = new OpenDroneIdDataManager(new OpenDroneIdDataManager.Callback() {
            @Override
            public void onNewAircraft(AircraftObject object) {
                mModel.setAllAircraft(dataManager.getAircraft());
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "onCreate: TIRAMISU");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onCreate: Requesting NEARBY_WIFI_DEVICES");
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.NEARBY_WIFI_DEVICES }, Constants.REQUEST_NEARBY_WIFI_DEVICES_PERMISSION);
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d(TAG, "onCreate: S version");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onCreate: Requesting BLUETOOTH_SCAN");
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.BLUETOOTH_SCAN }, Constants.REQUEST_BLUETOOTH_PERMISSION_SCAN);
                return;
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onCreate: Requesting BLUETOOTH_CONNECT");
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.BLUETOOTH_CONNECT }, Constants.REQUEST_BLUETOOTH_PERMISSION_CONNECT);
                return;
            }
        }

        finalizeOnCreate();
    }

    @SuppressWarnings("deprecation")
    private void finalizeOnCreate() {
        Log.d(TAG, "finalizeOnCreate");
        btScanner = new BluetoothScanner(this, dataManager);
        createNewLogfile();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                    wifiEnableLauncher.launch(panelIntent);
                } else {
                    wifiManager.setWifiEnabled(true);
                }
            }
        }

        BluetoothAdapter bluetoothAdapter = btScanner.getBluetoothAdapter();
        if (bluetoothAdapter != null) {
            // Is Bluetooth turned on?
            if (!bluetoothAdapter.isEnabled()) {
                // Prompt user to turn on Bluetooth (logic continues in result launcher).
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                bluetoothEnableLauncher.launch(enableBtIntent);
            } else {
                // Check permission
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "finalizeOnCreate: Requesting FINE_LOCATION_PERMISSION_REQUEST_CODE permission");
                    requestLocationPermission(Constants.FINE_LOCATION_PERMISSION_REQUEST_CODE);
                } else {
                    initialize();
                }
            }
        } else {
            Log.e(TAG, "finalizeOnCreate: Bluetooth is not supported");
            showToast(getString(R.string.bt_not_supported));
            forceStopApp();
            return;
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest.Builder(10 * 1000) // 10 seconds
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(5 * 1000) // 5 seconds
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        dataManager.receiverLocation = location;
                    }
                }
            }
        };
    }

    private void initialize() {
        mModel.setAllAircraft(dataManager.getAircraft());

        final Observer<Set<AircraftObject>> listObserver = airCrafts -> {
            if (airCrafts == null)
                return;
            setTitle(String.format(Locale.US, "%d drones", airCrafts.size()));
        };

        mModel.getAllAircraft().observe(this, listObserver);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            wiFiNaNScanner = new WiFiNaNScanner(this, dataManager, logger);

        wiFiBeaconScanner = new WiFiBeaconScanner(this, dataManager, logger);

        addDeviceList();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (BuildConfig.USE_GOOGLE_MAPS) {
            ft.replace(R.id.mapView, new AircraftMapView());
        } else {
            ft.replace(R.id.mapView, new AircraftOsMapView());
        }
        ft.commitNow();

        if (BuildConfig.USE_GOOGLE_MAPS) {
            findViewById(R.id.attribution).setVisibility(View.GONE);
            mMapView = (AircraftMapView) getSupportFragmentManager().findFragmentById(R.id.mapView);
            if (mMapView != null)
                mMapView.setMapSettings();
        } else {
            AircraftOsMapView mOsMapView = (AircraftOsMapView) getSupportFragmentManager().findFragmentById(R.id.mapView);
            if (mOsMapView != null)
                mOsMapView.setMapSettings();
        }
    }

    public void addDeviceList() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.holder, new DeviceList()).commitAllowingStateLoss();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");

        // Wake the main Activity thread regularly, to update time counters and other UI elements
        handler = new Handler(Looper.getMainLooper());
        runnableCode = () -> {
            for (AircraftObject aircraft : dataManager.aircraft.values()) {
                aircraft.updateShadowBasicId();
                aircraft.connection.setValue(aircraft.connection.getValue());
            }
            handler.postDelayed(runnableCode, 1000);
        };
        handler.post(runnableCode);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mFusedLocationClient != null)
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

            if (btScanner != null)
                btScanner.startScan();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && wiFiNaNScanner != null)
            wiFiNaNScanner.startScan();
        if (wiFiBeaconScanner != null)
            wiFiBeaconScanner.startCountDownTimer();

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        if (btScanner != null)
            btScanner.stopScan();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && wiFiNaNScanner != null)
            wiFiNaNScanner.stopScan();
        if (wiFiBeaconScanner != null)
            wiFiBeaconScanner.stopScan();

        handler.removeCallbacks(runnableCode);
        if (mFusedLocationClient != null)
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        super.onPause();
    }

    public void requestLocationPermission(int requestCode) {
        Log.d(TAG, "requestLocationPermission: request permission");

        // Location permission has not been granted yet, request it.
        PermissionUtils.requestPermission(this, requestCode,
                Manifest.permission.ACCESS_FINE_LOCATION, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.FINE_LOCATION_PERMISSION_REQUEST_CODE) {
            Log.d(TAG, "onRequestPermissionsResult: back from request FINE_LOCATION");
            if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                initialize();
            } else {
                Log.e(TAG, "onRequestPermissionsResult: Did not get ACCESS_FINE_LOCATION");
                showToast(getString(R.string.permission_required_toast));
                forceStopApp();
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "onRequestPermissionsResult: TIRAMISU");
            if (requestCode == Constants.REQUEST_NEARBY_WIFI_DEVICES_PERMISSION) {
                Log.d(TAG, "onRequestPermissionsResult: REQUEST_NEARBY_WIFI_DEVICES_PERMISSION");
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "onRequestPermissionsResult: Did not get NEARBY_WIFI_DEVICES");
                    showToast(getString(R.string.nearby_not_granted));
                    forceStopApp();
                    return;
                }
                else
                {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                        finalizeOnCreate();
                    } else {
                        Log.d(TAG, "onRequestPermissionsResult: Requesting BLUETOOTH_SCAN");
                        ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.BLUETOOTH_SCAN }, Constants.REQUEST_BLUETOOTH_PERMISSION_SCAN);
                        return;
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d(TAG, "onRequestPermissionsResult: S version");
            if (requestCode == Constants.REQUEST_BLUETOOTH_PERMISSION_SCAN) {
                Log.d(TAG, "onRequestPermissionsResult: REQUEST_BLUETOOTH_PERMISSION_SCAN");
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "onRequestPermissionsResult: Did not get BLUETOOTH_SCAN");
                    showToast(getString(R.string.nearby_not_granted));
                    forceStopApp();
                    return;
                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    finalizeOnCreate();
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: Requesting BLUETOOTH_CONNECT");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, Constants.REQUEST_BLUETOOTH_PERMISSION_CONNECT);
                }
            }
            if (requestCode == Constants.REQUEST_BLUETOOTH_PERMISSION_CONNECT) {
                Log.d(TAG, "onRequestPermissionsResult: REQUEST_BLUETOOTH_PERMISSION_CONNECT");
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "onRequestPermissionsResult: Did not get BLUETOOTH_CONNECT");
                    showToast(getString(R.string.nearby_not_granted));
                    forceStopApp();
                    return;
                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    finalizeOnCreate();
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: Requesting BLUETOOTH_SCAN");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, Constants.REQUEST_BLUETOOTH_PERMISSION_SCAN);
                }
            }
        }
    }

    void showToast(String message) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R)
            Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
        else {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content).getRootView(), message, Snackbar.LENGTH_LONG);
            View snackView = snackbar.getView();
            TextView snackTextView = snackView.findViewById(com.google.android.material.R.id.snackbar_text);
            snackTextView.setMaxLines(5);
            snackbar.show();
        }
    }

    void forceStopApp() {
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            }
            catch (Exception ignored) { }
            finish();
        }).start();
    }
}
