/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.data;

import android.os.SystemClock;

import java.sql.Timestamp;
import java.util.Locale;

public class MessageData {
    private int adCounter;
    private long timestamp;

    MessageData() {
        adCounter = 0;
        timestamp = 0;
    }

    public void setADCounter(int adCounter) {
        this.adCounter = adCounter;
    }

    int getADCounter() {
        return this.adCounter;
    }

    public String getADCounterAsString() {
        return String.format(Locale.US ,"%3d", this.adCounter);
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    long getTimestamp() {
        return timestamp;
    }

    public String getTimestampAsString() {
        long msSinceEvent = (SystemClock.elapsedRealtimeNanos() - getTimestamp()) / 1000000L;
        long actualTime = System.currentTimeMillis() - msSinceEvent;
        Timestamp time = new Timestamp(actualTime);
        return time.toString();
    }
}
