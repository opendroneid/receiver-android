/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.app;

import android.Manifest;
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
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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
import org.opendroneid.android.app.dialogs.AboutDialogFragment;
import org.opendroneid.android.app.dialogs.ChangeUrlDialogFragment;
import org.opendroneid.android.app.dialogs.UserDialogFragment;
import org.opendroneid.android.app.dialogs.UserSignInDialogFragment;
import org.opendroneid.android.app.network.manager.UserManager;
import org.opendroneid.android.app.network.models.user.User;
import org.opendroneid.android.bluetooth.BluetoothScanner;
import org.opendroneid.android.bluetooth.OpenDroneIdDataManager;
import org.opendroneid.android.bluetooth.WiFiBeaconScanner;
import org.opendroneid.android.bluetooth.WiFiNaNScanner;
import org.opendroneid.android.data.AircraftObject;
import org.opendroneid.android.log.LogWriter;
import org.opendroneid.android.views.BoxBottomLeftView;
import org.opendroneid.android.views.BoxBottomRightView;
import org.opendroneid.android.views.BoxTopLeftView;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

public class DebugActivity extends AppCompatActivity {
    public static final String SHARED_PREF_NAME = "DebugActivity";
    public static final String SHARED_PREF_ENABLE_LOG = "EnableLog";
    private static final String TAG = DebugActivity.class.getSimpleName();
    public LocationRequest locationRequest;
    public LocationCallback locationCallback;
    public FusedLocationProviderClient mFusedLocationClient;
    BluetoothScanner btScanner;
    WiFiNaNScanner wiFiNaNScanner;
    WiFiBeaconScanner wiFiBeaconScanner;
    OpenDroneIdDataManager dataManager;
    private AircraftViewModel mModel;
    private MenuItem mMenuLogItem;

    private AircraftMapView mMapView;
    private AircraftOsMapView mOsMapView;
    private MapView osvMap;
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
        /* When the flag org.gradle.project.map in gradle.properties is defined to google_map,
           the below code needs to be uncommented:
        if (BuildConfig.USE_GOOGLE_MAPS) {
            menu.findItem(R.id.maptypeHYBRID).setChecked(true); // Configured in AircraftMapView.setMapSettings()
        }*/
        checkBluetoothSupport(menu);
        checkNaNSupport(menu);
        checkWiFiSupport(menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void checkBluetoothSupport(Menu menu) {
        Object object = getSystemService(BLUETOOTH_SERVICE);
        if (object == null) return;
        BluetoothAdapter bluetoothAdapter = ((android.bluetooth.BluetoothManager) object).getAdapter();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bluetoothAdapter.isLeCodedPhySupported()) {
            menu.findItem(R.id.coded_phy).setTitle(getString(R.string.coded_phy_supported));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bluetoothAdapter.isLeExtendedAdvertisingSupported()) {
            menu.findItem(R.id.extended_advertising).setTitle(getString(R.string.ea_supported));
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void checkNaNSupport(Menu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)) {
            menu.findItem(R.id.wifi_nan).setTitle(getString(R.string.nan_supported));
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkWiFiSupport(Menu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            menu.findItem(R.id.wifi_beacon_scan).setTitle(getString(R.string.wifi_beacon_scan_supported));
        }
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
                if (wiFiNaNScanner != null) wiFiNaNScanner.setLogger(logger);
                if (wiFiBeaconScanner != null) wiFiBeaconScanner.setLogger(logger);
            } else {
                if (logger != null) logger.close();
                btScanner.setLogger(null);
                if (wiFiNaNScanner != null) wiFiNaNScanner.setLogger(null);
                if (wiFiBeaconScanner != null) wiFiBeaconScanner.setLogger(null);
            }
            return true;
        } else if (id == R.id.log_location) {
            String message;
            if (getLogEnabled()) message = getString(R.string.Logging_to) + loggerFile;
            else message = getString(R.string.Logging_not_activated);
            showToast(message);
            return true;
        }
        if (BuildConfig.USE_GOOGLE_MAPS) return mMapView.changeMapType(item);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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
            e.printStackTrace();
        }
        btScanner.setLogger(logger);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_debug);
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
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.NEARBY_WIFI_DEVICES}, Constants.REQUEST_NEARBY_WIFI_DEVICES_PERMISSION);
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d(TAG, "onCreate: S version");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onCreate: Requesting BLUETOOTH_SCAN");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, Constants.REQUEST_BLUETOOTH_PERMISSION_SCAN);
                return;
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onCreate: Requesting BLUETOOTH_CONNECT");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, Constants.REQUEST_BLUETOOTH_PERMISSION_CONNECT);
                return;
            }
        }

        setClickListeners();
        finalizeOnCreate();
    }

    private void finalizeOnCreate() {
        Log.d(TAG, "finalizeOnCreate");
        btScanner = new BluetoothScanner(this, dataManager);
        createNewLogfile();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)) {
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
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY).setMinUpdateIntervalMillis(5 * 1000) // 5 seconds
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

    private void setClickListeners() {
        BoxTopLeftView boxTopLeftView = findViewById(R.id.boxTopLeft);
        boxTopLeftView.setHomeIconClickListener(this::navigateToLocation);
        boxTopLeftView.setUserIconClickListener(this::openSignInDialog);

        BoxBottomRightView boxBottomRightView = findViewById(R.id.boxBottomRight);
        boxBottomRightView.setAboutIconClickListener(this::openAboutDialog);

        boxBottomRightView.setLogOutIconClickListener(this::logOutUser);


        BoxBottomLeftView boxBottomLeftView = findViewById(R.id.boxBottomLeft);
        boxBottomLeftView.setUrlClickListener(this::openChangeUrlDialog);

    }

    private void openSignInDialog() throws IOException, ClassNotFoundException {
        UserManager userManager = new UserManager(getApplicationContext());
        String token = "";
        try {
            token = userManager.getToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (token != null && !token.equals("")) {
            User user = userManager.getUser();
            if (user != null) {
                openUserDialog(user);
            }
        } else {
            UserSignInDialogFragment dialog = new UserSignInDialogFragment();
            dialog.show(getSupportFragmentManager(), "UserSignInDialogFragment");
            dialog.setCancelable(false);
        }
    }

    private void navigateToLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    osvMap = findViewById(R.id.map);
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    IGeoPoint geoPoint = new GeoPoint(latitude, longitude);
                    osvMap.getMapCenter();
                    osvMap.getController().animateTo(geoPoint, 18d, 3L);


                } else {
                    showToast("Unable to get current location");
                }
            });
        } else {
            // Request location permission
            requestLocationPermission(Constants.FINE_LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void logOutUser() {
        UserManager userManager = new UserManager(getApplicationContext());
        if (userManager.getToken() == null) {
            return;
        }

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_log_out_prompt, null);
        new AlertDialog.Builder(this, R.style.CustomAlertDialog).setCustomTitle(dialogView).setPositiveButton(R.string.action_confirm, (dialog, whichButton) -> {
            try {
                userManager.deleteToken();
                userManager.deleteUser();
                BoxBottomRightView boxBottomRightView = findViewById(R.id.boxBottomRight);
                boxBottomRightView.invalidate();
                Toast.makeText(getBaseContext(), getString(R.string.success_log_out), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), getString(R.string.error_log_out), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }).setNegativeButton(R.string.action_no, null).show();
    }

    private void openAboutDialog() {
        AboutDialogFragment dialog = new AboutDialogFragment();
        dialog.show(getSupportFragmentManager(), "AboutDialogFragment");
        dialog.setCancelable(true);
    }

    private void openUserDialog(User user) {
        UserDialogFragment dialog = new UserDialogFragment(user);
        dialog.show(getSupportFragmentManager(), "UserDialogFragment");
        dialog.setCancelable(false);
    }

    private void openChangeUrlDialog() {
        UserManager userManager = new UserManager(getApplicationContext());
        if (userManager.getToken() != null) {
            return;
        }
        ChangeUrlDialogFragment dialog = new ChangeUrlDialogFragment();
        dialog.show(getSupportFragmentManager(), "ChangeUrlDialogFragment");
        dialog.setCancelable(false);
    }

    private void initialize() {
        mModel.setAllAircraft(dataManager.getAircraft());
        FrameLayout frameLayout = findViewById(R.id.holder);
        final Observer<Set<AircraftObject>> listObserver = airCrafts -> {
            if (airCrafts == null) return;
            setTitle(String.format(Locale.US, "%d drones", airCrafts.size()));
            if (!airCrafts.isEmpty()) {
                frameLayout.setVisibility(View.VISIBLE);
            } else {
                frameLayout.setVisibility(View.GONE);
            }
        };

        mModel.getAllAircraft().observe(this, listObserver);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            wiFiNaNScanner = new WiFiNaNScanner(this, dataManager, logger);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            wiFiBeaconScanner = new WiFiBeaconScanner(this, dataManager, logger);

        addDeviceList();

        if (BuildConfig.USE_GOOGLE_MAPS) {
            mMapView = (AircraftMapView) getSupportFragmentManager().findFragmentById(R.id.mapView);
            if (mMapView != null) mMapView.setMapSettings();
        } else {
            mOsMapView = (AircraftOsMapView) getSupportFragmentManager().findFragmentById(R.id.mapView);
            if (mOsMapView != null) mOsMapView.setMapSettings();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onMapReady: call request permission");
                    requestLocationPermission(Constants.FINE_LOCATION_PERMISSION_REQUEST_CODE);
                } else {
                    initialize();
                }
            } else {
                Log.e(TAG, "onActivityResult: User declined to enable Bluetooth, exit the app.");
                showToast(getString(R.string.bt_not_enabled_leaving));
                forceStopApp();
            }
        } else if (requestCode == Constants.REQUEST_ENABLE_WIFI) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                Log.e(TAG, "onActivityResult: User declined to enable WiFi, exit the app.");
                showToast(getString(R.string.wifi_not_enabled_leaving));
                forceStopApp();
            }
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
        handler = new Handler();
        runnableCode = () -> {
            for (AircraftObject aircraft : dataManager.aircraft.values()) {
                aircraft.updateShadowBasicId();
                aircraft.connection.setValue(aircraft.connection.getValue());
            }
            handler.postDelayed(runnableCode, 1000);
        };
        handler.post(runnableCode);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mFusedLocationClient != null)
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

            if (btScanner != null) btScanner.startScan();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && wiFiNaNScanner != null)
            wiFiNaNScanner.startScan();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && wiFiBeaconScanner != null)
            wiFiBeaconScanner.startCountDownTimer();

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        if (btScanner != null) btScanner.stopScan();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && wiFiNaNScanner != null)
            wiFiNaNScanner.stopScan();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && wiFiBeaconScanner != null)
            wiFiBeaconScanner.stopScan();

        handler.removeCallbacks(runnableCode);
        if (mFusedLocationClient != null)
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        super.onPause();
    }

    public void requestLocationPermission(int requestCode) {
        Log.d(TAG, "requestLocationPermission: request permission");

        // Location permission has not been granted yet, request it.
        PermissionUtils.requestPermission(this, requestCode, Manifest.permission.ACCESS_FINE_LOCATION, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.FINE_LOCATION_PERMISSION_REQUEST_CODE) {
            Log.d(TAG, "onRequestPermissionsResult: back from request FINE_LOCATION");
            if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
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
            TextView snackTextView = snackView.findViewById(R.id.snackbar_text);
            snackTextView.setMaxLines(5);
            snackbar.show();
        }
    }

    void forceStopApp() {
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (Exception ignored) {
            }
            finish();
        }).start();
    }
}
