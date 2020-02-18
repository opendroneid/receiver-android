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
    String macAddress;
    int callbackType;
    int rssi;
    byte[] data;
    StringBuilder csvLog;

    final static String[] HEADER = new String[]{
            "session",
            "timestamp (nanos)",
            "mac address",
            "callbackType",
            "rssi",
            "payload"
    };

    static final String DELIM = ",";

    @NonNull
    public String toString() {
        return toString(true);
    }

    String toString(boolean withData) {
        String s = session + DELIM
                + timestamp + DELIM
                + macAddress + DELIM
                + callbackType + DELIM
                + rssi;
        if (withData) {
            // The first byte in the data array contains the length of the subsequent data
            int length = ((int) data[0] & 0xFF) + 1;
            s += DELIM + toHexString(data, length);
        }
        s += DELIM + csvLog;
        return s;
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
            entry.macAddress = fields[2];
            entry.callbackType = Integer.parseInt(fields[3]);
            entry.rssi = Integer.parseInt(fields[4]);
            entry.data = parseHexString(fields[5]);
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
