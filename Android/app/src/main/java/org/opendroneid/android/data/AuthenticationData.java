/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.data;

import org.opendroneid.android.Constants;

import java.sql.Timestamp;
import java.util.Locale;

public class AuthenticationData extends MessageData {

    private AuthTypeEnum authType;
    private int authDataPage;
    private int authPageCount;
    private int authLength;
    private long authTimestamp;
    private byte[] authData;

    public AuthenticationData() {
        super();
        authType = AuthTypeEnum.None;
        authDataPage = 0;
        authPageCount = 0;
        authLength = 0;
        authTimestamp = 0;
        authData = new byte[0];
    }

    public enum AuthTypeEnum {
        None(0),
        UAS_ID_Signature(1),
        Operator_ID_Signature(2),
        Message_Set_Signature(3),
        Network_Remote_ID(4),
        Private_Use_0xA(0xA),
        Private_Use_0xB(0xB),
        Private_Use_0xC(0xC),
        Private_Use_0xD(0xD),
        Private_Use_0xE(0xE),
        Private_Use_0xF(0xF);

        AuthTypeEnum(int id) { this.id = id; }
        public final int id;
    }

    public AuthTypeEnum getAuthType() { return authType; }
    void setAuthType(AuthTypeEnum authType) { this.authType = authType; }
    public void setAuthType(int authType) {
        switch(authType) {
            case 1: this.authType = AuthTypeEnum.UAS_ID_Signature; break;
            case 2: this.authType = AuthTypeEnum.Operator_ID_Signature; break;
            case 3: this.authType = AuthTypeEnum.Message_Set_Signature; break;
            case 4: this.authType = AuthTypeEnum.Network_Remote_ID; break;
            case 0xA: this.authType = AuthTypeEnum.Private_Use_0xA; break;
            case 0xB: this.authType = AuthTypeEnum.Private_Use_0xB; break;
            case 0xC: this.authType = AuthTypeEnum.Private_Use_0xC; break;
            case 0xD: this.authType = AuthTypeEnum.Private_Use_0xD; break;
            case 0xE: this.authType = AuthTypeEnum.Private_Use_0xE; break;
            case 0xF: this.authType = AuthTypeEnum.Private_Use_0xF; break;
            default: this.authType = AuthTypeEnum.None; break;
        }
    }

    int getAuthDataPage() { return authDataPage; }
    public void setAuthDataPage(int authDataPage) {
        if (authDataPage < 0)
            authDataPage = 0;
        if (authDataPage > (Constants.MAX_AUTH_DATA_PAGES - 1))
            authDataPage = Constants.MAX_AUTH_DATA_PAGES - 1;
        this.authDataPage = authDataPage;
    }

    int getAuthPageCount() { return authPageCount; }
    public String getAuthPageCountAsString() {
        return String.format(Locale.US,"%d pages", authPageCount);
    }
    public void setAuthPageCount(int authPageCount) {
        if (authPageCount < 0)
            authPageCount = 0;
        if (authPageCount > Constants.MAX_AUTH_DATA_PAGES)
            authPageCount = Constants.MAX_AUTH_DATA_PAGES;
        this.authPageCount = authPageCount;
    }

    int getAuthLength() { return authLength; }
    public String getAuthLengthAsString() {
        return String.format(Locale.US,"%d bytes", authLength);
    }
    public void setAuthLength(int authLength) {
        if (authLength < 0)
            authLength = 0;
        if (authLength > Constants.MAX_AUTH_DATA)
            authLength = Constants.MAX_AUTH_DATA;
        this.authLength = authLength;
    }

    long getAuthTimestamp() { return authTimestamp; }
    public String getAuthTimestampAsString() {
        if (authTimestamp == 0)
            return "Unknown";
        Timestamp time = new Timestamp((1546300800L + authTimestamp) * 1000);
        return time.toString();
    }
    public void setAuthTimestamp(long authTimestamp) { this.authTimestamp = authTimestamp; }


    byte[] getAuthData() { return authData; }
    public void setAuthData(byte[] authData) { this.authData = authData; }
    public String getAuthenticationDataAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < authLength; i++) {
            sb.append(String.format("%02X ", authData[i]));
        }
        return sb.toString();
    }
}
