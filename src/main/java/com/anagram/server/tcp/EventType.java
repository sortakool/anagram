package com.anagram.server.tcp;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rmanaloto on 8/17/14.
 */
public enum EventType {

    ADD((byte)'A'),
    DELETE((byte)'D'),
    PRINT((byte)'P')
    ;

    private static Map<Byte, EventType> typeCode2Enum;
    static {
        typeCode2Enum = new HashMap<>(EventType.values().length, 1.0f);
        for (EventType eventType : EventType.values()) {
            typeCode2Enum.put(eventType.getTypeCode(), eventType);
        }
    }

    private final byte typeCode;

    EventType(byte typeCode) {
        this.typeCode = typeCode;
    }

    public byte getTypeCode() {
        return typeCode;
    }

    public static EventType fromTypeCode(byte typeCode) {
        return typeCode2Enum.get(typeCode);
    }
}
