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

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opendroneid.android.Constants;
import org.opendroneid.android.R;
import org.opendroneid.android.app.network.ApiClient;
import org.opendroneid.android.app.network.models.drone.DroneDetectionPost;
import org.opendroneid.android.app.network.models.drone.DroneDetectionResponse;
import org.opendroneid.android.app.network.service.ApiService;
import org.opendroneid.android.data.AircraftObject;
import org.opendroneid.android.data.Connection;
import org.opendroneid.android.data.Identification;
import org.opendroneid.android.data.LocationData;

import com.google.gson.Gson;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ModelAdapter;
import com.mikepenz.fastadapter.commons.utils.FastAdapterUIUtils;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.select.SelectExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceList extends Fragment {
    private static final String TAG = "CustomAdapter";

    private AircraftViewModel mModel;
    private ModelAdapter<AircraftObject, ListItem> mItemAdapter;
    private FastAdapter<ListItem> mAdapter;
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

        model.getActiveAircraft().observe(getViewLifecycleOwner(), object -> {
            SelectExtension<ListItem> selectExtension = mAdapter.getExtension(SelectExtension.class);
            if (selectExtension == null)
                return;
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (getActivity() == null)
            return;
        super.onActivityCreated(savedInstanceState);
        AircraftViewModel model = new ViewModelProvider(getActivity()).get(AircraftViewModel.class);
        subscribeToModel(model);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.aircraft_list, container, false);
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

    private void showDetails(AircraftObject aircraft) {
        if (getActivity() == null)
            return;
        DetailViewModel model = new ViewModelProvider(getActivity()).get(DetailViewModel.class);
        model.select(aircraft);
        DeviceDetailFragment newFragment = DeviceDetailFragment.newInstance();
        newFragment.show(getParentFragmentManager(), "dialog");
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

        AircraftViewHolder(View v) {
            super(v);
            this.view = v;
            textView = v.findViewById(R.id.aircraftName);
            textView2 = v.findViewById(R.id.aircraftFun);
            rssiView = v.findViewById(R.id.rssi);

            Button button = v.findViewById(R.id.modButton);
            button.setText(R.string.info);
            button.setOnClickListener(v1 -> showDetails(aircraft));

            droneIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_drone_target);
            iconImageView = v.findViewById(R.id.drone_icon);
        }

        private void setIdText(Identification id) {
            if (id.getUasIdAsString().length() > Constants.MAX_ID_BYTE_SIZE)
                textView.setTextSize(9);
            else
                textView.setTextSize(16);
            textView.setText(String.format("%s", id.getUasIdAsString()));
        }

        @Override
        public void bindView(@NonNull ListItem aircraftItem, @NonNull List<Object> payloads) {
            if (getContext() == null)
                return;

            this.aircraft = aircraftItem.object;

            StateListDrawable selectableBackground =
                    FastAdapterUIUtils.getSelectableBackground(getContext(), Color.LTGRAY, true);
            view.setBackground(selectableBackground);
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
        final Observer<Connection> connectionObserver = new Observer<Connection>() {
            @Override
            public void onChanged(Connection connection) {
                if (connection != null)
                    rssiView.setText(String.format(Locale.US, "%s dBm", connection.rssi));
            }
        };

        final Observer<LocationData> locationObserver = new Observer<LocationData>() {
            @Override
            public void onChanged(LocationData locationData) {
                if (locationData != null) {
                    Resources res = getResources();
                    textView2.setText(String.format(Locale.US, "%s over %s, %s, %s away",
                            locationData.getHeightLessPreciseAsString(res),
                            locationData.getHeightType().toString(),
                            locationData.getSpeedHorizontalLessPreciseAsString(res),
                            locationData.getDistanceAsString()));
                }
            }
        };

        final Observer<Identification> observer = new Observer<Identification>() {
            @Override
            public void onChanged(Identification identification) {
                if (identification != null) {
                    Log.w(TAG, "on changed: " + identification.getIdType() + ", " + identification.getUasIdAsString() + ", " + this);
                    setIdText(identification);

                    assert droneIcon != null;
                    droneIcon.setColorFilter(0xff00ff00, PorterDuff.Mode.MULTIPLY);
                    iconImageView.setImageDrawable(droneIcon);
                }
            }
        };
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
