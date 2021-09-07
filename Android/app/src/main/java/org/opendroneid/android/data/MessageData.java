/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.data;

import android.os.SystemClock;
import org.opendroneid.android.Constants;
import java.sql.Timestamp;
import java.util.Locale;

public class MessageData {
    private int msgCounter = 0;
    private long timestamp = 0;
    private int msgVersion = 0;

    public void setMsgCounter(int msgCounter) { this.msgCounter = msgCounter; }
    int getMsgCounter() { return this.msgCounter; }
    public String getMsgCounterAsString() { return String.format(Locale.US ,"%3d", this.msgCounter); }

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    long getTimestamp() { return timestamp; }
    public String getTimestampAsString() {
        long msSinceEvent = (SystemClock.elapsedRealtimeNanos() - getTimestamp()) / 1000000L;
        long actualTime = System.currentTimeMillis() - msSinceEvent;
        Timestamp time = new Timestamp(actualTime);
        return time.toString();
    }

    public int getMsgVersion() { return msgVersion; }
    public void setMsgVersion(int msgVersion) { this.msgVersion = msgVersion; }
    public String getMsgVersionAsString() { return String.format(Locale.US ,"v.%d", this.msgVersion); }
    public boolean msgVersionUnsupported() { return msgVersion > Constants.MAX_MSG_VERSION; };
}
