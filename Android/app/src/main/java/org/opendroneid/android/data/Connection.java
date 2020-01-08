/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.data;

public class Connection extends MessageData {
    public int rssi;

    public String macAddress;
    public long lastSeen;
    public long firstSeen;

    public Connection() {
        super();
    }
}
