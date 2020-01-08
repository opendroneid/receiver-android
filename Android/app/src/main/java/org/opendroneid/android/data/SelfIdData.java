/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.data;

import org.opendroneid.android.Constants;

public class SelfIdData extends MessageData {

    private int descriptionType;
    private byte[] operationDescription;

    public SelfIdData() {
        super();
        descriptionType = 0;
        operationDescription = new byte[0];
    }

    public void setDescriptionType(int descriptionType) {
        if (descriptionType < 0)
            descriptionType = 0;
        if (descriptionType > 255)
            descriptionType = 255;
        this.descriptionType = descriptionType;
    }
    public int getDescriptionType() { return descriptionType; }

    public void setOperationDescription(byte[] operationDescription) {
        if (operationDescription.length <= Constants.MAX_STRING_BYTE_SIZE)
            this.operationDescription = operationDescription;
    }
    public byte[] getOperationDescription() { return operationDescription; }
}
