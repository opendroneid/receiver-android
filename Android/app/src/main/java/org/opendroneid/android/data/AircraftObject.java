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
import org.opendroneid.android.bluetooth.Gb46750Parser;

import java.util.ArrayList;

public class AircraftObject {
    final public MutableLiveData<Connection> connection = new MutableLiveData<>();
    final public MutableLiveData<Identification> identification1 = new MutableLiveData<>();
    final public MutableLiveData<Identification> identification2 = new MutableLiveData<>();
    final public MutableLiveData<Identification> id1Shadow = new MutableLiveData<>();
    final public MutableLiveData<Identification> id2Shadow = new MutableLiveData<>();
    final public MutableLiveData<LocationData> location = new MutableLiveData<>();
    final public MutableLiveData<AuthenticationData> authentication = new MutableLiveData<>();
    final public MutableLiveData<SelfIdData> selfid = new MutableLiveData<>();
    final public MutableLiveData<SystemData> system = new MutableLiveData<>();
    final public MutableLiveData<OperatorIdData> operatorid = new MutableLiveData<>();

    // 最新一条 GB 46750-2025 国标报文（无则为 null）。
    final public MutableLiveData<Gb46750Parser.Gb46750Message> gbMessage = new MutableLiveData<>();

    // 已接收报文滚动日志（ASTM + GB），最多保留 MAX_MESSAGE_LOG 条，供"报文"按钮弹窗展示。
    final public ArrayList<MsgLogEntry> messageLog = new ArrayList<>();
    private static final int MAX_MESSAGE_LOG = 50;
    private final Object messageLogLock = new Object();

    private final long macAddress;

    public AircraftObject(long macAddress) {
        this.macAddress = macAddress;
    }
    public long getMacAddress() { return macAddress; }

    public Connection getConnection() { return connection.getValue(); }
    public Identification getIdentification1() { return identification1.getValue(); }
    public Identification getIdentification2() { return identification2.getValue(); }
    public LocationData getLocation() { return location.getValue(); }
    public AuthenticationData getAuthentication() { return authentication.getValue(); }
    public SelfIdData getSelfID() { return selfid.getValue(); }
    public SystemData getSystem() { return system.getValue(); }
    public OperatorIdData getOperatorID() { return operatorid.getValue(); }
    public Gb46750Parser.Gb46750Message getGbMessage() { return gbMessage.getValue(); }

    /** 追加一条报文记录（线程安全，滚动裁剪到 MAX_MESSAGE_LOG 条）。 */
    public void appendMessage(MsgLogEntry entry) {
        if (entry == null)
            return;
        synchronized (messageLogLock) {
            messageLog.add(entry);
            while (messageLog.size() > MAX_MESSAGE_LOG)
                messageLog.remove(0);
        }
    }

    /** 返回当前报文日志的快照副本（线程安全）。 */
    public ArrayList<MsgLogEntry> getMessageLogSnapshot() {
        synchronized (messageLogLock) {
            return new ArrayList<>(messageLog);
        }
    }

    // Non-zero authentication data pages do not contain the following fields. Save them for displaying
    private int authLastPageIndexSave;
    private int authLengthSave;
    private long authTimestampSave;

    // Multiple authentication messages are possible, each transmitting a part of the
    // authentication signature. Collect the data into authDataCombined.
    private final byte[] authDataCombined = new byte[Constants.MAX_AUTH_DATA];

    public AuthenticationData combineAuthentication(AuthenticationData newData) {
        AuthenticationData currData = authentication.getValue();
        if (currData == null)
            currData = new AuthenticationData();

        currData.setMsgCounter(newData.getMsgCounter());
        currData.setTimestamp(newData.getTimestamp());
        currData.setMsgVersion(newData.getMsgVersion());

        int offset = 0;
        int amount = Constants.MAX_AUTH_PAGE_ZERO_SIZE;
        if (newData.getAuthDataPage() == 0)  {
            authLastPageIndexSave = newData.getAuthLastPageIndex();
            authLengthSave = newData.getAuthLength();
            authTimestampSave = newData.getAuthTimestamp();
        } else {
            offset = Constants.MAX_AUTH_PAGE_ZERO_SIZE + (newData.getAuthDataPage() - 1) * Constants.MAX_AUTH_PAGE_NON_ZERO_SIZE;
            amount = Constants.MAX_AUTH_PAGE_NON_ZERO_SIZE;
        }
        for (int i = offset; i < offset + amount; i++)
            authDataCombined[i] = newData.getAuthData()[i];

        currData.setAuthType(newData.getAuthType());
        currData.setAuthLastPageIndex(authLastPageIndexSave);
        currData.setAuthLength(authLengthSave);
        currData.setAuthTimestamp(authTimestampSave);
        currData.setAuthData(authDataCombined);
        return currData;
    }

    private int idToShow = 0;

    // When two different BasicId messages have been received, use this function to force a periodic
    // swap between their uasId in the list view. It is assumed this is called once per second.
    // The change logic is slowed down to once per three seconds.
    public void updateShadowBasicId() {
        switch (idToShow) {
            case 0 -> {
                id1Shadow.setValue(identification1.getValue());
                idToShow++;
            }
            case 3 -> {
                Identification id2 = identification2.getValue();
                if (id2 != null && id2.getIdType() != Identification.IdTypeEnum.None)
                    id2Shadow.setValue(identification2.getValue());
                idToShow++;
            }
            case 6 -> idToShow = 0;
            default -> idToShow++;
        }
    }

    @Override @NonNull
    public String toString() {
        return "AircraftObject{" +
                "macAddress=" + macAddress +
                ", identification1=" + identification1 +
                ", identification2=" + identification2 +
                '}';
    }
}
