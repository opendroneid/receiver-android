/*
 * Copyright (C) 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * 一条已接收报文（ASTM F3411 或 GB 46750-2025）的日志记录。
 * 用于"报文"按钮弹窗中按时间顺序展示每架飞机发出的全部报文。
 */
package org.opendroneid.android.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MsgLogEntry {
    /** 接收时刻（System.currentTimeMillis()）。 */
    public final long timeMillis;
    /** 传输类型：BT4 / WiFi / NAN 等。 */
    public final String transportType;
    /** 报文类型标签：如 BASIC_ID / LOCATION / SYSTEM 或 GB46750 V1.0。 */
    public final String messageType;
    /** 原始字节（十六进制，空格分隔）。 */
    public final String rawHex;
    /** 解码后的可读摘要。 */
    public final String summary;
    /** 是否为 GB 46750-2025 国标报文。 */
    public final boolean isGb;

    private static final DateFormat TIME_FMT = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    public MsgLogEntry(String transportType, String messageType, byte[] raw,
                       String summary, long timeMillis) {
        this.transportType = transportType;
        this.messageType = messageType;
        this.rawHex = toHex(raw);
        this.summary = (summary == null) ? "" : summary;
        this.timeMillis = timeMillis;
        this.isGb = messageType != null && messageType.startsWith("GB46750");
    }

    /** 格式化的接收时间（HH:mm:ss.SSS）。 */
    public String getTimeText() {
        return TIME_FMT.format(new Date(timeMillis));
    }

    private static String toHex(byte[] b) {
        if (b == null || b.length == 0)
            return "";
        StringBuilder sb = new StringBuilder(b.length * 3);
        for (byte x : b)
            sb.append(String.format(Locale.US, "%02X ", x & 0xFF));
        return sb.toString().trim();
    }
}
