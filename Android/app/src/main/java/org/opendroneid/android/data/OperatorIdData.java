/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.data;

import org.opendroneid.android.Constants;

public class OperatorIdData extends MessageData {

    private int operatorIdType;
    private byte[] operatorId;

    public OperatorIdData() {
        super();
        operatorIdType = 0;
        operatorId = new byte[0];
    }

    public void setOperatorIdType(int operatorIdType) {
        if (operatorIdType < 0)
            operatorIdType = 0;
        if (operatorIdType > 255)
            operatorIdType = 255;
        this.operatorIdType = operatorIdType;
    }
    public int getOperatorIdType() { return operatorIdType; }

    public void setOperatorId(byte[] operatorId) {
        if (operatorId.length <= Constants.MAX_ID_BYTE_SIZE)
            this.operatorId = operatorId;
    }
    public byte[] getOperatorId() { return operatorId; }
    public String getOperatorIdAsString() {
        if (operatorId != null) {
            for (int c : operatorId) {
                if ((c <= 31 || c >= 127) && c != 0) {
                    return "Invalid String";
                }
            }
            return new String(operatorId);
        }
        return "";
    }
}
