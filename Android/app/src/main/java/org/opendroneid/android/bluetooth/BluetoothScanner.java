/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import org.opendroneid.android.log.LogEntry;
import org.opendroneid.android.log.LogMessageEntry;
import org.opendroneid.android.log.LogWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class BluetoothScanner {
    private static final String TAG = "BluetoothManager";

    private final OpenDroneIdDataManager dataManager;
    private LogWriter logger;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;

    public BluetoothScanner(Context context, OpenDroneIdDataManager dataManager) {
        this.dataManager = dataManager;

        Object object = context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (object == null)
            return;
        bluetoothAdapter = ((android.bluetooth.BluetoothManager) object).getAdapter();
    }

    public void setLogger(LogWriter logger) { this.logger = logger; }

    private static String dumpBytes(byte[] bytes) {
        return LogEntry.toHexString(bytes, bytes.length);
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    private final ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            ScanRecord scanRecord = result.getScanRecord();
            if (scanRecord == null)
                return;
            byte[] bytes = scanRecord.getBytes();

            String addr = result.getDevice().getAddress().substring(0, 8);
            int advertiseFlags = scanRecord.getAdvertiseFlags();
            int rssi = result.getRssi();
            String string = String.format(Locale.US, "scan: addr=%s flags=0x%02X rssi=% d, len=%d",
                    addr, advertiseFlags, rssi, bytes != null ? bytes.length : -1);

            String transportType = "BT4";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bluetoothAdapter.isLeCodedPhySupported()) {
                if (result.getPrimaryPhy() == BluetoothDevice.PHY_LE_CODED)
                    transportType = "BT5";
            }

            LogMessageEntry logMessageEntry = new LogMessageEntry();
            dataManager.receiveDataBluetooth(bytes, result, logMessageEntry, transportType);

            StringBuilder csvLog = logMessageEntry.getMessageLogEntry();
            if (logger != null)
                logger.logBluetooth(callbackType, result, transportType, csvLog);

            Log.w(TAG, "onScanResult: " + string);
            if (bytes != null) {
                Log.w(TAG, "-- bytes: " + dumpBytes(bytes));
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
        }

        @Override
        public void onScanFailed(int errorCode) {
        }
    };

    /* OpenDroneID Bluetooth beacons identify themselves by setting the GAP AD Type to
     * "Service Data - 16-bit UUID" and the value to 0xFFFA	for ASTM International, ASTM Remote ID.
     * https://www.bluetooth.com/specifications/assigned-numbers/generic-access-profile/
     * https://www.bluetooth.com/specifications/assigned-numbers/16-bit-uuids-for-sdos/
     * Vol 3, Part B, Section 2.5.1 of the Bluetooth 5.1 Core Specification
     * The AD Application Code is set to 0x0D = Open Drone ID.
     */
    private static final UUID SERVICE_UUID = UUID.fromString("0000fffa-0000-1000-8000-00805f9b34fb");
    private static final ParcelUuid SERVICE_pUUID = new ParcelUuid(SERVICE_UUID);
    private static final byte[] OPEN_DRONE_ID_AD_CODE = new byte[]{(byte) 0x0D};

    @TargetApi(Build.VERSION_CODES.O)
    public void startScan() {
        if (bluetoothAdapter == null)
            return;

        Log.d(TAG, ">>>> startScan");
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        builder.setServiceData(SERVICE_pUUID, OPEN_DRONE_ID_AD_CODE);
        List<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(builder.build());

        ScanSettings scanSettings = new ScanSettings.Builder()
                                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                                    .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            bluetoothAdapter.isLeCodedPhySupported() &&
            bluetoothAdapter.isLeExtendedAdvertisingSupported()) {
            // Enable scanning also for devices advertising on an LE Coded PHY S2 or S8
            scanSettings = new ScanSettings.Builder()
                           .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                           .setLegacy(false)
                           .setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
                           .build();
        }

        bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
    }

    public void stopScan() {
        if (bluetoothLeScanner != null && scanCallback != null) {
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }
}
