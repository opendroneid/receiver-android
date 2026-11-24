/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.log;

import android.bluetooth.le.ScanResult;
import android.text.TextUtils;
import android.util.Log;

import org.opendroneid.android.Constants;
import org.opendroneid.android.bluetooth.OpenDroneIdParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class LogWriter {
    private static final String TAG = "LogWriter";
    private final BufferedWriter writer;
    private final ExecutorService exec;
    private static int session = 0;
    public static void bumpSession() { session++; }
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private boolean loggingActive = false;
    private RidfWriter ridf;

    public LogWriter(File file) throws IOException {
        writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
        exec = Executors.newSingleThreadExecutor();

        // Alongside the CSV, keep the raw frames in a RIDF capture so a log
        // session can be analyzed by Remote ID forensics tools without losing
        // data to the CSV columns. Best effort: the CSV log is the primary
        // artifact and must not fail because of the capture.
        try {
            String path = file.getPath();
            if (path.endsWith(".csv"))
                path = path.substring(0, path.length() - 4);
            ridf = new RidfWriter(new File(path + ".ridf"));
        } catch (IOException e) {
            Log.e(TAG, "RIDF capture disabled", e);
            ridf = null;
        }

        Log.i(TAG, "starting logging to " + file);
        exec.submit(() -> {
            try {
                loggingActive = true;
                long lastTime = System.currentTimeMillis();

                // write header
                writer.write(TextUtils.join(",", LogEntry.HEADER));
                writer.write("," + OpenDroneIdParser.BasicId.csvHeader());
                writer.write(OpenDroneIdParser.BasicId.csvHeader());
                writer.write(OpenDroneIdParser.Location.csvHeader());
                writer.write(OpenDroneIdParser.SelfID.csvHeader());
                writer.write(OpenDroneIdParser.SystemMsg.csvHeader());
                writer.write(OpenDroneIdParser.OperatorID.csvHeader());
                for (int i = 0; i < Constants.MAX_AUTH_DATA_PAGES; i++)
                    writer.write(OpenDroneIdParser.Authentication.csvHeader());
                writer.newLine();
                while (loggingActive) {
                    String log;
                    try {
                        log = logQueue.take();
                    } catch (InterruptedException e) {
                        break;
                    }
                    writer.write(log);
                    writer.newLine();
                    long time = System.currentTimeMillis();
                    if (time - lastTime > 1000) {
                        writer.flush();
                        if (ridf != null)
                            ridf.flush();
                        lastTime = time;
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "error writing log", e);
            } finally {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    Log.e(TAG, "error closing writer", e);
                }
            }
        });
    }

    public void logBluetooth(int msgVersion, ScanResult result,
                             String transportType, StringBuilder csvLog) {
        LogEntry entry = new LogEntry();
        entry.session = session;
        entry.timestamp = result.getTimestampNanos();
        entry.transportType = transportType;
        entry.macAddress = result.getDevice().getAddress();
        entry.msgVersion = msgVersion;
        entry.rssi = result.getRssi();
        byte[] scanBytes = null;
        if (result.getScanRecord() != null) {
            scanBytes = result.getScanRecord().getBytes();
            entry.data = scanBytes;
        }
        entry.csvLog = csvLog;
        logQueue.add(entry.toString());
        if (ridf != null) {
            int transport = "BT5".equals(transportType)
                    ? RidfWriter.TRANSPORT_BT5 : RidfWriter.TRANSPORT_BT4;
            // ODID payload starts at offset 6, as parsed by the data manager
            logRidf(transport, result.getTimestampNanos(), result.getRssi(),
                    RidfWriter.parseMac(result.getDevice().getAddress()),
                    RidfWriter.odidPayload(scanBytes, 6));
        }
    }

    public void logNaN(int msgVersion, Long timeNano, int peerHash, byte[] serviceSpecificInfo,
                       String transportType, StringBuilder csvLog) {
        LogEntry entry = new LogEntry();
        entry.session = session;
        entry.timestamp = timeNano;
        entry.transportType = transportType;
        entry.macAddress = "" + peerHash;
        entry.msgVersion = msgVersion;
        entry.rssi = 0;
        if (serviceSpecificInfo != null)
            entry.data = serviceSpecificInfo;
        entry.csvLog = csvLog;
        logQueue.add(entry.toString());
        // NAN discovery exposes neither the peer MAC nor RSSI to apps;
        // ODID payload starts at offset 1, as parsed by the data manager
        if (ridf != null)
            logRidf(RidfWriter.TRANSPORT_WIFI_NAN, timeNano, null, null,
                    RidfWriter.odidPayload(serviceSpecificInfo, 1));
    }

    public void logBeacon(int msgVersion, Long timeNano, android.net.wifi.ScanResult scanResult,
                          byte[] data, String transportType, StringBuilder csvLog) {
        LogEntry entry = new LogEntry();
        entry.session = session;
        entry.timestamp = timeNano;
        entry.transportType = transportType;
        entry.macAddress = scanResult.BSSID;
        entry.msgVersion = msgVersion;
        entry.rssi = scanResult.level;
        if (data != null)
            entry.data = data;
        entry.csvLog = csvLog;
        logQueue.add(entry.toString());
        if (ridf != null)
            logRidf(RidfWriter.TRANSPORT_WIFI_BEACON, timeNano, scanResult.level,
                    RidfWriter.parseMac(scanResult.BSSID),
                    RidfWriter.odidPayload(data, 1));
    }

    private void logRidf(int transport, long timeNano, Integer rssi,
                         byte[] address, byte[] payload) {
        try {
            ridf.log(transport, timeNano, rssi, address, payload);
        } catch (IOException e) {
            Log.e(TAG, "RIDF capture failed, disabling", e);
            ridf = null;
        }
    }

    public void close() {
        if (ridf != null) {
            ridf.close();
            ridf = null;
        }
        loggingActive = false;
        exec.shutdownNow();
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "close failed", e);
        }
    }
}

