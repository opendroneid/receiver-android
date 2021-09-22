/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.app;

import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.opendroneid.android.R;
import android.graphics.Color;

import java.util.Locale;

public class DeviceDetailFragment extends DialogFragment {
    private TextView msgVersion;
    private TextView receiveTime;
    private TextView conMac;
    private TextView conRssi;
    private TextView conStarted;
    private TextView conLastUpdate;
    private TextView conMsgDelta;
    private TextView distance;

    private TextView infoLastUpdate1;
    private TextView infoType1;
    private TextView infoIdType1;
    private TextView infoUasId1;

    private TextView infoLastUpdate2;
    private TextView infoType2;
    private TextView infoIdType2;
    private TextView infoUasId2;

    private TextView posLastUpdate;
    private TextView status;
    private TextView heightType;
    private TextView direction;
    private TextView horiSpeed;
    private TextView vertSpeed;
    private TextView lat;
    private TextView lon;
    private TextView altitudePressure;
    private TextView altitudeGeodetic;
    private TextView height;
    private TextView horizontalAccuracy;
    private TextView verticalAccuracy;
    private TextView baroAccuracy;
    private TextView speedAccuracy;
    private TextView timestamp;
    private TextView timeAccuracy;

    private TextView authLastUpdate;
    private TextView authType;
    private TextView authTimestamp;
    private TextView authLength;
    private TextView authData;

    private TextView selfIdLastUpdate;
    private TextView selfIdType;
    private TextView selfIdDescription;

    private TextView systemLastUpdate;
    private TextView operatorLocationType;
    private TextView classificationType;
    private TextView systemLatitude;
    private TextView systemLongitude;
    private TextView systemAreaCount;
    private TextView systemAreaRadius;
    private TextView systemAreaCeiling;
    private TextView systemAreaFloor;
    private TextView category;
    private TextView classValue;
    private TextView systemAltitudeGeo;

    private TextView operatorIdLastUpdate;
    private TextView operatorIdType;
    private TextView operatorId;

    static DeviceDetailFragment newInstance() {
        return new DeviceDetailFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (getActivity() == null)
            return;

        super.onActivityCreated(savedInstanceState);
        DetailViewModel model = new ViewModelProvider(getActivity()).get(DetailViewModel.class);

        model.connection.observe(getViewLifecycleOwner(), connection -> {
            if (connection == null) return;
            String combo = connection.rssi + " dBm, " + connection.transportType;
            conRssi.setText(combo);
            conMac.setText(connection.macAddress);
            msgVersion.setText(connection.getMsgVersionAsString());
            if (connection.msgVersionUnsupported())
                msgVersion.setTextColor(Color.RED);
            else
                msgVersion.setTextColor(Color.GRAY);
            receiveTime.setText(connection.getTimestampAsString());
            conStarted.setText(String.format(Locale.US,"%s ago", DeviceList.elapsed(connection.firstSeen)));
            conLastUpdate.setText(String.format(Locale.US,"%s ago", DeviceList.elapsed(connection.lastSeen)));
            conMsgDelta.setText(connection.getMsgDeltaAsString());
        });

        model.identification1.observe(getViewLifecycleOwner(), identification -> {
            if (identification == null) return;

            receiveTime.setText(identification.getTimestampAsString());
            infoLastUpdate1.setText(identification.getMsgCounterAsString());
            infoType1.setText(identification.getUaType().name());
            infoIdType1.setText(identification.getIdType().name());
            infoUasId1.setText(identification.getUasIdAsString());
        });

        model.identification2.observe(getViewLifecycleOwner(), identification -> {
            if (identification == null) return;

            receiveTime.setText(identification.getTimestampAsString());
            infoLastUpdate2.setText(identification.getMsgCounterAsString());
            infoType2.setText(identification.getUaType().name());
            infoIdType2.setText(identification.getIdType().name());
            infoUasId2.setText(identification.getUasIdAsString());
        });

        model.location.observe(getViewLifecycleOwner(), locationData -> {
            if (locationData == null) return;

            receiveTime.setText(locationData.getTimestampAsString());
            posLastUpdate.setText(locationData.getMsgCounterAsString());
            status.setText(locationData.getStatus().name());
            direction.setText(locationData.getDirectionAsString());
            horiSpeed.setText(locationData.getSpeedHorizontalAsString());
            vertSpeed.setText(locationData.getSpeedVerticalAsString());
            lat.setText(locationData.getLatitudeAsString());
            lon.setText(locationData.getLongitudeAsString());
            altitudePressure.setText(locationData.getAltitudePressureAsString());
            altitudeGeodetic.setText(locationData.getAltitudeGeodeticAsString());
            heightType.setText(locationData.getHeightType().name());
            height.setText(locationData.getHeightAsString());
            horizontalAccuracy.setText(locationData.getHorizontalAccuracyAsString());
            verticalAccuracy.setText(locationData.getVerticalAccuracyAsString(locationData.getVerticalAccuracy()));
            baroAccuracy.setText(locationData.getVerticalAccuracyAsString(locationData.getBaroAccuracy()));
            speedAccuracy.setText(locationData.getSpeedAccuracyAsString());
            timestamp.setText(locationData.getLocationTimestampAsString());
            timeAccuracy.setText(locationData.getTimeAccuracyAsString());
            distance.setText(locationData.getDistanceAsString());
        });

        model.authentication.observe(getViewLifecycleOwner(), authenticationData -> {
            if (authenticationData == null) return;

            receiveTime.setText(authenticationData.getTimestampAsString());
            authLastUpdate.setText(authenticationData.getMsgCounterAsString());
            authType.setText(authenticationData.getAuthType().name());
            authLength.setText(authenticationData.getAuthLengthAsString());
            authTimestamp.setText(authenticationData.getAuthTimestampAsString());
            authData.setText(authenticationData.getAuthenticationDataAsString());
        });

        model.selfid.observe(getViewLifecycleOwner(), selfIdData -> {
            if (selfIdData == null) return;

            receiveTime.setText(selfIdData.getTimestampAsString());
            selfIdLastUpdate.setText(selfIdData.getMsgCounterAsString());
            selfIdType.setText(String.valueOf(selfIdData.getDescriptionType()));
            selfIdDescription.setText(new String(selfIdData.getOperationDescription()));
        });

        model.system.observe(getViewLifecycleOwner(), systemData -> {
            if (systemData == null) return;

            receiveTime.setText(systemData.getTimestampAsString());
            systemLastUpdate.setText(systemData.getMsgCounterAsString());
            operatorLocationType.setText(systemData.getOperatorLocationType().name());
            classificationType.setText(systemData.getclassificationType().name());
            systemLatitude.setText(systemData.getOperatorLatitudeAsString());
            systemLongitude.setText(systemData.getOperatorLongitudeAsString());
            systemAreaCount.setText(String.valueOf(systemData.getAreaCount()));
            systemAreaRadius.setText(systemData.getAreaRadiusAsString());
            systemAreaCeiling.setText(systemData.getAreaCeilingAsString());
            systemAreaFloor.setText(systemData.getAreaFloorAsString());
            category.setText(systemData.getCategory().name());
            classValue.setText(systemData.getClassValue().name());
            systemAltitudeGeo.setText(systemData.getOperatorAltitudeGeoAsString());
        });

        model.operatorid.observe(getViewLifecycleOwner(), operatorIdData -> {
            if (operatorIdData == null) return;

            receiveTime.setText(operatorIdData.getTimestampAsString());
            operatorIdLastUpdate.setText(operatorIdData.getMsgCounterAsString());
            operatorIdType.setText(String.valueOf(operatorIdData.getOperatorIdType()));
            operatorId.setText(new String(operatorIdData.getOperatorId()));
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.aircraft_details, container, false);
        msgVersion = view.findViewById(R.id.msgVersion);
        receiveTime = view.findViewById(R.id.receiveTime);
        conMac = view.findViewById(R.id.conMac);
        conRssi = view.findViewById(R.id.conRssi);
        conStarted = view.findViewById(R.id.conStarted);
        conLastUpdate = view.findViewById(R.id.conLastUpdate);
        conMsgDelta = view.findViewById(R.id.conMsgDelta);
        distance = view.findViewById(R.id.distance);

        infoLastUpdate1 = view.findViewById(R.id.infoLastUpdate1);
        infoType1 = view.findViewById(R.id.infoType1);
        infoIdType1 = view.findViewById(R.id.infoIdType1);
        infoUasId1 = view.findViewById(R.id.infoUasId1);

        infoLastUpdate2 = view.findViewById(R.id.infoLastUpdate2);
        infoType2 = view.findViewById(R.id.infoType2);
        infoIdType2 = view.findViewById(R.id.infoIdType2);
        infoUasId2 = view.findViewById(R.id.infoUasId2);

        posLastUpdate = view.findViewById(R.id.posLastUpdate);
        status = view.findViewById(R.id.status);
        direction = view.findViewById(R.id.direction);
        horiSpeed = view.findViewById(R.id.horiSpeed);
        vertSpeed = view.findViewById(R.id.vertSpeed);
        lat = view.findViewById(R.id.lat);
        lon = view.findViewById(R.id.lon);
        altitudePressure = view.findViewById(R.id.altitudePressure);
        altitudeGeodetic = view.findViewById(R.id.altitudeGeodetic);
        heightType = view.findViewById(R.id.heightType);
        height = view.findViewById(R.id.height);
        horizontalAccuracy = view.findViewById(R.id.horizontalAccuracy);
        verticalAccuracy = view.findViewById(R.id.verticalAccuracy);
        baroAccuracy = view.findViewById(R.id.baroAccuracy);
        speedAccuracy = view.findViewById(R.id.speedAccuracy);
        timestamp = view.findViewById(R.id.timestamp);
        timeAccuracy = view.findViewById(R.id.timeAccuracy);

        authLastUpdate = view.findViewById(R.id.authLastUpdate);
        authType = view.findViewById(R.id.authType);
        authLength = view.findViewById(R.id.authLength);
        authTimestamp = view.findViewById(R.id.authTimestamp);
        authData = view.findViewById(R.id.authData);

        selfIdLastUpdate = view.findViewById(R.id.selfIdLastUpdate);
        selfIdType = view.findViewById(R.id.selfIdType);
        selfIdDescription = view.findViewById(R.id.selfIdDescription);

        systemLastUpdate = view.findViewById(R.id.systemLastUpdate);
        operatorLocationType = view.findViewById(R.id.operatorLocationType);
        classificationType = view.findViewById(R.id.classificationType);
        systemLatitude = view.findViewById(R.id.systemLatitude);
        systemLongitude = view.findViewById(R.id.systemLongitude);
        systemAreaCount = view.findViewById(R.id.systemAreaCount);
        systemAreaRadius = view.findViewById(R.id.systemAreaRadius);
        systemAreaCeiling = view.findViewById(R.id.systemAreaCeiling);
        systemAreaFloor = view.findViewById(R.id.systemAreaFloor);
        category = view.findViewById(R.id.category);
        classValue = view.findViewById(R.id.classValue);
        systemAltitudeGeo = view.findViewById(R.id.systemAltitudeGeo);

        operatorIdLastUpdate = view.findViewById(R.id.operatorIdLastUpdate);
        operatorIdType = view.findViewById(R.id.operatorIdType);
        operatorId = view.findViewById(R.id.operatorId);
        return view;
    }
}
