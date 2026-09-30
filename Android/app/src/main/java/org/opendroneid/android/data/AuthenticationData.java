/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.data;

import android.content.res.Resources;

import org.opendroneid.android.Constants;
import org.opendroneid.android.R;

import java.sql.Timestamp;
import java.util.Locale;

public class AuthenticationData extends MessageData {

    private AuthTypeEnum authType;
    private int authDataPage;
    private int authLastPageIndex;
    private int authLength;
    private long authTimestamp;
    private byte[] authData;

    public AuthenticationData() {
        super();
        authType = AuthTypeEnum.None;
        authDataPage = 0;
        authLastPageIndex = 0;
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
        Specific_Authentication(5),
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
        this.authType = switch (authType) {
            case 1 -> AuthTypeEnum.UAS_ID_Signature;
            case 2 -> AuthTypeEnum.Operator_ID_Signature;
            case 3 -> AuthTypeEnum.Message_Set_Signature;
            case 4 -> AuthTypeEnum.Network_Remote_ID;
            case 5 -> AuthTypeEnum.Specific_Authentication;
            case 0xA -> AuthTypeEnum.Private_Use_0xA;
            case 0xB -> AuthTypeEnum.Private_Use_0xB;
            case 0xC -> AuthTypeEnum.Private_Use_0xC;
            case 0xD -> AuthTypeEnum.Private_Use_0xD;
            case 0xE -> AuthTypeEnum.Private_Use_0xE;
            case 0xF -> AuthTypeEnum.Private_Use_0xF;
            default -> AuthTypeEnum.None;
        };
    }

    int getAuthDataPage() { return authDataPage; }
    public void setAuthDataPage(int authDataPage) {
        if (authDataPage < 0)
            authDataPage = 0;
        if (authDataPage > (Constants.MAX_AUTH_DATA_PAGES - 1))
            authDataPage = Constants.MAX_AUTH_DATA_PAGES - 1;
        this.authDataPage = authDataPage;
    }

    int getAuthLastPageIndex() { return authLastPageIndex; }
    public String getAuthLastPageIndexAsString() {
        return String.format(Locale.US,"%d pages", authLastPageIndex);
    }
    public void setAuthLastPageIndex(int authLastPageIndex) {
        if (authLastPageIndex < 0)
            authLastPageIndex = 0;
        if (authLastPageIndex > (Constants.MAX_AUTH_DATA_PAGES - 1))
            authLastPageIndex = Constants.MAX_AUTH_DATA_PAGES - 1;
        this.authLastPageIndex = authLastPageIndex;
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
    public String getAuthTimestampAsString(Resources res) {
        if (authTimestamp == 0)
            return res.getString(R.string.unknown);
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
