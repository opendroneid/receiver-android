/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.data;

import androidx.annotation.NonNull;

import org.opendroneid.android.Constants;

public class SelfIdData extends MessageData {

    private descriptionTypeEnum descriptionType;
    private byte[] operationDescription;

    public SelfIdData() {
        super();
        descriptionType = descriptionTypeEnum.Text;
        operationDescription = new byte[0];
    }

    public enum descriptionTypeEnum {
        Text,
        Emergency,
        Extended_Status { @NonNull public String toString() { return "Ext_Status"; } },
        Invalid,
    }

    public descriptionTypeEnum getDescriptionType() { return descriptionType; }
    public void setDescriptionType(int descriptionType) {
        switch(descriptionType) {
            case 0: this.descriptionType = descriptionTypeEnum.Text; break;
            case 1: this.descriptionType = descriptionTypeEnum.Emergency; break;
            case 2: this.descriptionType = descriptionTypeEnum.Extended_Status; break;
            default: this.descriptionType = descriptionTypeEnum.Invalid; break;
        }
    }

    public void setOperationDescription(byte[] operationDescription) {
        if (operationDescription.length <= Constants.MAX_STRING_BYTE_SIZE)
            this.operationDescription = operationDescription;
    }
    public byte[] getOperationDescription() { return operationDescription; }
    public String getOperationDescriptionAsString() {
        if (operationDescription != null) {
            for (int c : operationDescription) {
                if ((c <= 31 || c >= 127) && c != 0) {
                    return "Invalid String";
                }
            }
            return new String(operationDescription);
        }
        return "";
    }
}
