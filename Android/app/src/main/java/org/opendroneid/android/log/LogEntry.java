/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.log;

import androidx.annotation.NonNull;

public class LogEntry {
    int session;
    long timestamp;
    String transportType;
    String macAddress;
    int msgVersion;
    int rssi;
    byte[] data;
    StringBuilder csvLog;

    final static String[] HEADER = new String[]{
            "session",
            "timestamp (nanos)",
            "transportType",
            "macAddress",
            "msgVersion",
            "rssi",
            "payload"
    };

    static final String DELIM = ",";

    @NonNull
    public String toString() {
        return session + DELIM
                + timestamp + DELIM
                + transportType + DELIM
                + macAddress + DELIM
                + msgVersion + DELIM
                + rssi + DELIM
                + toHexString(data, data.length) + DELIM
                + csvLog;
    }

    static LogEntry fromString(String line) {
        String[] fields = line.split("\\s*[,]\\s*");
        if (fields.length < 6) {
            return null;
        }

        try {
            LogEntry entry = new LogEntry();
            entry.session = Integer.parseInt(fields[0]);
            entry.timestamp = Long.parseLong(fields[1]);
            entry.transportType = fields[2];
            entry.macAddress = fields[3];
            entry.msgVersion = Integer.parseInt(fields[4]);
            entry.rssi = Integer.parseInt(fields[5]);
            entry.data = parseHexString(fields[6]);
            return entry;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toHexString(byte[] bytes, int len) {
        StringBuilder sb = new StringBuilder((len) * 3);
        int i = 0;
        for (byte b : bytes) {
            i++;
            if (i > len)
                break;
            int val = b & 0xFF;
            sb.append(String.format("%02X ", val));
        }
        return sb.toString();
    }

    private static byte[] parseHexString(String hexString) {
        String[] byteStrings = hexString.split("\\s+");
        byte[] bytes = new byte[byteStrings.length];
        for (int i = 0; i < byteStrings.length; i++) {
            bytes[i] = (byte) Integer.parseInt(byteStrings[i], 16);
        }
        return bytes;
    }

}
