/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android;

public class Constants {
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int FINE_LOCATION_PERMISSION_REQUEST_CODE = 2;
    public static final int REQUEST_ENABLE_WIFI = 3;
    public static final int REQUEST_BLUETOOTH_PERMISSION_SCAN = 4;
    public static final int REQUEST_BLUETOOTH_PERMISSION_CONNECT = 5;
    public static final int REQUEST_NEARBY_WIFI_DEVICES_PERMISSION = 6;

    public static final String DELIM = ",";

    public static final int MAX_ID_BYTE_SIZE = 20;
    public static final int MAX_STRING_BYTE_SIZE = 23;
    public static final int MAX_AUTH_DATA_PAGES = 16;
    public static final int MAX_AUTH_PAGE_ZERO_SIZE = 17;
    public static final int MAX_AUTH_PAGE_NON_ZERO_SIZE = 23;
    public static final int MAX_AUTH_DATA = MAX_AUTH_PAGE_ZERO_SIZE + (MAX_AUTH_DATA_PAGES - 1) * MAX_AUTH_PAGE_NON_ZERO_SIZE;
    public static final int MAX_MESSAGE_SIZE = 25;
    public static final int MAX_MESSAGES_IN_PACK = 9;
    public static final int MAX_MESSAGE_PACK_SIZE = MAX_MESSAGE_SIZE * MAX_MESSAGES_IN_PACK;

/* The continued development of the relevant standards is reflected in the remote ID protocol
 * version number that is transmitted in the header of each drone ID message.
 *
 * The following protocol versions have been in use:
 * 0. ASTM F3411-19. Published Feb 14, 2020. https://www.astm.org/f3411-19.html
 * 1. ASD-STAN prEN 4709-002 P1. Published 31-Oct-2021.
 *    http://asd-stan.org/downloads/asd-stan-pren-4709-002-p1/
 *
 *    ASTM F3411 v1.1 draft sent for first ballot round autumn 2021
 *
 * 2. ASTM F3411 v1.1 draft sent for second ballot round Q1 2022. (ASTM F3411-22 ?)
 *    The delta to protocol version 1 is small:
 *    - New enum values:
 *      LocationData.StatusEnum.Remote_ID_System_Failure
 *      SelfIdData.descriptionTypeEnum.Emergency,
 *      SelfIdData.descriptionTypeEnum.Extended_Status,
 *    - New Timestamp field in the System message
 *
 * Since the strategy of the standardization for drone ID has been to not break backwards
 * compatibility when adding new functionality, this implementation allows decoding messages
 * with a higher version number than defined below. It is assumed that newer versions can be
 * decoded but some data elements might be missing in the output. The message version displayed
 * in the detailed info view will be drawn with red color in this case. */

    public static final int MAX_MSG_VERSION = 2;
}
