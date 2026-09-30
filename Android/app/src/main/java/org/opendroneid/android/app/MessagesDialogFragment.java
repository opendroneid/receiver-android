/*
 * Copyright (C) 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * "报文"按钮弹窗：展示选中飞机发出的全部报文（ASTM F3411 与 GB 46750-2025），
 * 国标报文会展开其 21 项解码内容。
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.opendroneid.android.R;
import org.opendroneid.android.data.AircraftObject;
import org.opendroneid.android.data.MsgLogEntry;

import java.util.ArrayList;
import java.util.Locale;

public class MessagesDialogFragment extends DialogFragment {
    private TextView title;
    private LinearLayout container;

    static MessagesDialogFragment newInstance() {
        return new MessagesDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_messages, container, false);
        title = view.findViewById(R.id.messages_title);
        this.container = view.findViewById(R.id.messages_container);
        Button close = view.findViewById(R.id.messages_close);
        close.setOnClickListener(v -> dismiss());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 让弹窗宽度占满可用空间，便于展示十六进制原始报文
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() == null)
            return;
        DetailViewModel model = new ViewModelProvider(getActivity()).get(DetailViewModel.class);
        // 国标报文到达时实时刷新
        model.gbMessage.observe(getViewLifecycleOwner(), gb -> render(model));
        render(model);
    }

    private void render(DetailViewModel model) {
        AircraftObject ac = model.getSelected();
        if (ac == null || getContext() == null)
            return;
        ArrayList<MsgLogEntry> log = ac.getMessageLogSnapshot();
        title.setText(String.format(Locale.US, "%s（%d）", getString(R.string.messages_title), log.size()));
        container.removeAllViews();
        if (log.isEmpty()) {
            TextView tv = new TextView(getContext());
            tv.setText(R.string.messages_empty);
            tv.setPadding(4, 8, 4, 8);
            container.addView(tv);
            return;
        }
        for (MsgLogEntry e : log) {
            container.addView(buildEntry(e));
        }
    }

    private View buildEntry(MsgLogEntry e) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(12, 10, 12, 10);
        card.setBackgroundColor(e.isGb ? 0xFFE8F5E9 : 0xFFF5F5F5);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);
        card.setLayoutParams(lp);

        TextView head = new TextView(getContext());
        head.setText(String.format(Locale.US, "[%s] %s · %s", e.messageType, e.transportType, e.getTimeText()));
        head.setTextSize(14);
        head.setTextColor(e.isGb ? 0xFF1B5E20 : 0xFF1565C0);
        card.addView(head);

        TextView summary = new TextView(getContext());
        summary.setText(e.summary);
        summary.setTextSize(12);
        card.addView(summary);

        TextView raw = new TextView(getContext());
        raw.setText(e.rawHex);
        raw.setTextSize(10);
        card.addView(raw);

        return card;
    }
}
