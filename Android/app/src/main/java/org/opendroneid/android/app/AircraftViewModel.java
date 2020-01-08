/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.app;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.opendroneid.android.data.AircraftObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AircraftViewModel extends ViewModel {
    private final MutableLiveData<Set<AircraftObject>> aircraft = new MutableLiveData<>();
    private final MutableLiveData<AircraftObject> selected = new MutableLiveData<>();

    public AircraftViewModel() {
        Set<AircraftObject> list = new HashSet<>();
        aircraft.postValue(list);
    }

    void setActiveAircraft(AircraftObject object) {
        selected.postValue(object);
    }

    LiveData<AircraftObject> getActiveAircraft() {
        return selected;
    }

    void setAllAircraft(Map<Long, AircraftObject> objects) {
        aircraft.postValue(new HashSet<>(objects.values()));
    }

    LiveData<Set<AircraftObject>> getAllAircraft() {
        return aircraft;
    }

}
