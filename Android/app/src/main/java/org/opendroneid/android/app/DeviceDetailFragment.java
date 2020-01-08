/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.app;

import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.opendroneid.android.R;

import java.util.Locale;

public class DeviceDetailFragment extends DialogFragment {
    private TextView receiveTime;
    private TextView conMac;
    private TextView conRssi;
    private TextView conStarted;
    private TextView conLastUpdate;

    private TextView infoLastUpdate;
    private TextView infoType;
    private TextView infoIdType;
    private TextView infoUasId;

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
    private TextView systemFlags;
    private TextView systemLatitude;
    private TextView systemLongitude;
    private TextView systemAreaCount;
    private TextView systemAreaRadius;
    private TextView systemAreaCeiling;
    private TextView systemAreaFloor;

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
        DetailViewModel model = ViewModelProviders.of(getActivity()).get(DetailViewModel.class);

        model.connection.observe(this, connection -> {
            if (connection == null) return;
            conRssi.setText(String.valueOf(connection.rssi));
            conMac.setText(connection.macAddress);
            receiveTime.setText(connection.getTimestampAsString());
            conStarted.setText(String.format(Locale.US,"%s ago", DeviceList.elapsed(connection.firstSeen)));
            conLastUpdate.setText(String.format(Locale.US,"%s ago", DeviceList.elapsed(connection.lastSeen)));
        });

        model.identification.observe(this, identification -> {
            if (identification == null) return;

            receiveTime.setText(identification.getTimestampAsString());
            infoLastUpdate.setText(identification.getADCounterAsString());
            infoType.setText(identification.getUaType().name());
            infoIdType.setText(identification.getIdType().name());
            infoUasId.setText(identification.getUasIdAsString());
        });

        model.location.observe(this, locationData -> {
            if (locationData == null) return;

            receiveTime.setText(locationData.getTimestampAsString());
            posLastUpdate.setText(locationData.getADCounterAsString());
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
        });

        model.authentication.observe(this, authenticationData -> {
            if (authenticationData == null) return;

            receiveTime.setText(authenticationData.getTimestampAsString());
            authLastUpdate.setText(authenticationData.getADCounterAsString());
            authType.setText(authenticationData.getAuthType().name());
            authLength.setText(authenticationData.getAuthLengthAsString());
            authTimestamp.setText(authenticationData.getAuthTimestampAsString());
            authData.setText(authenticationData.getAuthenticationDataAsString());
        });

        model.selfid.observe(this, selfIdData -> {
            if (selfIdData == null) return;

            receiveTime.setText(selfIdData.getTimestampAsString());
            selfIdLastUpdate.setText(selfIdData.getADCounterAsString());
            selfIdType.setText(String.valueOf(selfIdData.getDescriptionType()));
            selfIdDescription.setText(new String(selfIdData.getOperationDescription()));
        });

        model.system.observe(this, systemData -> {
            if (systemData == null) return;

            receiveTime.setText(systemData.getTimestampAsString());
            systemLastUpdate.setText(systemData.getADCounterAsString());
            systemFlags.setText(systemData.getFlags().name());
            systemLatitude.setText(systemData.getOperatorLatitudeAsString());
            systemLongitude.setText(systemData.getOperatorLongitudeAsString());
            systemAreaCount.setText(String.valueOf(systemData.getAreaCount()));
            systemAreaRadius.setText(systemData.getAreaRadiusAsString());
            systemAreaCeiling.setText(systemData.getAreaCeilingAsString());
            systemAreaFloor.setText(systemData.getAreaFloorAsString());
        });

        model.operatorid.observe(this, operatorIdData -> {
            if (operatorIdData == null) return;

            receiveTime.setText(operatorIdData.getTimestampAsString());
            operatorIdLastUpdate.setText(operatorIdData.getADCounterAsString());
            operatorIdType.setText(String.valueOf(operatorIdData.getOperatorIdType()));
            operatorId.setText(new String(operatorIdData.getOperatorId()));
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.aircraft_details, container, false);
        receiveTime = view.findViewById(R.id.receiveTime);
        conMac = view.findViewById(R.id.conMac);
        conRssi = view.findViewById(R.id.conRssi);
        conStarted = view.findViewById(R.id.conStarted);
        conLastUpdate = view.findViewById(R.id.conLastUpdate);

        infoLastUpdate = view.findViewById(R.id.infoLastUpdate);
        infoType = view.findViewById(R.id.infoType);
        infoIdType = view.findViewById(R.id.infoIdType);
        infoUasId = view.findViewById(R.id.infoUasId);

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
        systemFlags = view.findViewById(R.id.systemFlags);
        systemLatitude = view.findViewById(R.id.systemLatitude);
        systemLongitude = view.findViewById(R.id.systemLongitude);
        systemAreaCount = view.findViewById(R.id.systemAreaCount);
        systemAreaRadius = view.findViewById(R.id.systemAreaRadius);
        systemAreaCeiling = view.findViewById(R.id.systemAreaCeiling);
        systemAreaFloor = view.findViewById(R.id.systemAreaFloor);

        operatorIdLastUpdate = view.findViewById(R.id.operatorIdLastUpdate);
        operatorIdType = view.findViewById(R.id.operatorIdType);
        operatorId = view.findViewById(R.id.operatorId);
        return view;
    }
}
