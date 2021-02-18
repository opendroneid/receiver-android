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
    private static int session = 0;
    public static void bumpSession() { session++; }
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private boolean loggingActive = false;

    public LogWriter(File file) throws IOException {
        writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
        ExecutorService exec = Executors.newSingleThreadExecutor();

        Log.i(TAG, "starting logging to " + file);
        exec.submit(() -> {
            try {
                loggingActive = true;
                long last = System.currentTimeMillis();

                // write header
                writer.write(TextUtils.join(",", LogEntry.HEADER));
                writer.write("," + OpenDroneIdParser.BasicId.csvHeader());
                writer.write(OpenDroneIdParser.Location.csvHeader());
                for (int i = 0; i < Constants.MAX_AUTH_DATA_PAGES; i++)
                    writer.write(OpenDroneIdParser.Authentication.csvHeader());
                writer.write(OpenDroneIdParser.SelfID.csvHeader());
                writer.write(OpenDroneIdParser.SystemMsg.csvHeader());
                writer.write(OpenDroneIdParser.OperatorID.csvHeader());
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
                    if (time - last > 1000) {
                        writer.flush();
                        last = time;
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "error writing log", e);
            } finally {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void logBluetooth(int callbackType, ScanResult result,
                             String transportType, StringBuilder csvLog) {
        LogEntry entry = new LogEntry();
        entry.session = session;
        entry.timestamp = result.getTimestampNanos();
        entry.transportType = transportType;
        entry.macAddress = result.getDevice().getAddress();
        entry.callbackType = callbackType;
        entry.rssi = result.getRssi();
        if (result.getScanRecord() != null)
            entry.data = result.getScanRecord().getBytes();
        entry.csvLog = csvLog;
        logQueue.add(entry.toString());
    }

    public void logNaN(Long timeNano, int peerHash, byte[] serviceSpecificInfo,
                       String transportType, StringBuilder csvLog) {
        LogEntry entry = new LogEntry();
        entry.session = session;
        entry.timestamp = timeNano;
        entry.transportType = transportType;
        entry.macAddress = Integer.toString(peerHash);
        entry.callbackType = 0;
        entry.rssi = 0;
        if (serviceSpecificInfo != null)
            entry.data = serviceSpecificInfo;
        entry.csvLog = csvLog;
        logQueue.add(entry.toString());
    }

    public void logBeacon(Long timeNano, android.net.wifi.ScanResult scanResult, byte[] data,
                          String transportType, StringBuilder csvLog) {
        LogEntry entry = new LogEntry();
        entry.session = session;
        entry.timestamp = timeNano;
        entry.transportType = transportType;
        entry.macAddress = scanResult.BSSID;
        entry.callbackType = 0;
        entry.rssi = scanResult.level;
        if (data != null)
            entry.data = data;
        entry.csvLog = csvLog;
        logQueue.add(entry.toString());
    }

    public void close() {
        loggingActive = false;
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

