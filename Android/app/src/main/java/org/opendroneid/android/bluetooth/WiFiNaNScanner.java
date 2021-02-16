/*
 * Copyright (C) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.bluetooth;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.aware.AttachCallback;
import android.net.wifi.aware.DiscoverySessionCallback;
import android.net.wifi.aware.IdentityChangedListener;
import android.net.wifi.aware.PeerHandle;
import android.net.wifi.aware.SubscribeConfig;
import android.net.wifi.aware.SubscribeDiscoverySession;
import android.net.wifi.aware.WifiAwareManager;
import android.net.wifi.aware.WifiAwareSession;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.opendroneid.android.log.LogMessageEntry;
import org.opendroneid.android.log.LogWriter;

import java.util.Arrays;
import java.util.List;

public class WiFiNaNScanner {

    private final OpenDroneIdDataManager dataManager;
    private LogWriter logger;
    private boolean wifiAwareSupported = false;
    private WifiAwareManager wifiAwareManager;
    private WifiAwareSession wifiAwareSession;
    private Handler handler;
    Context context;
    private static final String TAG = WiFiNaNScanner.class.getSimpleName();

    public void setLogger(LogWriter logger) { this.logger = logger; }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public WiFiNaNScanner(Context context, OpenDroneIdDataManager dataManager, LogWriter logger) {
        this.dataManager = dataManager;
        this.logger = logger;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)) {
            //Toast.makeText(context, "WiFi Aware is not supported", Toast.LENGTH_LONG).show();
            return;
        }
        wifiAwareSupported = true;
        this.context = context;
        handler = new Handler();

        wifiAwareManager = (WifiAwareManager) context.getSystemService(Context.WIFI_AWARE_SERVICE);
        if (wifiAwareManager != null && !wifiAwareManager.isAvailable()) {
            Toast.makeText(context, "WiFi Aware is currently not available. Code to properly handle this must be added.", Toast.LENGTH_LONG).show();
        }

        IntentFilter filter = new IntentFilter(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED);
        BroadcastReceiver myReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (wifiAwareManager.isAvailable()) {
                    //Toast.makeText(context, "WiFi Aware became available.", Toast.LENGTH_LONG).show();
                    startScan();
                } else {
                    Toast.makeText(context, "WiFi Aware was lost. Code to properly handle this must be added.", Toast.LENGTH_LONG).show();
                }
            }
        };
        context.registerReceiver(myReceiver, filter);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private final AttachCallback attachCallback = new AttachCallback() {
        @Override
        public void onAttached(WifiAwareSession session) {
            if (!wifiAwareSupported)
                return;

            wifiAwareSession = session;
            SubscribeConfig config = new SubscribeConfig.Builder()
                    .setServiceName("org.opendroneid.remoteid")
                    .build();

            wifiAwareSession.subscribe(config, new DiscoverySessionCallback() {
                @Override
                public void onSubscribeStarted(@NonNull SubscribeDiscoverySession session) {
                    //Toast.makeText(context, "onSubscribeStarted", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onServiceDiscovered(PeerHandle peerHandle, byte[] serviceSpecificInfo, List<byte[]> matchFilter) {
                    Log.i(TAG, "onServiceDiscovered: " + serviceSpecificInfo.length +": " + Arrays.toString(serviceSpecificInfo));

                    LogMessageEntry logMessageEntry = new LogMessageEntry();
                    long timeNano = SystemClock.elapsedRealtimeNanos();
                    dataManager.receiveDataNaN(serviceSpecificInfo, peerHandle.hashCode(), timeNano, logMessageEntry);

                    StringBuilder csvLog = logMessageEntry.getMessageLogEntry();
                    if (logger != null)
                        logger.logNaN(timeNano, peerHandle.hashCode(), serviceSpecificInfo, csvLog);
                }
            }, null);
        }

        @Override
        public void onAttachFailed() {
            Toast.makeText(context, "wifiAware onAttachFailed. Code to properly handle this must be added.", Toast.LENGTH_LONG).show();
        }
    };

    @TargetApi(Build.VERSION_CODES.O)
    private final IdentityChangedListener identityChangedListener = new IdentityChangedListener() {
        @Override
        public void onIdentityChanged(byte[] mac) {
            /*Byte[] macAddress = new Byte[mac.length];
            int i = 0;
            for (byte b: mac)
                macAddress[i++] = b;
            Toast.makeText(context, "identityChangedListener. MAC: " + Arrays.toString(macAddress), Toast.LENGTH_LONG).show();*/
        }
    };

    @TargetApi(Build.VERSION_CODES.O)
    public void startScan() {
        if (!wifiAwareSupported)
            return;
        //Toast.makeText(context, "WiFi NaN attaching", Toast.LENGTH_LONG).show();
        if (wifiAwareManager.isAvailable())
            wifiAwareManager.attach(attachCallback, identityChangedListener, handler);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void stopScan() {
        if (!wifiAwareSupported)
            return;
        Toast.makeText(context, "WiFi NaN stopping scanning. Code to properly handle this must be added.", Toast.LENGTH_LONG).show();
        if (wifiAwareManager.isAvailable() && wifiAwareSession != null)
            wifiAwareSession.close();
    }
}
