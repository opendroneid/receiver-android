/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.log;

import org.opendroneid.android.Constants;
import org.opendroneid.android.bluetooth.OpenDroneIdParser;

import java.util.ArrayList;
import java.util.Collections;

public class LogMessageEntry {

    private static final String DELIM = Constants.DELIM;
    private static final String DELIM_BASIC_ID = DELIM + DELIM + DELIM;
    private static final String DELIM_LOCATION = DELIM + DELIM + DELIM + DELIM + DELIM + DELIM +
                                                 DELIM + DELIM + DELIM + DELIM + DELIM + DELIM +
                                                 DELIM + DELIM + DELIM + DELIM + DELIM + DELIM +
                                                 DELIM;
    private static final String DELIM_AUTHENTICATION = DELIM + DELIM + DELIM + DELIM + DELIM +
                                                       DELIM;
    private static final String DELIM_SELF_ID = DELIM + DELIM;
    private static final String DELIM_SYSTEM = DELIM + DELIM + DELIM + DELIM + DELIM + DELIM +
                                               DELIM + DELIM + DELIM + DELIM;
    private static final String DELIM_OPERATOR = DELIM + DELIM;

    private final ArrayList<OpenDroneIdParser.Message> messages;

    public LogMessageEntry() { this.messages = new ArrayList<>(); }

    public void add(OpenDroneIdParser.Message<?> message) { messages.add(message); }

    @SuppressWarnings("unchecked")
    public StringBuilder getMessageLogEntry() {
        if (messages.size() == 0)
            return null;

        Collections.sort(messages);

        StringBuilder entry = new StringBuilder();
        int i = 0;

        if (messages.get(i).header.type == OpenDroneIdParser.Type.BASIC_ID) {
            OpenDroneIdParser.Message<OpenDroneIdParser.BasicId> message =
                    (OpenDroneIdParser.Message<OpenDroneIdParser.BasicId>) messages.get(i);
            entry.append(message.payload.toCsvString());
            i++;
        } else {
            entry.append(DELIM_BASIC_ID);
        }

        if (i < messages.size() && messages.get(i).header.type == OpenDroneIdParser.Type.LOCATION) {
            OpenDroneIdParser.Message<OpenDroneIdParser.Location> message =
                    (OpenDroneIdParser.Message<OpenDroneIdParser.Location>) messages.get(i);
            entry.append(message.payload.toCsvString());
            i++;
        } else {
            entry.append(DELIM_LOCATION);
        }

        for (int j = 0; j < Constants.MAX_AUTH_DATA_PAGES; j++)
        {
            if (i < messages.size() && messages.get(i).header.type == OpenDroneIdParser.Type.AUTH) {
                OpenDroneIdParser.Message<OpenDroneIdParser.Authentication> message =
                        (OpenDroneIdParser.Message<OpenDroneIdParser.Authentication>) messages.get(i);
                if (message.payload.getAuthDataPage() == j) {
                    entry.append(message.payload.toCsvString());
                    i++;
                } else {
                    entry.append(DELIM_AUTHENTICATION);
                }
            } else {
                entry.append(DELIM_AUTHENTICATION);
            }
        }

        if (i < messages.size() && messages.get(i).header.type == OpenDroneIdParser.Type.SELFID) {
            OpenDroneIdParser.Message<OpenDroneIdParser.SelfID> message =
                    (OpenDroneIdParser.Message<OpenDroneIdParser.SelfID>) messages.get(i);
            entry.append(message.payload.toCsvString());
            i++;
        } else {
            entry.append(DELIM_SELF_ID);
        }

        if (i < messages.size() && messages.get(i).header.type == OpenDroneIdParser.Type.SYSTEM) {
            OpenDroneIdParser.Message<OpenDroneIdParser.SystemMsg> message =
                    (OpenDroneIdParser.Message<OpenDroneIdParser.SystemMsg>) messages.get(i);
            entry.append(message.payload.toCsvString());
            i++;
        } else {
            entry.append(DELIM_SYSTEM);
        }

        if (i < messages.size() && messages.get(i).header.type == OpenDroneIdParser.Type.OPERATOR_ID) {
            OpenDroneIdParser.Message<OpenDroneIdParser.OperatorID> message =
                    (OpenDroneIdParser.Message<OpenDroneIdParser.OperatorID>) messages.get(i);
            entry.append(message.payload.toCsvString());
        } else {
            entry.append(DELIM_OPERATOR);
        }

        return entry;
    }
}
