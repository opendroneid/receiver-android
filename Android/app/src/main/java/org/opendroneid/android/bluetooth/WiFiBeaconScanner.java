/*
 * Copyright (C) 2021 Skydio Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. *
 */

package org.opendroneid.android.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.opendroneid.android.log.LogMessageEntry;
import org.opendroneid.android.log.LogWriter;

public class WiFiBeaconScanner {
    private static final int CIDLen = 3;
    private static final int DriStartByteOffset = 4;
    private static final int ScanTimerInterval = 2;
    private static final int[] DRI_CID = { 0xFA, 0x0B, 0xBC };
    private static final int VendorTypeLen = 1;
    private static final int VendorTypeValue = 0x0D;
    private boolean WiFiScanEnabled = true;
    private final OpenDroneIdDataManager dataManager;
    private LogWriter logger;
    private WifiManager wifiManager;
    Context context;
    int scanSuccess;
    int scanFailed;
    String startTime;
    CountDownTimer countDownTimer;
    boolean beaconScanDebugEnable;

    private static final String TAG = WiFiBeaconScanner.class.getSimpleName();

    public void setLogger(LogWriter logger) { this.logger = logger; }

    public WiFiBeaconScanner(Context context, OpenDroneIdDataManager dataManager, LogWriter logger) {
        this.dataManager = dataManager;
        this.logger = logger;

        this.startTime = getCurrTimeStr();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI)) {
            Toast.makeText(context, "WiFi Scanning is not supported", Toast.LENGTH_LONG).show();
            WiFiScanEnabled = false;
            return;
        }

        this.context = context;
        beaconScanDebugEnable = false;

        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Log.d(TAG, "Turning on Wi-Fi");
            wifiManager.setWifiEnabled(true);
        }
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        BroadcastReceiver myReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onReceive(Context context, Intent intent) {
                handleScanResults(intent);
            }
        };

        context.registerReceiver(myReceiver, filter);
    }

    void processRemoteIdVendorIE(ScanResult scanResult, ByteBuffer buf) {
        if (buf.remaining() < 30)
            return;
        byte[] dri_CID = new byte[CIDLen];
        byte[] arr = new byte[buf.remaining()];
        buf.get(dri_CID, 0, CIDLen);
        byte[] vendorType = new byte[VendorTypeLen];
        buf.get(vendorType);
        if ((dri_CID[0] & 0xFF) == DRI_CID[0] && (dri_CID[1] & 0xFF) == DRI_CID[1] &&
                (dri_CID[2] & 0xFF) == DRI_CID[2] && vendorType[0] == VendorTypeValue) {
            buf.position(DriStartByteOffset);
            buf.get(arr, 0, buf.remaining());
            LogMessageEntry logMessageEntry = new LogMessageEntry();
            long timeNano = SystemClock.elapsedRealtimeNanos();
            String transportType = "Beacon";
            dataManager.receiveDataWiFiBeacon(arr, scanResult.BSSID, scanResult.BSSID.hashCode(),
                    scanResult.level, timeNano, logMessageEntry, transportType);

            StringBuilder csvLog = logMessageEntry.getMessageLogEntry();
            if (logger != null)
                logger.logBeacon(logMessageEntry.getMsgVersion(), timeNano, scanResult, arr, transportType, csvLog);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void handleScanResults(Intent intent) {
        if (wifiManager == null) {
            Toast.makeText(context, "WiFi beacon scanner attach failed.", Toast.LENGTH_LONG).show();
            return;
        }
        boolean freshScanResult = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
        String action = intent.getAction();
        if (freshScanResult && WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            List<ScanResult> wifiList = wifiManager.getScanResults();
            for (ScanResult scanResult : wifiList) {
                try {
                    handleResult(scanResult);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            startScan();
        }
    }

    void handleResult(ScanResult scanResult) throws NoSuchFieldException, IllegalAccessException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            // On earlier Android APIs, the information element field is hidden.
            // Use reflection to access it.
            Object value = ScanResult.class.getField("informationElements").get(scanResult);
            ScanResult.InformationElement[] elements = (ScanResult.InformationElement[]) value;
            if (elements == null)
                return;
            for (ScanResult.InformationElement element : elements) {
                if (element == null)
                    continue;
                Object valueId = element.getClass().getField("id").get(element);
                if (valueId == null)
                    continue;
                int id = (int) valueId;
                if (id == 221) {
                    Object valueBytes = element.getClass().getField("bytes").get(element);
                    if (valueBytes == null)
                        continue;
                    ByteBuffer buf = ByteBuffer.wrap(((byte[]) valueBytes)).asReadOnlyBuffer();
                    processRemoteIdVendorIE(scanResult, buf);
                }
            }
        } else {
            for (ScanResult.InformationElement element : scanResult.getInformationElements()) {
                if (element != null && element.getId() == 221) {
                    ByteBuffer buf = element.getBytes();
                    processRemoteIdVendorIE(scanResult, buf);
                }
            }
        }
    }

    public void startScan() {
        if (!WiFiScanEnabled) {
            return;
        }
        boolean ret = wifiManager.startScan();
        if (ret) {
            scanSuccess++;
        } else {
            scanFailed++;
        }
        Log.d(TAG, "start_scan:" + ret);
        printScanStats(ret);
    }

    public void stopScan() {
        if (!WiFiScanEnabled) {
            return;
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        Log.d(TAG, "Stopping WiFi Beacon scanning");
    }

    // There are 2 ways to control WiFi scan:
    // Continuous scan: Calls startSCan() from scan completion callback
    // Periodic scan: countdown timer triggers startScan after expiry of the timer.
    // If phone is debug mode and scan throttling is off, scan is triggered from onReceive() callback.
    // But if scan throttling is turned on on the phone (default setting on the phone), then scan throttling kick in.
    // In case of throttling, startScan() fails. We need timer thread to periodically kick off scanning.
    public void startCountDownTimer() {
        countDownTimer = new CountDownTimer(Long.MAX_VALUE, ScanTimerInterval * 1000) {
            // This is called after every ScanTimerInterval sec.
            public void onTick(long millisUntilFinished) {
                startScan();
            }

            public void onFinish() {
            }
        }.start();
    }

    private String getCurrTimeStr() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void printScanStats(boolean ret) {
        StringBuilder sb = new StringBuilder();
        sb.append("Started: ").append(startTime).append(" success: ").append(scanSuccess);
        sb.append(", failed: ").append(scanFailed).append(" curr-time: ");
        sb.append(getCurrTimeStr()).append(", curr-status: ").append(ret);

        Log.d(TAG, sb.toString());

        if (beaconScanDebugEnable) {
            Toast.makeText(context, sb, Toast.LENGTH_LONG).show();
        }
    }

    public void SetBeaconScanDebug(boolean enable) {
        beaconScanDebugEnable = enable;
    }
}
