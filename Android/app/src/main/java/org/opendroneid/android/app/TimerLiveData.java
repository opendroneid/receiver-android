/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.app;

import androidx.lifecycle.LiveData;
import android.os.Handler;
import android.os.Looper;

/**
 * Live data that updates at a fixed rate, value is set to last timestamp
 */
public class TimerLiveData extends LiveData<Long> {
    private static final int TIMER_EVENT = 1;

    private final int interval;
    private final Handler.Callback callback = message -> {
        tick();
        return true;
    };

    TimerLiveData(int intervalMillis) {
        this.interval = intervalMillis;
    }

    private final Handler timerHandler = new Handler(Looper.getMainLooper(), callback);

    private void tick() {
        postValue(System.currentTimeMillis());
        timerHandler.sendEmptyMessageDelayed(TIMER_EVENT, interval);
    }

    @Override
    protected void onActive() {
        super.onActive();
        tick();
    }

    @Override
    protected void onInactive() {
        timerHandler.removeMessages(TIMER_EVENT);
        super.onInactive();
    }
}