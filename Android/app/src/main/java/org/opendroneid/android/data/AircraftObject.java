/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.data;

import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;

import org.opendroneid.android.Constants;

public class AircraftObject {
    final public MutableLiveData<Connection> connection = new MutableLiveData<>();
    final public MutableLiveData<Identification> identification = new MutableLiveData<>();
    final public MutableLiveData<LocationData> location = new MutableLiveData<>();
    final public MutableLiveData<AuthenticationData> authentication = new MutableLiveData<>();
    final public MutableLiveData<SelfIdData> selfid = new MutableLiveData<>();
    final public MutableLiveData<SystemData> system = new MutableLiveData<>();
    final public MutableLiveData<OperatorIdData> operatorid = new MutableLiveData<>();

    private final long macAddress;

    public AircraftObject(long macAddress) {
        this.macAddress = macAddress;
    }
    public long getMacAddress() { return macAddress; }

    public Connection getConnection() { return connection.getValue(); }
    public Identification getIdentification() { return identification.getValue(); }
    public LocationData getLocation() { return location.getValue(); }
    public AuthenticationData getAuthentication() { return authentication.getValue(); }
    public SelfIdData getSelfID() { return selfid.getValue(); }
    public SystemData getSystem() { return system.getValue(); }
    public OperatorIdData getOperatorID() { return operatorid.getValue(); }

    // Non-zero authentication data pages do not contain the following fields. Save them for displaying
    private int authPageCountSave;
    private int authLengthSave;
    private long authTimestampSave;

    // Multiple authentication messages are possible, each transmitting a part of the
    // authentication signature. Collect the data into authDataCombined.
    private byte[] authDataCombined = new byte[Constants.MAX_AUTH_DATA];

    public AuthenticationData combineAuthentication(AuthenticationData newData) {
        AuthenticationData currData = authentication.getValue();
        if (currData == null)
            currData = new AuthenticationData();

        currData.setADCounter(newData.getADCounter());
        currData.setTimestamp(newData.getTimestamp());

        int offset = 0;
        int amount = Constants.MAX_AUTH_PAGE_ZERO_SIZE;
        if (newData.getAuthDataPage() == 0)  {
            authPageCountSave = newData.getAuthPageCount();
            authLengthSave = newData.getAuthLength();
            authTimestampSave = newData.getAuthTimestamp();
        } else {
            offset = Constants.MAX_AUTH_PAGE_ZERO_SIZE + (newData.getAuthDataPage() - 1) * Constants.MAX_AUTH_PAGE_NON_ZERO_SIZE;
            amount = Constants.MAX_AUTH_PAGE_NON_ZERO_SIZE;
        }
        for (int i = offset; i < offset + amount; i++)
            authDataCombined[i] = newData.getAuthData()[i];

        currData.setAuthType(newData.getAuthType());
        currData.setAuthPageCount(authPageCountSave);
        currData.setAuthLength(authLengthSave);
        currData.setAuthTimestamp(authTimestampSave);
        currData.setAuthData(authDataCombined);
        return currData;
    }

    @Override @NonNull
    public String toString() {
        return "AircraftObject{" +
                "macAddress=" + macAddress +
                ", identification=" + identification +
                '}';
    }
}
