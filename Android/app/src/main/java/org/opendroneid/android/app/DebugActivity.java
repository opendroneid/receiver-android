/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.app;

import android.Manifest;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.annotation.TargetApi;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

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

    private static final String TAG = DebugActivity.class.getSimpleName();

    public static final String SHARED_PREF_NAME = "DebugActivity";
    public static final String SHARED_PREF_ENABLE_LOG = "EnableLog";
    private MenuItem mMenuLogItem;

    private File loggerFile;
    private LogWriter logger;

    private Handler handler;
    private Runnable runnableCode;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mMenuLogItem = menu.findItem(R.id.menu_log);
        mMenuLogItem.setChecked(getLogEnabled());
        checkBluetoothSupport(menu);
        checkNaNSupport(menu);
        checkWiFiSupport(menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void checkBluetoothSupport(Menu menu) {
        Object object = getSystemService(BLUETOOTH_SERVICE);
        if (object == null)
            return;
        BluetoothAdapter bluetoothAdapter = ((android.bluetooth.BluetoothManager) object).getAdapter();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bluetoothAdapter.isLeCodedPhySupported()) {
            menu.findItem(R.id.coded_phy).setTitle(R.string.coded_phy_supported);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bluetoothAdapter.isLeExtendedAdvertisingSupported()) {
            menu.findItem(R.id.extended_advertising).setTitle(R.string.ea_supported);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void checkNaNSupport(Menu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)) {
            menu.findItem(R.id.wifi_nan).setTitle(R.string.nan_supported);
        }
    }
    @TargetApi(Build.VERSION_CODES.R)
    private void checkWiFiSupport(Menu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            menu.findItem(R.id.wifi_beacon_scan).setTitle(R.string.wifi_beacon_scan_supported);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear:
                dataManager.getAircraft().clear();
                mModel.setAllAircraft(dataManager.getAircraft());
                LogWriter.bumpSession();
                return true;
            case R.id.menu_log:
                boolean enabled = !getLogEnabled();
                setLogEnabled(enabled);
                mMenuLogItem.setChecked(enabled);
                if (enabled) {
                    createNewLogfile();
                    wiFiNaNScanner.setLogger(logger);
                    wiFiBeaconScanner.setLogger(logger);
                } else {
                    logger.close();
                    btScanner.setLogger(null);
                    wiFiNaNScanner.setLogger(null);
                    wiFiBeaconScanner.setLogger(null);
                }
                return true;
            case R.id.log_location:
                if (getLogEnabled())
                    Toast.makeText(getBaseContext(), "Logging to " + loggerFile, Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getBaseContext(), "Logging not activated", Toast.LENGTH_LONG).show();
                return true;
        }
        return false;
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
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "OpenDroneID");
        if (!file.mkdirs()) {
            file = getExternalFilesDir(null);
        }
        String pattern = "yyyy-MM-dd_HH-mm-ss.SSS";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.US);
        return new File(file, "log_" + Build.MODEL + "_" + name + "_" + simpleDateFormat.format(new Date()) + ".csv");
    }

    private void createNewLogfile() {
        loggerFile = getLoggerFileDir(btScanner.getBluetoothAdapter().getName());

        try {
            logger = new LogWriter(loggerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        btScanner.setLogger(logger);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_debug);
        mModel = ViewModelProviders.of(this).get(AircraftViewModel.class);

        dataManager = new OpenDroneIdDataManager(new OpenDroneIdDataManager.Callback() {
            @Override
            public void onNewAircraft(AircraftObject object) {
                mModel.setAllAircraft(dataManager.getAircraft());
            }
        });

        btScanner = new BluetoothScanner(this, dataManager);
        createNewLogfile();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                    startActivityForResult(panelIntent, Constants.REQUEST_ENABLE_WIFI);
                } else {
                    wifiManager.setWifiEnabled(true);
                }
            }
        }

        BluetoothAdapter bluetoothAdapter = btScanner.getBluetoothAdapter();
        if (bluetoothAdapter != null) {
            // Is Bluetooth turned on?
            if (!bluetoothAdapter.isEnabled()) {
                // Prompt user to turn on Bluetooth (logic continues in onActivityResult()).
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
            } else {
                // Check permission
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onMapReady: call request permission");
                    requestLocationPermission(Constants.FINE_LOCATION_PERMISSION_REQUEST_CODE);
                } else {
                    initialize();
                }
            }
        } else {
            // Bluetooth is not supported.
            showErrorText(R.string.bt_not_supported);
        }

        dataManager.mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        dataManager.locationRequest = LocationRequest.create();
        dataManager.locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        dataManager.locationRequest.setInterval(10 * 1000); // 10 seconds
        dataManager.locationRequest.setFastestInterval(5 * 1000); // 5 seconds

        dataManager.locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        dataManager.receiverLocation = location;
                    }
                }
            }
        };
        dataManager.activity = this;
        dataManager.getReceiverLocation();
    }

    private void initialize() {
        mModel.setAllAircraft(dataManager.getAircraft());

        final Observer<Set<AircraftObject>> listObserver = airCrafts -> {
            if (airCrafts == null)
                return;
            setTitle(String.format(Locale.US, "%d drones", airCrafts.size()));
        };

        mModel.getAllAircraft().observe(this, listObserver);

        btScanner.startScan();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            wiFiNaNScanner = new WiFiNaNScanner(this, dataManager, logger);
            wiFiNaNScanner.startScan();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wiFiBeaconScanner = new WiFiBeaconScanner(this, dataManager, logger);
            if (wiFiBeaconScanner != null) {
                wiFiBeaconScanner.startScan();
            }
        }

        addDeviceList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ENABLE_BT) {
                if (resultCode == RESULT_OK) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "onMapReady: call request permission");
                        requestLocationPermission(Constants.FINE_LOCATION_PERMISSION_REQUEST_CODE);
                    } else {
                        initialize();
                    }
                } else {
                    // User declined to enable Bluetooth, exit the app.
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_LONG).show();
                    finish();
                }
        } else if (requestCode == Constants.REQUEST_ENABLE_WIFI) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                Toast.makeText(this, R.string.wifi_not_enabled_leaving, Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void addDeviceList() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.holder, new DeviceList()).commitAllowingStateLoss();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");

        // Wake up the main Activity thread once per second, in order to update time counters
        handler = new Handler();
        runnableCode = () -> {
            for (AircraftObject aircraft : dataManager.aircraft.values()) {
                aircraft.connection.setValue(aircraft.connection.getValue());
            }
            handler.postDelayed(runnableCode, 1000);
        };
        handler.post(runnableCode);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        //btScanner.stopScan();
        handler.removeCallbacks(runnableCode);
        super.onPause();
    }

    private void showErrorText(int messageId) {
        Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
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
        if (requestCode == Constants.FINE_LOCATION_PERMISSION_REQUEST_CODE) {
            Log.d(TAG, "onRequestPermissionsResult: back from request FINE_LOCATION");
            if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                initialize();
            } else {
                showErrorText(R.string.permission_required_toast);
            }

        }
    }
}
