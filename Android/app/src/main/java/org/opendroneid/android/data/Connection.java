/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.data;

import java.util.Locale;

public class Connection extends MessageData {
    public int rssi;
    public String transportType;
    public String macAddress;
    public long lastSeen;
    public long firstSeen;
    public long msgDelta;

    public String getMsgDeltaAsString() {
        if (msgDelta / 1000 == 0)
            return String.format(Locale.US,"%3d ms", msgDelta);
        else {
            double seconds = msgDelta;
            seconds /= 1000;
            return String.format(Locale.US,"%.1f s", seconds);
        }
    }

    public Connection() {
        super();
    }
}
