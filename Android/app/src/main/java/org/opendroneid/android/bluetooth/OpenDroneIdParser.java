/*
 * Copyright (C) 2019 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.opendroneid.android.bluetooth;

import androidx.annotation.NonNull;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import org.opendroneid.android.Constants;
import org.opendroneid.android.log.LogMessageEntry;

public class OpenDroneIdParser {
    private static final String TAG = "OpenDroneIdParser";
    private static final String DELIM = Constants.DELIM;

    public enum Type {
        BASIC_ID(0),
        LOCATION(1),
        AUTH(2),
        SELFID(3),
        SYSTEM(4),
        OPERATOR_ID(5),
        MESSAGE_PACK(0xF);

        Type(int id) { this.id = id; }
        public final int id;

        public static Type fromId(int id) {
            if (id == BASIC_ID.id) {
                return BASIC_ID;
            } else if (id == LOCATION.id) {
                return LOCATION;
            } else if (id == AUTH.id) {
                return AUTH;
            } else if (id == SELFID.id) {
                return SELFID;
            } else if (id == SYSTEM.id) {
                return SYSTEM;
            } else if (id == OPERATOR_ID.id) {
                return OPERATOR_ID;
            } else if (id == MESSAGE_PACK.id) {
                return MESSAGE_PACK;
            } else {
                return null;
            }
        }
    }

    public static class Header {
        public Type type;
        public int version;

        @Override @NonNull
        public String toString() {
            return "Header{" +
                    "type=" + type +
                    ", version=" + version +
                    '}';
        }
    }

    public interface Payload {
        String toCsvString();
    }

    private static final double LAT_LONG_MULTIPLIER = 1e-7;
    private static final double SPEED_VERTICAL_MULTIPLIER = 0.5;

    public static class BasicId implements Payload {
        int idType;
        int uaType;
        byte[] uasId = new byte[Constants.MAX_ID_BYTE_SIZE];

        public static String csvHeader() {
            return "idType" + DELIM
                    + "uaType" + DELIM
                    + "uasId" + DELIM;
        }

        @Override
        public String toCsvString() {
            return idType + DELIM
                    + uaType + DELIM
                    + new String(uasId) + DELIM;
        }

        @Override @NonNull
        public String toString() {
            return "BasicId{" +
                    "idType=" + idType +
                    ", uaType=" + uaType +
                    ", uasId='" + Arrays.toString(uasId) + '\'' +
                    '}';
        }
    }

    public static class Location implements Payload {
        int status;
        int heightType;
        int EWDirection;
        int speedMult;
        int Direction;
        int speedHori;
        int speedVert;
        int droneLat;
        int droneLon;
        int altitudePressure;
        int altitudeGeodetic;
        int height;
        int horizontalAccuracy;
        int verticalAccuracy;
        int baroAccuracy;
        int speedAccuracy;
        int timestamp;
        int timeAccuracy;

        static double calcSpeed(int value, int mult) {
            if (mult == 0)
                return value * 0.25;
            else
                return (value * 0.75) + (255 * 0.25);
        }

        static double calcDirection(int value, int EW) {
            if (EW == 0)
                return value;
            else
                return value + 180;
        }

        double getDirection() { return calcDirection(Direction, EWDirection); }
        double getSpeedHori() { return calcSpeed(speedHori, speedMult); }

        double getSpeedVert() { return SPEED_VERTICAL_MULTIPLIER * speedVert; }

        double getLatitude() {
            return LAT_LONG_MULTIPLIER * droneLat;
        }
        double getLongitude() {
            return LAT_LONG_MULTIPLIER * droneLon;
        }

        static double calcAltitude(int value) { return (double) value / 2 - 1000; }
        double getAltitudePressure() { return calcAltitude(altitudePressure); }
        double getAltitudeGeodetic() { return calcAltitude(altitudeGeodetic); }
        double getHeight() { return calcAltitude(height); }

        double getTimeAccuracy() { return timeAccuracy * 0.1; }

        public static String csvHeader() {
            return "status" + DELIM
                    + "heightType" + DELIM
                    + "EWDirection" + DELIM
                    + "speedMult" + DELIM
                    + "direction" + DELIM
                    + "speedHori" + DELIM
                    + "speedVert" + DELIM
                    + "droneLat" + DELIM
                    + "droneLon" + DELIM
                    + "altitudePressure" + DELIM
                    + "altitudeGeodetic" + DELIM
                    + "height" + DELIM
                    + "horizontalAccuracy" + DELIM
                    + "verticalAccuracy" + DELIM
                    + "baroAccuracy" + DELIM
                    + "speedAccuracy" + DELIM
                    + "timestamp" + DELIM
                    + "timeAccuracy" + DELIM;
        }

        @Override
        public String toCsvString() {
            return status + DELIM
                    + heightType + DELIM
                    + EWDirection + DELIM
                    + speedMult + DELIM
                    + Direction + DELIM
                    + speedHori + DELIM
                    + speedVert + DELIM
                    + droneLat + DELIM
                    + droneLon + DELIM
                    + altitudePressure + DELIM
                    + altitudeGeodetic + DELIM
                    + height + DELIM
                    + horizontalAccuracy + DELIM
                    + verticalAccuracy + DELIM
                    + baroAccuracy + DELIM
                    + speedAccuracy + DELIM
                    + timestamp + DELIM
                    + timeAccuracy + DELIM;
        }
        
        @Override @NonNull
        public String toString() {
            return "Location{" +
                    "status=" + status +
                    ", heightType=" + heightType +
                    ", EWDirection=" + EWDirection +
                    ", speedMult=" + speedMult +
                    ", direction=" + Direction +
                    ", speedHori=" + speedHori +
                    ", speedVert=" + speedVert +
                    ", droneLat=" + droneLat +
                    ", droneLon=" + droneLon +
                    ", altitudePressure=" + altitudePressure +
                    ", altitudeGeodetic=" + altitudeGeodetic +
                    ", height=" + height +
                    ", horizontalAccuracy=" + horizontalAccuracy +
                    ", verticalAccuracy=" + verticalAccuracy +
                    ", baroAccuracy=" + baroAccuracy +
                    ", speedAccuracy=" + speedAccuracy +
                    ", timestamp=" + timestamp +
                    ", timeAccuracy=" + timeAccuracy +
                    '}';
        }
    }

    public static class Authentication implements Payload {
        int authType;
        int authDataPage;
        int authPageCount;
        int authLength;
        int authTimestamp;
        byte[] authData = new byte[Constants.MAX_AUTH_DATA];

        public int getAuthDataPage() { return authDataPage; }

        public static String csvHeader() {
            return "authType" + DELIM
                    + "authDataPage" + DELIM
                    + "authPageCount" + DELIM
                    + "authLength" + DELIM
                    + "authTimestamp" + DELIM
                    + "authData" + DELIM;
        }

        @Override
        public String toCsvString() {
            return authType + DELIM
                    + authDataPage + DELIM
                    + authPageCount + DELIM
                    + authLength + DELIM
                    + authTimestamp + DELIM
                    + new String(authData) + DELIM;
        }

        @Override @NonNull
        public String toString() {
            return "Authentication{" +
                    "authType=" + authType +
                    ", authDataPage=" + authDataPage +
                    ", authPageCount=" + authPageCount +
                    ", authLength=" + authLength +
                    ", authTimestamp=" + authTimestamp +
                    ", authData='" + Arrays.toString(authData) + '\'' +
                    '}';
        }
    }

    public static class SelfID implements Payload {
        int descriptionType;
        byte[] operationDescription = new byte[Constants.MAX_STRING_BYTE_SIZE];

        public static String csvHeader() {
            return "descriptionType" + DELIM
                    + "operationDescription" + DELIM;
        }

        @Override
        public String toCsvString() {
            return descriptionType + DELIM
                    + new String(operationDescription) + DELIM;
        }

        @Override @NonNull
        public String toString() {
            return "SelfID{" +
                    "descriptionType=" + descriptionType +
                    ", operationDescription='" + Arrays.toString(operationDescription) + '\'' +
                    '}';
        }
    }

    public static class SystemMsg implements Payload {
        int flags;
        int operatorLatitude;
        int operatorLongitude;
        int areaCount;
        int areaRadius;
        int areaCeiling;
        int areaFloor;

        double getLatitude() {
            return LAT_LONG_MULTIPLIER * operatorLatitude;
        }
        double getLongitude() {
            return LAT_LONG_MULTIPLIER * operatorLongitude;
        }

        int getAreaRadius() { return areaRadius * 10; }
        static double calcAltitude(int value) { return (double) value / 2 - 1000; }
        double getAreaCeiling() { return calcAltitude(areaCeiling); }
        double getAreaFloor() { return calcAltitude(areaFloor); }

        public static String csvHeader() {
            return "flags" + DELIM
                    + "operatorLatitude" + DELIM
                    + "operatorLongitude" + DELIM
                    + "areaCount" + DELIM
                    + "areaRadius" + DELIM
                    + "areaCeiling" + DELIM
                    + "areaFloor" + DELIM;
        }

        @Override
        public String toCsvString() {
            return flags + DELIM
                    + operatorLatitude + DELIM
                    + operatorLongitude + DELIM
                    + areaCount + DELIM
                    + areaRadius + DELIM
                    + areaCeiling + DELIM
                    + areaFloor + DELIM;
        }

        @Override @NonNull
        public String toString() {
            return "PilotLocation{" +
                    "flags=" + flags +
                    ", operatorLatitude=" + operatorLatitude +
                    ", operatorLongitude=" + operatorLongitude +
                    ", areaCount=" + areaCount +
                    ", areaRadius=" + areaRadius +
                    ", areaCeiling=" + areaCeiling +
                    ", areaFloor=" + areaFloor +
                    '}';
        }
    }

    public static class OperatorID implements Payload {
        int operatorIdType;
        byte[] operatorId = new byte[Constants.MAX_ID_BYTE_SIZE];

        public static String csvHeader() {
            return "operatorIdType" + DELIM
                    + "operatorId" + DELIM;
        }

        @Override
        public String toCsvString() {
            return operatorIdType + DELIM
                    + new String(operatorId) + DELIM;
        }
        @Override @NonNull
        public String toString() {
            return "OperatorID{" +
                    "operatorIdType=" + operatorIdType +
                    ", operatorId='" + Arrays.toString(operatorId) + '\'' +
                    '}';
        }
    }

    public static class MessagePack implements Payload {
        int messageSize;
        int messagesInPack;
        byte[] messages = new byte[Constants.MAX_MESSAGE_PACK_SIZE];

        @Override @NonNull
        public String toString() {
            return "MessagePack{" +
                    "messageSize=" + messageSize +
                    ", messagesInPack=" + messagesInPack +
                    ", messages='" + Arrays.toString(messages) + '\'' +
                    '}';
        }

        @Override public String toCsvString() { return null; }
    }

    public static class Message<T extends Payload> implements Comparable<Message> {
        final int adCounter;
        final long timestamp;
        public final Header header;
        public final T payload;

        Message(Header header, T payload, long timestamp, int adCounter) {
            this.adCounter = adCounter;
            this.header = header;
            this.payload = payload;
            this.timestamp = timestamp;
        }

        @Override
        public int compareTo(@NonNull Message o)
        {
            if (this.header.type == Type.AUTH && o.header.type == Type.AUTH ) {
                Authentication authThis = (OpenDroneIdParser.Authentication) this.payload;
                Authentication authO = (OpenDroneIdParser.Authentication) o.payload;
                return (authThis.authDataPage - authO.authDataPage);
            } else {
                return this.header.type.compareTo(o.header.type);
            }
        }
    }

    static Message<Payload> parseAdvertisingData(byte[] payload, long timestamp, LogMessageEntry logMessageEntry) {
        if (payload.length < 6 + 25)
            return null;

        int adCounter = payload[5] & 0xFF;
        return parseMessage(payload, 6, timestamp, logMessageEntry, adCounter);
    }

    static Message<Payload> parseMessage(byte[] payload, int offset, long timestamp, LogMessageEntry logMessageEntry, int adCounter) {
        if (payload.length < offset + 25)
            return null;

        ByteBuffer byteBuffer = ByteBuffer.wrap(payload, offset, 25);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        Header header = new Header();
        int b = byteBuffer.get() & 0xFF;
        int type = (b & 0xF0) >> 4;
        header.type = Type.fromId(type);
        if (header.type == null) {
            Log.e(TAG, "Header type unknown");
            return null;
        }
        header.version = b & 0x0F;

        Payload payloadObj = null;

        switch (header.type) {
            case BASIC_ID:
                payloadObj = parseBasicId(byteBuffer);
                break;
            case LOCATION:
                payloadObj = parseLocation(byteBuffer);
                break;
            case AUTH:
                payloadObj = parseAuthentication(byteBuffer);
                break;
            case SELFID:
                payloadObj = parseSelfID(byteBuffer);
                break;
            case SYSTEM:
                payloadObj = parseSystem(byteBuffer);
                break;
            case OPERATOR_ID:
                payloadObj = parseOperatorID(byteBuffer);
                break;
            case MESSAGE_PACK:
                payloadObj = parseMessagePack(payload);
                break;
            default:
                Log.w(TAG, "Received unhandled message type: id=" + type);

        }
        Message<Payload> message = new Message<>(header, payloadObj, timestamp, adCounter);
        if (header.type != Type.MESSAGE_PACK)
            logMessageEntry.add(message);
        return message;
    }

    private static BasicId parseBasicId(ByteBuffer byteBuffer) {
        BasicId basicId = new BasicId();

        int type = byteBuffer.get();
        basicId.idType = (type & 0xF0) >> 4;
        basicId.uaType = type & 0x0F;
        byteBuffer.get(basicId.uasId, 0,Constants.MAX_ID_BYTE_SIZE);
        return basicId;
    }

    private static Location parseLocation(ByteBuffer byteBuffer) {
        Location location = new Location();

        int b = byteBuffer.get();

        location.status = (b & 0xF0) >> 4;
        location.heightType = (b & 0x04) >> 2;
        location.EWDirection = (b & 0x02) >> 1;
        location.speedMult = b & 0x01;

        location.Direction = byteBuffer.get() & 0xFF;
        location.speedHori = byteBuffer.get() & 0xFF;
        location.speedVert = byteBuffer.get();

        location.droneLat = byteBuffer.getInt();
        location.droneLon = byteBuffer.getInt();

        location.altitudePressure = byteBuffer.getShort();
        location.altitudeGeodetic = byteBuffer.getShort();
        location.height = byteBuffer.getShort();

        int horiVertAccuracy = byteBuffer.get();
        location.horizontalAccuracy = horiVertAccuracy & 0x0F;
        location.verticalAccuracy = (horiVertAccuracy & 0xF0) >> 4;
        int speedBaroAccuracy = byteBuffer.get();
        location.baroAccuracy = (speedBaroAccuracy & 0xF0) >> 4;
        location.speedAccuracy = speedBaroAccuracy & 0x0F;
        location.timestamp = byteBuffer.getShort() & 0xFFFF;
        location.timeAccuracy = byteBuffer.get() & 0x0F;
        return location;
    }

    private static Authentication parseAuthentication(ByteBuffer byteBuffer) {
        Authentication authentication = new Authentication();

        int type = byteBuffer.get();
        authentication.authType = (type & 0xF0) >> 4;
        authentication.authDataPage = type & 0x0F;

        int offset = 0;
        int amount = Constants.MAX_AUTH_PAGE_ZERO_SIZE;
        if (authentication.authDataPage == 0) {
            authentication.authPageCount = byteBuffer.get();
            authentication.authLength = byteBuffer.get();
            authentication.authTimestamp = byteBuffer.getInt();
        } else {
            offset = Constants.MAX_AUTH_PAGE_ZERO_SIZE + (authentication.authDataPage - 1) * Constants.MAX_AUTH_PAGE_NON_ZERO_SIZE;
            amount = Constants.MAX_AUTH_PAGE_NON_ZERO_SIZE;
        }
        if (authentication.authDataPage >= 0 && authentication.authDataPage < 5)
            for (int i = offset; i < offset + amount; i++)
                authentication.authData[i] = byteBuffer.get();
        return authentication;
    }

    private static SelfID parseSelfID(ByteBuffer byteBuffer) {
        SelfID selfID = new SelfID();
        selfID.descriptionType = byteBuffer.get();
        byteBuffer.get(selfID.operationDescription, 0, Constants.MAX_STRING_BYTE_SIZE);
        return selfID;
    }

    private static SystemMsg parseSystem(ByteBuffer byteBuffer) {
        SystemMsg s = new SystemMsg();
        s.flags = byteBuffer.get();
        s.operatorLatitude = byteBuffer.getInt();
        s.operatorLongitude = byteBuffer.getInt();
        s.areaCount = byteBuffer.getShort() & 0xFFFF;
        s.areaRadius = byteBuffer.get() & 0xFF;
        s.areaCeiling = byteBuffer.getShort();
        s.areaFloor = byteBuffer.getShort();
        return s;
    }

    private static OperatorID parseOperatorID(ByteBuffer byteBuffer) {
        OperatorID operatorID = new OperatorID();
        operatorID.operatorIdType = byteBuffer.get();
        byteBuffer.get(operatorID.operatorId, 0, Constants.MAX_ID_BYTE_SIZE);
        return operatorID;
    }

    private static MessagePack parseMessagePack(byte[] payload) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(payload, 6 + 1, 2);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        MessagePack messagePack = new MessagePack();
        messagePack.messageSize = byteBuffer.get();
        messagePack.messagesInPack = byteBuffer.get();

        if (messagePack.messageSize != Constants.MAX_MESSAGE_SIZE ||
            messagePack.messagesInPack <= 0 ||
            messagePack.messagesInPack > Constants.MAX_MESSAGES_IN_PACK ||
            payload.length < 6 + 1 + 2 + messagePack.messageSize*messagePack.messagesInPack)
            return null;

        // Now that we know how much data is in the message, re-wrap and extract the data
        byteBuffer = ByteBuffer.wrap(payload, 6 + 1 + 2, messagePack.messageSize*messagePack.messagesInPack);
        byteBuffer.get(messagePack.messages, 0, messagePack.messageSize * messagePack.messagesInPack);
        return messagePack;
    }
}
