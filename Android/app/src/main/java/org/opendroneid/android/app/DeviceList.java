/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.app;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
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

import org.opendroneid.android.R;
import org.opendroneid.android.data.AircraftObject;
import org.opendroneid.android.data.Connection;
import org.opendroneid.android.data.Identification;
import org.opendroneid.android.data.LocationData;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ModelAdapter;
import com.mikepenz.fastadapter.commons.utils.FastAdapterUIUtils;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.select.SelectExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.Locale;

public class DeviceList extends Fragment {
    private static final String TAG = "CustomAdapter";

    private AircraftViewModel mModel;
    private ModelAdapter<AircraftObject, ListItem> mItemAdapter;
    private FastAdapter<ListItem>  mAdapter;

    public static DeviceList newInstance() {
        return new DeviceList();
    }

    private void subscribeToModel(AircraftViewModel model) {
        mModel = model;
        final Observer<Set<AircraftObject>> listObserver = aircraftList -> {
            if (aircraftList == null)
                return;
            Log.d(TAG, "DeviceList onChanged: " + aircraftList);
            mItemAdapter.setNewList(new ArrayList<>(aircraftList));
        };

        model.getActiveAircraft().observe(this, object -> {
            SelectExtension<ListItem> selectExtension = mAdapter.getSelectExtension();
            if (object == null) {
                selectExtension.deselect();
            } else {
                selectExtension.selectByIdentifier(object.getMacAddress(), false, false);
            }
        });
        mModel.getAllAircraft().observe(this, listObserver);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (getActivity() == null)
            return;
        super.onActivityCreated(savedInstanceState);
        AircraftViewModel model = ViewModelProviders.of(getActivity()).get(AircraftViewModel.class);
        subscribeToModel(model);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.aircraft_list, null);
        // Set CustomAdapter as the adapter for RecyclerView.
        // Create the ItemAdapter holding your Items

        mItemAdapter = new ModelAdapter<>(ListItem::new);

        // Create the managing FastAdapter, by passing in the itemAdapter
        mAdapter = FastAdapter.with(mItemAdapter);
        mAdapter.setHasStableIds(true);
        mAdapter.withSelectable(true);

        mAdapter.withSelectionListener((item, selected) -> {
            Log.d(TAG, "onSelectionChanged: "+item + " selected="+selected);
            if (selected && item != null) {
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

    private void showDetails(AircraftObject aircraft){
        if (getActivity() == null || getFragmentManager() == null)
            return;
        DetailViewModel model = ViewModelProviders.of(getActivity()).get(DetailViewModel.class);
        model.select(aircraft);
        DeviceDetailFragment newFragment = DeviceDetailFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public class AircraftViewHolder extends FastAdapter.ViewHolder<ListItem> {
        private final TextView textView;
        private final TextView textView2;
        private final TextView lastSeen;
        private AircraftObject aircraft;
        private View view;
        private ImageView iconImageView;
        private Drawable droneIcon;

        AircraftViewHolder(View v) {
            super(v);
            this.view = v;
            textView = v.findViewById(R.id.aircraftName);
            textView2 = v.findViewById(R.id.aircraftFun);

            Button button = v.findViewById(R.id.modButton);
            button.setText(R.string.info);
            lastSeen = v.findViewById(R.id.last_seen);
            button.setOnClickListener(v1 -> showDetails(aircraft));

            droneIcon = getResources().getDrawable(R.mipmap.ic_plane_icon);
            iconImageView = v.findViewById(R.id.drone_icon);
        }

        @Override
        public void bindView(@NonNull ListItem aircraftItem, @NonNull List<Object> payloads) {
            if (getContext() == null)
                return;

            this.aircraft = aircraftItem.object;

            StateListDrawable selectableBackground =
                    FastAdapterUIUtils.getSelectableBackground(getContext(), Color.LTGRAY, true);
            view.setBackground(selectableBackground);
            Identification id = aircraft.getIdentification();
            if (id != null)
                textView.setText(String.format("Aircraft %s", id.getUasIdAsString()));

            observer = identification -> {
                Log.w(TAG, "on changed: " + identification);
                if (identification == null) return;
                textView.setText(String.format("%s", identification.getUasIdAsString()));

                droneIcon.setColorFilter( 0xff00ff00, PorterDuff.Mode.MULTIPLY );
                iconImageView.setImageDrawable(droneIcon);
            };

            aircraft.connection.observe(DeviceList.this, connectionObserver);
            aircraft.location.observe(DeviceList.this, locationObserver);
            aircraft.identification.observe(DeviceList.this, observer);
        }

        @Override
        public void unbindView(@NonNull ListItem aircraftItem) {
            aircraft.identification.removeObserver(observer);
            aircraft.connection.removeObserver(connectionObserver);
            aircraft.location.removeObserver(locationObserver);
        }
        final Observer<Connection> connectionObserver = new Observer<Connection>() {
            @Override
            public void onChanged(@Nullable Connection connection) {
                if (connection != null)
                    lastSeen.setText(String.format(Locale.US, "%s dbm", connection.rssi));
            }
        };
        final Observer<LocationData> locationObserver = new Observer<LocationData>() {
            @Override
            public void onChanged(@Nullable LocationData locationData) {
                if (locationData != null)
                    textView2.setText(String.format(Locale.US, "%s, %s",
                                      locationData.getHeightAsString(),
                                      locationData.getSpeedHorizontalAsString()));
            }
        };

        Observer<Identification> observer;
    }

    public class ListItem extends AbstractItem<ListItem, AircraftViewHolder> {

        private final AircraftObject object;

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
            return 0;
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
