/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.app;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opendroneid.android.Constants;
import org.opendroneid.android.R;
import org.opendroneid.android.data.AircraftObject;
import org.opendroneid.android.data.Connection;
import org.opendroneid.android.data.Identification;
import org.opendroneid.android.data.LocationData;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.select.SelectExtension;
import com.mikepenz.fastadapter.select.SelectExtensionKt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.Locale;

public class DeviceList extends Fragment {
    private static final String TAG = "CustomAdapter";

    private AircraftViewModel mModel;
    private ItemAdapter<ListItem> mItemAdapter;
    private FastAdapter<ListItem> mAdapter;

    private void subscribeToModel(AircraftViewModel model) {
        mModel = model;
        final Observer<Set<AircraftObject>> listObserver = aircraftList -> {
            if (aircraftList == null)
                return;
            Log.d(TAG, "DeviceList onChanged: " + aircraftList);
            List<ListItem> items = new ArrayList<>();
            for (AircraftObject aircraft : aircraftList) {
                items.add(new ListItem(aircraft));
            }
            mItemAdapter.set(items);
        };

        model.getActiveAircraft().observe(getViewLifecycleOwner(), object -> {
            SelectExtension<ListItem> selectExtension = SelectExtensionKt.getSelectExtension(mAdapter);
            if (object == null) {
                selectExtension.deselect();
            } else {
                selectExtension.selectByIdentifier(object.getMacAddress(), false, false);
            }
        });
        mModel.getAllAircraft().observe(getViewLifecycleOwner(), listObserver);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() == null)
            return;
        AircraftViewModel model = new ViewModelProvider(getActivity()).get(AircraftViewModel.class);
        subscribeToModel(model);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.aircraft_list, container, false);
        // Set CustomAdapter as the adapter for RecyclerView.
        // Create the ItemAdapter holding your Items

        mItemAdapter = new ItemAdapter<>();

        // Create the managing FastAdapter, by passing in the itemAdapter
        mAdapter = FastAdapter.with(mItemAdapter);
        mAdapter.setHasStableIds(true);

        SelectExtension<ListItem> selectExtension = SelectExtensionKt.getSelectExtension(mAdapter);
        selectExtension.setSelectable(true);
        selectExtension.setSelectionListener((item, selected) -> {
            Log.d(TAG, "onSelectionChanged: " + item + " selected=" + selected);
            if (selected) {
                if (mModel.getActiveAircraft().getValue() != item.object) {
                    // only set if different
                    mModel.setActiveAircraft(item.object);
                }
            }
        });

        RecyclerView mRecyclerView = viewGroup.findViewById(R.id.device_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.scrollToPosition(0);

        return viewGroup;
    }

    static String elapsed(long start) {
        long millis = System.currentTimeMillis() - start;
        return String.format(Locale.US, "%02d:%02d ",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    private void showDetails(AircraftObject aircraft) {
        if (getActivity() == null)
            return;
        DetailViewModel model = new ViewModelProvider(getActivity()).get(DetailViewModel.class);
        model.select(aircraft);
        DeviceDetailFragment newFragment = DeviceDetailFragment.newInstance();
        newFragment.show(getParentFragmentManager(), "dialog");
    }

    private void showMessages(AircraftObject aircraft) {
        if (getActivity() == null)
            return;
        DetailViewModel model = new ViewModelProvider(getActivity()).get(DetailViewModel.class);
        model.select(aircraft);
        MessagesDialogFragment newFragment = MessagesDialogFragment.newInstance();
        newFragment.show(getParentFragmentManager(), "messages_dialog");
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public class AircraftViewHolder extends FastAdapter.ViewHolder<ListItem> {
        private final TextView textView;
        private final TextView textView2;
        private final TextView rssiView;
        private AircraftObject aircraft;
        private final View view;
        private final ImageView iconImageView;
        private final Drawable droneIcon;

        private final Observer<Connection> connectionObserver;
        private final Observer<LocationData> locationObserver;
        private final Observer<Identification> observer;

        AircraftViewHolder(View v) {
            super(v);
            this.view = v;
            textView = v.findViewById(R.id.aircraftName);
            textView2 = v.findViewById(R.id.aircraftFun);
            rssiView = v.findViewById(R.id.rssi);

            Button button = v.findViewById(R.id.modButton);
            button.setText(R.string.info);
            button.setOnClickListener(v1 -> showDetails(aircraft));

            Button msgButton = v.findViewById(R.id.msgButton);
            msgButton.setText(R.string.message);
            msgButton.setOnClickListener(v1 -> showMessages(aircraft));

            droneIcon = ContextCompat.getDrawable(requireActivity(), R.mipmap.ic_plane_icon);
            iconImageView = v.findViewById(R.id.drone_icon);

            connectionObserver = connection -> {
                if (connection != null)
                    rssiView.setText(String.format(Locale.US, "%s dBm", connection.rssi));
            };
            locationObserver = locationData -> {
                if (locationData != null) {
                    Resources res = getResources();
                    textView2.setText(String.format(Locale.US, "%s over %s, %s, %s away",
                            locationData.getHeightLessPreciseAsString(res),
                            locationData.getHeightType().toString(),
                            locationData.getSpeedHorizontalLessPreciseAsString(res),
                            locationData.getDistanceAsString()));
                }
            };

            observer = identification -> {
                if (identification != null) {
                    Log.w(TAG, "on changed: " + identification.getIdType() + ", " + identification.getUasIdAsString() + ", " + this);
                    setIdText(identification);

                    assert droneIcon != null;
                    droneIcon.setColorFilter(new PorterDuffColorFilter(0xff00ff00, PorterDuff.Mode.MULTIPLY));
                    iconImageView.setImageDrawable(droneIcon);
                }
            };
        }

        private void setIdText(Identification id) {
            if (id.getUasIdAsString().length() > Constants.MAX_ID_BYTE_SIZE)
                textView.setTextSize(9);
            else
                textView.setTextSize(16);
            textView.setText(String.format("%s", id.getUasIdAsString()));
        }

        @Override
        public void bindView(@NonNull ListItem aircraftItem, @NonNull List<? extends Object> payloads) {
            if (getContext() == null)
                return;

            this.aircraft = aircraftItem.object;

            view.setBackground(ContextCompat.getDrawable(getContext(), android.R.drawable.list_selector_background));
            Identification id = aircraft.getIdentification1();
            if (id != null)
                setIdText(id);

            aircraft.connection.observe(DeviceList.this, connectionObserver);
            aircraft.location.observe(DeviceList.this, locationObserver);
            aircraft.id1Shadow.observe(DeviceList.this, observer);
            aircraft.id2Shadow.observe(DeviceList.this, observer);
        }

        @Override
        public void unbindView(@NonNull ListItem aircraftItem) {
            aircraft.id1Shadow.removeObserver(observer);
            aircraft.id2Shadow.removeObserver(observer);
            aircraft.connection.removeObserver(connectionObserver);
            aircraft.location.removeObserver(locationObserver);
        }
    }

    public class ListItem extends AbstractItem<AircraftViewHolder> {

        final AircraftObject object;

        ListItem(AircraftObject object) {
            this.object = object;
        }

        @NonNull
        @Override
        public AircraftViewHolder getViewHolder(@NonNull View v) {
            return new AircraftViewHolder(v);
        }

        @Override
        public long getIdentifier() {
            return object.getMacAddress();
        }

        @Override
        public int getType() {
            return R.id.device_list; // Just a unique ID
        }

        @Override
        public int getLayoutRes() {
            return R.layout.listitem_aircraft;
        }

        @Override @NonNull
        public String toString() {
            return "ListItem{" +
                    "object=" + object +
                    '}';
        }
    }

}
