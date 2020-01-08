/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.bluetooth;

import android.bluetooth.le.ScanResult;

import org.opendroneid.android.Constants;
import org.opendroneid.android.data.AircraftObject;
import org.opendroneid.android.data.Connection;
import org.opendroneid.android.data.Identification;
import org.opendroneid.android.data.AuthenticationData;
import org.opendroneid.android.data.LocationData;
import org.opendroneid.android.data.SelfIdData;
import org.opendroneid.android.data.SystemData;
import org.opendroneid.android.data.OperatorIdData;
import org.opendroneid.android.log.LogMessageEntry;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class OpenDroneIdDataManager {
    private final ConcurrentHashMap<Long, AircraftObject> aircraft = new ConcurrentHashMap<>();

    private final Callback callback;

    public static class Callback {
        public void onNewAircraft(AircraftObject object) {}
    }

    public OpenDroneIdDataManager(Callback callback) {
        this.callback = callback;
    }

    public Map<Long, AircraftObject> getAircraft() {
        return aircraft;
    }

    @SuppressWarnings("unchecked")
    void receiveData(byte[] data, ScanResult result, LogMessageEntry logMessageEntry) {
        OpenDroneIdParser.Message<?> message = OpenDroneIdParser.parseAdvertisingData(data, result.getTimestampNanos(), logMessageEntry);
        if (message == null)
            return;

        // Handle connection
        boolean newAircraft = false;
        String macAddress = result.getDevice().getAddress();
        String macAddressCleaned = macAddress.replace(":", "");
        long macAddressLong = Long.parseLong(macAddressCleaned,16);
        AircraftObject ac = aircraft.get(macAddressLong);
        if (ac == null) {
            ac = createNewAircraft(macAddress, macAddressLong);
            newAircraft = true;
        }
        ac.getConnection().lastSeen = System.currentTimeMillis();
        ac.getConnection().rssi = result.getRssi();
        ac.getConnection().setTimestamp(result.getTimestampNanos());
        ac.connection.setValue(ac.connection.getValue());

        if (newAircraft) {
            aircraft.put(macAddressLong, ac);
            callback.onNewAircraft(ac);
        }

        if (message.header.type == OpenDroneIdParser.Type.MESSAGE_PACK)
            handleMessagePack(ac, (OpenDroneIdParser.Message<OpenDroneIdParser.MessagePack>) message, result.getTimestampNanos(), logMessageEntry, message.adCounter);
        else
            handleMessages(ac, message);
    }

    @SuppressWarnings("unchecked")
    private void handleMessages(AircraftObject ac, OpenDroneIdParser.Message<?> message) {
        switch (message.header.type) {
            case BASIC_ID:
                handleBasicId(ac, (OpenDroneIdParser.Message<OpenDroneIdParser.BasicId>) message);
                break;
            case LOCATION:
                handleLocation(ac, (OpenDroneIdParser.Message<OpenDroneIdParser.Location>) message);
                break;
            case AUTH:
                handleAuthentication(ac, (OpenDroneIdParser.Message<OpenDroneIdParser.Authentication>) message);
                break;
            case SELFID:
                handleSelfID(ac, (OpenDroneIdParser.Message<OpenDroneIdParser.SelfID>) message);
                break;
            case SYSTEM:
                handleSystem(ac, (OpenDroneIdParser.Message<OpenDroneIdParser.SystemMsg>) message);
                break;
            case OPERATOR_ID:
                handleOperatorID(ac, (OpenDroneIdParser.Message<OpenDroneIdParser.OperatorID>) message);
                break;
        }
    }

    private AircraftObject createNewAircraft(String macAddress, long macAddressLong) {
        AircraftObject ac = new AircraftObject(macAddressLong);
        Connection connection = new Connection();
        connection.firstSeen = System.currentTimeMillis();
        connection.macAddress = macAddress;
        ac.connection.setValue(connection);

        ac.identification.setValue(new Identification());
        ac.location.setValue(new LocationData());
        ac.authentication.setValue(new AuthenticationData());
        ac.selfid.setValue(new SelfIdData());
        ac.system.setValue(new SystemData());
        ac.operatorid.setValue(new OperatorIdData());
        return ac;
    }

    private void handleBasicId(AircraftObject ac, OpenDroneIdParser.Message<OpenDroneIdParser.BasicId> message) {
        OpenDroneIdParser.BasicId raw = message.payload;
        Identification data = new Identification();
        data.setADCounter(message.adCounter);
        data.setTimestamp(message.timestamp);

        data.setUaType(raw.uaType);
        data.setIdType(raw.idType);
        data.setUasId(raw.uasId);
        ac.identification.postValue(data);
    }

    private void handleLocation(AircraftObject ac, OpenDroneIdParser.Message<OpenDroneIdParser.Location> message) {
        OpenDroneIdParser.Location raw = message.payload;
        LocationData data = new LocationData();
        data.setADCounter(message.adCounter);
        data.setTimestamp(message.timestamp);

        data.setStatus(raw.status);
        data.setHeightType(raw.heightType);
        data.setDirection(raw.getDirection());
        data.setSpeedHorizontal(raw.getSpeedHori());
        data.setSpeedVertical(raw.getSpeedVert());
        data.setLatitude(raw.getLatitude());
        data.setLongitude(raw.getLongitude());
        data.setAltitudePressure(raw.getAltitudePressure());
        data.setAltitudeGeodetic(raw.getAltitudeGeodetic());
        data.setHeight(raw.getHeight());
        data.setHorizontalAccuracy(raw.horizontalAccuracy);
        data.setVerticalAccuracy(raw.verticalAccuracy);
        data.setBaroAccuracy(raw.baroAccuracy);
        data.setSpeedAccuracy(raw.speedAccuracy);
        data.setLocationTimestamp(raw.timestamp);
        data.setTimeAccuracy(raw.getTimeAccuracy());
        ac.location.postValue(data);
    }

    private void handleAuthentication(AircraftObject ac, OpenDroneIdParser.Message<OpenDroneIdParser.Authentication> message) {
        OpenDroneIdParser.Authentication raw = message.payload;
        AuthenticationData data = new AuthenticationData();
        data.setADCounter(message.adCounter);
        data.setTimestamp(message.timestamp);

        data.setAuthType(raw.authType);
        data.setAuthDataPage(raw.authDataPage);
        if (raw.authDataPage == 0) {
            data.setAuthPageCount(raw.authPageCount);
            data.setAuthLength(raw.authLength);
            data.setAuthTimestamp(raw.authTimestamp);
        }
        data.setAuthData(raw.authData);
        ac.authentication.postValue(ac.combineAuthentication(data));
    }

    private void handleSelfID(AircraftObject ac, OpenDroneIdParser.Message<OpenDroneIdParser.SelfID> message) {
        OpenDroneIdParser.SelfID raw = message.payload;
        SelfIdData data = new SelfIdData();
        data.setADCounter(message.adCounter);
        data.setTimestamp(message.timestamp);

        data.setDescriptionType(raw.descriptionType);
        data.setOperationDescription(raw.operationDescription);
        ac.selfid.postValue(data);
    }

    private void handleSystem(AircraftObject ac, OpenDroneIdParser.Message<OpenDroneIdParser.SystemMsg> message) {
        OpenDroneIdParser.SystemMsg raw = message.payload;
        SystemData data = new SystemData();
        data.setADCounter(message.adCounter);
        data.setTimestamp(message.timestamp);

        data.setFlags(raw.flags);
        data.setOperatorLatitude(raw.getLatitude());
        data.setOperatorLongitude(raw.getLongitude());
        data.setAreaCount(raw.areaCount);
        data.setAreaRadius(raw.getAreaRadius());
        data.setAreaCeiling(raw.getAreaCeiling());
        data.setAreaFloor(raw.getAreaFloor());
        ac.system.postValue(data);
    }

    private void handleOperatorID(AircraftObject ac, OpenDroneIdParser.Message<OpenDroneIdParser.OperatorID> message) {
        OpenDroneIdParser.OperatorID raw = message.payload;
        OperatorIdData data = new OperatorIdData();
        data.setADCounter(message.adCounter);
        data.setTimestamp(message.timestamp);

        data.setOperatorIdType(raw.operatorIdType);
        data.setOperatorId(raw.operatorId);
        ac.operatorid.postValue(data);
    }

    private void handleMessagePack(AircraftObject ac, OpenDroneIdParser.Message<OpenDroneIdParser.MessagePack> message, long timestamp, LogMessageEntry logMessageEntry, int adCounter) {
        OpenDroneIdParser.MessagePack raw = message.payload;
        if (raw == null)
            return;

        if (raw.messageSize != Constants.MAX_MESSAGE_SIZE ||
            raw.messagesInPack <= 0 ||
            raw.messagesInPack > Constants.MAX_MESSAGES_IN_PACK)
            return;

        for (int i = 0; i < raw.messagesInPack; i++) {
            int offset = i*raw.messageSize;
            byte[] data = Arrays.copyOfRange(raw.messages, offset, offset + raw.messageSize);
            OpenDroneIdParser.Message<?> subMessage = OpenDroneIdParser.parseMessage(data, 0, timestamp, logMessageEntry, adCounter);
            if (subMessage == null)
                return;

            handleMessages(ac, subMessage);
        }
    }
}
