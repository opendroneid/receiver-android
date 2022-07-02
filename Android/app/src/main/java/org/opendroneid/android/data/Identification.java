/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.data;

import org.opendroneid.android.Constants;
import java.util.Arrays;

public class Identification extends MessageData {

    private UaTypeEnum uaType;
    private IdTypeEnum idType;
    private byte[] uasId;

    public Identification() {
        super();
        uaType = UaTypeEnum.None;
        idType = IdTypeEnum.None;
        uasId = new byte[0];
    }

    public enum UaTypeEnum {
        None,
        Aeroplane,
        Helicopter_or_Multirotor,
        Gyroplane,
        Hybrid_Lift, // VTOL. Fixed wing aircraft that can take off vertically
        Ornithopter,
        Glider,
        Kite,
        Free_balloon,
        Captive_balloon,
        Airship,
        Free_fall_parachute, // Unpowered
        Rocket,
        Tethered_powered_aircraft,
        Ground_obstacle,
        Other,
    }

    public enum IdTypeEnum {
        None,
        Serial_Number,
        CAA_Registration_ID,
        UTM_Assigned_ID,
        Specific_Session_ID,
    }

    public UaTypeEnum getUaType() { return uaType; }
    public void setUaType(int uaType) {
        switch(uaType) {
            case 1: this.uaType = UaTypeEnum.Aeroplane; break;
            case 2: this.uaType = UaTypeEnum.Helicopter_or_Multirotor; break;
            case 3: this.uaType = UaTypeEnum.Gyroplane; break;
            case 4: this.uaType = UaTypeEnum.Hybrid_Lift; break;
            case 5: this.uaType = UaTypeEnum.Ornithopter; break;
            case 6: this.uaType = UaTypeEnum.Glider; break;
            case 7: this.uaType = UaTypeEnum.Kite; break;
            case 8: this.uaType = UaTypeEnum.Free_balloon; break;
            case 9: this.uaType = UaTypeEnum.Captive_balloon; break;
            case 10: this.uaType = UaTypeEnum.Airship; break;
            case 11: this.uaType = UaTypeEnum.Free_fall_parachute; break;
            case 12: this.uaType = UaTypeEnum.Rocket; break;
            case 13: this.uaType = UaTypeEnum.Tethered_powered_aircraft; break;
            case 14: this.uaType = UaTypeEnum.Ground_obstacle; break;
            case 15: this.uaType = UaTypeEnum.Other; break;
            default: this.uaType = UaTypeEnum.None; break;
        }
    }

    public IdTypeEnum getIdType() { return idType; }
    public void setIdType(int idType) {
        switch(idType) {
            case 1: this.idType = IdTypeEnum.Serial_Number; break;
            case 2: this.idType = IdTypeEnum.CAA_Registration_ID; break;
            case 3: this.idType = IdTypeEnum.UTM_Assigned_ID; break;
            case 4: this.idType = IdTypeEnum.Specific_Session_ID; break;
            default: this.idType = IdTypeEnum.None; break;
        }
    }

    public byte[] getUasId() { return uasId; }
    public String getUasIdAsString() {
        if (uasId != null) {
            if (idType == IdTypeEnum.Serial_Number || idType == IdTypeEnum.CAA_Registration_ID) {
                for (int c : uasId) {
                    if ((c <= 31 || c >= 127) && c != 0) {
                        return "Invalid ID String";
                    }
                }
                return new String(uasId);
            } else if (idType == IdTypeEnum.UTM_Assigned_ID || idType == IdTypeEnum.Specific_Session_ID) {
                StringBuilder sb = new StringBuilder();
                sb.append("0x");
                for (byte b : uasId) {
                    sb.append(String.format("%02X", b));
                }
                return sb.toString();
            }
        }
        return "";
    }
    public void setUasId(byte[] uasId) {
        if (uasId.length <= Constants.MAX_ID_BYTE_SIZE)
            this.uasId = uasId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Identification that = (Identification) o;
        return uaType == that.uaType &&
               idType == that.idType &&
               Arrays.equals(uasId, that.uasId);
    }
}
