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
    private int msgCounter;
    private long timestamp;

    MessageData() {
        msgCounter = 0;
        timestamp = 0;
    }

    public void setMsgCounter(int msgCounter) {
        this.msgCounter = msgCounter;
    }

    int getMsgCounter() {
        return this.msgCounter;
    }

    public String getMsgCounterAsString() {
        return String.format(Locale.US ,"%3d", this.msgCounter);
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
