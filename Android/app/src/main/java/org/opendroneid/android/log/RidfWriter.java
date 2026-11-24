/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.log;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Writes the raw Remote ID frames of a logging session as a RIDF file, next
 * to the CSV log. RIDF is a small binary record format (one record per
 * received frame: transport, receive time, RSSI, transmitter address and the
 * raw message or message pack) that Remote ID analysis tools can read
 * directly, so a capture can be shared without losing the information the
 * CSV columns drop. The byte layout is documented at
 * https://github.com/Mattbusel/remoteid/blob/main/docs/ridf.md
 */
public class RidfWriter {
    private static final String TAG = "RidfWriter";

    // Transport codes, matching the receivers this app implements.
    public static final int TRANSPORT_BT4 = 1;
    public static final int TRANSPORT_BT5 = 2;
    public static final int TRANSPORT_WIFI_BEACON = 3;
    public static final int TRANSPORT_WIFI_NAN = 4;

    private static final int FLAG_RSSI = 0x01;
    private static final int FLAG_ADDRESS = 0x08;

    private static final byte[] FILE_HEADER = {'R', 'I', 'D', 'F', 0, 0, 0, 0};
    private static final int FIXED_SIZE = 36;

    private final BufferedOutputStream out;
    private final byte[] fixed = new byte[FIXED_SIZE];

    public RidfWriter(File file) throws IOException {
        out = new BufferedOutputStream(new FileOutputStream(file));
        out.write(FILE_HEADER);
        Log.i(TAG, "writing RIDF capture to " + file);
    }

    /**
     * Appends one received frame. {@code payload} is the Remote ID message or
     * message pack, header byte first, at most 255 bytes. {@code timeNano} is
     * a monotonic timestamp (SystemClock.elapsedRealtimeNanos() or the BLE
     * scan timestamp); all records of one file must share the same clock.
     * {@code rssi} and {@code address} may be null when the transport does
     * not provide them.
     */
    public synchronized void log(int transport, long timeNano, Integer rssi,
                                 byte[] address, byte[] payload) throws IOException {
        if (payload == null || payload.length == 0 || payload.length > 255)
            return;

        int flags = 0;
        if (rssi != null)
            flags |= FLAG_RSSI;
        if (address != null && address.length == 6)
            flags |= FLAG_ADDRESS;

        int recordLen = FIXED_SIZE - 2 + payload.length;
        put16(0, recordLen);
        fixed[2] = 0; // RIDF version
        fixed[3] = (byte) transport;
        fixed[4] = (byte) flags;
        fixed[5] = 0; // message counter not recorded
        put64(6, timeNano);
        fixed[14] = rssi != null ? rssi.byteValue() : 0;
        fixed[15] = (byte) payload.length;
        for (int i = 0; i < 6; i++)
            fixed[16 + i] = (flags & FLAG_ADDRESS) != 0 ? address[i] : 0;
        put16(22, 0); // receiver id
        put32(24, 0); // receiver latitude
        put32(28, 0); // receiver longitude
        put32(32, 0); // receiver altitude

        out.write(fixed);
        out.write(payload);
    }

    public synchronized void flush() throws IOException {
        out.flush();
    }

    public synchronized void close() {
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            Log.e(TAG, "close failed", e);
        }
    }

    /** Parses "AA:BB:CC:DD:EE:FF" into 6 bytes in text order; null on failure. */
    public static byte[] parseMac(String mac) {
        if (mac == null)
            return null;
        String[] parts = mac.split(":");
        if (parts.length != 6)
            return null;
        try {
            byte[] out = new byte[6];
            for (int i = 0; i < 6; i++)
                out[i] = (byte) Integer.parseInt(parts[i], 16);
            return out;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // The ODID payload starts after the AD structure header of the service
    // data advertisement; mirrors the offset used by the parser.
    public static byte[] odidPayload(byte[] data, int offset) {
        if (data == null || data.length <= offset)
            return null;
        byte[] payload = new byte[data.length - offset];
        System.arraycopy(data, offset, payload, 0, payload.length);
        return payload;
    }

    private void put16(int at, int v) {
        fixed[at] = (byte) v;
        fixed[at + 1] = (byte) (v >> 8);
    }

    private void put32(int at, int v) {
        fixed[at] = (byte) v;
        fixed[at + 1] = (byte) (v >> 8);
        fixed[at + 2] = (byte) (v >> 16);
        fixed[at + 3] = (byte) (v >> 24);
    }

    private void put64(int at, long v) {
        for (int i = 0; i < 8; i++)
            fixed[at + i] = (byte) (v >> (8 * i));
    }
}
