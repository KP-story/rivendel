package com.kp.common.data.vo;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.kp.common.utilities.DataTypeUtils.*;

public class VObject extends HashMap<String, Object> {
    public  static          ObjectMapper objectMapper= new ObjectMapper();

    public VObject(Map<? extends String, ?> m) {
        super(m);
    }

    public VObject() {
    }

    public static VObject fromJSON(String content) throws JsonProcessingException {
        return objectMapper.readValue(content,VObject.class);

    }

    public String toJson( ) throws JsonProcessingException {
        return objectMapper.writeValueAsString(this);

    }
    public boolean getBoolean(String fieldName) {
        return getBooleanValueFrom(
                this.get(fieldName) instanceof byte[] ? new String((byte[]) this.get(fieldName)) : this.get(fieldName));
    }

    public boolean getBoolean(String fieldName, boolean defaultValue) {
        if (this.containsKey(fieldName)) {
            return this.getBoolean(fieldName);
        }
        return defaultValue;
    }

    public byte getByte(String fieldName) {
        return getByteValueFrom(get(fieldName));
    }

    public byte getByte(String fieldName, byte defaultValue) {
        if (this.containsKey(fieldName)) {
            return this.getByte(fieldName);
        }
        return defaultValue;
    }

    public short getShort(String fieldName) {
        return getShortValueFrom(this.get(fieldName));
    }

    public short getShort(String fieldName, short defaultValue) {
        if (this.containsKey(fieldName)) {
            return this.getShort(fieldName);
        }
        return defaultValue;
    }

    public int getInteger(String fieldName) {
        return getIntegerValueFrom(get(fieldName));
    }

    public int getInteger(String fieldName, int defaultValue) {
        if (this.containsKey(fieldName)) {
            return this.getInteger(fieldName);
        }
        return defaultValue;
    }

    public float getFloat(String fieldName) {
        return getFloatValueFrom(this.get(fieldName));
    }

    public float getFloat(String fieldName, float defaultValue) {
        if (this.containsKey(fieldName)) {
            return this.getFloat(fieldName);
        }
        return defaultValue;
    }

    public long getLong(String fieldName) {
        return getLongValueFrom(this.get(fieldName));
    }

    public long getLong(String fieldName, long defaultValue) {
        if (this.containsKey(fieldName)) {
            return this.getLong(fieldName);
        }
        return defaultValue;
    }

    public double getDouble(String fieldName) {
        return getDoubleValueFrom(this.get(fieldName));
    }

    public double getDouble(String fieldName, double defaultValue) {
        if (this.containsKey(fieldName)) {
            return this.getDouble(fieldName);
        }
        return defaultValue;
    }

    public String getString(String fieldName) {

        return getStringValueFrom(this.get(fieldName));
    }

    public String getString(String fieldName, String defaultValue) {
        if (this.containsKey(fieldName)) {
            return this.getString(fieldName);
        }
        return defaultValue;
    }

    public byte[] getRaw(String fieldName) {
        Object data = this.get(fieldName);
        if (data instanceof String) {
            return ((String) data).getBytes();
        }
        return (byte[]) data;
    }

    public byte[] getRaw(String fieldName, byte[] defaultValue) {
        if (this.containsKey(fieldName)) {
            return this.getRaw(fieldName);

        }
        return defaultValue;

    }

    public VArray getVArray(String fieldName) {
        return new VArray((Collection) get(fieldName));

    }

    public VObject getVObject(String fieldName) {
        return (VObject) get(fieldName);
    }

    public VObject getAndCastVObject(String fieldName) {
        return new VObject((Map<? extends String, ?>) get(fieldName));
    }

    void append(int numTab, StringBuilder _builder) {
        StringBuilder builder = _builder == null ? new StringBuilder() : _builder;
        String tabs = "";
        if (numTab > 0) {
            for (int i = 0; i < numTab; i++) {
                tabs += "\t";
            }
        }
        builder.append("{\n");
        boolean flag = true;
        for (Entry<String, Object> entry : this.entrySet()) {
            if (flag) {
                flag = false;
            } else {
                builder.append(",\n");
            }
            builder.append(tabs + "\t");
            builder.append(entry.getKey());
            builder.append(":");
            builder.append(entry.getValue().getClass().getSimpleName());

            builder.append(" = ");
            if (entry.getValue() instanceof VObject) {
                VObject puo = new VObject((Map<? extends String, ?>) entry.getValue());
                puo.append(numTab + 1, builder);
            } else if (isArrayOrCollection(entry.getValue().getClass())) {
                VArray arr = new VArray((Collection) entry.getValue());
                arr.append(numTab + 1, builder);
            } else {
                builder.append(entry.getValue().toString());
            }
        }
        builder.append("\n").append(tabs).append("}");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.append(0, sb);
        return sb.toString();
    }


}