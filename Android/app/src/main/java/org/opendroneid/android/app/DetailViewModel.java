/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.app;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import org.opendroneid.android.data.AircraftObject;
import org.opendroneid.android.data.Connection;
import org.opendroneid.android.data.Identification;
import org.opendroneid.android.data.LocationData;
import org.opendroneid.android.data.AuthenticationData;
import org.opendroneid.android.data.SelfIdData;
import org.opendroneid.android.data.OperatorIdData;
import org.opendroneid.android.data.SystemData;

import java.util.concurrent.atomic.AtomicInteger;

public class DetailViewModel extends ViewModel {
    private final MutableLiveData<AircraftObject> selected = new MutableLiveData<>();

    private final TimerLiveData timer = new TimerLiveData(500);

    void select(AircraftObject item) {
        selected.setValue(item);
    }

    public LiveData<AircraftObject> getSelected() {
        return selected;
    }

    private static class FrequencyEvent {
    }

    public void foo() {
        final AtomicInteger count = new AtomicInteger(0);

        final MediatorLiveData<FrequencyEvent> frequencyMediator = new MediatorLiveData<>();
        frequencyMediator.addSource(timer, new Observer<Long>() {
            long lastTime = 0;

            @Override
            public void onChanged(Long time) {
                long td = lastTime - time;
                if (td > 1000L) {
                    int c = count.getAndSet(0);
                    frequencyMediator.getValue();

                    long avg =  c / td;
                }
            }
        });
        frequencyMediator.addSource(location, locationData -> count.incrementAndGet());
    }

    final LiveData<Identification> identification = Transformations.switchMap(selected,
            input -> input.identification);

    final LiveData<Connection> connection = Transformations.switchMap(selected,
            input -> input.connection);

    public final LiveData<LocationData> location = Transformations.switchMap(selected,
            input -> input.location);

    final LiveData<AuthenticationData> authentication = Transformations.switchMap(selected,
            input -> input.authentication);

    final LiveData<SelfIdData> selfid = Transformations.switchMap(selected,
            input -> input.selfid);

    public final LiveData<SystemData> system = Transformations.switchMap(selected,
            input -> input.system);

    final LiveData<OperatorIdData> operatorid = Transformations.switchMap(selected,
            input -> input.operatorid);
}
